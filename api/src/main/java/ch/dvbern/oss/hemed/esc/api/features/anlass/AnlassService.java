package ch.dvbern.oss.hemed.esc.api.features.anlass;

import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureException;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureMessage;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppValidationException;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppValidationMessage;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.durchfuehrung.UntersuchungDurchfuehrung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.durchfuehrung.UntersuchungDurchfuehrungService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.EinladungService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.json.AnlassWithDateTimeJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.kind.UntersuchungKindDaten;
import ch.dvbern.oss.hemed.esc.api.features.anlass.kind.UntersuchungKindDatenService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.json.AnlassWithOpenSlotsOptionJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.massnahmen.UntersuchungMassnahmen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.massnahmen.UntersuchungMassnahmenService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.AnlassStatus;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.AnlassTyp;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.UntersuchungService;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import ch.dvbern.oss.hemed.esc.api.features.kinder.NachkontrolleService;
import ch.dvbern.oss.hemed.esc.api.features.schule.klasse.Klasse;
import ch.dvbern.oss.hemed.esc.api.shared.aktivitaet.AktivitaetHistoryService;
import ch.dvbern.oss.hemed.esc.api.shared.aufgabe.AutoAufgabeService;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import ch.dvbern.oss.hemed.esc.api.shared.config.service.ConfigService;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.checkerframework.checker.nullness.qual.NonNull;

@Service
@RequiredArgsConstructor
public class AnlassService {

    private final AnlassRepo anlassRepo;
    private final EinladungService einladungService;
    private final AutoAufgabeService autoAufgabeService;
    private final UntersuchungService untersuchungService;
    private final UntersuchungKindDatenService untersuchungKindDatenService;
    private final UntersuchungDurchfuehrungService untersuchungDurchfuehrungService;
    private final UntersuchungMassnahmenService untersuchungMassnahmenService;
    private final ConfigService configService;
    private final NachkontrolleService nachkontrolleService;
    private final AktivitaetHistoryService aktivitaetHistoryService;

    @NonNull
    public Anlass persist(@NonNull Anlass anlass) {
        anlassRepo.persist(anlass);
        return anlass;
    }

    @NonNull
    public Anlass persistAndFlush(@NonNull Anlass anlass) {
        anlassRepo.persistAndFlush(anlass);
        return anlass;
    }

    @NonNull
    public Anlass persistWithOptimisticLocking(@NonNull Anlass entity, @NonNull Long versionFromFrontend) {
        return anlassRepo.persistWithOptimisticLocking(entity, versionFromFrontend);
    }

    public void remove(@NonNull ID<Anlass> anlassID) {
        List<Einladung> einladungen = einladungService.findByAnlass(anlassID);
        if (CollectionUtils.isNotEmpty(einladungen)) {
            throw new AppValidationException(AppValidationMessage.anlassCanNotBeDeleted());
        }
        this.einladungService.removeEinladungen(einladungen.stream().map(Einladung::getId).toList());
        aktivitaetHistoryService.removeForAnlass(anlassID);
        anlassRepo.remove(anlassID);
    }

    @NonNull
    public List<Anlass> findAll() {
        return anlassRepo.findAll();
    }

    @NonNull
    public Anlass getById(@NonNull ID<Anlass> anlassID) {
        return anlassRepo.getById(anlassID);
    }

    @NonNull
    public List<Klasse> findKlassenForAnlass(@NonNull ID<Anlass> anlassId) {
        return anlassRepo.findKlassenForAnlass(anlassId);
    }

    @NonNull
    public Anlass changeAnlassStatus(@NonNull ID<Anlass> anlassID, @NonNull AnlassStatus status) {
        Anlass anlass = getById(anlassID);
        anlass.disableOptimisticLockingCheck();
        anlass.setAnlassStatus(status);
        if (status == AnlassStatus.DURCHGEFUEHRT) {
            onAnlassDurchgefuehrt(anlass);
        }
        if (status == AnlassStatus.ABGESCHLOSSEN) {
            onAnlassAbgeschlossen(anlass);
        }
        return persistAndFlush(anlass);
    }

    private void onAnlassDurchgefuehrt(@NonNull Anlass anlass) {
        if (anlass.getAnlassConfig().getAnlassTyp() == AnlassTyp.KLASSE_8
            || anlass.getAnlassConfig().getAnlassTyp() == AnlassTyp.KLASSE_9
            || anlass.getAnlassConfig().getAnlassTyp() == AnlassTyp.FREMDSPRACHEN
        ) {
            autoAufgabeService.createAufgabeGruppenFolgetermin(anlass);
        }
    }

    private void onAnlassAbgeschlossen(@NonNull Anlass anlass) {
        List<Einladung> einladungList = einladungService.findByAnlass(anlass.getId());
        for (Einladung einladung : einladungList) {
            Untersuchung untersuchung = untersuchungService.getOrCreateUntersuchung(einladung.getId());
            // Kinderdaten kopieren auf Untersuchung
            if (untersuchung.getUntersuchungKindDaten() == null) {
                // Die Daten wurden noch nicht kopiert und müssen daher kopiert werden
                UntersuchungKindDaten untersuchungKindDaten =
                    UntersuchungKindDaten.ofKind(einladung.getDossier().getKind());
                untersuchungKindDatenService.persist(untersuchungKindDaten);
                untersuchung.setUntersuchungKindDaten(untersuchungKindDaten);
            }
            if (untersuchung.getUntersuchungDurchfuehrung() == null) {
                // Die Untersuchung wurde noch nicht durchgeführt, daher müssen wir die Defaults setzen
                UntersuchungDurchfuehrung defaultsForNextUntersuchung =
                    untersuchungDurchfuehrungService.getDefaultUntersuchungDurchfuehrung(untersuchung);
                untersuchungDurchfuehrungService.persist(defaultsForNextUntersuchung);
                untersuchung.setUntersuchungDurchfuehrung(defaultsForNextUntersuchung);
            }
            // Nachkontrolle-Flags aufs Dossier kopieren, falls es keine neueren Massnahmen gibt
            if (untersuchung.getUntersuchungMassnahmen() != null) {
                Optional<UntersuchungMassnahmen> latestByKind =
                    untersuchungMassnahmenService.findLatestByKind(einladung.getDossier().getKind().getId());
                if (latestByKind.isPresent()) {
                    UntersuchungMassnahmen untersuchungMassnahmen = latestByKind.get();
                    if (untersuchungMassnahmen.getId().equals(untersuchung.getUntersuchungMassnahmen().getId())) {
                        // Wir sind auf dem neuesten Anlass: Die Flags aufs Dossier kopieren
                        nachkontrolleService.updateNachkontrolle(
                            untersuchung.getEinladung().getDossier().getId(),
                            untersuchungMassnahmen.getNachkontrolle());
                    }
                }
            }
        }
    }

    @NonNull
    public List<Anlass> findAllForUserInFuture(@NonNull ID<Benutzer> benutzerID) {
        return anlassRepo.findAllForUserInFuture(benutzerID, configService.getDashboardAnlassLimit());
    }

    public long getCountAllforUserInFuture(@NonNull ID<Benutzer> benutzerID) {
        return anlassRepo.getCountAllforUserInFuture(benutzerID);
    }

    public void requireAnlassAbgeschlossen(@NonNull ID<Anlass> anlassId) {
        Anlass anlass = getById(anlassId);
        if (anlass.getAnlassStatus() != AnlassStatus.ABGESCHLOSSEN) {
            throw new AppFailureException(AppFailureMessage.anlassIsNotAbgeschlossen());
        }
    }

    @NonNull
    public List<AnlassWithOpenSlotsOptionJson> findFutureGruppenAnlaesseOfTypeHavingLessThanMaxEinladungen(
        @NonNull List<AnlassTyp> anlassTypen,
        @NonNull ID<Kind> kindId,
        int maxEinladungen
    ) {
        return anlassRepo.findFutureGruppenAnlaesseOfTypeHavingLessThanMaxEinladungen(anlassTypen, kindId, maxEinladungen);
    }

    @NonNull
    public List<AnlassWithDateTimeJson> findScheduledAnlaesseOfTypeForKind(
        @NonNull List<AnlassTyp> anlassTypen,
        @NonNull ID<Kind> kindId,
        @NonNull List<ID<Anlass>> excludeAnlassIds
    ) {
        return anlassRepo.findScheduledAnlaesseOfTypeForKind(kindId, anlassTypen, excludeAnlassIds);
    }
}
