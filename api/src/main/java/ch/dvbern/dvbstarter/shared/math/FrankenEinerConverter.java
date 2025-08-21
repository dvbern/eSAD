package ch.dvbern.dvbstarter.shared.math;

import com.fasterxml.jackson.databind.module.SimpleModule;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class FrankenEinerConverter extends GenericFluentBigDecimalConverter<FrankenEiner> {
    public FrankenEinerConverter() {
        super(FrankenEiner.FRANKEN_EINER);
    }

    public static void registerJackson(
        SimpleModule module
    ) {
        registerJackson(module, FrankenEiner.class, FrankenEiner.FRANKEN_EINER);
    }
}
