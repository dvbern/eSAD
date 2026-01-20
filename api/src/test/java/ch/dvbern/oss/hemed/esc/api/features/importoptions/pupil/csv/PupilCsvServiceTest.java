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
            .returns("S84475", PupilCsvDTO::schuelerId)
            .returns("Jankovic", PupilCsvDTO::schuelerName)
            .returns("Ana", PupilCsvDTO::schuelerVorname)
            .returns("2", PupilCsvDTO::schuelerGeschlecht)
            .returns("30.09.2019", PupilCsvDTO::schuelerGeburtsdatum)
            .returns("111111", PupilCsvDTO::klasseId)
            .returns("KIGA 2A Wasserwiesen", PupilCsvDTO::klasseBezeichnung)
            .returns("Kindergarten", PupilCsvDTO::klasseStufe)
            .returns("1", PupilCsvDTO::klasseProgrammjahr)
            .returns("222221", PupilCsvDTO::schuleId)
            .returns("Wasserwiesen", PupilCsvDTO::schuleBezeichnung);
    }
}
