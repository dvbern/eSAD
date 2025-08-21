package ch.dvbern.oss.hemed.esc.api.shared.aktivitaet;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Anlass;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import ch.dvbern.oss.hemed.esc.api.shared.abstractentity.AbstractEntity;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;

@Service
@RequiredArgsConstructor
public class AktivitaetHistoryService {

    private final KindAktivitaetHistoryRepo kindAktivitaetHistoryRepo;
    private final AnlassAktivitaetHistoryRepo anlassAktivitaetHistoryRepo;

    @NonNull
    private KindAktivitaetHistory persistKindAktivitaet(@NonNull KindAktivitaetHistory kindAktivitaetHistory) {
        kindAktivitaetHistoryRepo.persist(kindAktivitaetHistory);
        return kindAktivitaetHistory;
    }

    @NonNull
    private AnlassAktivitaetHistory persistAnlassAktivitaet(@NonNull AnlassAktivitaetHistory anlassAktivitaetHistory) {
        anlassAktivitaetHistoryRepo.persist(anlassAktivitaetHistory);
        return anlassAktivitaetHistory;
    }

    @NonNull
    public List<KindAktivitaetHistory> findAllKindAktivitaetenForBenutzer(@NonNull ID<Benutzer> benutzerId) {
        return kindAktivitaetHistoryRepo.findAllForBenutzer(benutzerId);
    }
    @NonNull
    public List<AnlassAktivitaetHistory> findAllAnlassAktivitaetenForBenutzer(@NonNull ID<Benutzer> benutzerId) {
        return anlassAktivitaetHistoryRepo.findAllForBenutzer(benutzerId);
    }

    public void createAktivitaetKind(@NonNull Kind kind) {
        KindAktivitaetHistory kindAktivitaetHistory = new KindAktivitaetHistory(kind);
        persistKindAktivitaet(kindAktivitaetHistory);
    }

    public void createAktivitaetAnlass(@NonNull Anlass anlass) {
        AnlassAktivitaetHistory anlassAktivitaetHistory = new AnlassAktivitaetHistory(anlass);
        persistAnlassAktivitaet(anlassAktivitaetHistory);
    }

    public void removeForKind(@NonNull ID<Kind> kindID) {
        List<KindAktivitaetHistory> histories = kindAktivitaetHistoryRepo.findAllForKind(kindID);
        histories.stream().map(AbstractEntity::getId).forEach(kindAktivitaetHistoryRepo::remove);
    }
    public void removeForAnlass(@NonNull ID<Anlass> anlassID) {
        List<AnlassAktivitaetHistory> histories = anlassAktivitaetHistoryRepo.findAllForAnlass(anlassID);
        histories.stream().map(AbstractEntity::getId).forEach(anlassAktivitaetHistoryRepo::remove);
    }
}
