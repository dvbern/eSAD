/*
 * Copyright (C) 2026 DV Bern AG, Switzerland
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.oss.hemed.esc.api.features.example.developer;

import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppValidationException;
import ch.dvbern.oss.hemed.esc.api.features.spss.CsvReportType;
import ch.dvbern.oss.hemed.esc.api.features.spss.SpssReportType;
import ch.dvbern.oss.hemed.esc.api.shared.documentqueue.DocumentQueueMapper;
import ch.dvbern.oss.hemed.esc.api.shared.documentqueue.json.DocumentQueueSpssSaveJson;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileTyp;
import ch.dvbern.oss.hemed.esc.api.testutil.AbstractITest;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsSupportBS;
import groovy.util.logging.Slf4j;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Slf4j
@RequiredArgsConstructor
@QuarkusTest
class DocumentQueueMapperITest extends AbstractITest {

    private final DocumentQueueMapper documentQueueMapper;

    @ParameterizedTest
    @CsvSource({
        "PLAYGROUND             ,                   ,                   ,           ,Beispiel Eintrag",
        "PLAYGROUND_ERROR       ,                   ,                   ,           ,Beispiel Fehler-Eintrag",
        "SPSS_REPORT            ,KINDERGARTEN       ,                   ,           ,Kindergarten",
        "SPSS_REPORT            ,KLASSE_4           ,                   ,           ,4_Primar",
        "SPSS_REPORT            ,KLASSE_8           ,                   ,           ,2_Sek",
        "SPSS_REPORT            ,KLASSE_9           ,                   ,           ,3_Sek",
        "SPSS_REPORT            ,FREMDSPRACHEN      ,                   ,           ,Fremdsprache",
        "SPSS_REPORT            ,IMPF_ANLAESSE      ,                   ,           ,Impfanlässe",
        "CSV_REPORT             ,                   ,SCHULUNTERSUCHUNG  ,           ,Schuluntersuchung",
        "CSV_REPORT             ,                   ,IMPFSTOFFE         ,           ,Impfstoffe",
        "MASSENBRIEF_ZIP        ,                   ,                   ,Test-Name  ,Test-Name Massenbrief",
        "MASSENBRIEF_ZIP        ,                   ,                   ,Arztbesuch ,Arztbesuch Massenbrief",
        // Test with missing parameters
        "SPSS_REPORT            ,                   ,                   ,           ,-",
        "CSV_REPORT             ,                   ,                   ,           ,-",
        "MASSENBRIEF_ZIP        ,                   ,                   ,           ,-",
        // Test unsupported FileTypes
        "ESCADA_SYNC            ,                   ,                   ,           ,-",
        "PUPIL_SYNC             ,                   ,                   ,           ,-",
        "IMPFAUSWEIS            ,                   ,                   ,           ,-",
        "NOTIZUPLOAD            ,                   ,                   ,           ,-",
        "AUFGABEUPLOAD          ,                   ,                   ,           ,-",
        "FRAGEBOGEN             ,                   ,                   ,           ,-",
        "KINDERDOSSIER          ,                   ,                   ,           ,-",
        "BRIEFVORLAGE           ,                   ,                   ,           ,-",
        "ANMELDUNG_BESTAETIGUNG ,                   ,                   ,           ,-",
    })
    void getDisplayNameTestMassenbrief(
        @NonNull FileTyp fileTyp,
        @Nullable SpssReportType spssReportType,
        @Nullable CsvReportType csvReportType,
        @Nullable String briefvorlageName,
        @NonNull String expectedDisplayName
    ) {
        // ACT
        String actualDisplayName =
            documentQueueMapper.getDisplayName(fileTyp, spssReportType, csvReportType, briefvorlageName);

        // ASSERT
        assertThat(actualDisplayName).isEqualTo(expectedDisplayName);
    }

    @Test
    @TestAsSupportBS
    @TestTransaction
    void addTooManylaygroundDocumentQueue() {
        // ARRANGE & ACT

        assertThatThrownBy(this::queueTooManyForSingleUser
        ).isInstanceOf(AppValidationException.class)
            .hasMessageContaining("AppValidation.TOO_MANY_QUEUED_DOCUMENTS, args: max=" + 10);

    }

    private void queueTooManyForSingleUser() {
        for (int i = 0; i < 11; i++) {
            DocumentQueueSpssSaveJson queueJobTooMany = new DocumentQueueSpssSaveJson(FileTyp.PLAYGROUND, null);
            this.documentQueueMapper.create(queueJobTooMany);
        }
    }
}
