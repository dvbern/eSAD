package ch.dvbern.oss.hemed.esc.api.features.briefvorlagen;

import java.util.List;

import ch.dvbern.dvbstarter.infrastructure.db.baserepo.BaseRepo;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.Repository;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;

@Repository
@RequiredArgsConstructor
public class BriefvorlageRepo extends BaseRepo<Briefvorlage> {

    @NonNull
    public List<Briefvorlage> findAll() {
        return db.selectFrom(QBriefvorlage.briefvorlage)
            .orderBy(QBriefvorlage.briefvorlage.name.asc())
            .fetch();
    }
}
