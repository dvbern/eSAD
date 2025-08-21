package ch.dvbern.oss.hemed.esc.api.features.impfstoff;

import java.util.List;

import ch.dvbern.dvbstarter.infrastructure.db.baserepo.BaseRepo;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.Repository;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;

@Repository
@RequiredArgsConstructor
public class ImpfstoffRepo extends BaseRepo<Impfstoff> {

    @NonNull
    public List<Impfstoff> findAll() {
        return db.selectFrom(QImpfstoff.impfstoff)
            .orderBy(QImpfstoff.impfstoff.name.asc())
            .fetch();
    }
}
