package ch.dvbern.oss.hemed.esc.api.features.anlass;

import java.util.Optional;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.dvbstarter.shared.types.id.ID;
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
    public UntersuchungDurchfuehrung getById(@NonNull ID<UntersuchungDurchfuehrung> durchfuehrungID) {
        return durchfuehrungRepo.getById(durchfuehrungID);
    }

    public void untersuchungAbschliessen(@NonNull Untersuchung untersuchung) {
        Benutzer arzt = untersuchung.getEinladung().getAnlass().getArzt();
        Benutzer mpa = untersuchung.getEinladung().getAnlass().getMpa();
        UntersuchungDurchfuehrung durchfuehrung =  UntersuchungDurchfuehrung.forUntersuchungAbschliessen(
            untersuchung,
            arzt,
            mpa
        );
        persist(durchfuehrung);
    }

    @NonNull
    public UntersuchungDurchfuehrung getDefaultUntersuchungDurchfuehrung(@NonNull Untersuchung untersuchung) {
        Optional<Einladung> urspruenglicheEinladungForNachzueglerOptional = einladungService.findUrspruenglicheEinladungForNachzuegler(
            untersuchung.getEinladung().getAnlass(), untersuchung.getEinladung().getDossier().getKind().getId());
        Nachkontrolle nachkontrolle =
            nachkontrolleService.getOrCreateByDossier(untersuchung.getEinladung().getDossier().getId());
        return UntersuchungDurchfuehrung.defaultsForNextUntersuchung(untersuchung, urspruenglicheEinladungForNachzueglerOptional.orElse(null), nachkontrolle);
    }
}
