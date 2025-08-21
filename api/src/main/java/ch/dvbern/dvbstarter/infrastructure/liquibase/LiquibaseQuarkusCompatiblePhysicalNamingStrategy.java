package ch.dvbern.dvbstarter.infrastructure.liquibase;

import java.util.Locale;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.PhysicalNamingStrategy;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;

public class LiquibaseQuarkusCompatiblePhysicalNamingStrategy implements PhysicalNamingStrategy {
    private static @Nullable Identifier allLowerCase(@Nullable Identifier name) {
        if (name == null) {
            return null;
        }

        var lowerCase = name.getText().toLowerCase(Locale.US);
        var result = Identifier.toIdentifier(lowerCase, name.isQuoted());

        return result;
    }

    @Override
    public @Nullable Identifier toPhysicalCatalogName(
        @Nullable Identifier name,
        JdbcEnvironment jdbcEnvironment
    ) {
        return allLowerCase(name);
    }

    @Override
    public @Nullable Identifier toPhysicalSchemaName(
        @Nullable Identifier name,
        JdbcEnvironment jdbcEnvironment
    ) {
        return allLowerCase(name);
    }

    @Override
    public @Nullable Identifier toPhysicalTableName(
        @Nullable Identifier name,
        JdbcEnvironment jdbcEnvironment
    ) {
        return allLowerCase(name);
    }

    @Override
    public @Nullable Identifier toPhysicalSequenceName(
        @Nullable Identifier name,
        JdbcEnvironment jdbcEnvironment
    ) {
        return allLowerCase(name);
    }

    @Override
    public @Nullable Identifier toPhysicalColumnName(
        @Nullable Identifier name,
        JdbcEnvironment jdbcEnvironment
    ) {
        return allLowerCase(name);
    }
}
