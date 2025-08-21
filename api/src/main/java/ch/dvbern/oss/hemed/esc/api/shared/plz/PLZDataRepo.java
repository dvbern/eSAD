package ch.dvbern.oss.hemed.esc.api.shared.plz;

import java.util.List;

import ch.dvbern.dvbstarter.infrastructure.db.baserepo.BaseRepo;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.Repository;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;

@Repository
@RequiredArgsConstructor
public class PLZDataRepo extends BaseRepo<PLZData> {

    public void createAll(@NonNull List<PLZData> plzData) {
        plzData.forEach(db::persist);
    }

    public void dropAll() {
        db.delete(QPLZData.pLZData).execute();
    }

    @NonNull
    public List<PLZData> findOrteForPLZ(String plz) {
        return db.selectFrom(QPLZData.pLZData)
            .where(QPLZData.pLZData.plz.eq(plz)
                .and(QPLZData.pLZData.plzTyp.in("10", "20"))    // Filter for "real" adresses, by "plzTyp" magic.
            )
            .fetch();
    }
}
