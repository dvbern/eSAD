package ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren;

import java.time.LocalDate;
import java.util.List;

import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.json.UntersuchungHoerenOtoskopieSaveJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.types.OtoskopieResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungHoerenOtoskopieBuilder;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class UntersuchungHoerenOtoskopieMapperTest {

    @Test
    void toEntity_should_keep_bemerkungen_otoskopie_if_ANDERE() {
        // ARRANGE
        Untersuchung untersuchung = UntersuchungBuilder.defaultUntersuchung(LocalDate.now());
        UntersuchungHoerenOtoskopieSaveJson json =
            UntersuchungHoerenOtoskopieBuilder.createJax(builder -> builder
                .withOtoskopieRechts(List.of(OtoskopieResultat.ANDERES), null,"Bemerkung Rechts")
                .withOtoskopieLinks(List.of(OtoskopieResultat.ANDERES), null, "Bemerkung Links")
            ).getOtoskopieData();
        // ACT
        UntersuchungHoerenOtoskopie entity = UntersuchungHoerenOtoskopieMapper.toEntity(untersuchung, null, json);
        // ASSERT
        Assertions.assertThat(entity.getOtoskopieRechtsResultatAndere()).isEqualTo("Bemerkung Rechts");
        Assertions.assertThat(entity.getOtoskopieLinksResultatAndere()).isEqualTo("Bemerkung Links");
    }

    @Test
    void toEntity_should_keep_bemerkungen_otoskopie_if_multiple_including_ANDERE() {
        // ARRANGE
        Untersuchung untersuchung = UntersuchungBuilder.defaultUntersuchung(LocalDate.now());
        UntersuchungHoerenOtoskopieSaveJson json =
            UntersuchungHoerenOtoskopieBuilder.createJax(builder -> builder
                .withOtoskopieRechts(
                    List.of(OtoskopieResultat.NORMAL, OtoskopieResultat.ANDERES),null,
                    "Bemerkung Rechts")
                .withOtoskopieLinks(
                    List.of(OtoskopieResultat.NORMAL, OtoskopieResultat.ANDERES),null,
                    "Bemerkung Links")
            ).getOtoskopieData();
        // ACT
        UntersuchungHoerenOtoskopie entity = UntersuchungHoerenOtoskopieMapper.toEntity(untersuchung, null, json);
        // ASSERT
        Assertions.assertThat(entity.getOtoskopieRechtsResultatAndere()).isEqualTo("Bemerkung Rechts");
        Assertions.assertThat(entity.getOtoskopieLinksResultatAndere()).isEqualTo("Bemerkung Links");
    }

    @Test
    void toEntity_should_remove_bemerkungen_otoskopie_if_not_ANDERE() {
        // ARRANGE
        Untersuchung untersuchung = UntersuchungBuilder.defaultUntersuchung(LocalDate.now());
        UntersuchungHoerenOtoskopieSaveJson json =
            UntersuchungHoerenOtoskopieBuilder.createJax(builder -> builder
                .withOtoskopieRechts(List.of(OtoskopieResultat.NORMAL), null, "Bemerkung Rechts")
                .withOtoskopieLinks(List.of(OtoskopieResultat.NORMAL), null, "Bemerkung Links")
            ).getOtoskopieData();
        // ACT
        UntersuchungHoerenOtoskopie entity = UntersuchungHoerenOtoskopieMapper.toEntity(untersuchung, null, json);
        // ASSERT
        Assertions.assertThat(entity.getOtoskopieRechtsResultatAndere()).isNull();
        Assertions.assertThat(entity.getOtoskopieLinksResultatAndere()).isNull();
    }

    @Test
    void toEntity_should_remove_bemerkungen_otoskopie_if_null() {
        // ARRANGE
        Untersuchung untersuchung = UntersuchungBuilder.defaultUntersuchung(LocalDate.now());
        UntersuchungHoerenOtoskopieSaveJson json =
            UntersuchungHoerenOtoskopieBuilder.createJax(builder -> builder
                .withOtoskopieRechts(null, null, "Bemerkung Rechts")
                .withOtoskopieLinks(null, null, "Bemerkung Links")
            ).getOtoskopieData();
        // ACT
        UntersuchungHoerenOtoskopie entity = UntersuchungHoerenOtoskopieMapper.toEntity(untersuchung, null, json);
        // ASSERT
        Assertions.assertThat(entity.getOtoskopieRechtsResultatAndere()).isNull();
        Assertions.assertThat(entity.getOtoskopieLinksResultatAndere()).isNull();
    }

    @Test
    void toEntity_should_respect_independence_between_links_and_rechts() {
        // ARRANGE
        Untersuchung untersuchung = UntersuchungBuilder.defaultUntersuchung(LocalDate.now());
        UntersuchungHoerenOtoskopieSaveJson json =
            UntersuchungHoerenOtoskopieBuilder.createJax(builder -> builder
                .withOtoskopieRechts(List.of(OtoskopieResultat.ANDERES), null, "Bemerkung Rechts")
                .withOtoskopieLinks(List.of(OtoskopieResultat.NORMAL), null, "Bemerkung Links")
            ).getOtoskopieData();
        // ACT
        UntersuchungHoerenOtoskopie entity = UntersuchungHoerenOtoskopieMapper.toEntity(untersuchung, null, json);
        // ASSERT
        Assertions.assertThat(entity.getOtoskopieRechtsResultatAndere()).isEqualTo("Bemerkung Rechts");
        Assertions.assertThat(entity.getOtoskopieLinksResultatAndere()).isNull();
    }
}
