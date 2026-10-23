package ch.dvbern.oss.hemed.esc.api.features.kinder;

import java.util.List;
import java.util.Optional;

import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppValidationMessage;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.schule.Schule;
import ch.dvbern.oss.hemed.esc.api.shared.types.IgnoreForIdClassTest;
import ch.dvbern.oss.hemed.esc.api.shared.util.KindValidationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;

@Service
@RequiredArgsConstructor
@Slf4j
public class DossierService {

    private final DossierRepo dossierRepo;
    private final KindService kindService;

    @NonNull
    public Dossier persist(@NonNull Dossier dossier) {
        dossierRepo.persist(dossier);
        return dossier;
    }

    @NonNull
    public Dossier persistWithOptimisticLocking(@NonNull Dossier dossier, @NonNull Long versionFromFrontend) {
        dossierRepo.persistWithOptimisticLocking(dossier, versionFromFrontend);
        return dossier;
    }

    public void remove(@NonNull ID<Dossier> dossierID) {
        LOG.info("ESC-DELETE-DOSSIER: Deleting Dossier with ID {}", dossierID.getId());
        dossierRepo.remove(dossierID);
    }

    @NonNull
    public Dossier getById(@NonNull ID<Dossier> dossierID) {
        return dossierRepo.getById(dossierID);
    }

    @NonNull
    public Optional<Dossier> findByIdLockedQuery(@NonNull ID<Dossier> dossierID) {
        return dossierRepo.findByIdLockedQuery(dossierID);
    }

    @NonNull
    public Dossier getByIdEnsureActive(@NonNull ID<Dossier> dossierID) {
        Dossier existingDossier = getById(dossierID);
        KindValidationUtil.throwErrorIfKindIsInactive(
            existingDossier.getKind(),
            AppValidationMessage.cannotEditEntityForInactiveKind(
                existingDossier.getKind(),
                existingDossier
            ));
        return existingDossier;
    }

    @NonNull
    public Dossier getOrCreateByKind(@NonNull ID<Kind> kindID) {
        Optional<Dossier> dossierOptional = dossierRepo.findByKind(kindID);
        return dossierOptional.orElseGet(() -> {
            Kind kind = kindService.getById(kindID);
            return persist(Dossier.forKind(kind));
        });
    }

    @NonNull
    public Optional<Dossier> findByKind(@NonNull ID<Kind> kindID) {
        return dossierRepo.findByKind(kindID);
    }

    @NonNull
    public ID<Dossier> getDossierIdByKind(@NonNull ID<Kind> kindID) {
        Optional<Dossier> dossierOptional = dossierRepo.findByKind(kindID);
        return dossierOptional.orElseThrow().getId();
    }

    @NonNull
    public Optional<Dossier> findByEsadId(@IgnoreForIdClassTest @NonNull String externalId) {
        return dossierRepo.findByEsadId(externalId);
    }

    @NonNull
    public List<Dossier> findBySchule(@NonNull ID<Schule> schuleId) {
        return dossierRepo.findBySchule(schuleId);
    }
}
