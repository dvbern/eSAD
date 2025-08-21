package ch.dvbern.dvbstarter.shared.math;

import com.fasterxml.jackson.databind.module.SimpleModule;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class ZahlConverter extends GenericFluentBigDecimalConverter<Zahl> {
    public ZahlConverter() {
        super(Zahl.ZAHL);
    }

    public static void registerJackson(
        SimpleModule module
    ) {
        registerJackson(module, Zahl.class, Zahl.ZAHL);
    }
}
