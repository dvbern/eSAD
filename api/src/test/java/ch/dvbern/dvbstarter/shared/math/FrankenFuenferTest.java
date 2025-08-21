package ch.dvbern.dvbstarter.shared.math;

import java.math.MathContext;
import java.math.RoundingMode;

import com.github.honoluluhenk.fluentbigdecimals.CashRounding;
import com.github.honoluluhenk.fluentbigdecimals.CashRoundingUnits;
import com.github.honoluluhenk.fluentbigdecimals.scaler.CashRoundingScaler;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FrankenFuenferTest {
    @Test
    void has_valid_configuration() {
        assertThat(FrankenFuenfer.FRANKEN_FUENFER.getMathContext())
            .isEqualTo(new MathContext(21, RoundingMode.HALF_UP));
        assertThat(FrankenFuenfer.FRANKEN_FUENFER.getScaler())
            .isInstanceOf(CashRoundingScaler.class);

        // rounding does not implement equals/hashcode :(
        var rounding = ((CashRoundingScaler) FrankenFuenfer.FRANKEN_FUENFER.getScaler()).getCashRounding();
        assertThat(rounding.getRoundingMode())
            .isEqualTo(CashRounding.CASH_ROUNDING_MODE);
        assertThat(rounding.getUnit())
            .isEqualTo(CashRoundingUnits.ROUND_DOT05.getUnit());
    }
}
