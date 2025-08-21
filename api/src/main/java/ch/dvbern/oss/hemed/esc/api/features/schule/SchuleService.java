package ch.dvbern.oss.hemed.esc.api.features.schule;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.shared.types.IgnoreForIdClassTest;
import com.cronutils.utils.StringUtils;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import static ch.dvbern.oss.hemed.esc.api.features.schule.EscadaDummySchulenIds.DUMMY_SCHULE_ID;

@Service
@RequiredArgsConstructor
public class SchuleService {

    private final SchuleRepo schuleRepo;

    public List<Schule> findAll() {
        return schuleRepo.findAll();
    }

    @NonNull
    public List<Schule> findAllAktiv() {
        return schuleRepo.findAllAktiv();
    }

    @NonNull
    public Optional<Schule> findById(@NonNull ID<Schule> id) {
        return schuleRepo.findById(id);
    }

    @NonNull
    public Schule getById(@NonNull ID<Schule> id) {
        return schuleRepo.getById(id);
    }

    @NonNull
    public Schule persist(@NonNull Schule schule) {
        schuleRepo.persist(schule);
        return schule;
    }

    public void remove(@NonNull ID<Schule> id) {
        schuleRepo.remove(id);
    }

    @NonNull
    public Schule getByExternalId(@IgnoreForIdClassTest @NonNull String externalId) {
        return schuleRepo.getByExternalId(externalId);
    }

    public Optional<Schule> findByExternalId(@IgnoreForIdClassTest @NonNull String externalId) {
        return schuleRepo.findByExternalId(externalId);
    }

    @NonNull
    public Schule getDummySchuleForUnassignedEscadaKlassen(@Nullable String schulkreisBezeichnung) {
        if (StringUtils.isEmpty(schulkreisBezeichnung)) {
            return schuleRepo.getById(DUMMY_SCHULE_ID);
        }
        ID<Schule> schuleID = EscadaDummySchulenIds.getDummySchuleForUnassignedEscadaKlassen(
            Objects.requireNonNull(schulkreisBezeichnung)
        );
        return schuleRepo.getById(Objects.requireNonNullElse(schuleID, DUMMY_SCHULE_ID));
    }
}
