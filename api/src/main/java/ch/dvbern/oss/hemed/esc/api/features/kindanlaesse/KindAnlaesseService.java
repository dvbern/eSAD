package ch.dvbern.oss.hemed.esc.api.features.kindanlaesse;

import java.util.ArrayList;
import java.util.List;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Anlass;
import ch.dvbern.oss.hemed.esc.api.features.anlass.AnlassService;
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
    private final AnlassService anlassService;

    @NonNull
    public List<Einladung> findAllEinladungForKind(@NonNull ID<Kind> kindID) {
        return dossierRepo.findAllEinladungForKind(kindID);
    }

    public void removeForKind(@NonNull ID<Kind> kindID, boolean deleteAnlassIfLastEinladung) {
        // Problem: Wenn ein Kind im Anlass1 eine Impfung1 hat, wird diese auf Anlass2 kopiert, referenziert
        // aber noch Anlass1 (anlassImpfungErstellt). Wir müssen daher die Anlässe vorerst nur sammeln,
        // Die Einladungen mit deleteAnlassIfLastEinladung=false löschen, und erst danach falls notwendig
        // die Anlässe löschen
        List<Einladung> einladungenOfOtherKind = findAllEinladungForKind(kindID);
        List<ID<Anlass>> anlassIDsToPotentiallyDelete = new ArrayList<>();
        for (Einladung einladung : einladungenOfOtherKind) {
            if (deleteAnlassIfLastEinladung) {
                anlassIDsToPotentiallyDelete.add(einladung.getAnlass().getId());
            }
            einladungService.removeEinladung(einladung.getId(), false);
        }
        // Jetzt sind sicher alle Referenzen von Impfungen auf Anlässe weg. Falls nötig, können wir
        // nun die Anlässe löschen
        for (ID<Anlass> anlassID : anlassIDsToPotentiallyDelete) {
            List<Einladung> einladungenAnlass = einladungService.findByAnlass(anlassID);
            if (einladungenAnlass.isEmpty()) {
                anlassService.remove(anlassID);
            }
        }
    }
}
