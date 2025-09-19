package ch.dvbern.oss.hemed.esc.api.features.anlass;

import java.util.List;
import java.util.Optional;

import ch.dvbern.dvbstarter.infrastructure.db.SmartJPAQuery;
import ch.dvbern.dvbstarter.infrastructure.db.baserepo.BaseRepo;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.Repository;
import ch.dvbern.dvbstarter.shared.clock.AppClock;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.kinder.QDossier;
import ch.dvbern.oss.hemed.esc.api.features.kinder.QKind;
import ch.dvbern.oss.hemed.esc.api.features.schule.klasse.Klasse;
import ch.dvbern.oss.hemed.esc.api.features.schule.klasse.QKlasse;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import com.querydsl.core.types.dsl.Expressions;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;

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
}
