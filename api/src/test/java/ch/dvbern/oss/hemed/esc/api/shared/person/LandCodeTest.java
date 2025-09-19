package ch.dvbern.oss.hemed.esc.api.shared.person;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LandCodeTest {

    @Test
    void checkEachLandBelongsToLandCode() {
        List<Land> laender = Arrays.stream(Land.values()).toList();

        for (Land land : laender) {
            LandCode code = LandCode.getCode(land);

            assertThat(land).isNotNull();
            assertThat(code).isNotNull();
        }
    }

    @Test
    void checkLandEnumHasSameAmountOfLaenderAsLandCode() {
        List<Land> laender = Arrays.stream(Land.values()).toList();
        List<Land> laenderFromLandCode = new ArrayList<>();
        List<LandCode> codes = Arrays.stream(LandCode.values()).toList();

        for (LandCode code : codes) {
            laenderFromLandCode.addAll(code.getLaender());
        }
        assertThat(laender).hasSize(laenderFromLandCode.size());
    }
}
