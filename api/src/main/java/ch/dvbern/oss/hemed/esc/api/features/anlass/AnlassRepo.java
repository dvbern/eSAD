package ch.dvbern.oss.hemed.esc.api.features.anlass;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import ch.dvbern.dvbstarter.infrastructure.db.SmartJPAQuery;
import ch.dvbern.dvbstarter.infrastructure.db.baserepo.BaseRepo;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.Repository;
import ch.dvbern.dvbstarter.shared.clock.AppClock;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.QAnlassConfig;
import ch.dvbern.oss.hemed.esc.api.features.anlass.json.AnlassWithDateTimeJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.json.AnlassWithOpenSlotsOptionJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.json.QAnlassWithDateTimeJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.json.QAnlassWithOpenSlotsOptionJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.AnlassArt;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.AnlassStatus;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.AnlassTyp;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.QEinladung;
import ch.dvbern.oss.hemed.esc.api.features.kinder.QDossier;
import ch.dvbern.oss.hemed.esc.api.features.kinder.QKind;
import ch.dvbern.oss.hemed.esc.api.features.schule.klasse.Klasse;
import ch.dvbern.oss.hemed.esc.api.features.schule.klasse.QKlasse;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import com.querydsl.core.types.SubQueryExpression;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;

import static com.querydsl.core.types.dsl.Expressions.asNumber;


@Slf4j
@Repository
@RequiredArgsConstructor
public class AnlassRepo extends BaseRepo<Anlass> {

    private final AppClock appClock;

    @NonNull
    public List<Anlass> findAll() {
        return db.selectFrom(QAnlass.anlass)
            .orderBy(QAnlass.anlass.datum.asc())
            .orderBy(
                Expressions.cases()
                    .when(QAnlass.anlass.zeitVonBis.von.isNull()).then(0)
                    .otherwise(1)
                    .asc()
            )
            .orderBy(QAnlass.anlass.zeitVonBis.von.asc())
            .fetch();
    }

    @NonNull
    public List<Klasse> findKlassenForAnlass(@NonNull ID<Anlass> anlassId) {
        return db.selectDistinct(QKlasse.klasse)
            .from(QEinladung.einladung)
            .innerJoin(QEinladung.einladung.anlass, QAnlass.anlass)
            .innerJoin(QEinladung.einladung.dossier, QDossier.dossier)
            .innerJoin(QDossier.dossier.kind, QKind.kind)
            .innerJoin(QKind.kind.klasse, QKlasse.klasse)
            .where(QAnlass.anlass.id.eq(anlassId.getUUID()))
            .fetch();
    }

    @NonNull
    public List<Anlass> findAllForUserInFuture(@NonNull ID<Benutzer> benutzerID, long limit) {
        SmartJPAQuery<?> basequery = createFutureAnlassBasequery(benutzerID);

        return basequery
            .select(QAnlass.anlass)
            .orderBy(QAnlass.anlass.datum.asc())
            .limit(limit)
            .fetch();
    }

    @NonNull
    public Long getCountAllforUserInFuture(@NonNull ID<Benutzer> benutzerID) {
        SmartJPAQuery<?> basequery = createFutureAnlassBasequery(benutzerID);
        Optional<Long> count = basequery
            .select(QAnlass.anlass.count())
            .fetchOne();
        return count.orElse(0L);
    }

    @NonNull
    private SmartJPAQuery<?> createFutureAnlassBasequery(@NonNull ID<Benutzer> benutzerID) {
        return db
            .from(QAnlass.anlass)
            .where(
                QAnlass.anlass.datum.goe(appClock.offsetDateTime().now().toLocalDate())
                    .and(
                        QAnlass.anlass.arzt.id.eq(benutzerID.getUUID())
                            .or(QAnlass.anlass.mpa.id.eq(benutzerID.getUUID()))
                            .or(QAnlass.anlass.weiterePerson.id.eq(benutzerID.getUUID()))
                    )
            );
    }

    @NonNull
    public List<AnlassWithOpenSlotsOptionJson> findFutureGruppenAnlaesseOfTypeHavingLessThanMaxEinladungen(
        @NonNull List<AnlassTyp> anlassTypen,
        @NonNull ID<Kind> kindId,
        int maxEinladungen
    ) {
        List<AnlassStatus> anlassEndedStatus = List.of( AnlassStatus.ABGESAGT, AnlassStatus.ABGESCHLOSSEN, AnlassStatus.DURCHGEFUEHRT );
        BooleanExpression anlassTerminQuery = createFutureAnlassTerminOrLaterTodayQuerry();
        SubQueryExpression<Anlass> anlaesseForKind = kindInAnlassOfTypeQuery(kindId, Arrays.asList(AnlassTyp.values())).asSubQuery();

        return db.select(
            new QAnlassWithOpenSlotsOptionJson(
                QAnlass.anlass.id,
                QAnlassConfig.anlassConfig.anlassTyp,
                QAnlass.anlass.titel,
                asNumber(maxEinladungen).subtract(QEinladung.einladung.countDistinct()),
                QAnlass.anlass.datum,
                QAnlass.anlass.zeitVonBis.von,
                QAnlass.anlass.zeitVonBis.bis)
            )
            .from(QAnlass.anlass)
            .leftJoin(QAnlass.anlass.anlassConfig, QAnlassConfig.anlassConfig)
            .leftJoin(QEinladung.einladung)
                .on(QEinladung.einladung.anlass.eq(QAnlass.anlass))
            .where(
                QAnlassConfig.anlassConfig.anlassTyp.in(anlassTypen)
                    .and(QAnlass.anlass.anlassArt.eq(AnlassArt.GRUPPEN_ANLASS))
                    .and(QAnlass.anlass.anlassStatus.notIn(anlassEndedStatus))
                    .and(anlassTerminQuery)
                    .and(QAnlass.anlass.notIn(anlaesseForKind))
            ).groupBy(
                QAnlass.anlass.id,
                QAnlassConfig.anlassConfig.anlassTyp,
                QAnlass.anlass.titel,
                QAnlass.anlass.datum,
                QAnlass.anlass.zeitVonBis.von,
                QAnlass.anlass.zeitVonBis.bis
            ).orderBy(
                QAnlass.anlass.datum.asc(),
                QAnlass.anlass.zeitVonBis.von.asc()
            ).having(QEinladung.einladung.count().lt(maxEinladungen))
            .fetch();
    }

    @NonNull
    public List<AnlassWithDateTimeJson> findScheduledAnlaesseOfTypeForKind(
        @NonNull ID<Kind> kindId,
        @NonNull List<AnlassTyp> anlassTypen,
        @NonNull List<ID<Anlass>> excludeAnlassIds
    ) {
        List<AnlassStatus> anlassEndedStatus = List.of( AnlassStatus.ABGESAGT, AnlassStatus.ABGESCHLOSSEN, AnlassStatus.DURCHGEFUEHRT );
        List<UUID> excludeAnlassUUIDs = excludeAnlassIds.stream().map(ID::getUUID).toList();

        return kindInAnlassOfTypeQuery(kindId, anlassTypen)
            .select(new QAnlassWithDateTimeJson(
                QAnlass.anlass.id,
                QAnlassConfig.anlassConfig.anlassTyp,
                QAnlass.anlass.titel,
                QAnlass.anlass.datum,
                QAnlass.anlass.zeitVonBis.von,
                QAnlass.anlass.zeitVonBis.bis)
            )
            .where(
                QAnlass.anlass.anlassStatus.notIn(anlassEndedStatus)
                .and(QAnlass.anlass.datum.goe(appClock.getToday()))
                .and(QAnlass.anlass.id.notIn(excludeAnlassUUIDs))
            ).orderBy(
                QAnlass.anlass.datum.asc(),
                QAnlass.anlass.zeitVonBis.von.asc()
            ).fetch();
    }

    @NonNull
    private SmartJPAQuery<Anlass> kindInAnlassOfTypeQuery(@NonNull ID<Kind> kindId, @NonNull List<AnlassTyp> anlassTypen){
        return  db.selectDistinct(QAnlass.anlass)
            .from(QAnlass.anlass)
            .leftJoin(QAnlass.anlass.anlassConfig, QAnlassConfig.anlassConfig)
            .leftJoin(QEinladung.einladung).on(QEinladung.einladung.anlass.eq(QAnlass.anlass))
            .leftJoin(QDossier.dossier).on(QEinladung.einladung.dossier.eq(QDossier.dossier))
            .where(
                QDossier.dossier.kind.id.eq(kindId.getUUID())
                    .and(QAnlassConfig.anlassConfig.anlassTyp.in(anlassTypen))
            );
    }

    @NonNull
    private BooleanExpression createFutureAnlassTerminOrLaterTodayQuerry(){
        LocalDate tomorrow = appClock.getToday().plusDays(1);
        LocalDateTime laterToday = appClock.getNow().plusHours(1);

        return QAnlass.anlass.datum.goe(tomorrow).or(
            QAnlass.anlass.datum.eq(appClock.getToday())
                .and(QAnlass.anlass.zeitVonBis.von.isNotNull())
                .and(QAnlass.anlass.zeitVonBis.von.goe(laterToday)));
    }
}
