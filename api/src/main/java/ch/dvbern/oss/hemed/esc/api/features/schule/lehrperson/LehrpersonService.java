package ch.dvbern.oss.hemed.esc.api.features.schule.lehrperson;

import java.util.List;
import java.util.Optional;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.schule.klasse.Klasse;
import ch.dvbern.oss.hemed.esc.api.shared.types.IgnoreForIdClassTest;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@Service
@RequiredArgsConstructor
public class LehrpersonService {

    private final LehrpersonRepo lehrpersonRepo;

    @NonNull
    public Lehrperson getById(@NonNull ID<Lehrperson> id) {
        return lehrpersonRepo.getById(id);
    }

    @NonNull
    public Lehrperson persist(@NonNull Lehrperson lehrperson) {
        lehrpersonRepo.persist(lehrperson);
        return lehrperson;
    }

    @NonNull
    public List<Lehrperson> findAktivByKlasse(@NonNull ID<Klasse> klasseId) {
        return lehrpersonRepo.findAktivByKlasse(klasseId);
    }

    @NonNull
    public List<Lehrperson> findAllOptionsActive(@Nullable ID<Klasse> klasseToExclude) {
        return lehrpersonRepo.findAllOptionsActive(klasseToExclude);
    }

    @NonNull
    public List<Lehrperson> findByKlasse(@NonNull ID<Klasse> klasseId) {
        return lehrpersonRepo.findByKlasse(klasseId);
    }

    @NonNull
    public Optional<Lehrperson> findById(@NonNull ID<Lehrperson> id) {
        return lehrpersonRepo.findById(id);
    }

    public void remove(@NonNull ID<Lehrperson> lehrpersonID) {
        lehrpersonRepo.remove(lehrpersonID);
    }

    @NonNull
    public Lehrperson getByExternalId(@IgnoreForIdClassTest @NonNull String externalId) {
        return lehrpersonRepo.getByExternalId(externalId);
    }

    @NonNull
    public Optional<Lehrperson> findByExternalId(@IgnoreForIdClassTest @NonNull String externalId) {
        return lehrpersonRepo.findByExternalId(externalId);
    }

    @NonNull
    public List<Lehrperson> findAll() {
        return lehrpersonRepo.findAll();
    }
}
