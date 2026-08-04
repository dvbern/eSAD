package ch.dvbern.oss.hemed.esc.api.features.anlass.impfung;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import ch.dvbern.dvbstarter.infrastructure.db.SmartJPAQuery;
import ch.dvbern.dvbstarter.infrastructure.db.baserepo.BaseRepo;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.Repository;
import ch.dvbern.dvbstarter.shared.clock.AppClock;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.QAnlass;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.QAnlassConfig;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.types.RequirementSetting;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.QEinladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.QUntersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.impfstoff.ImpfstoffCharge;
import ch.dvbern.oss.hemed.esc.api.features.impfstoff.QImpfstoffCharge;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import ch.dvbern.oss.hemed.esc.api.features.kinder.QDossier;
import ch.dvbern.oss.hemed.esc.api.features.kinder.QKind;
import com.querydsl.jpa.JPAExpressions;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@Repository
@RequiredArgsConstructor
public class UntersuchungImpfungInternRepo extends BaseRepo<UntersuchungImpfungIntern> {

    private final AppClock appClock;

    @NonNull
    public List<UntersuchungImpfungIntern> findByUntersuchung(
        @NonNull ID<Untersuchung> untersuchungID
    ) {
        return findByUntersuchungBaseQuery(untersuchungID).fetch();
    }

    /**
     * This method fetches the attached Anlass or the UntersuchungImpfungIntern,
     * used for CSV report where we do not have
     * a transaction.
     *
     */
    @NonNull
    public List<UntersuchungImpfungIntern> findByUntersuchungWithFetchedAnlass(
        @NonNull ID<Untersuchung> untersuchungID
    ) {
        var query = findByUntersuchungBaseQuery(untersuchungID);
        query = query.leftJoin(
                QUntersuchungImpfungIntern.untersuchungImpfungIntern.anlassImpfungErfasst,
                QAnlass.anlass)
            .fetchJoin();
        return query.fetch();
    }

    private SmartJPAQuery<UntersuchungImpfungIntern> findByUntersuchungBaseQuery(
        @NonNull ID<Untersuchung> untersuchungID) {
        return db.selectFrom(QUntersuchungImpfungIntern.untersuchungImpfungIntern)
            .innerJoin(
                QUntersuchungImpfungIntern.untersuchungImpfungIntern.untersuchungImpfungen,
                QUntersuchungImpfungen.untersuchungImpfungen)
            .innerJoin(QUntersuchungImpfungen.untersuchungImpfungen.untersuchung, QUntersuchung.untersuchung)
            .leftJoin(QUntersuchungImpfungIntern.untersuchungImpfungIntern.krankheitenImmunitaeten).fetchJoin()
            .where(QUntersuchung.untersuchung.id.eq(untersuchungID.getUUID()))
            .orderBy(QUntersuchungImpfungIntern.untersuchungImpfungIntern.impfstoff.name.asc())
            .orderBy(QUntersuchungImpfungIntern.untersuchungImpfungIntern.datumImpfung.asc().nullsLast());
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

    @NonNull
    public List<ImpfstoffCharge> getImpfstoffChargenNotUsedSince(int monthsUnused, int batchsize) {
        LocalDate cutoffDate = appClock.getToday().minusMonths(monthsUnused);
        QUntersuchungImpfungIntern impfungIntern = QUntersuchungImpfungIntern.untersuchungImpfungIntern;
        return db.selectDistinct(QImpfstoffCharge.impfstoffCharge)
            .from(QImpfstoffCharge.impfstoffCharge)
            // die noch aktiv sind
            .where(QImpfstoffCharge.impfstoffCharge.aktiv.isTrue())
            // Mind. einmal verwendet
            .where(QImpfstoffCharge.impfstoffCharge.in(
                JPAExpressions.select(impfungIntern.charge)
                    .from(impfungIntern)
                    .where(impfungIntern.charge.isNotNull())
            ))
            // Aber nicht seit dem Stichtag
            .where(QImpfstoffCharge.impfstoffCharge.notIn(
                JPAExpressions.select(impfungIntern.charge)
                    .from(impfungIntern)
                    .where(impfungIntern.charge.isNotNull()
                        .and(impfungIntern.datumImpfung.goe(cutoffDate)))
            ))
            .limit(batchsize)
            .fetch();
    }

    @NonNull
    public List<ImpfstoffCharge> getImpfstoffChargenNotReferencedSince(int monthsUnreferenced, int batchsize) {
        LocalDate cutoffDate = appClock.getToday().minusMonths(monthsUnreferenced);
        QUntersuchungImpfungIntern impfungIntern = QUntersuchungImpfungIntern.untersuchungImpfungIntern;
        return db.selectDistinct(QImpfstoffCharge.impfstoffCharge)
            .from(QImpfstoffCharge.impfstoffCharge)
            // Nie verwendet
            .where(QImpfstoffCharge.impfstoffCharge.notIn(
                JPAExpressions.select(impfungIntern.charge)
                    .from(impfungIntern)
                    .where(impfungIntern.charge.isNotNull())
            ))
            // Und älter als der Stichtag
            .where(QImpfstoffCharge.impfstoffCharge.timestampErstellt.loe(cutoffDate.atStartOfDay()
                .atOffset(ZoneOffset.UTC)))
            .limit(batchsize)
            .fetch();
    }

    @NonNull
    public List<UntersuchungImpfungIntern> findByEinladungWithFetchedAnlass(@NonNull ID<Einladung> einladungID) {
        return db.selectFrom(QUntersuchungImpfungIntern.untersuchungImpfungIntern)
            .innerJoin(
                QUntersuchungImpfungIntern.untersuchungImpfungIntern.untersuchungImpfungen,
                QUntersuchungImpfungen.untersuchungImpfungen)
            .fetchJoin()
            .innerJoin(QUntersuchungImpfungen.untersuchungImpfungen.untersuchung, QUntersuchung.untersuchung)
            .fetchJoin()
            .leftJoin(QUntersuchungImpfungIntern.untersuchungImpfungIntern.anlassImpfungErfasst, QAnlass.anlass).fetchJoin()
            .where(QUntersuchung.untersuchung.einladung.id.eq(einladungID.getUUID()))
            .fetch();
    }

    @NonNull
    public List<UntersuchungImpfungIntern> findLastImpfungenBeforeEinladung(
        @NonNull Einladung einladung) {
        Untersuchung lastUntersuchung = findLastUntersuchungWithImpfungenBeforeEinladung(einladung);
        if (lastUntersuchung == null) {
            return Collections.emptyList();
        }
        return findByUntersuchungWithFetchedAnlass(lastUntersuchung.getId());
    }

    @Nullable
    private Untersuchung findLastUntersuchungWithImpfungenBeforeEinladung(@NonNull Einladung einladung) {
        return db.select(QUntersuchung.untersuchung)
            .from(QUntersuchungImpfungIntern.untersuchungImpfungIntern)
            .innerJoin(
                QUntersuchungImpfungIntern.untersuchungImpfungIntern.untersuchungImpfungen,
                QUntersuchungImpfungen.untersuchungImpfungen)
            .innerJoin(QUntersuchungImpfungen.untersuchungImpfungen.untersuchung, QUntersuchung.untersuchung)
            .innerJoin(QUntersuchung.untersuchung.einladung, QEinladung.einladung)
            .innerJoin(QEinladung.einladung.anlass, QAnlass.anlass)
            .innerJoin(QAnlass.anlass.anlassConfig, QAnlassConfig.anlassConfig)
            .innerJoin(QEinladung.einladung.dossier, QDossier.dossier)
            .where(
                QDossier.dossier.id.eq(einladung.getDossier().getId().getUUID())
                    .and(QAnlass.anlass.datum.loe(einladung.getAnlass().getDatum()))
                    .and(QAnlass.anlass.id.ne(einladung.getAnlass().getId().getUUID()))
                    .and(QAnlassConfig.anlassConfig.impfungenListe.ne(RequirementSetting.NOT_VISIBLE))
            )
            .orderBy(QEinladung.einladung.anlass.datum.desc())
            .fetchFirst();
    }
}
