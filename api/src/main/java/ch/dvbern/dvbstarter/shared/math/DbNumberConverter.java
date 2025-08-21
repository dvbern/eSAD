package ch.dvbern.dvbstarter.shared.math;

import com.fasterxml.jackson.databind.module.SimpleModule;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class DbNumberConverter extends GenericFluentBigDecimalConverter<DbNumber> {
    public DbNumberConverter() {
        super(DbNumber.DB_NUMBER);
    }

    public static void registerJackson(
        SimpleModule module
    ) {
        registerJackson(module, DbNumber.class, DbNumber.DB_NUMBER);
    }

}
