package ch.dvbern.oss.hemed.esc.api.shared.archivierung;

import java.util.List;
import java.util.Objects;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.dvbstarter.shared.clock.AppClock;
import ch.dvbern.dvbstarter.shared.i18n.translations.TL;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Anlass;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.EinladungService;
import ch.dvbern.oss.hemed.esc.api.features.archivierung.Archivdokument;
import ch.dvbern.oss.hemed.esc.api.features.archivierung.ArchivdokumentRepo;
import ch.dvbern.oss.hemed.esc.api.features.archivierung.ArchivierungStatus;
import ch.dvbern.oss.hemed.esc.api.features.archivierung.ArchivierungTyp;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Dossier;
import ch.dvbern.oss.hemed.esc.api.features.kinder.DossierService;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import ch.dvbern.oss.hemed.esc.api.features.kinder.KindService;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileData;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileDataService;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileTyp;
import ch.dvbern.oss.hemed.esc.api.shared.mail.service.MailDAO;
import ch.dvbern.oss.hemed.esc.api.shared.person.RemoveKindService;
import ch.dvbern.oss.hemed.esc.api.shared.printer.PrintResultDTO;
import ch.dvbern.oss.hemed.esc.api.shared.printer.PrintUtil;
import ch.dvbern.oss.hemed.esc.api.shared.printer.kinderdossier.KinderdossierPrintService;
import ch.dvbern.oss.hemed.esc.api.shared.printer.kinderdossier.KinderdossierPrintVariant;
import ch.dvbern.oss.hemed.esc.api.shared.settings.TenantSettingsService;
import ch.dvbern.oss.hemed.esc.api.shared.types.EntityAktivStatus;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArchivierungService {

    private final ArchivdokumentRepo archivdokumentRepo;
    private final DossierService dossierService;
    private final KinderdossierPrintService kinderdossierPrintService;
    private final FileDataService fileDataService;
    private final KindService kindService;
    private final TenantSettingsService tenantSettingsService;
    private final EinladungService einladungService;
    private final MailDAO mailDAO;
    private final RemoveKindService removeKindService;
    private final TL tl;
    private final AppClock appClock;

    @Transactional(TxType.REQUIRES_NEW)
    public void archivieren(@NonNull ID<Dossier> dossierId) {
        Dossier dossier = dossierService.getById(dossierId);
        Kind kind = dossier.getKind();
        // Dossier darf nur einmal definitiv archiviert werden (durch Batchjob, nicht durch Verantwortlichkeitswechsel)
        if (dossier.getTimestampDefinitivArchiviert() != null) {
            LOG.warn(
                "ESC-ARCHIVIERUNG: Kind kann nicht archiviert werden, da es bereits definitiv archiviert ist. "
                    + "Dossier-ID={}",
                dossier.getId());
            return;
        }
        // Archivdokument muss immer einem verantwortlichen Benutzer zugeordnet werden können
        Benutzer zustaendigerArzt = getZustaendigerArzt(dossier);
        // Gesamtes Kinderdossier exportieren
        createGesamtArchivdokument(dossier, zustaendigerArzt, ArchivierungTyp.KIND_DEAKTIVIERT);
        // Falls notwendig, einzelne Anlässe archivieren
        createTeilArchivdokumente(dossier, ArchivierungTyp.KIND_DEAKTIVIERT);

        kind.setKindAktivStatus(EntityAktivStatus.INAKTIV);
        kind.disableOptimisticLockingCheck();
        kindService.persistWithoutDuplicateCheck(kind);
        // Timestamp setzen, damit das Dossier nicht mehr archiviert werden kann
        dossier.setTimestampDefinitivArchiviert(appClock.getNow());
        dossier.disableOptimisticLockingCheck();
        dossierService.persist(dossier);
    }

    public void createGesamtArchivdokument(@NonNull Dossier dossier, @NonNull Benutzer zustaendigerArzt, @NonNull ArchivierungTyp archivierungTyp) {
        createArchivdokument(dossier, zustaendigerArzt, archivierungTyp, null);
    }

    public void createTeilArchivdokumente(@NonNull Dossier dossier, @NonNull ArchivierungTyp archivierungTyp) {
        List<ID<Benutzer>> berechtigtSchuleOrDossier = dossier.getBerechtigteAerzteIdsFromDossierOrSchule();
        einladungService.findAllByDossier(dossier.getId()).forEach(einladung -> {
                Benutzer arztAnlass = einladung.getAnlass().getArzt();
                if (arztAnlass != null && !berechtigtSchuleOrDossier.contains(arztAnlass.getId())) {
                    // Dieser Anlass muss einzeln archiviert werden
                    createArchivdokument(dossier, arztAnlass, archivierungTyp, einladung.getAnlass().getId());
                }
            }
        );
    }

    public void createTeilArchivdokument(@NonNull Dossier dossier, @NonNull ArchivierungTyp archivierungTyp, @NonNull Anlass anlass) {
        Objects.requireNonNull(anlass.getArzt(), "Anlass muss einen Arzt haben");
        createArchivdokument(dossier, anlass.getArzt(), archivierungTyp, anlass.getId());
    }

    @NonNull
    private Benutzer getZustaendigerArzt(@NonNull Dossier dossier) {
        Benutzer zustaendigerArzt = dossier.getZustaendigArztFromDossierOrSchule();
        if (zustaendigerArzt == null) {
            String supportEmail = tenantSettingsService.getTenantSetting().getSupportEmail();
            try {
                String msg =
                    "Kind kann nicht archiviert werden, da es keinem zuständigen Arzt zugeordnet ist. Dossier-ID="
                        + dossier.getId();
                mailDAO.sendEmailSync(supportEmail, "ESC-ARCHIVIERUNG: Kein zuständiger Arzt", msg);
            } catch (Exception e) {
                LOG.error("ESC-ARCHIVIERUNG: Error sending error mail to {}", supportEmail, e);
            }
            throw new IllegalStateException(
                "Kind kann nicht archiviert werden, da das Dossier keinem Benutzer zugeordnet ist. Dossier-ID="
                    + dossier.getId());
        }
        return zustaendigerArzt;
    }

    private void createArchivdokument(
        @NonNull Dossier dossier,
        @NonNull Benutzer zustaendigerArzt,
        @NonNull ArchivierungTyp archivierungTyp,
        @Nullable ID<Anlass> anlassID
    ) {
        PrintResultDTO printResultDTO = kinderdossierPrintService.printKinderdossier(
            KinderdossierPrintVariant.GESAMTES,
            dossier.getKind().getId(),
            anlassID);
        FileData dossierExportFile = fileDataService.createFileData(
            printResultDTO,
            FileTyp.KINDERDOSSIER,
            tl.translate("archivierung.filename.prefix"));
        // ID, Name, Vorname, Geburtsdatum
        String kindIdentifier = dossier.getKind().getId().getUUID() + ", " + dossier.getKind().getFullNameIfAllowed(false) + ", " + PrintUtil.formatDate(dossier.getKind().getGeburtsdatum());
        Archivdokument archivdokument = new Archivdokument(
            appClock.offsetDateTime().now(),
            archivierungTyp,
            ArchivierungStatus.OFFEN,
            dossier.getId().getUUID(),
            kindIdentifier,
            dossierExportFile,
            zustaendigerArzt,
            null
        );
        archivdokumentRepo.persist(archivdokument);
    }

    @Transactional(TxType.REQUIRES_NEW)
    public void delete(@NonNull ID<Dossier> dossierId) {
        Dossier dossier = dossierService.getById(dossierId);
        Kind kind = dossier.getKind();
        Archivdokument archivdokument = archivdokumentRepo.getByDossierForDeletion(dossierId);

        if (kind.getKindAktivStatus() != EntityAktivStatus.INAKTIV) {
            LOG.warn(
                "ESC-ARCHIVIERUNG-DELETE: Kind kann nicht gelöscht werden, da es immer noch AKTIV ist. Dossier-ID={}",
                dossier.getId());
            return;
        }

        if (archivdokument == null
            || archivdokument.getArchivierungStatus() != ArchivierungStatus.DOWNLOADED_READY_FOR_DELETION) {
            LOG.warn(
                "ESC-ARCHIVIERUNG-DELETE: Kind kann nicht gelöscht werden, es noch nicht für die Löschung markiert "
                    + "worden ist. Dossier-ID={}",
                dossier.getId());
            return;
        }

        removeKindService.remove(kind.getId());

        LOG.info(
            "ESC-ARCHIVIERUNG-DELETE: Kind und sämtliche verknüpften Daten wurde gelöscht -> {} {} {}",
            kind.getName().substring(0, 3),
            kind.getVorname().substring(0, 3),
            kind.getGeburtsdatum().getYear());
    }
}
