package ch.dvbern.oss.hemed.esc.api.features.arzt;

import java.util.List;
import java.util.UUID;

import ch.dvbern.dvbstarter.infrastructure.db.baserepo.BaseRepo;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.Repository;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;

@Repository
@RequiredArgsConstructor
public class ArztRepo extends BaseRepo<Arzt> {

    @NonNull
    public List<Arzt> findAll() {
        return db.selectFrom(QArzt.arzt)
            .orderBy(QArzt.arzt.name.asc())
            .fetch();
    }

    @NonNull
    public List<Arzt> findByIds(@NonNull List<ID<Arzt>> ids) {
        List<UUID> uuidList = ids.stream().map(ID::getUUID).toList();
        return db.selectFrom(QArzt.arzt)
            .where(QArzt.arzt.id.in(uuidList))
            .orderBy(QArzt.arzt.name.asc())
            .fetch();
    }
}
