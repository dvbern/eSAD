package ch.dvbern.dvbstarter.shared.math;

import java.io.Serial;
import java.math.BigDecimal;

import com.github.honoluluhenk.fluentbigdecimals.Configuration;
import com.github.honoluluhenk.fluentbigdecimals.ConfigurationFactory;
import com.github.honoluluhenk.fluentbigdecimals.scaler.FixedScaleScaler;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import static java.math.RoundingMode.HALF_UP;

@Schema(type = SchemaType.NUMBER)
public class FrankenEiner extends MathUtil<FrankenEiner> {

    @SuppressWarnings("java:S1700" /* allows for pretty static import */)
    public static final Configuration<FrankenEiner> FRANKEN_EINER =
        ConfigurationFactory
            .databaseDBNotation(19, 2, HALF_UP)
            .withScaler(new FixedScaleScaler(2))
            .withFactory(FrankenEiner::new);
    public static final FrankenEiner ZERO = FrankenEiner.valueOf(BigDecimal.ZERO);
    public static final FrankenEiner ONE = FrankenEiner.valueOf(BigDecimal.ONE);
    public static final FrankenEiner TEN = FrankenEiner.valueOf(BigDecimal.TEN);
    @Serial
    private static final long serialVersionUID = -8700749021887271559L;

    private FrankenEiner(BigDecimal value, Configuration<FrankenEiner> configuration) {
        super(value, configuration);
    }

    public static FrankenEiner valueOf(BigDecimal value) {
        return FRANKEN_EINER.of(value);
    }

    // NOTE: *must* have a valueOf method for queryParams
    public static FrankenEiner valueOf(String value) {
        return FRANKEN_EINER.of(value);
    }

    public static FrankenEiner valueOf(long value) {
        return FRANKEN_EINER.of(value);
    }

    public static FrankenEiner valueOf(double value) {
        return FRANKEN_EINER.of(value);
    }

}
