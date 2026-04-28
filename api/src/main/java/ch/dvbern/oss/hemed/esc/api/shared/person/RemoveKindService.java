package ch.dvbern.oss.hemed.esc.api.shared.person;

import java.util.Optional;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.archivierung.ArchivdokumentService;
import ch.dvbern.oss.hemed.esc.api.features.kindanlaesse.KindAnlaesseService;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Dossier;
import ch.dvbern.oss.hemed.esc.api.features.kinder.DossierService;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import ch.dvbern.oss.hemed.esc.api.features.kinder.KindRepo;
import ch.dvbern.oss.hemed.esc.api.features.kinder.NachkontrolleService;
import ch.dvbern.oss.hemed.esc.api.features.kinder.duplikate.DuplikatService;
import ch.dvbern.oss.hemed.esc.api.features.kinder.external.KindExternalService;
import ch.dvbern.oss.hemed.esc.api.features.kinder.kontakt_person_kind.KontaktPersonKindService;
import ch.dvbern.oss.hemed.esc.api.features.notizen.NotizService;
import ch.dvbern.oss.hemed.esc.api.shared.aktivitaet.AktivitaetHistoryService;
import ch.dvbern.oss.hemed.esc.api.shared.aufgabe.AufgabeService;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;

@Service
@RequiredArgsConstructor
public class RemoveKindService {

    private final KindRepo kindRepo;
    private final KontaktPersonKindService kontaktPersonKindService;
    private final DossierService dossierService;
    private final NotizService notizService;
    private final AufgabeService aufgabeService;
    private final KindAnlaesseService kindAnlaesseService;
    private final DuplikatService duplikatService;
    private final NachkontrolleService nachkontrolleService;
    private final KindExternalService kindExternalService;
    private final AktivitaetHistoryService aktivitaetHistoryService;
    private final ArchivdokumentService archivdokumentService;

    public void remove(@NonNull ID<Kind> kindID) {
        Optional<Dossier> dossierOptional = dossierService.findByKind(kindID);

        // Abhaengige Objekte entfernen
        // Kontaktpersonen entfernen
        kontaktPersonKindService.removeForKind(kindID);
        // Anlaesse inkl. Untersuchungen entfernen
        kindAnlaesseService.removeForKind(kindID, true);
        // Notizen entfernen
        notizService.removeForKind(kindID);
        // Aufgaben entfernen
        aufgabeService.removeForKind(kindID);
        // Duplikate entfernen
        duplikatService.removeForKind(kindID);
        // KindAktivitaeten entfernen
        aktivitaetHistoryService.removeForKind(kindID);
        // Dossier entfernen wenn vorhanden
        if (dossierOptional.isPresent()) {
            // Nachkontrollen entfernen
            nachkontrolleService.removeForDossier(dossierOptional.get().getId());
            // Archivdokument entfernen
            archivdokumentService.removeForDossier(dossierOptional.get().getId());
            // Dossier und Kind entfernen
            dossierService.remove(dossierOptional.get().getId());
        }
        // KindExternal entfernen
        kindExternalService.removeForKind(kindID);
        // Kind entfernen
        kindRepo.remove(kindID);
    }
}
