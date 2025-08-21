package ch.dvbern.dvbstarter.shared.math;

import java.math.MathContext;
import java.math.RoundingMode;

import com.github.honoluluhenk.fluentbigdecimals.scaler.FixedScaleScaler;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GanzZahlTest {
    @Test
    void has_valid_configuration() {
        assertThat(GanzZahl.GANZ_ZAHL.getMathContext())
            .isEqualTo(new MathContext(30, RoundingMode.HALF_UP));
        assertThat(GanzZahl.GANZ_ZAHL.getScaler())
            .isEqualTo(new FixedScaleScaler(0));
    }
}
