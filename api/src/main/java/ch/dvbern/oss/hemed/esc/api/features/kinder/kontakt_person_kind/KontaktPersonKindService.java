package ch.dvbern.oss.hemed.esc.api.features.kinder.kontakt_person_kind;

import java.util.List;
import java.util.Optional;

import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppValidationException;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppValidationMessage;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import ch.dvbern.oss.hemed.esc.api.features.kinder.KindService;
import ch.dvbern.oss.hemed.esc.api.shared.types.IgnoreForIdClassTest;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@Service
@RequiredArgsConstructor
public class KontaktPersonKindService {

    private final KontaktPersonKindRepo kontaktPersonKindRepo;
    private final KindService kindService;

    @NonNull
    public KontaktPersonKind getById(@NonNull ID<KontaktPersonKind> id) {
        return kontaktPersonKindRepo.getById(id);
    }

    @NonNull
    public Optional<KontaktPersonKind> findById(@NonNull ID<KontaktPersonKind> id) {
        return kontaktPersonKindRepo.findById(id);
    }

    @NonNull
    public KontaktPersonKind persist(@NonNull KontaktPersonKind kontaktPerson) {
        kontaktPersonKindRepo.persist(kontaktPerson);
        return kontaktPerson;
    }

    @NonNull
    public List<KontaktPersonKind> findAll() {
        return kontaktPersonKindRepo.findAll();
    }

    @NonNull
    public List<KontaktPersonKind> findAktivByKind(@NonNull ID<Kind> kindID) {
        return kontaktPersonKindRepo.findAktivByKind(kindID);
    }

    @NonNull
    public List<KontaktPersonKind> findByKind(@NonNull ID<Kind> kindID) {
        return kontaktPersonKindRepo.findByKind(kindID);
    }

    @NonNull
    public List<KontaktPersonKind> findAllAktivOptions(@Nullable ID<Kind> kindToExclude) {
        return kontaktPersonKindRepo.findAllOptionsActive(kindToExclude);
    }

    @NonNull
    public KontaktPersonKind getByExternalId(@IgnoreForIdClassTest @NonNull String externalId) {
        return kontaktPersonKindRepo.getByExternalId(externalId);
    }

    @NonNull
    public Optional<KontaktPersonKind> findByExternalId(@IgnoreForIdClassTest @NonNull String externalId) {
        return kontaktPersonKindRepo.findByExternalId(externalId);
    }

    public void remove(@NonNull ID<KontaktPersonKind> kontaktPersonID) {
        KontaktPersonKind kp = getKontaktPersonKindAndEnsureNotEscada(kontaktPersonID);
        kontaktPersonKindRepo.remove(kp.getId());
    }

    public void removeForKind(@NonNull ID<Kind> kindID) {
        List<KontaktPersonKind> kpOfOtherKind = findByKind(kindID);
        for (KontaktPersonKind kontaktPersonKind : kpOfOtherKind) {
            removeFromKind(kindID, kontaktPersonKind.getId());
        }
    }

    public void removeFromKind(@NonNull ID<Kind> kindID, @NonNull ID<KontaktPersonKind> kontaktPersonKindID) {
        KontaktPersonKind kp = getKontaktPersonKindAndEnsureNotEscada(kontaktPersonKindID);
        Kind kind = kindService.getById(kindID);
        kp.getKinder().remove(kind);
        persist(kp);
        // Loeschen, falls es die letzte Verknuepfung war
        if (kp.getKinder().isEmpty()) {
            remove(kp.getId());
        }
    }

    @NonNull
    public KontaktPersonKind addToKind(
        @NonNull ID<Kind> kindID,
        @NonNull ID<KontaktPersonKind> kontaktPersonKindID
    ) {
        KontaktPersonKind kp = getKontaktPersonKindAndEnsureNotEscada(kontaktPersonKindID);
        Kind kind = kindService.getById(kindID);
        kp.getKinder().add(kind);
        return persist(kp);
    }

    @NonNull
    private KontaktPersonKind getKontaktPersonKindAndEnsureNotEscada(
        @NonNull ID<KontaktPersonKind> kontaktPersonID
    ) {
        KontaktPersonKind kp = getById(kontaktPersonID);
        // Darf nicht editiert werden, wenn aus ESACADA importiert und dort noch AKTIV
        if (kp.getExternalImportInfo() != null && kp.getExternalImportInfo().isAktivInExternalSystem()) {
            throw new AppValidationException(AppValidationMessage.escadaObjectsCannotBeUpdated(kp));
        }
        return kp;
    }
}
