package ch.dvbern.oss.hemed.esc.api.features.kindanlaesse;

import java.util.List;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.EinladungService;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;

@Service
@RequiredArgsConstructor
public class KindAnlaesseService {

    private final KindAnlaesseRepo dossierRepo;
    private final EinladungService einladungService;

    @NonNull
    public List<Einladung> findAllEinladungForKind(@NonNull ID<Kind> kindID) {
        return dossierRepo.findAllEinladungForKind(kindID);
    }

    public void removeForKind(@NonNull ID<Kind> kindID) {
        List<Einladung> einladungenOfOtherKind = findAllEinladungForKind(kindID);
        for (Einladung einladung : einladungenOfOtherKind) {
            einladungService.removeEinladung(einladung.getId());
        }
    }
}
