package ch.dvbern.oss.hemed.esc.api.features.anlass;

import ch.dvbern.oss.hemed.esc.api.features.anlass.json.AnlassConfigJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.RequirementSetting;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.AnlassConfigBuilder;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class AnlassConfigMapperTest {

    @Test
    void toJson_shouldMapAllFields() {
        // ARRANGE
        AnlassConfig entity = AnlassConfigBuilder.defaultAnlassConfig();
        entity.setShowTriageTriage(false);
        entity.setShowTriageKindWeiterleiten(false);

        // ACT
        AnlassConfigJson json = AnlassConfigMapper.toJson(entity);

        // ASSERT
        assertNotNull(json);
        assertEquals(entity.getAnlassTyp(), json.getAnlassTyp());
        Assertions.assertThat(json.isShowTabHoeren()).isTrue();
        Assertions.assertThat(json.getHoerenAudiogramm()).isEqualTo(RequirementSetting.OPTIONAL);
        Assertions.assertThat(json.isShowTabTriage()).isFalse();
        Assertions.assertThat(json.isShowTriageTriage()).isFalse();
        Assertions.assertThat(json.isShowTriageKindWeiterleiten()).isFalse();
        Assertions.assertThat(json.isShowChecklist()).isTrue();
        Assertions.assertThat(json.getCheckEinwilligungVorhanden()).isEqualTo(RequirementSetting.OPTIONAL);
        Assertions.assertThat(json.getCheckKontraindikationenAbgeklaert()).isEqualTo(RequirementSetting.OPTIONAL);
        Assertions.assertThat(json.getCheckDoppelkontrolleImpfentscheidDurchgefuehrt())
            .isEqualTo(RequirementSetting.OPTIONAL);
    }
}
