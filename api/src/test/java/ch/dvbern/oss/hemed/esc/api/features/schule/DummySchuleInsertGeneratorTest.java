/*
 *
 * Copyright (C) 2025 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.oss.hemed.esc.api.features.schule;

import java.util.HashMap;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringSubstitutor;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@Slf4j
@Disabled("Only for generating SQL")
class DummySchuleInsertGeneratorTest {

    private static String fullSql = "";

    @AfterAll
    static void afterAll() {
        LOG.info(fullSql);
    }

    @ParameterizedTest
    @CsvSource({
        "d6bff17c-a9e2-45ff-bff1-7ca9e265ffc5, Allgemeine Gewerbeschule Basel",
        "1727c8ac-5e00-4455-a7c8-ac5e00f4551f, Berufsfachschule Basel",
        "5ff2707c-303d-4618-b270-7c303da618b0, Fachmaturitätsschule",
        "6dde6527-e296-495c-9e65-27e296e95c4d, Gymnasium Bäumlihof",
        "3a688b01-7fd2-41ec-a88b-017fd281ec21, Gymnasium Kirschgarten / MfB",
        "78c2faf9-bc6d-4fcc-82fa-f9bc6dffccff, Gymnasium Kirschgarten / Passerelle",
        "d947ae8d-6ba2-427f-87ae-8d6ba2727f1f, Gymnasium Kirschgarten",
        "005750c6-c373-4774-9750-c6c373d774f1, Gymnasium Leonhard",
        "4f622ad8-b25f-4f6d-a22a-d8b25f5f6dc4, Gymnasium am Münsterplatz",
        "727db5a4-ec80-4fc3-bdb5-a4ec805fc33f, Schule für Gestaltung",
        "1e7d5454-0a4d-4ef1-bd54-540a4d3ef138, Wirtschaftsgymnasium und Wirtschaftsmittelschule",
        "b0a067bf-a9b0-4e33-a067-bfa9b0fe333b, Zentrum für Brückenangebote"
    })
    void generate_statement(String uuidString, String schulkreis) {

        String sqltemplate = """

                   INSERT INTO schule (id,
                        timestamperstellt,
                        timestampmutiert,
                        usererstellt,
                        usermutiert,
                        version,
                        tenant,
                        adressezusatz,
                        land,
                        ort,
                        plz,
                        strasseundnr,
                        bemerkungen,
                        email,
                        schulhaus,
                        schulhausnummer,
                        schulkreis,
                        schulstandort,
                        telefon,
                        zustaendigarzt_id,
                        zustaendigmpa_id,
                        quartier_id,
                        externalsystemid,
                        aktiv)
                    VALUES ('${uuid}',
                        current_timestamp,
                        current_timestamp,
                        'liquibase',
                        'liquibase',
                        0,
                        'BS',
                        null,
                        null,
                        null,
                        null,
                        null,
                        '${bemerkung}',
                        null,
                        '${schulhaus}',
                         null,
                        '${schulkreis}',
                        '${schulstandort}',
                        null,
                        null,
                        null, null, null, true);
            """;

        Map<String, Object> params = new HashMap<>();
        params.put("uuid", uuidString);
        params.put(
            "bemerkung",
            "Schule wird für Import benutzt falls in Escada bei einer Klasse keine Schule angegeben wird");
        params.put("schulhaus", schulkreis);
        params.put("schulkreis", schulkreis);
        params.put("schulstandort", schulkreis);

        String sqlResult = StringSubstitutor.replace(sqltemplate, params, "${", "}");

        fullSql += "-- INSERT " + schulkreis;
        fullSql = fullSql + sqlResult;

        Assertions.assertThat(fullSql).isNotEmpty();

    }

}
