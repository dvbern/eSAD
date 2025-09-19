package ch.dvbern.oss.hemed.esc.api.features.schule.kontaktpersonschule;

import java.util.List;
import java.util.Optional;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.schule.Schule;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@Service
@RequiredArgsConstructor
public class KontaktPersonSchuleService {

    private final KontaktPersonSchuleRepo kontaktPersonSchuleRepo;

    @NonNull
    public KontaktPersonSchule getById(@NonNull ID<KontaktPersonSchule> id) {
        return kontaktPersonSchuleRepo.getById(id);
    }

    @NonNull
    public KontaktPersonSchule persist(@NonNull KontaktPersonSchule kontaktPerson) {
        kontaktPersonSchuleRepo.persist(kontaktPerson);
        return kontaktPerson;
    }

    @NonNull
    public KontaktPersonSchule persistWithOptimisticLocking(@NonNull KontaktPersonSchule kontaktPerson, @NonNull Long versionFromFrontend) {
        kontaktPersonSchuleRepo.persistWithOptimisticLocking(kontaktPerson, versionFromFrontend);
        return kontaktPerson;
    }

    @NonNull
    public List<KontaktPersonSchule> findAktivBySchule(@NonNull ID<Schule> schuleID) {
        return kontaktPersonSchuleRepo.findAktivBySchule(schuleID);
    }

    @NonNull
    public List<KontaktPersonSchule> findAllOptionsActive(@Nullable ID<Schule> schuleToExclude) {
        return kontaktPersonSchuleRepo.findAllOptionsActive(schuleToExclude);
    }

    @NonNull
    public Optional<KontaktPersonSchule> findById(@NonNull ID<KontaktPersonSchule> id) {
        return kontaktPersonSchuleRepo.findById(id);
    }

    public void remove(@NonNull ID<KontaktPersonSchule> kontaktPersonID) {
        kontaktPersonSchuleRepo.remove(kontaktPersonID);
    }
}
