package ch.dvbern.oss.hemed.esc.api.features.kinder;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.shared.anlass.anlassflags.AnlassFlags;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class NachkontrolleService {

    private final NachkontrolleRepo nachkontrolleRepo;
    private final DossierService dossierService;

    /**
     * Returns the Nachkontrolle of a Dossier, creating it the first time it is needed.
     * <p>
     * Several requests can need it for the first time at once - loading the Einladungen of an Anlass
     * creates the Nachkontrolle of every Kind in it, opening a dossier does so for one - and would all
     * insert one, so that all but the first fail on the unique dossier_id. Only when the Nachkontrolle
     * is missing does this lock the Dossier and look again, so those requests queue up and reuse the
     * first one's. Once the Nachkontrolle exists, which is nearly always, nothing is locked.
     */
    @NonNull
    public Nachkontrolle getOrCreateByDossier(@NonNull ID<Dossier> dossierID) {
        Optional<Nachkontrolle> existing = nachkontrolleRepo.findByDossierId(dossierID);
        if (existing.isPresent()) {
            return existing.get();
        }
        Optional<Dossier> lockedDossier = dossierService.findByIdLockedQuery(dossierID);
        // created by this very transaction and not written yet, so no other request can reach it
        return lockedDossier.map(dossier -> nachkontrolleRepo.findByDossierId(dossierID)
                .orElseGet(() -> persist(Nachkontrolle.forDossier(dossier))))
            .orElseGet(() -> persist(Nachkontrolle.forDossier(dossierService.getById(dossierID))));
    }

    @NonNull
    public Optional<Nachkontrolle> findByDossier(@NonNull ID<Dossier> dossierID) {
        return nachkontrolleRepo.findByDossierId(dossierID);
    }

    @NonNull
    public Nachkontrolle persist(@NonNull Nachkontrolle nachkontrolle) {
        nachkontrolleRepo.persist(nachkontrolle);
        return nachkontrolle;
    }

    @NonNull
    public Nachkontrolle persistWithOptimisticLocking(@NonNull Nachkontrolle nachkontrolle, @NonNull Long versionFromFrontend) {
        nachkontrolleRepo.persistWithOptimisticLocking(nachkontrolle, versionFromFrontend);
        return nachkontrolle;
    }

    public void updateNachkontrolle(@NonNull ID<Dossier> dossierID, @NonNull AnlassFlags flags) {
        Nachkontrolle nachkontrolle = getOrCreateByDossier(dossierID);
        nachkontrolle.disableOptimisticLockingCheck();
        nachkontrolle.setAnlassFlags(flags);
        nachkontrolleRepo.persist(nachkontrolle);
    }

    public void removeForDossier(@NonNull ID<Dossier> dossierID) {
        Optional<Nachkontrolle> nachkontrolleOptional = nachkontrolleRepo.findByDossierId(dossierID);
        nachkontrolleOptional.ifPresent(nachkontrolle -> nachkontrolleRepo.remove(nachkontrolle.getId()));
    }
}
