package ch.dvbern.dvbstarter.shared.math;

import java.io.Serial;
import java.math.BigDecimal;

import ch.dvbern.dvbstarter.shared.openapi.OpenApiConst;
import com.github.honoluluhenk.fluentbigdecimals.Configuration;
import com.github.honoluluhenk.fluentbigdecimals.ConfigurationFactory;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Default-Präzision für Datenbanken, i.e. für Entities.
 */
@Schema(type = SchemaType.NUMBER, format = OpenApiConst.Format.NUMBER_PRECISE)
public class DbNumber extends MathUtil<DbNumber> {
    @SuppressWarnings("java:S1700" /* allows for pretty static import */)
    public static final Configuration<DbNumber> DB_NUMBER =
        ConfigurationFactory
            .databaseJavaNotation(30, 10)
            .withFactory(DbNumber::new);
    public static final DbNumber ZERO = DbNumber.valueOf(BigDecimal.ZERO);
    public static final DbNumber ONE = DbNumber.valueOf(BigDecimal.ONE);
    public static final DbNumber TEN = DbNumber.valueOf(BigDecimal.TEN);
    @Serial
    private static final long serialVersionUID = 1136059771822861030L;

    private DbNumber(BigDecimal value, Configuration<DbNumber> configuration) {
        super(value, configuration);
    }

    public static DbNumber valueOf(BigDecimal value) {
        return DB_NUMBER.of(value);
    }

    public static DbNumber valueOf(String value) {
        return DB_NUMBER.of(value);
    }

    public static DbNumber valueOf(long value) {
        return DB_NUMBER.of(value);
    }
}
