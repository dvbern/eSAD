package ch.dvbern.oss.hemed.esc.api.features.schule.quartier;

import java.util.List;

import ch.dvbern.dvbstarter.infrastructure.db.baserepo.BaseRepo;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.Repository;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;

@Repository
@RequiredArgsConstructor
public class QuartierRepo extends BaseRepo<Quartier> {

    @NonNull
    public List<Quartier> findAll() {
        return db.findAll(QQuartier.quartier);
    }
}
