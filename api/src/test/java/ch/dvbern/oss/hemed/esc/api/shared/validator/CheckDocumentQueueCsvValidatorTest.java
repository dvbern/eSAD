package ch.dvbern.oss.hemed.esc.api.shared.validator;

import java.time.LocalDate;

import ch.dvbern.oss.hemed.esc.api.shared.documentqueue.DocumentQueue;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileTyp;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.DocumentQueueBuilder;
import org.assertj.core.api.Assertions;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class CheckDocumentQueueCsvValidatorTest {

    @ParameterizedTest
    @CsvSource({
        "SPSS_REPORT   ,2025-01-01  , 2025-02-02    , true",
        "SPSS_REPORT   ,            ,               , true",
        "CSV_REPORT    ,2025-01-01  , 2025-02-02    , true",
        "CSV_REPORT    ,            , 2025-02-02    , false",
        "CSV_REPORT    ,2025-02-02  ,               , false",
        "CSV_REPORT    ,            ,               , false",
    })
    void validate(
        @NonNull FileTyp fileType,
        @Nullable LocalDate datumVon,
        @Nullable LocalDate datumBis,
        boolean expectedValid
    ) {
        DocumentQueue documentQueue = DocumentQueueBuilder.defaultDocumentQueue();
        documentQueue.setType(fileType);
        documentQueue.setDatumVon(datumVon);
        documentQueue.setDatumBis(datumBis);

        CheckDocumentQueueCsvValidator validatorUnderTest = new CheckDocumentQueueCsvValidator();
        var result = validatorUnderTest.isEntityValid(documentQueue);
        Assertions.assertThat(result).isEqualTo(expectedValid);
    }
}
