package ch.dvbern.oss.hemed.esc.api.features.kinder;

import java.util.List;
import java.util.Optional;

import ch.dvbern.dvbstarter.infrastructure.db.baserepo.BaseRepo;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.Repository;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.schule.QSchule;
import ch.dvbern.oss.hemed.esc.api.features.schule.Schule;
import ch.dvbern.oss.hemed.esc.api.shared.types.EntityAktivStatus;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;

@Repository
@RequiredArgsConstructor
public class DossierRepo extends BaseRepo<Dossier> {

    @NonNull
    public Optional<Dossier> findByKind(@NonNull ID<Kind> kindID) {
        return db
            .selectFrom(QDossier.dossier)
            .innerJoin(QDossier.dossier.kind)
            .where(QKind.kind.id.eq(kindID.getUUID()))
            .fetchOne();
    }

    @NonNull
    public List<Dossier> findBySchule(@NonNull ID<Schule> schuleId) {
        return db.selectFrom(QDossier.dossier)
            .innerJoin(QDossier.dossier.kind, QKind.kind)
            .innerJoin(QKind.kind.schule, QSchule.schule) // trust schule on kind to be same as schule on klasse
            .where(QSchule.schule.id.eq(schuleId.getUUID())
                .and(QKind.kind.kindAktivStatus.ne(EntityAktivStatus.INAKTIV)))
            .orderBy(QKind.kind.name.asc(), QKind.kind.vorname.asc())
            .fetch();
    }
}
