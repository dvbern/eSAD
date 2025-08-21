package ch.dvbern.oss.hemed.esc.api.shared.person;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
class LandCodeTest {

    @Test
    void checkEachLandBelongsToLandCode() {
        List<Land> laender = Arrays.stream(Land.values()).toList();

        for (Land land : laender) {
            LandCode code = LandCode.getCode(land);

            LOG.info("Land: {} belongs to LandCode: {}", land, code);
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
        LOG.info("Länder: {}", laender.size());
        LOG.info("Länder from LandCode: {}", laenderFromLandCode.size());
        assertThat(laender).hasSize(laenderFromLandCode.size());
    }
}
