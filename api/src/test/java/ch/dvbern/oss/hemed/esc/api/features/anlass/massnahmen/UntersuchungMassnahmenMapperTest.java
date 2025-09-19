package ch.dvbern.oss.hemed.esc.api.features.anlass.massnahmen;

import java.time.LocalDate;
import java.util.Collections;

import ch.dvbern.oss.hemed.esc.api.features.anlass.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.infomaterial.InfomaterialMapper;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungMassnahmenBuilder;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UntersuchungMassnahmenMapperTest {

    private final InfomaterialMapper infomaterialMapper = mock(InfomaterialMapper.class);
    private final UntersuchungMassnahmenMapper mapper = new UntersuchungMassnahmenMapper(null, infomaterialMapper, null);

    @BeforeEach
    void setUp() {
        when(infomaterialMapper.toEntityList(ArgumentMatchers.any())).thenReturn(Collections.emptySet());
    }

    @Test
    void toEntity_should_keep_bemerkungen_andere_if_needed() {
        // ARRANGE
        Untersuchung untersuchung = UntersuchungBuilder.defaultUntersuchung(LocalDate.now());
        UntersuchungMassnahmenSaveJson json =
            UntersuchungMassnahmenBuilder.createJax(builder -> builder
                .withAnderes(true, "Anderes Bemerkung")
                .withBereitsBehandeltAnderes(true, "Bereits behandelt Bemerkung")
            ).getData();
        // ACT
        UntersuchungMassnahmen entity = mapper.toEntity(untersuchung, null, json);
        // ASSERT
        Assertions.assertThat(entity.getAnderesBemerkung()).isEqualTo("Anderes Bemerkung");
        Assertions.assertThat(entity.getBereitsBehandeltAnderesBemerkung()).isEqualTo("Bereits behandelt Bemerkung");
    }

    @Test
    void toEntity_should_remove_bemerkungen_andere_if_not_needed() {
        // ARRANGE
        Untersuchung untersuchung = UntersuchungBuilder.defaultUntersuchung(LocalDate.now());
        UntersuchungMassnahmenSaveJson json =
            UntersuchungMassnahmenBuilder.createJax(builder -> builder
                .withAnderes(false, "Anderes Bemerkung")
                .withBereitsBehandeltAnderes(false, "Bereits behandelt Bemerkung")
            ).getData();
        // ACT
        UntersuchungMassnahmen entity = mapper.toEntity(untersuchung, null, json);
        // ASSERT
        Assertions.assertThat(entity.getAnderesBemerkung()).isNull();
        Assertions.assertThat(entity.getBereitsBehandeltAnderesBemerkung()).isNull();
    }
}
