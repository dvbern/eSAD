package ch.dvbern.dvbstarter.shared.math;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.function.Function;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.github.honoluluhenk.fluentbigdecimals.AbstractFluentBigDecimal;
import com.github.honoluluhenk.fluentbigdecimals.Configuration;
import jakarta.persistence.AttributeConverter;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.checkerframework.checker.nullness.qual.Nullable;

@AllArgsConstructor(access = AccessLevel.PROTECTED)
abstract class GenericFluentBigDecimalConverter<Value extends AbstractFluentBigDecimal<Value>>
    implements AttributeConverter<Value, BigDecimal> {

    private final Configuration<Value> config;

    static <Value extends AbstractFluentBigDecimal<Value>> void registerJackson(
        SimpleModule module,
        Class<Value> clazz,
        Configuration<Value> config
    ) {

        module.addSerializer(new GenericSerializer<>(clazz));
        module.addDeserializer(
            clazz,
            new GenericDeserializer<>(clazz, bd -> ofExact(config, bd))
        );
    }

    private static <Value extends AbstractFluentBigDecimal<Value>> Value ofExact(
        Configuration<Value> config,
        BigDecimal bdValue
    ) {
        var parsed = config.of(bdValue);
        if (parsed.compareTo(bdValue) != 0) {
            throw new IllegalArgumentException("Value was rounded after parsing: %s: %s"
                .formatted(bdValue, config));
        }

        return parsed;
    }

    @Override
    public @Nullable BigDecimal convertToDatabaseColumn(@Nullable Value attribute) {
        if (attribute == null) {
            return null;
        }

        var result = attribute.getValue();

        return result;
    }

    @Override
    public @Nullable Value convertToEntityAttribute(@Nullable BigDecimal bdValue) {
        if (bdValue == null) {
            return null;
        }

        var result = ofExact(config, bdValue);

        return result;
    }

    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    public static class GenericSerializer<Value extends AbstractFluentBigDecimal<Value>> extends JsonSerializer<Value> {
        private final Class<Value> clazz;

        @Override
        public Class<Value> handledType() {
            return clazz;
        }

        @Override
        public void serialize(Value value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            if (value != null) {
                var bdValue = value.getValue();
                gen.writeNumber(bdValue);
            }
        }
    }

    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    public static class GenericDeserializer<Value extends AbstractFluentBigDecimal<Value>>
        extends JsonDeserializer<Value> {
        private final Class<Value> clazz;
        private final Function<BigDecimal, Value> fromJSON;

        @Override
        public Class<?> handledType() {
            return clazz;
        }

        @Nullable
        @Override
        public Value deserialize(
            JsonParser p,
            DeserializationContext ctxt
        ) throws IOException {

            var bdValue = p.getDecimalValue();
            if (bdValue == null) {
                return null;
            }
            var result = fromJSON.apply(bdValue);

            return result;
        }
    }

}
