package ch.dvbern.dvbstarter.infrastructure.liquibase;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.PhysicalNamingStrategy;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.from;
import static org.hibernate.boot.model.naming.Identifier.toIdentifier;

@DisplayNameGeneration(ReplaceUnderscores.class)
class LiquibaseQuarkusCompatiblePhysicalNamingStrategyTest {

    private final PhysicalNamingStrategy strategy = new LiquibaseQuarkusCompatiblePhysicalNamingStrategy();

    @ParameterizedTest
    @CsvSource(value = {
        "null           , false, null   ",
        "null           ,  true, null   ",
        "foobar         , false, foobar ",
        "'foobar'       ,  true, foobar ",
        "FOOBAR         , false, foobar ",
        "'FOOBAR'       ,  true, foobar ",
        "foo_bar        , false, foo_bar",
        "'foo_bar'      ,  true, foo_bar",
        "FOO_BAR        , false, foo_bar",
        "'FOO_BAR'      ,  true, foo_bar",
    }, nullValues = "null")
    void all_namings_are_lowercase(@Nullable String text, boolean quoted, @Nullable String expectedString) {
        var mockEnvironment = Mockito.mock(JdbcEnvironment.class);
        var expected = Identifier.toIdentifier(expectedString);
        assertThat(strategy)
            .returns(expected, from(s -> s.toPhysicalCatalogName(toIdentifier(text, quoted), mockEnvironment)))
            .returns(expected, from(s -> s.toPhysicalSchemaName(toIdentifier(text, quoted), mockEnvironment)))
            .returns(expected, from(s -> s.toPhysicalTableName(toIdentifier(text, quoted), mockEnvironment)))
            .returns(expected, from(s -> s.toPhysicalSequenceName(toIdentifier(text, quoted), mockEnvironment)))
            .returns(expected, from(s -> s.toPhysicalColumnName(toIdentifier(text, quoted), mockEnvironment)))
        ;
    }
}
