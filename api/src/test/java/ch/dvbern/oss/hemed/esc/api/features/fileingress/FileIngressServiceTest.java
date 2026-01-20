package ch.dvbern.oss.hemed.esc.api.features.fileingress;

import java.time.LocalDate;

import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppValidationException;
import ch.dvbern.oss.hemed.esc.api.features.kinder.edokid_id.EdokidId;
import com.ibm.icu.impl.Pair;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FileIngressServiceTest {

    @ParameterizedTest
    @CsvSource({
        "2016-02-29__312332101.pdf,312332101,true",
        "invalid-filename.pdf,312332101,false",

    })
    void validate(
        String filename,
        String esadId,
        boolean expectedValid
    ) {

        if (expectedValid) {
            Pair<LocalDate, EdokidId> localDateEdokidIdPair = FileIngressService.parseFilename(esadId, filename);
            EdokidId parsedEdokidId = localDateEdokidIdPair.second;

            assertThat(localDateEdokidIdPair.first).isNotNull();
            assertThat(parsedEdokidId).isNotNull();
            assertThat(parsedEdokidId).hasToString(parsedEdokidId.toFormattedString().replace(" ", ""));



        } else {
            assertThatThrownBy(() -> FileIngressService.parseFilename(esadId, filename))
                .isInstanceOf(AppValidationException.class);
        }
    }
}
