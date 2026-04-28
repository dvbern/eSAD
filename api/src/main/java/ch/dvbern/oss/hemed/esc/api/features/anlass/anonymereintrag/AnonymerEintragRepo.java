package ch.dvbern.oss.hemed.esc.api.features.anlass.anonymereintrag;

import ch.dvbern.dvbstarter.infrastructure.db.baserepo.BaseRepo;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureException;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureMessage;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.Repository;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;

@Slf4j
@Repository
@RequiredArgsConstructor
public class AnonymerEintragRepo extends BaseRepo<AnonymerEintrag> {

    @Override
    @NonNull
    public AnonymerEintrag getById(@NonNull ID<AnonymerEintrag> id) {
        return db.select(QAnonymerEintrag.anonymerEintrag)
            .from(QAnonymerEintrag.anonymerEintrag)
            .where(QAnonymerEintrag.anonymerEintrag.id.eq(id.getUUID()))
            .fetchOne()
            .orElseThrow(() -> new AppFailureException(AppFailureMessage.entityNotFound(id)));
    }
}
