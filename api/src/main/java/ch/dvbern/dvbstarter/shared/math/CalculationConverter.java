package ch.dvbern.dvbstarter.shared.math;

import com.fasterxml.jackson.databind.module.SimpleModule;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class CalculationConverter extends GenericFluentBigDecimalConverter<Calculation> {
    public CalculationConverter() {
        super(Calculation.CALCULATION);
    }

    public static void registerJackson(
        SimpleModule module
    ) {
        registerJackson(module, Calculation.class, Calculation.CALCULATION);
    }
}
