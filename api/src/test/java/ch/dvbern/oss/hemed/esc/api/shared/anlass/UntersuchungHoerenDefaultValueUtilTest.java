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

package ch.dvbern.oss.hemed.esc.api.shared.anlass;

import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.AnlassConfig;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.types.RequirementSetting;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.json.AudiogrammSaveJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.types.Dezibel;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.AnlassConfigBuilder;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UntersuchungHoerenDefaultValueUtilTest {

    @Test
    void buildsAudiogrammFromConfigDefaultValues() {
        AnlassConfig config = AnlassConfigBuilder.allSameRequirementSetting(RequirementSetting.OPTIONAL);
        config.setHoerenAudiogrammDefaultValue(Dezibel.DB20);
        config.setHoerenAudiogramm250HzDefaultValue(Dezibel.DB30);
        config.setHoerenAudiogramm8000HzDefaultValue(Dezibel.DB40);

        AudiogrammSaveJson result = UntersuchungHoerenDefaultValueUtil.getDefaultAudiogramm(config);

        assertThat(result).isNotNull();
        assertThat(result.getDbHz250()).isEqualTo(Dezibel.DB30);
        assertThat(result.getDbHz500()).isEqualTo(Dezibel.DB20);
        assertThat(result.getDbHz1000()).isEqualTo(Dezibel.DB20);
        assertThat(result.getDbHz2000()).isEqualTo(Dezibel.DB20);
        assertThat(result.getDbHz4000()).isEqualTo(Dezibel.DB20);
        assertThat(result.getDbHz6000()).isEqualTo(Dezibel.DB20);
        assertThat(result.getDbHz8000()).isEqualTo(Dezibel.DB40);
    }

    @Test
    void returnsNullWhenAudiogrammNotVisible() {
        AnlassConfig config = AnlassConfigBuilder.allSameRequirementSetting(RequirementSetting.NOT_VISIBLE);
        config.setHoerenAudiogrammDefaultValue(Dezibel.DB20);
        config.setHoerenAudiogramm250HzDefaultValue(Dezibel.DB20);
        config.setHoerenAudiogramm8000HzDefaultValue(Dezibel.DB20);

        assertThat(UntersuchungHoerenDefaultValueUtil.getDefaultAudiogramm(config)).isNull();
    }

    @Test
    void gatesEdgeFrequenciesByTheirOwnVisibility() {
        AnlassConfig config = AnlassConfigBuilder.allSameRequirementSetting(RequirementSetting.OPTIONAL);
        config.setHoerenAudiogramm250Hz(RequirementSetting.NOT_VISIBLE);
        config.setHoerenAudiogramm8000Hz(RequirementSetting.NOT_VISIBLE);
        config.setHoerenAudiogrammDefaultValue(Dezibel.DB20);
        config.setHoerenAudiogramm250HzDefaultValue(Dezibel.DB20);
        config.setHoerenAudiogramm8000HzDefaultValue(Dezibel.DB20);

        AudiogrammSaveJson result = UntersuchungHoerenDefaultValueUtil.getDefaultAudiogramm(config);

        assertThat(result).isNotNull();
        assertThat(result.getDbHz250()).isNull();
        assertThat(result.getDbHz8000()).isNull();
        assertThat(result.getDbHz500()).isEqualTo(Dezibel.DB20);
    }

    @Test
    void propagatesNullMainDefaultToMainFrequencies() {
        AnlassConfig config = AnlassConfigBuilder.allSameRequirementSetting(RequirementSetting.OPTIONAL);
        config.setHoerenAudiogrammDefaultValue(null);
        config.setHoerenAudiogramm250HzDefaultValue(null);
        config.setHoerenAudiogramm8000HzDefaultValue(null);

        AudiogrammSaveJson result = UntersuchungHoerenDefaultValueUtil.getDefaultAudiogramm(config);

        assertThat(result).isNotNull();
        assertThat(result.getDbHz500()).isNull();
        assertThat(result.getDbHz250()).isNull();
        assertThat(result.getDbHz8000()).isNull();
    }
}
