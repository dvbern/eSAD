package ch.dvbern.dvbstarter.shared.math;

import java.io.Serial;
import java.math.BigDecimal;

import com.github.honoluluhenk.fluentbigdecimals.CashRounding;
import com.github.honoluluhenk.fluentbigdecimals.CashRoundingUnits;
import com.github.honoluluhenk.fluentbigdecimals.Configuration;
import com.github.honoluluhenk.fluentbigdecimals.ConfigurationFactory;
import com.github.honoluluhenk.fluentbigdecimals.scaler.CashRoundingScaler;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import static java.math.RoundingMode.HALF_UP;

@Schema(type = SchemaType.NUMBER)
public class FrankenFuenfer extends MathUtil<FrankenFuenfer> {

    @SuppressWarnings("java:S1700" /* allows for pretty static import */)
    public static final Configuration<FrankenFuenfer> FRANKEN_FUENFER =
        ConfigurationFactory
            .databaseDBNotation(19, 2, HALF_UP)
            .withScaler(new CashRoundingScaler(CashRounding.of(CashRoundingUnits.ROUND_DOT05)))
            .withFactory(FrankenFuenfer::new);
    public static final FrankenFuenfer ZERO = FrankenFuenfer.valueOf(BigDecimal.ZERO);
    public static final FrankenFuenfer ONE = FrankenFuenfer.valueOf(BigDecimal.ONE);
    public static final FrankenFuenfer TEN = FrankenFuenfer.valueOf(BigDecimal.TEN);
    @Serial
    private static final long serialVersionUID = 6461737803039223285L;

    private FrankenFuenfer(BigDecimal value, Configuration<FrankenFuenfer> configuration) {
        super(value, configuration);
    }

    public static FrankenFuenfer valueOf(BigDecimal value) {
        return FRANKEN_FUENFER.of(value);
    }

    // NOTE: *must* have a valueOf method for queryParams
    public static FrankenFuenfer valueOf(String value) {
        return FRANKEN_FUENFER.of(value);
    }

    public static FrankenFuenfer valueOf(long value) {
        return FRANKEN_FUENFER.of(value);
    }

    public static FrankenFuenfer valueOf(double value) {
        return FRANKEN_FUENFER.of(value);
    }

}
