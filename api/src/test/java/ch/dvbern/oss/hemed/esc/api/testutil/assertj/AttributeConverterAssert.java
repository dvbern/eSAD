package ch.dvbern.oss.hemed.esc.api.testutil.assertj;

import java.util.Objects;

import jakarta.persistence.AttributeConverter;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.AbstractThrowableAssert;
import org.checkerframework.checker.nullness.qual.Nullable;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SuppressWarnings({ "ResultOfMethodCallIgnored", "UnusedReturnValue" })
public class AttributeConverterAssert<JavaType, Db>
    extends AbstractAssert<AttributeConverterAssert<JavaType, Db>, AttributeConverter<JavaType, Db>> {

    protected AttributeConverterAssert(AttributeConverter<JavaType, Db> target) {
        super(target, AttributeConverterAssert.class);
    }

    public static <JavaType, Db> AttributeConverterAssert<JavaType, Db> assertThatAttributeConverter(AttributeConverter<JavaType, Db> converter) {
        return new AttributeConverterAssert<>(converter);
    }

    public AttributeConverterAssert<JavaType, Db> convertsJavaToDb(
        @Nullable JavaType javaValue,
        @Nullable Db expectedDbValue
    ) {
        isNotNull();

        var dbConverted = this.actual.convertToDatabaseColumn(javaValue);
        if (!Objects.equals(dbConverted, expectedDbValue)) {
            failWithActualExpectedAndMessage(
                dbConverted,
                expectedDbValue,
                "Expected db value to be <%s> but was <%s>", expectedDbValue, dbConverted);
        }

        return this;
    }

    public AttributeConverterAssert<JavaType, Db> convertsDbToJava(
        @Nullable Db dbValue,
        @Nullable JavaType expectedJavaValue
    ) {
        isNotNull();

        var convertedJava = this.actual.convertToEntityAttribute(dbValue);
        if (!Objects.equals(convertedJava, expectedJavaValue)) {
            failWithActualExpectedAndMessage(
                convertedJava,
                expectedJavaValue,
                "Expected java value to be <%s> but was <%s>",
                expectedJavaValue,
                convertedJava
            );
        }

        return this;
    }

    public AttributeConverterAssert<JavaType, Db> convertsBackAndForth(
        @Nullable JavaType javaValue,
        @Nullable Db dbValue
    ) {
        isNotNull();

        convertsJavaToDb(javaValue, dbValue);
        convertsDbToJava(dbValue, javaValue);

        return this;
    }

    public AttributeConverterAssert<JavaType, Db> convertsJavaNullTo(@Nullable Db expectedDbValue) {
        isNotNull();

        var dbConverted = this.actual.convertToDatabaseColumn(null);
        if (!Objects.equals(dbConverted, expectedDbValue)) {
            failWithActualExpectedAndMessage(
                dbConverted,
                expectedDbValue,
                "Expected db value to be <%s> but was <%s>",
                expectedDbValue,
                dbConverted
            );
        }

        return this;
    }

    public AttributeConverterAssert<JavaType, Db> convertsDbNullTo(@Nullable JavaType expectedJavaValue) {
        isNotNull();

        var convertedJava = this.actual.convertToEntityAttribute(null);
        if (!Objects.equals(convertedJava, expectedJavaValue)) {
            failWithActualExpectedAndMessage(
                convertedJava,
                expectedJavaValue,
                "Expected java value to be null but was <%s>",
                expectedJavaValue,
                convertedJava
            );
        }

        return this;
    }

    public AttributeConverterAssert<JavaType, Db> convertsNullToNull() {
        isNotNull();

        convertsJavaNullTo(null);
        convertsDbNullTo(null);

        return this;
    }

    public <T extends Throwable> AbstractThrowableAssert<?, T> throwsForInvalidDbValue(
        Class<T> exceptionClass,
        Db bigDecimal) {
        isNotNull();

        @SuppressWarnings("unchecked")
        AbstractThrowableAssert<?, T> result = (AbstractThrowableAssert<?, T>) assertThatThrownBy(
            () -> this.actual.convertToEntityAttribute(bigDecimal)
        ).isInstanceOf(exceptionClass);

        return result;
    }

    public <T extends Throwable> AbstractThrowableAssert<?, T> throwsForInvalidJavaValue(
        Class<T> exceptionClass,
        JavaType bigDecimal
    ) {
        isNotNull();

        @SuppressWarnings("unchecked")
        AbstractThrowableAssert<?, T> result = (AbstractThrowableAssert<?, T>) assertThatThrownBy(
            () -> this.actual.convertToDatabaseColumn(bigDecimal)
        ).isInstanceOf(exceptionClass);

        return result;
    }
}
