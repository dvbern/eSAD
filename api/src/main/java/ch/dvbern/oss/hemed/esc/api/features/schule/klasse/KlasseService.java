package ch.dvbern.oss.hemed.esc.api.features.schule.klasse;

import java.util.List;
import java.util.Optional;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.schule.Schule;
import ch.dvbern.oss.hemed.esc.api.features.schule.klasse.klasseexternal.EscadaKlasseStufe;
import ch.dvbern.oss.hemed.esc.api.features.schule.lehrperson.Lehrperson;
import ch.dvbern.oss.hemed.esc.api.shared.types.IgnoreForIdClassTest;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;

@Service
@RequiredArgsConstructor
public class KlasseService {

    private final KlasseRepo klasseRepo;

    @NonNull
    public Klasse persist(@NonNull Klasse klasse) {
        klasseRepo.persist(klasse);
        return klasse;
    }

    @NonNull
    public List<Klasse> findAll() {
        return klasseRepo.findAll();
    }

    @NonNull
    public List<Klasse> findBySchule(@NonNull ID<Schule> schuleID) {
        return klasseRepo.findBySchule(schuleID);
    }

    @NonNull
    public List<Klasse> findAktivBySchule(@NonNull ID<Schule> schuleID) {
        return klasseRepo.findAktivBySchule(schuleID);
    }

    @NonNull
    public List<EscadaKlasseStufe> extractEscadaKlassenStufen(@NonNull ID<Schule> schuleID) {
        return klasseRepo.extractEscadaKlassenStufen(schuleID);
    }

    public Klasse getById(@NotNull @NonNull ID<Klasse> id) {
        return klasseRepo.getById(id);
    }

    @NonNull
    public Optional<Klasse> findById(@NonNull ID<Klasse> klasseId) {
        return klasseRepo.findById(klasseId);
    }

    public void remove(@NonNull ID<Klasse> klasseId) {
        klasseRepo.remove(klasseId);
    }

    @NonNull
    public Klasse getByExternalId(@IgnoreForIdClassTest @NonNull String externalId) {
        return klasseRepo.getByExternalId(externalId);
    }

    @NonNull
    public Klasse getByExternalIdLockedQuery(@IgnoreForIdClassTest @NonNull String externalId) {
        return klasseRepo.getByExternalIdLockedQuery(externalId);
    }

    @NonNull
	public Optional<Klasse> findByExternalId(@IgnoreForIdClassTest @NonNull String externalId) {
        return klasseRepo.findByExternalId(externalId);
	}

    public List<Klasse> findByLehrperson(@NonNull ID<Lehrperson> id) {
        return klasseRepo.findByLehrperson(id);
    }

    public void addLehrpersonToKlasse(@NonNull Klasse klasse, @NonNull Lehrperson lehrperson) {
        klasse.addLehrperson(lehrperson);
        persist(klasse);
    }

    public void removeLehrpersonFromKlasse(@NonNull Klasse klasse, @NonNull Lehrperson lehrperson) {
        klasse.removeLehrperson(lehrperson);
        persist(klasse);
    }
}
