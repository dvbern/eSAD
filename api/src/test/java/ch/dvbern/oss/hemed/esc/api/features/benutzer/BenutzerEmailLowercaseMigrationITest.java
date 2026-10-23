package ch.dvbern.oss.hemed.esc.api.features.benutzer;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Statement;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import jakarta.persistence.EntityManager;

import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantEnum;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import ch.dvbern.oss.hemed.esc.api.testutil.AbstractITest;
import ch.dvbern.oss.hemed.esc.api.testutil.FixtureService;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.BenutzerBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsAdminBS;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.hibernate.Session;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests the changeSet {@code db/base/0087-benutzer-email-lowercase.xml}: it lowercases the Benutzer emails and adds
 * the check constraint {@value #CHECK_CONSTRAINT}, which keeps them lowercase.
 */
@QuarkusTest
@RequiredArgsConstructor
@DisplayNameGeneration(ReplaceUnderscores.class)
class BenutzerEmailLowercaseMigrationITest extends AbstractITest {

    private static final String MIGRATION_CHANGELOG = "db/base/0087-benutzer-email-lowercase.xml";
    private static final String CHECK_CONSTRAINT = "chk_benutzer_email_lowercase";
    private static final String ID_PREFIX_PLACEHOLDER = "{id}";
    private static final OffsetDateTime ERSTELLT_OLDEST = OffsetDateTime.parse("2025-01-01T00:00:00+01:00");

    private final BenutzerRepo benutzerRepo;
    private final FixtureService fixtureService;
    private final EntityManager entityManager;

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("emailsBeforeAndAfterMigration")
    @TestAsAdminBS
    @TestTransaction
    // rollback after test, since it drops the check constraint to store emails in mixed case as before the migration
    void migration_stores_the_emails_in_lowercase(
        @SuppressWarnings("unused") String beschreibung,
        @NonNull List<String> emailsOldestFirst,
        @NonNull List<String> expectedEmails
    ) {
        // ARRANGE
        String domain = UUID.randomUUID().toString();
        List<Benutzer> benutzerOldestFirst = Stream.generate(this::persistBenutzer)
            .limit(emailsOldestFirst.size())
            .toList();
        // flushing the Benutzer later would look up the current user in a new transaction, which would wait for the
        // lock on the table that dropping the constraint takes
        entityManager.flush();
        entityManager.clear();
        entityManager.createNativeQuery("ALTER TABLE benutzer DROP CONSTRAINT " + CHECK_CONSTRAINT)
            .executeUpdate();
        IntStream.range(0, benutzerOldestFirst.size())
            .forEach(index -> storeEmailAndErstellt(
                benutzerOldestFirst.get(index),
                withDomain(emailsOldestFirst.get(index), domain),
                ERSTELLT_OLDEST.plusMinutes(index)
            ));

        // ACT
        executeMigrationSql();

        // ASSERT
        List<String> emailsAfterMigration = benutzerOldestFirst.stream()
            .map(this::readEmail)
            .toList();
        List<String> expected = IntStream.range(0, benutzerOldestFirst.size())
            .mapToObj(index -> withDomain(expectedEmails.get(index), domain)
                .replace(ID_PREFIX_PLACEHOLDER, idPrefix(benutzerOldestFirst.get(index))))
            .toList();
        assertThat(emailsAfterMigration)
            .containsExactlyElementsOf(expected);
    }

    static Stream<Arguments> emailsBeforeAndAfterMigration() {
        String longLocalPart = "a".repeat(64);
        return Stream.of(
            Arguments.of(
                "email in mixed case is lowercased",
                List.of("Anna.Muster@Example.COM"),
                List.of("anna.muster@example.com")
            ),
            Arguments.of(
                "email in lowercase stays unchanged",
                List.of("anna.muster@example.com"),
                List.of("anna.muster@example.com")
            ),
            Arguments.of(
                "email in mixed case gets a suffix if another Benutzer has it in lowercase",
                List.of("Anna.Muster@Example.COM", "anna.muster@example.com"),
                List.of("anna.muster+{id}@example.com", "anna.muster@example.com")
            ),
            Arguments.of(
                "of several emails in mixed case the oldest is lowercased and the others get a suffix",
                List.of("Anna.Muster@Example.COM", "ANNA.MUSTER@EXAMPLE.COM", "anna.MUSTER@example.com"),
                List.of("anna.muster@example.com", "anna.muster+{id}@example.com", "anna.muster+{id}@example.com")
            ),
            Arguments.of(
                "suffix shortens a long part before the @ so that the email stays valid",
                List.of(longLocalPart + "@example.com", longLocalPart.toUpperCase(Locale.ROOT) + "@example.com"),
                List.of(longLocalPart + "@example.com", "a".repeat(55) + "+{id}@example.com")
            )
        );
    }

    @ParameterizedTest(name = "[{index}] {0}")
    // the tests only migrate the databases of BS and BE, see application.properties
    @EnumSource(value = TenantEnum.class, names = { "BS", "BE" })
    @TestAsAdminBS
    void database_rejects_an_email_with_capital_letters(@NonNull TenantEnum tenant) {
        // ARRANGE
        String emailWithCapitalLetters = withDomain("Anna.Muster@example.com", UUID.randomUUID().toString());

        // ACT & ASSERT
        // should throw because we have a db constraint that does not allow uppercase in email on Benutzer
        assertThatThrownBy(() -> fixtureService.storeBenutzerWithRawEmail(tenant, emailWithCapitalLetters))
            .rootCause()
            .hasMessageContaining(CHECK_CONSTRAINT);
    }

    @NonNull
    private Benutzer persistBenutzer() {
        Benutzer benutzer = BenutzerBuilder.create(builder -> { });
        benutzerRepo.persist(benutzer);
        return benutzer;
    }

    private void storeEmailAndErstellt(
        @NonNull Benutzer benutzer,
        @NonNull String email,
        @NonNull OffsetDateTime erstellt
    ) {
        entityManager
            .createNativeQuery(
                "UPDATE benutzer SET email = :email, timestamperstellt = :erstellt WHERE id = CAST(:id AS uuid)"
            )
            .setParameter("email", email)
            .setParameter("erstellt", erstellt)
            .setParameter("id", idOf(benutzer))
            .executeUpdate();
    }

    /**
     * Runs the SQL of the changeSet, like Liquibase does at deployment.
     */
    private void executeMigrationSql() {
        String sql = readMigrationSql();
        entityManager.unwrap(Session.class)
            .doWork(connection -> {
                try (Statement statement = connection.createStatement()) {
                    statement.execute(sql);
                }
            });
    }

    @NonNull
    private String readEmail(@NonNull Benutzer benutzer) {
        return (String) entityManager
            .createNativeQuery("SELECT email FROM benutzer WHERE id = CAST(:id AS uuid)")
            .setParameter("id", idOf(benutzer))
            .getSingleResult();
    }

    /**
     * Puts the given domain in front of the domain of the email, so that each test works with emails of its own.
     */
    @NonNull
    private static String withDomain(@NonNull String email, @NonNull String domain) {
        return email.replace("@", "@" + domain + '.');
    }

    @NonNull
    private static String idPrefix(@NonNull Benutzer benutzer) {
        return idOf(benutzer).substring(0, 8);
    }

    @NonNull
    private static String idOf(@NonNull Benutzer benutzer) {
        return benutzer.getId().getId();
    }

    /**
     * Reads the SQL of the changeSet from the changelog, i.e. the text of its only sql element.
     */
    @NonNull
    private static String readMigrationSql() {
        ClassLoader classLoader = BenutzerEmailLowercaseMigrationITest.class.getClassLoader();
        try (InputStream changelog = Objects.requireNonNull(classLoader.getResourceAsStream(MIGRATION_CHANGELOG))) {
            NodeList sqlElements = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder()
                .parse(changelog)
                .getElementsByTagName("sql");
            assertThat(sqlElements.getLength())
                .as("sql elements in %s", MIGRATION_CHANGELOG)
                .isEqualTo(1);
            return sqlElements.item(0).getTextContent();
        } catch (IOException | ParserConfigurationException | SAXException e) {
            throw new IllegalStateException("Could not read the SQL from " + MIGRATION_CHANGELOG, e);
        }
    }
}
