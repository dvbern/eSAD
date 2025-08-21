package ch.dvbern.dvbstarter.shared.math;

import com.fasterxml.jackson.databind.module.SimpleModule;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class GanzZahlConverter extends GenericFluentBigDecimalConverter<GanzZahl> {
    public GanzZahlConverter() {
        super(GanzZahl.GANZ_ZAHL);
    }

    public static void registerJackson(
        SimpleModule module
    ) {
        registerJackson(module, GanzZahl.class, GanzZahl.GANZ_ZAHL);
    }
}
