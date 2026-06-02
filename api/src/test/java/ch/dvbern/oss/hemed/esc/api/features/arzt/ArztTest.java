package ch.dvbern.oss.hemed.esc.api.features.arzt;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class ArztTest {

    @ParameterizedTest
    @CsvSource(value = {
        "Muster, , Muster",
        "Muster, '', Muster",
        "Muster, Max, Muster Max"
    })
    void getNameVorname_shouldCombineNameAndVorname(String name, String vorname, String expected) {
        // ARRAGE
        Arzt arzt = new Arzt();
        arzt.setName(name);
        arzt.setVorname(vorname);

        // ACT
        String result = arzt.getNameVorname();

        // ASSERT
        assertThat(result).isEqualTo(expected);
    }

}
