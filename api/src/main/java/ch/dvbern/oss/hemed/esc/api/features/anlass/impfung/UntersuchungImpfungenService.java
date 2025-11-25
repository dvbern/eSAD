package ch.dvbern.oss.hemed.esc.api.features.anlass.impfung;

import java.util.List;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.dvbstarter.shared.clock.AppClock;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.UntersuchungRepo;
import ch.dvbern.oss.hemed.esc.api.features.benutzer.BenutzerService;
import ch.dvbern.oss.hemed.esc.api.shared.aufgabe.AutoAufgabeService;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;

@Service
@RequiredArgsConstructor
public class UntersuchungImpfungenService {

    private final BenutzerService benutzerService;
    private final UntersuchungImpfungenRepo untersuchungImpfungenRepo;
    private final UntersuchungRepo untersuchungRepo;
    private final AutoAufgabeService autoAufgabeService;
    private final UntersuchungImpfstatusKrankheitRepo untersuchungImpfstatusKrankheitRepo;
    private final UntersuchungImpfungInternRepo untersuchungImpfungInternRepo;
    private final AppClock appClock;

    @NonNull
    public UntersuchungImpfungen persist(@NonNull UntersuchungImpfungen impfungen) {
        untersuchungImpfungenRepo.persist(impfungen);
        return impfungen;
    }

    @NonNull
    public UntersuchungImpfungen persistWithOptimisticLocking(@NonNull UntersuchungImpfungen entity, @NonNull Long versionFromFrontend) {
        return untersuchungImpfungenRepo.persistWithOptimisticLocking(entity, versionFromFrontend);
    }

    @NonNull
    public UntersuchungImpfungen getById(@NonNull ID<UntersuchungImpfungen> impfungenID) {
        return untersuchungImpfungenRepo.getById(impfungenID);
    }

    @NonNull
    public UntersuchungImpfungen getOrCreateUntersuchungImpfungen(@NonNull ID<Untersuchung> untersuchungID) {
        Untersuchung untersuchung = untersuchungRepo.getById(untersuchungID);
        if (untersuchung.getUntersuchungImpfungen() == null) {
            UntersuchungImpfungen impfungen = UntersuchungImpfungen.forUntersuchung(untersuchung);
            untersuchungImpfungenRepo.persist(impfungen);
            untersuchung.setUntersuchungImpfungen(impfungen);
        }
        return untersuchung.getUntersuchungImpfungen();
    }

    @NonNull
    public UntersuchungImpfungen visierenDoppelkontrolleVisum1(
        @NonNull UntersuchungImpfungen untersuchungImpfung,
        @NonNull Long versionFromFrontend
    ) {
        Benutzer currentUser = benutzerService.getCurrentBenutzerOrThrowException();
        untersuchungImpfung.setVisum1(new Visum(currentUser, appClock.offsetDateTime().now().toLocalDate()));
        untersuchungImpfungenRepo.persistWithOptimisticLocking(untersuchungImpfung, versionFromFrontend);
        return untersuchungImpfung;
    }

    @NonNull
    public UntersuchungImpfungen anfordernDoppelkontrolleVisum2(
        @NonNull UntersuchungImpfungen untersuchungImpfung,
        @NonNull Long versionFromFrontend,
        @NonNull ID<Benutzer> id
    ) {
        Benutzer empfaenger = benutzerService.getById(id);
        untersuchungImpfung.setVisum2(new Visum(empfaenger, null));
        untersuchungImpfungenRepo.persistWithOptimisticLocking(untersuchungImpfung, versionFromFrontend);
        autoAufgabeService.createAufgabeDoppelkontrolleImpfentscheid(
            untersuchungImpfung.getUntersuchung().getEinladung(),
            empfaenger
        );
        return untersuchungImpfung;
    }

    @NonNull
    public UntersuchungImpfungen visierenDoppelkontrolleVisum2(
        @NonNull UntersuchungImpfungen untersuchungImpfung,
        @NonNull Long versionFromFrontend
    ) {
        Benutzer currentUser = benutzerService.getCurrentBenutzerOrThrowException();
        untersuchungImpfung.setVisum2(new Visum(currentUser, appClock.offsetDateTime().now().toLocalDate()));
        untersuchungImpfungenRepo.persistWithOptimisticLocking(untersuchungImpfung, versionFromFrontend);
        return untersuchungImpfung;
    }

    @NonNull
    public UntersuchungImpfungen resetDoppelkontrolle(
        @NonNull UntersuchungImpfungen untersuchungImpfung,
        @NonNull Long versionFromFrontend
    ) {
        untersuchungImpfung.setVisum1(new Visum(null, null));
        untersuchungImpfung.setVisum2(new Visum(null, null));
        untersuchungImpfungenRepo.persistWithOptimisticLocking(untersuchungImpfung, versionFromFrontend);
        return untersuchungImpfung;
    }

    public void removeByUntersuchung(@NonNull Untersuchung untersuchung) {
        List<UntersuchungImpfstatusKrankheit> impfstatusList =
            untersuchungImpfstatusKrankheitRepo.findByUntersuchung(untersuchung.getId());
        for (UntersuchungImpfstatusKrankheit impfstatus : impfstatusList) {
            untersuchungImpfstatusKrankheitRepo.remove(impfstatus.getId());
        }
        List<UntersuchungImpfungIntern> impfungenInternList =
            untersuchungImpfungInternRepo.findByUntersuchung(untersuchung.getId());
        for (UntersuchungImpfungIntern impfungIntern : impfungenInternList) {
            untersuchungImpfungInternRepo.remove(impfungIntern.getId());
        }
    }

    public void untersuchungAbschliessen(@NonNull Untersuchung untersuchung) {
        UntersuchungImpfungen impfungen = getOrCreateUntersuchungImpfungen(untersuchung.getId());
        persist(impfungen);
    }
}
