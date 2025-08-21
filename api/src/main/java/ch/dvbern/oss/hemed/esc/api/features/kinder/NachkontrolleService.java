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

    @NonNull
    public Nachkontrolle getOrCreateByDossier(@NonNull ID<Dossier> dossierID) {
        Optional<Nachkontrolle> nachkontrolleOptional = nachkontrolleRepo.findByDossierId(dossierID);
        return nachkontrolleOptional.orElseGet(() -> {
            Dossier dossier = dossierService.getById(dossierID);
            Nachkontrolle nachkontrolle = Nachkontrolle.forDossier(dossier);
            return persist(nachkontrolle);
        });
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

    public void updateNachkontrolle(@NonNull ID<Dossier> dossierID, @NonNull AnlassFlags flags) {
        Nachkontrolle nachkontrolle = getOrCreateByDossier(dossierID);
        nachkontrolle.setAnlassFlags(flags);
        nachkontrolleRepo.persist(nachkontrolle);
    }

    public void removeForDossier(@NonNull ID<Dossier> dossierID) {
        Optional<Nachkontrolle> nachkontrolleOptional = nachkontrolleRepo.findByDossierId(dossierID);
        nachkontrolleOptional.ifPresent(nachkontrolle -> nachkontrolleRepo.remove(nachkontrolle.getId()));
    }
}
