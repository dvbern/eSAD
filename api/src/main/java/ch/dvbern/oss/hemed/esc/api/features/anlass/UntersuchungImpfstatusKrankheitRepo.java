package ch.dvbern.oss.hemed.esc.api.features.anlass;

import java.util.List;
import java.util.Optional;

import ch.dvbern.dvbstarter.infrastructure.db.baserepo.BaseRepo;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.Repository;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.impfstoff.types.Krankheit;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import ch.dvbern.oss.hemed.esc.api.features.kinder.QDossier;
import ch.dvbern.oss.hemed.esc.api.features.kinder.QKind;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;

@Repository
@RequiredArgsConstructor
public class UntersuchungImpfstatusKrankheitRepo extends BaseRepo<UntersuchungImpfstatusKrankheit> {

    @NonNull
    public List<UntersuchungImpfstatusKrankheit> findByUntersuchung(
        @NonNull ID<Untersuchung> untersuchungID
    ) {
        return db.selectFrom(QUntersuchungImpfstatusKrankheit.untersuchungImpfstatusKrankheit)
            .innerJoin(
                QUntersuchungImpfstatusKrankheit.untersuchungImpfstatusKrankheit.untersuchungImpfungen,
                QUntersuchungImpfungen.untersuchungImpfungen)
            .innerJoin(QUntersuchungImpfungen.untersuchungImpfungen.untersuchung, QUntersuchung.untersuchung)
            .where(QUntersuchung.untersuchung.id.eq(untersuchungID.getUUID()))
            .fetch();
    }

    @NonNull
    public Optional<UntersuchungImpfstatusKrankheit> findByUntersuchungAndKrankheit(
        @NonNull ID<UntersuchungImpfungen> untersuchungImpfungenID,
        @NonNull Krankheit krankheit
    ) {
        return db.selectFrom(QUntersuchungImpfstatusKrankheit.untersuchungImpfstatusKrankheit)
            .innerJoin(
                QUntersuchungImpfstatusKrankheit.untersuchungImpfstatusKrankheit.untersuchungImpfungen,
                QUntersuchungImpfungen.untersuchungImpfungen)
            .where(QUntersuchungImpfungen.untersuchungImpfungen.id.eq(untersuchungImpfungenID.getUUID())
                .and(QUntersuchungImpfstatusKrankheit.untersuchungImpfstatusKrankheit.krankheit.eq(krankheit)))
            .fetchOne();
    }

    @NonNull
    public Optional<UntersuchungImpfstatusKrankheit> findMostRecentlyChangedByDossier(@NonNull ID<Kind> kindId) {
        UntersuchungImpfstatusKrankheit untersuchungImpfstatusKrankheit =
            db.select(QUntersuchungImpfstatusKrankheit.untersuchungImpfstatusKrankheit)
                .from(QUntersuchungImpfstatusKrankheit.untersuchungImpfstatusKrankheit)
                .innerJoin(
                    QUntersuchungImpfstatusKrankheit.untersuchungImpfstatusKrankheit.untersuchungImpfungen,
                    QUntersuchungImpfungen.untersuchungImpfungen)
                .innerJoin(QUntersuchungImpfungen.untersuchungImpfungen.untersuchung, QUntersuchung.untersuchung)
                .innerJoin(QUntersuchung.untersuchung.einladung, QEinladung.einladung)
                .innerJoin(QEinladung.einladung.dossier, QDossier.dossier)
                .leftJoin(QDossier.dossier.kind, QKind.kind)
                .where(QKind.kind.id.eq(kindId.getUUID()))
                .where(QUntersuchungImpfstatusKrankheit.untersuchungImpfstatusKrankheit.timestampLastManualChange.isNotNull())
                .orderBy(QUntersuchungImpfstatusKrankheit.untersuchungImpfstatusKrankheit.timestampLastManualChange.desc())
                .fetchFirst();
        return Optional.ofNullable(untersuchungImpfstatusKrankheit);
    }
}
