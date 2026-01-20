package ch.dvbern.oss.hemed.esc.api.shared.person;

import java.util.List;

import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureException;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureMessage;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.dvbstarter.shared.types.sozialversicherungsnummer.SozialversicherungsNummer;
import ch.dvbern.oss.hemed.esc.api.externalimportinfojson.ExternalImportInfo;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.EinladungService;
import ch.dvbern.oss.hemed.esc.api.features.kindanlaesse.KindAnlaesseService;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Dossier;
import ch.dvbern.oss.hemed.esc.api.features.kinder.DossierService;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import ch.dvbern.oss.hemed.esc.api.features.kinder.KindService;
import ch.dvbern.oss.hemed.esc.api.features.kinder.duplikate.Duplikat;
import ch.dvbern.oss.hemed.esc.api.features.kinder.duplikate.DuplikatService;
import ch.dvbern.oss.hemed.esc.api.features.kinder.duplikate.DuplikatStatus;
import ch.dvbern.oss.hemed.esc.api.features.kinder.kontakt_person_kind.KontaktPersonKind;
import ch.dvbern.oss.hemed.esc.api.features.kinder.kontakt_person_kind.KontaktPersonKindService;
import ch.dvbern.oss.hemed.esc.api.features.notizen.Notiz;
import ch.dvbern.oss.hemed.esc.api.features.notizen.NotizService;
import ch.dvbern.oss.hemed.esc.api.shared.aufgabe.Aufgabe;
import ch.dvbern.oss.hemed.esc.api.shared.aufgabe.AufgabeService;
import ch.dvbern.oss.hemed.esc.api.shared.notizen.AutoNotizService;
import ch.dvbern.oss.hemed.esc.api.shared.printer.PrintResultDTO;
import ch.dvbern.oss.hemed.esc.api.shared.printer.kinderdossier.KinderdossierPrintService;
import ch.dvbern.oss.hemed.esc.api.shared.printer.kinderdossier.KinderdossierPrintVariant;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;

@Service
@RequiredArgsConstructor
@Slf4j
public class DuplikatMergeService {

    private final KontaktPersonKindService kontaktPersonKindService;
    private final DossierService dossierService;
    private final NotizService notizService;
    private final AufgabeService aufgabeService;
    private final KindAnlaesseService kindAnlaesseService;
    private final EinladungService einladungService;
    private final DuplikatService duplikatService;
    private final KindService kindService;
    private final AutoNotizService autoNotizService;
    private final KinderdossierPrintService kinderdossierPrintService;
    private final EntityManager entityManager;

    @NonNull
    public Duplikat merge(@NonNull ID<Duplikat> duplikatId, @NonNull ID<Kind> basisKindId) {
        Duplikat duplikat = duplikatService.getById(duplikatId);
        if (duplikat.getKind1() == null || duplikat.getKind2() == null) {
            throw new AppFailureException(AppFailureMessage.internalError(
                "Basis Kind ID must match one of the duplikat's Kind IDs"));
        }
        Kind basisKind = duplikat.getKind1().getId().equals(basisKindId) ? duplikat.getKind1() : duplikat.getKind2();
        Kind otherKind = duplikat.getKind1().getId().equals(basisKindId) ? duplikat.getKind2() : duplikat.getKind1();
        basisKind.disableOptimisticLockingCheck();
        otherKind.disableOptimisticLockingCheck();
        Dossier basisDossier = dossierService.getOrCreateByKind(basisKind.getId());
        dossierService.getOrCreateByKind(otherKind.getId()); // Muss erstellt werden, damit der Export funktioniert

        // Vor dem Mergen beide Dossiers exportieren
        PrintResultDTO exportBasisKind =
            kinderdossierPrintService.printKinderdossier(KinderdossierPrintVariant.GESAMTES, basisKind.getId(), null);
        PrintResultDTO exportOtherKind =
            kinderdossierPrintService.printKinderdossier(KinderdossierPrintVariant.GESAMTES, otherKind.getId(), null);
        // Notiz auf BasisKind erstellen
        autoNotizService.createDuplikatMergeNotiz(basisDossier, exportBasisKind, exportOtherKind);

        // ExternalImportInfo umhaengen wenn basiskind nicht aus escada
        ExternalImportInfo otherKindExternalImportInfo = otherKind.getExternalImportInfo();
        if (basisKind.getExternalSystemIdOrNull() == null
            && otherKindExternalImportInfo != null
            && otherKind.getExternalSystemIdOrNull() != null
        ) {
            // Wir uebernehmen die ExternalSystemId des anderen Kindes
            basisKind.setExternalImportInfo(
                new ExternalImportInfo(
                    otherKindExternalImportInfo.getIdInExternalSystem(),
                    otherKindExternalImportInfo.getAktivInExternalSystem(),
                    otherKindExternalImportInfo.getTimestampFirstImport(),
                    otherKindExternalImportInfo.getTimestampLatestImport(),
                    otherKindExternalImportInfo.getImportTyp()
                )
            );
        }
        // AhvNummer umhaengen wenn basiskind nicht aus escada
        if (basisKind.getSozialversicherungsNummer() == null && otherKind.getSozialversicherungsNummer() != null) {
            // Wir uebernehmen die AHV-Nummer des anderen Kindes
            SozialversicherungsNummer sozialversicherungsNummer = otherKind.getSozialversicherungsNummer();
            otherKind.setSozialversicherungsNummer(null); // VNR des anderen Kindes auf null setzen, damit sie
            // eindeutig ist
            kindService.persistWithoutDuplicateCheck(otherKind);
            entityManager.flush();
            basisKind.setSozialversicherungsNummer(sozialversicherungsNummer);
        }

        // Kontaktpersonen umhaengen
        List<KontaktPersonKind> kpOfOtherKind = kontaktPersonKindService.findByKind(otherKind.getId());
        for (KontaktPersonKind kontaktPersonKind : kpOfOtherKind) {
            // Wir uebernehmen die Kontaktpersonen des anderen Kindes in das Kind das wir behalten
            kontaktPersonKind.getKinder().add(basisKind);
            kontaktPersonKind.getKinder().remove(otherKind);
            kontaktPersonKind.disableOptimisticLockingCheck();
            kontaktPersonKindService.persist(kontaktPersonKind);
        }
        // Notizen umhaengen
        List<Notiz> notizenOfOtherKind = notizService.findAllForKind(otherKind.getId());
        for (Notiz notiz : notizenOfOtherKind) {
            // Wir uebernehmen die Notizen des anderen Kindes
            notiz.disableOptimisticLockingCheck();
            notiz.setDossier(basisDossier);
            notizService.persist(notiz);
        }
        // Aufgaben umhaengen
        List<Aufgabe> aufgabenOfOtherKind = aufgabeService.findAllForKind(otherKind.getId());
        for (Aufgabe aufgabe : aufgabenOfOtherKind) {
            // Wir uebernehmen die Aufgaben des anderen Kindes
            aufgabe.disableOptimisticLockingCheck();
            aufgabe.setKind(basisKind);
            aufgabeService.persist(aufgabe);
        }
        // Anlaesse inkl. Untersuchungen umhaengen
        List<Einladung> einladungenOfOtherKind = kindAnlaesseService.findAllEinladungForKind(otherKind.getId());
        for (Einladung einladung : einladungenOfOtherKind) {
            // Wir uebernehmen die Einladungen des anderen Kindes
            einladung.setDossier(basisDossier);
            einladungService.persist(einladung);
        }

        // Status
        duplikat.setDuplikatStatus(DuplikatStatus.GEMERGT);
        if (duplikat.getKind1().getId().equals(otherKind.getId())) {
            duplikat.setKind1(null);
        }
        if (duplikat.getKind2().getId().equals(otherKind.getId())) {
            duplikat.setKind2(null);
        }
        Duplikat persistedDuplikat = duplikatService.persist(duplikat);

        // Dossier und Kind entfernen
        kindService.remove(otherKind.getId());

        LOG.info(
            "ESC-KIND-MERGE: Kinder wurden gemerged basiskind={} duplikat={}",
            basisKind.getId().getUUID(),
            otherKind.getId().getUUID());

        return persistedDuplikat;
    }
}
