/*
 * Copyright © 2026 DV Bern AG, Switzerland
 *
 * Das vorliegende Dokument, einschliesslich aller seiner Teile, ist urheberrechtlich
 * geschützt. Jede Verwertung ist ohne Zustimmung der DV Bern AG unzulässig. Dies gilt
 * insbesondere für Vervielfältigungen, die Einspeicherung und Verarbeitung in
 * elektronischer Form. Wird das Dokument einem Kunden im Rahmen der Projektarbeit zur
 * Ansicht übergeben, ist jede weitere Verteilung durch den Kunden an Dritte untersagt.
 */

package ch.dvbern.oss.hemed.esc.api.shared.util;

import java.time.LocalDate;

import ch.dvbern.oss.hemed.esc.api.shared.person.Geschlecht;
import ch.dvbern.oss.hemed.esc.api.testutil.AbstractITest;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
class HashParityITest extends AbstractITest {

    @PersistenceContext
    EntityManager entityManager;

    @ParameterizedTest
    @CsvSource({
        "Müller, Hans, MAENNLICH, 2010-05-20",
        "MÜLLER, HANS, MAENNLICH, 2010-05-20",
        "Müßig, François, MAENNLICH, 2015-08-12",
        "Åse, Øyvind, WEIBLICH, 2020-01-01",
        "René, Noël, MAENNLICH, 2022-03-10",
        "Çetin, Jörg, MAENNLICH, 2022-03-10",
        "Ødegård, Åse, WEIBLICH, 2018-06-15",
        "ægir, thor, MAENNLICH, 2019-07-20",
        // Characters where Java NFD and PostgreSQL unaccent() would diverge
        // without explicit REPLACE rules.
        "Đorđević, Nikola, MAENNLICH, 2015-04-11",
        "Łukasz, Wałęsa, MAENNLICH, 2012-09-30",
        "Þórður, Ðagur, MAENNLICH, 2017-11-05",
        "Œuvre, Cœur, WEIBLICH, 2019-02-14",
        "ÆLFRED, ÆTHELRED, MAENNLICH, 2016-06-06"
    })
    void verifyJavaAndSqlHashParity(
        @NonNull String name,
        @NonNull String vorname,
        @NonNull Geschlecht geschlecht,
        @NonNull LocalDate geburtsdatum
    ) {
        // 1. Generate Hash using Java Utility
        String javaHash = HashValueGeneratorUtil.generateHashValueForKindValues(
            name,
            vorname,
            geschlecht,
            geburtsdatum
        );

        // 2. Generate Hash using SQL (mimicking the migration logic in 0072-add-hashvalue-for-kind.xml)
        String sql = """
            SELECT encode(digest(
                               LOWER(
                                 unaccent(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(
                                 REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(
                                                                                                           CAST(?1 AS TEXT),
                                                                                                           'Ä', 'Ae'), 'Ö', 'Oe'), 'Ü', 'Ue'), 'ä', 'ae'), 'ö', 'oe'), 'ü', 'ue'),
                                                                 'ß', 'ss'), 'æ', 'ae'), 'Ø', 'Oe'), 'ø', 'oe'), 'Å', 'Aa'), 'å', 'aa'),
                                                                 'Æ', 'Ae'), 'Đ', 'D'), 'đ', 'd'), 'Ł', 'L'), 'ł', 'l'),
                                                                 'Þ', 'Th'), 'þ', 'th'), 'Ð', 'D'), 'ð', 'd'),
                                                                 'Œ', 'Oe'), 'œ', 'oe'))
                                 ||
                                 unaccent(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(
                                 REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(
                                                                                                           CAST(?2 AS TEXT),
                                                                                                           'Ä', 'Ae'), 'Ö', 'Oe'), 'Ü', 'Ue'), 'ä', 'ae'), 'ö', 'oe'), 'ü', 'ue'),
                                                                 'ß', 'ss'), 'æ', 'ae'), 'Ø', 'Oe'), 'ø', 'oe'), 'Å', 'Aa'), 'å', 'aa'),
                                                                 'Æ', 'Ae'), 'Đ', 'D'), 'đ', 'd'), 'Ł', 'L'), 'ł', 'l'),
                                                                 'Þ', 'Th'), 'þ', 'th'), 'Ð', 'D'), 'ð', 'd'),
                                                                 'Œ', 'Oe'), 'œ', 'oe'))
                                 ||
                                 CAST(?3 AS TEXT) ||
                                 TO_CHAR(CAST(?4 AS DATE), 'YYYY-MM-DD')
                               ),
                               'sha256'
                             ), 'hex')
            """;


        String sqlHash = (String) entityManager.createNativeQuery(sql)
            .setParameter(1, name)
            .setParameter(2, vorname)
            .setParameter(3, geschlecht.name())
            .setParameter(4, geburtsdatum)
            .getSingleResult();

        // 3. Assert Parity
        assertThat(javaHash)
            .withFailMessage("Mismatch for %s %s: Java[%s] vs SQL[%s]", name, vorname, javaHash, sqlHash)
            .isEqualTo(sqlHash);
    }
}
