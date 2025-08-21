package ch.dvbern.dvbstarter.shared.math;

import java.math.MathContext;
import java.math.RoundingMode;

import com.github.honoluluhenk.fluentbigdecimals.scaler.FixedScaleScaler;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FrankenEinerTest {
    @Test
    void has_valid_configuration() {
        assertThat(FrankenEiner.FRANKEN_EINER.getMathContext())
            .isEqualTo(new MathContext(21, RoundingMode.HALF_UP));
        assertThat(FrankenEiner.FRANKEN_EINER.getScaler())
            .isEqualTo(new FixedScaleScaler(2));
    }
}
