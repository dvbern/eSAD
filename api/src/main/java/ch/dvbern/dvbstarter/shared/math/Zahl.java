package ch.dvbern.dvbstarter.shared.math;

import java.io.Serial;
import java.math.BigDecimal;

import ch.dvbern.dvbstarter.shared.openapi.OpenApiConst.Format;
import com.github.honoluluhenk.fluentbigdecimals.Configuration;
import com.github.honoluluhenk.fluentbigdecimals.ConfigurationFactory;
import com.github.honoluluhenk.fluentbigdecimals.scaler.FixedScaleScaler;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import static java.math.RoundingMode.HALF_UP;

/**
 * Zahl mit 2 Nachkommastellen.
 */
@Schema(type = SchemaType.NUMBER, format = Format.NUMBER_ZAHL)
public class Zahl extends MathUtil<Zahl> {

    @SuppressWarnings("java:S1700" /* allows for pretty static import */)
    public static final Configuration<Zahl> ZAHL =
        ConfigurationFactory
            .databaseDBNotation(19, 2, HALF_UP)
            .withScaler(new FixedScaleScaler(2))
            .withFactory(Zahl::new);
    public static final Zahl ZERO = Zahl.valueOf(BigDecimal.ZERO);
    public static final Zahl ONE = Zahl.valueOf(BigDecimal.ONE);
    public static final Zahl TEN = Zahl.valueOf(BigDecimal.TEN);
    @Serial
    private static final long serialVersionUID = 6461737803039223285L;

    private Zahl(BigDecimal value, Configuration<Zahl> configuration) {
        super(value, configuration);
    }

    public static Zahl valueOf(BigDecimal value) {
        return ZAHL.of(value);
    }

    // NOTE: *must* have a valueOf method for queryParams
    public static Zahl valueOf(String value) {
        return ZAHL.of(value);
    }

    public static Zahl valueOf(long value) {
        return ZAHL.of(value);
    }

    public static Zahl valueOf(double value) {
        return ZAHL.of(value);
    }

}
