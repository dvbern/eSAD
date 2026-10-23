package ch.dvbern.oss.hemed.esc.api.features.hellodata;

import java.util.stream.Stream;

import ch.dvbern.dvbstarter.infrastructure.db.baserepo.ReadOnlyRepo;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.Repository;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.QUntersuchungImpfungIntern;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.QUntersuchungImpfungKrankheitImmunitaet;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.QUntersuchungImpfungen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.QUntersuchung;
import ch.dvbern.oss.hemed.esc.api.features.hellodata.json.HelloDataImpfungInternJson;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringExpression;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.hibernate.jpa.HibernateHints;

@Repository
@RequiredArgsConstructor
public class HelloDataImpfungInternRepo extends ReadOnlyRepo {

    private static final int FETCH_SIZE = 500;

    @NonNull
    public Stream<HelloDataImpfungInternJson> streamAll() {
        QUntersuchungImpfungIntern impfungIntern = QUntersuchungImpfungIntern.untersuchungImpfungIntern;
        QUntersuchungImpfungen impfungen = QUntersuchungImpfungen.untersuchungImpfungen;
        QUntersuchung untersuchung = QUntersuchung.untersuchung;
        QUntersuchungImpfungKrankheitImmunitaet krankheitImmunitaet =
            QUntersuchungImpfungKrankheitImmunitaet.untersuchungImpfungKrankheitImmunitaet;

        // this creates per krankheit a strin in the form MASERN:true;MUMPS:false
        StringExpression aggregatedDiseases = Expressions.stringTemplate(
            "coalesce(listagg("
                + "case when {0} is not null then cast({0} as string) || ':' || cast({1} as string) else null end, "
                + "';') within group (order by {0}), '')",
            krankheitImmunitaet.krankheit,
            krankheitImmunitaet.immunitaetGenuegend
        );

        return db.select(Projections.constructor(
                HelloDataImpfungInternJson.class,
                impfungIntern.id,
                untersuchung.id,
                impfungen.id,
                impfungIntern.impfstoff.id,
                impfungIntern.charge.id,
                impfungIntern.datumImpfung,
                impfungIntern.impfStatus,
                impfungIntern.anlassImpfungErfasst.id,
                impfungIntern.anlassImpfungDurchgefuehrt.id,
                aggregatedDiseases
            ))
            .from(impfungIntern)
            .innerJoin(impfungIntern.untersuchungImpfungen, impfungen)
            .innerJoin(impfungen.untersuchung, untersuchung)
            .leftJoin(impfungIntern.krankheitenImmunitaeten, krankheitImmunitaet)
            .groupBy(
                impfungIntern.id,
                untersuchung.id,
                impfungen.id,
                impfungIntern.impfstoff.id,
                impfungIntern.charge.id,
                impfungIntern.datumImpfung,
                impfungIntern.impfStatus,
                impfungIntern.anlassImpfungErfasst.id,
                impfungIntern.anlassImpfungDurchgefuehrt.id
            )
            .orderBy(impfungIntern.id.asc())
            .setHint(HibernateHints.HINT_FETCH_SIZE, FETCH_SIZE)
            .stream();
    }
}
