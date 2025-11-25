package ch.dvbern.oss.hemed.esc.api.features.anlass.impfung;

import java.util.List;
import java.util.Optional;

import ch.dvbern.dvbstarter.infrastructure.db.baserepo.BaseRepo;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.Repository;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.QEinladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.QUntersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import ch.dvbern.oss.hemed.esc.api.features.kinder.QDossier;
import ch.dvbern.oss.hemed.esc.api.features.kinder.QKind;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;

@Repository
@RequiredArgsConstructor
public class UntersuchungImpfungInternRepo extends BaseRepo<UntersuchungImpfungIntern> {

    @NonNull
    public List<UntersuchungImpfungIntern> findByUntersuchung(
        @NonNull ID<Untersuchung> untersuchungID
    ) {
        return db.selectFrom(QUntersuchungImpfungIntern.untersuchungImpfungIntern)
            .innerJoin(
                QUntersuchungImpfungIntern.untersuchungImpfungIntern.untersuchungImpfungen,
                QUntersuchungImpfungen.untersuchungImpfungen)
            .innerJoin(QUntersuchungImpfungen.untersuchungImpfungen.untersuchung, QUntersuchung.untersuchung)
            .leftJoin(QUntersuchungImpfungIntern.untersuchungImpfungIntern.krankheitenImmunitaeten).fetchJoin()
            .where(QUntersuchung.untersuchung.id.eq(untersuchungID.getUUID()))
            .fetch();
    }

    @NonNull
    public Optional<UntersuchungImpfungIntern> findMostRecentlyChangedByDossier(@NonNull ID<Kind> kindId) {
        UntersuchungImpfungIntern untersuchungImpfungIntern =
            db.selectDistinct(QUntersuchungImpfungIntern.untersuchungImpfungIntern)
                .from(QUntersuchungImpfungIntern.untersuchungImpfungIntern)
                .innerJoin(
                    QUntersuchungImpfungIntern.untersuchungImpfungIntern.untersuchungImpfungen,
                    QUntersuchungImpfungen.untersuchungImpfungen)
                .innerJoin(QUntersuchungImpfungen.untersuchungImpfungen.untersuchung, QUntersuchung.untersuchung)
                .innerJoin(QUntersuchung.untersuchung.einladung, QEinladung.einladung)
                .innerJoin(QEinladung.einladung.dossier, QDossier.dossier)
                .leftJoin(QDossier.dossier.kind, QKind.kind)
                .where(QKind.kind.id.eq(kindId.getUUID()))
                .where(QUntersuchungImpfungIntern.untersuchungImpfungIntern.timestampLastManualChange.isNotNull())
                .orderBy(QUntersuchungImpfungIntern.untersuchungImpfungIntern.timestampLastManualChange.desc())
                .fetchFirst();
        return Optional.ofNullable(untersuchungImpfungIntern);
    }

}
