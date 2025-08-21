package ch.dvbern.dvbstarter.shared.math;

import java.io.Serial;
import java.math.BigDecimal;

import com.github.honoluluhenk.fluentbigdecimals.Configuration;
import com.github.honoluluhenk.fluentbigdecimals.ConfigurationFactory;
import com.github.honoluluhenk.fluentbigdecimals.scaler.FixedScaleScaler;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import static java.math.RoundingMode.HALF_UP;

/**
 * Zahl ohne Nachkommastellen.
 */
@Schema(type = SchemaType.INTEGER)
public class GanzZahl extends MathUtil<GanzZahl> {

    @SuppressWarnings("java:S1700" /* allows for pretty static import */)
    public static final Configuration<GanzZahl> GANZ_ZAHL =
        ConfigurationFactory
            .databaseDBNotation(30, 0, HALF_UP)
            .withScaler(new FixedScaleScaler(0))
            .withFactory(GanzZahl::new);
    public static final GanzZahl ZERO = GanzZahl.valueOf(BigDecimal.ZERO);
    public static final GanzZahl ONE = GanzZahl.valueOf(BigDecimal.ONE);
    public static final GanzZahl TEN = GanzZahl.valueOf(BigDecimal.TEN);
    @Serial
    private static final long serialVersionUID = 6461737803039223285L;

    private GanzZahl(BigDecimal value, Configuration<GanzZahl> configuration) {
        super(value, configuration);
    }

    public static GanzZahl valueOf(BigDecimal value) {
        return GANZ_ZAHL.of(value);
    }

    // NOTE: *must* have a valueOf method for queryParams
    public static GanzZahl valueOf(String value) {
        return GANZ_ZAHL.of(value);
    }

    public static GanzZahl valueOf(long value) {
        return GANZ_ZAHL.of(value);
    }

}
