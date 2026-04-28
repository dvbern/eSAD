package ch.dvbern.oss.hemed.esc.api.features.anlass.durchfuehrung;

import java.util.Optional;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.EinladungService;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Nachkontrolle;
import ch.dvbern.oss.hemed.esc.api.features.kinder.NachkontrolleService;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;

@Service
@RequiredArgsConstructor
public class UntersuchungDurchfuehrungService {

    private final UntersuchungDurchfuehrungRepo durchfuehrungRepo;
    private final EinladungService einladungService;
    private final NachkontrolleService nachkontrolleService;


    @NonNull
    public UntersuchungDurchfuehrung persist(@NonNull UntersuchungDurchfuehrung durchfuehrung) {
        durchfuehrungRepo.persist(durchfuehrung);
        return durchfuehrung;
    }

    @NonNull
    public UntersuchungDurchfuehrung persistWithOptimisticLocking(@NonNull UntersuchungDurchfuehrung durchfuehrung, @NonNull Long versionFromFrontend) {
        return durchfuehrungRepo.persistWithOptimisticLocking(durchfuehrung, versionFromFrontend);
    }

    @NonNull
    public UntersuchungDurchfuehrung getById(@NonNull ID<UntersuchungDurchfuehrung> durchfuehrungID) {
        return durchfuehrungRepo.getById(durchfuehrungID);
    }

    public void untersuchungAbschliessen(@NonNull Untersuchung untersuchung) {
        Benutzer arzt = untersuchung.getAnlass().getArzt();
        Benutzer mpa = untersuchung.getAnlass().getMpa();
        UntersuchungDurchfuehrung durchfuehrung =  UntersuchungDurchfuehrung.forUntersuchungAbschliessen(
            untersuchung,
            arzt,
            mpa
        );
        durchfuehrung.disableOptimisticLockingCheck();
        persist(durchfuehrung);
    }

    @NonNull
    public UntersuchungDurchfuehrung getDefaultUntersuchungDurchfuehrung(@NonNull Untersuchung untersuchung) {
        Optional<Einladung> urspruenglicheEinladungForNachzueglerOptional = einladungService.findUrspruenglicheEinladungForNachzuegler(
            untersuchung.getAnlass(), untersuchung.getEinladungRequired().getDossier().getKind().getId());
        Nachkontrolle nachkontrolle =
            nachkontrolleService.getOrCreateByDossier(untersuchung.getEinladungRequired().getDossier().getId());
        return UntersuchungDurchfuehrung.defaultsForNextUntersuchung(untersuchung, urspruenglicheEinladungForNachzueglerOptional.orElse(null), nachkontrolle);
    }
}
