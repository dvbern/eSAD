package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.dvbstarter.shared.clock.AppClock;
import ch.dvbern.dvbstarter.shared.clock.FixedClockProvider;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Anlass;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.AnlassBuilder;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JahrSuCsvColumnTest extends AbstractBaseColumnTest {

    private final FixedClockProvider clockProvider = FixedClockProvider.now();
    private final AppClock appClock = new AppClock(clockProvider);

    @Test
    void extract() {
        // ARRANGE
        final JahrSuCsvColumn column = new JahrSuCsvColumn(config, null);
        Einladung einladung = new Einladung();
        Anlass anlass = AnlassBuilder.defaultAnlass(appClock.offsetDateTime().now().toLocalDate());
        anlass.setSchuljahr(2024);
        einladung.setAnlass(anlass);

        assertThat(column.extract(einladung)).isEqualTo("2024");
    }
}
