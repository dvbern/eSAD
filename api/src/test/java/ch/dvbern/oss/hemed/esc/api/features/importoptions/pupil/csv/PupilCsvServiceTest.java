package ch.dvbern.oss.hemed.esc.api.features.importoptions.pupil.csv;

import java.io.File;
import java.io.Reader;
import java.nio.file.Path;
import java.util.List;

import ch.dvbern.oss.hemed.esc.api.testutil.TestUtil;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import static ch.dvbern.dvbstarter.infrastructure.fileio.FileUtil.createReaderFromPath;

class PupilCsvServiceTest {

    private final PupilCsvService pupilCsvService = new PupilCsvService();

    @Test
    void parsePupilCSV() {
        // ARRANGE
        File file = TestUtil.loadComplexPupilFile();

        // transform file to reader
        Reader reader = createReaderFromPath(Path.of(file.getPath()));

        // ACT
        List<PupilCsvDTO> pupilCsvDTOS = pupilCsvService.parsePupilCSV(reader);

        //ASSERT
        Assertions.assertThat(pupilCsvDTOS).isNotEmpty().hasSize(49);
        Assertions.assertThat(pupilCsvDTOS.getFirst())
            .returns("S84860", PupilCsvDTO::schuelerId)
            .returns("Šabani", PupilCsvDTO::schuelerName)
            .returns("Zuzanna", PupilCsvDTO::schuelerVorname)
            .returns("1", PupilCsvDTO::schuelerGeschlecht)
            .returns("21.08.2002", PupilCsvDTO::schuelerGeburtsdatum)
            .returns("32a368ec-669f-4fd8-a503-806b72322fe5", PupilCsvDTO::klasseId)
            .returns("KIGA 2b Gerzler", PupilCsvDTO::klasseBezeichnung)
            .returns("Kindergarten", PupilCsvDTO::klasseStufe)
            .returns("2", PupilCsvDTO::klasseProgrammjahr)
            .returns("95b8a129-5020-4814-ab4c-ca37360dbc49", PupilCsvDTO::schuleId)
            .returns("Kindergarten Gerzler", PupilCsvDTO::schuleBezeichnung);
    }
}
