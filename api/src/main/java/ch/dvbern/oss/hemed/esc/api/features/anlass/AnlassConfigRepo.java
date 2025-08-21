package ch.dvbern.oss.hemed.esc.api.features.anlass;

import java.time.LocalDate;

import ch.dvbern.dvbstarter.infrastructure.db.baserepo.BaseRepo;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureException;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureMessage;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.Repository;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.AnlassTyp;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;

@Repository
@RequiredArgsConstructor
public class AnlassConfigRepo extends BaseRepo<AnlassConfig> {

    @NonNull
    public AnlassConfig getByAnlassTypAndStichtag(@NonNull AnlassTyp anlassTyp, @NonNull LocalDate stichtag) {
        return db.selectFrom(QAnlassConfig.anlassConfig)
            .where(QAnlassConfig.anlassConfig.anlassTyp.eq(anlassTyp)
                .and(QAnlassConfig.anlassConfig.gueltigkeitsZeitraum.datumVon.before(stichtag.minusDays(1)))
                .and(QAnlassConfig.anlassConfig.gueltigkeitsZeitraum.datumBis.after(stichtag.plusDays(1)))
            )
            .fetchOne()
            .orElseThrow(() -> new AppFailureException(AppFailureMessage.anlassConfigNotFound(anlassTyp)));
    }
}
