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

package ch.dvbern.oss.hemed.esc.api.features.escada.csv;

import java.io.File;
import java.io.Reader;
import java.nio.file.Path;
import java.util.List;

import ch.dvbern.oss.hemed.esc.api.testutil.TestUtil;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import static ch.dvbern.dvbstarter.infrastructure.fileio.FileUtil.createReaderFromPath;

class EscadaCsvServiceTest {

    private final EscadaCsvService escadaCsvService = new EscadaCsvService();

    @Test
    void parseEscadaCSV() {
        // ARRANGE
        File file = TestUtil.loadComplexEscadaFile();
        // transform file to reader
        Reader reader = createReaderFromPath(Path.of(file.getPath()));
        // ACT
        List<EscadaCsvDTO> escadaCsvDTOS = escadaCsvService.parseEscadaCSV(reader);
        // ASSERT
        Assertions.assertThat(escadaCsvDTOS).isNotEmpty().hasSize(28251);
        Assertions.assertThat(escadaCsvDTOS.getFirst())
            .returns("244969", EscadaCsvDTO::schuelerId)
            .returns("244969", EscadaCsvDTO::schuelerId)
            .returns("Eltern", EscadaCsvDTO::wohntyp)
            .returns("Oneta", EscadaCsvDTO::schuelerName)
            .returns("Sibylle", EscadaCsvDTO::schuelerVorname)
            .returns("2017-11-10T00:00:00", EscadaCsvDTO::schuelerGeburtsdatum)
            .returns("2", EscadaCsvDTO::schuelerGeschlecht)
            .returns("756.2298.3284.00", EscadaCsvDTO::schuelerSozialversicheurng)
            .returns("Thiersteinerallee 53", EscadaCsvDTO::schuelerAdresse)
            .returns(null, EscadaCsvDTO::schuelerAdresse1)
            .returns(null, EscadaCsvDTO::schuelerAdresse2)
            .returns("4053", EscadaCsvDTO::schuelerPlz)
            .returns("Basel", EscadaCsvDTO::schuelerOrt)
            .returns("079 685 98 41", EscadaCsvDTO::schuelerMobile)
            .returns(null, EscadaCsvDTO::schuelerEmail)
            .returns("Marokko", EscadaCsvDTO::schuelerNationalitaet)
            .returns("MA", EscadaCsvDTO::schuelerStaatcodeIso)
            .returns("Deutsch", EscadaCsvDTO::schuelerSprache)
            .returns("Arabisch", EscadaCsvDTO::schuelerErstsprache)
            .returns("Volksschulen", EscadaCsvDTO::schulkreisBezeichnung)
            .returns("17", EscadaCsvDTO::schulanlageId)
            .returns("Primarstufe Margarethen", EscadaCsvDTO::schulanlageBezeichnung)
            .returns("57", EscadaCsvDTO::schulhausId)
            .returns("Margarethen", EscadaCsvDTO::schulhausBezeichnung)
            .returns("12009662", EscadaCsvDTO::klasseId)
            .returns("PS  MA MARGA 1A", EscadaCsvDTO::klasseBezeichnung)
            .returns("Primarstufe", EscadaCsvDTO::klasseStufe)
            .returns("RK", EscadaCsvDTO::klasseSchulart)
            .returns("1", EscadaCsvDTO::klasseProgrammjahr)
            .returns("319190", EscadaCsvDTO::klasseLehrpersonId)
            .returns("Frau", EscadaCsvDTO::klasseLehrpersonAnrede)
            .returns("Galli", EscadaCsvDTO::klasseLehrpersonName)
            .returns("Jennifer", EscadaCsvDTO::klasseLehrpersonVorname)
            .returns(null, EscadaCsvDTO::klasseLehrperson2Id)
            .returns(null, EscadaCsvDTO::klasseLehrperson2Anrede)
            .returns(null, EscadaCsvDTO::klasseLehrperson2Name)
            .returns(null, EscadaCsvDTO::klasseLehrperson2Vorname)
            .returns(null, EscadaCsvDTO::klasseLehrperson3Id)
            .returns(null, EscadaCsvDTO::klasseLehrperson3Anrede)
            .returns(null, EscadaCsvDTO::klasseLehrperson3Name)
            .returns(null, EscadaCsvDTO::klasseLehrperson3Vorname)
            .returns(null, EscadaCsvDTO::klasseLehrperson4Id)
            .returns(null, EscadaCsvDTO::klasseLehrperson4Anrede)
            .returns(null, EscadaCsvDTO::klasseLehrperson4Name)
            .returns(null, EscadaCsvDTO::klasseLehrperson4Vorname)
            .returns("325924", EscadaCsvDTO::adressierungNr)
            .returns("Herr", EscadaCsvDTO::adressierungAnrede)
            .returns("Aebi", EscadaCsvDTO::adressierungName)
            .returns("Reto", EscadaCsvDTO::adressierungVorname)
            .returns("Klosterberg", EscadaCsvDTO::adressierungStrasse)
            .returns("13", EscadaCsvDTO::adressierungHausnummer)
            .returns("4053", EscadaCsvDTO::adressierungPlz)
            .returns("Basel", EscadaCsvDTO::adressierungOrt)
            .returns(null, EscadaCsvDTO::adressierungTelefonPrivat)
            .returns(null, EscadaCsvDTO::adressierungTelefonGeschaeft)
            .returns("077 983 37 00", EscadaCsvDTO::adressierungNatel)
            .returns("hubertus45@example.com", EscadaCsvDTO::adressierungEmail)
            .returns("615134", EscadaCsvDTO::adressierungNr2)
            .returns("Frau", EscadaCsvDTO::adressierungAnrede2)
            .returns("Ullrich", EscadaCsvDTO::adressierungName2)
            .returns("Melanie", EscadaCsvDTO::adressierungVorname2)
            .returns("Holbeinstrasse", EscadaCsvDTO::adressierungStrasse2)
            .returns("19", EscadaCsvDTO::adressierungHausnummer2)
            .returns("4053", EscadaCsvDTO::adressierungPlz2)
            .returns("Basel", EscadaCsvDTO::adressierungOrt2)
            .returns(null, EscadaCsvDTO::adressierungTelefonPrivat2)
            .returns(null, EscadaCsvDTO::adressierungTelefonGeschaeft2)
            .returns(null, EscadaCsvDTO::adressierungNatel2)
            .returns("froehricht@example.org", EscadaCsvDTO::adressierungEmail2);
    }

}
