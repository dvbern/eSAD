package ch.dvbern.dvbstarter.shared.math;

import com.fasterxml.jackson.databind.module.SimpleModule;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class FrankenFuenferConverter extends GenericFluentBigDecimalConverter<FrankenFuenfer> {
    public FrankenFuenferConverter() {
        super(FrankenFuenfer.FRANKEN_FUENFER);
    }

    public static void registerJackson(
        SimpleModule module
    ) {
        registerJackson(module, FrankenFuenfer.class, FrankenFuenfer.FRANKEN_FUENFER);
    }
}
