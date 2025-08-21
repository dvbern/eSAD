package ch.dvbern.dvbstarter.shared.math;

import java.math.MathContext;
import java.math.RoundingMode;

import com.github.honoluluhenk.fluentbigdecimals.scaler.MaxScaleScaler;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DbNumberTest {
    @Test
    void has_valid_configuration() {
        assertThat(DbNumber.DB_NUMBER.getMathContext())
            .isEqualTo(new MathContext(40, RoundingMode.HALF_UP));
        assertThat(DbNumber.DB_NUMBER.getScaler())
            .isEqualTo(new MaxScaleScaler(10));
    }
}
