package ch.dvbern.oss.hemed.esc.api.features.kindanlaesse;

import java.util.List;

import ch.dvbern.dvbstarter.infrastructure.db.baserepo.BaseRepo;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.Repository;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.QAnlass;
import ch.dvbern.oss.hemed.esc.api.features.anlass.QEinladung;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import ch.dvbern.oss.hemed.esc.api.features.kinder.QKind;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;

@Repository
@RequiredArgsConstructor
public class KindAnlaesseRepo extends BaseRepo<Einladung> {

    @NonNull
    public List<Einladung> findAllEinladungForKind(@NonNull ID<Kind> kindId) {
        return db.selectFrom(QEinladung.einladung)
            .leftJoin(QEinladung.einladung.dossier.kind, QKind.kind)
            .innerJoin(QEinladung.einladung.anlass, QAnlass.anlass)
            .where(QKind.kind.id.eq(kindId.getUUID()))
            .orderBy(QAnlass.anlass.datum.asc())
            .fetch();
    }
}
