package ch.dvbern.dvbstarter.shared.math;

import java.math.MathContext;
import java.math.RoundingMode;

import com.github.honoluluhenk.fluentbigdecimals.scaler.NopScaler;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CalculationTest {

    @Test
    void has_valid_configuration() {
        assertThat(Calculation.CALCULATION.getMathContext())
            .isEqualTo(new MathContext(15, RoundingMode.HALF_UP));
        assertThat(Calculation.CALCULATION.getScaler())
            .isEqualTo(new NopScaler());
    }
}
