package ch.dvbern.oss.hemed.esc.api.features.anlass;

import java.time.LocalDate;

import ch.dvbern.oss.hemed.esc.api.features.anlass.json.UntersuchungTriageSaveJson;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungTriageBuilder;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class UntersuchungTriageMapperTest {


    @Test
    void toEntity_should_keep_bemerkungen_if_checked() {
        // ARRANGE
        Untersuchung untersuchung = UntersuchungBuilder.defaultUntersuchung(LocalDate.now());
        UntersuchungTriageSaveJson json =
            UntersuchungTriageBuilder.createJax(builder -> builder
                .withTriageSu(true, "Bemerkung SU")
                .withTriageMpa(true, "Bemerkungen Mpa")
                .withBefundSu(true, "Bemerkungen Befund SU")
                .withWunschSchueler(true, "Bemerkungen Wunsch Schueler")
                .withVorinformationLehrperson(true, "Bemerkungen Vorinformation Lehrperson")
            ).getData();
        // ACT
        UntersuchungTriage entity = UntersuchungTriageMapper.toEntity(untersuchung, null, json);
        // ASSERT
        Assertions.assertThat(entity.getTriageSuBemerkungen()).isEqualTo("Bemerkung SU");
        Assertions.assertThat(entity.getTriageMpaBemerkungen()).isEqualTo("Bemerkungen Mpa");
        Assertions.assertThat(entity.getBefundSuBemerkungen()).isEqualTo("Bemerkungen Befund SU");
        Assertions.assertThat(entity.getWunschSchuelerBemerkungen()).isEqualTo("Bemerkungen Wunsch Schueler");
        Assertions.assertThat(entity.getVorinformationLehrpersonBemerkungen()).isEqualTo("Bemerkungen Vorinformation Lehrperson");
    }

    @Test
    void toEntity_should_remove_bemerkungen_if_not_checked() {
        // ARRANGE
        Untersuchung untersuchung = UntersuchungBuilder.defaultUntersuchung(LocalDate.now());
        UntersuchungTriageSaveJson json =
            UntersuchungTriageBuilder.createJax(builder -> builder
                .withTriageSu(false, "Bemerkung SU")
                .withTriageMpa(false, "Bemerkungen Mpa")
                .withBefundSu(false, "Bemerkungen Befund SU")
                .withWunschSchueler(false, "Bemerkungen Wunsch Schueler")
                .withVorinformationLehrperson(false, "Bemerkungen Vorinformation Lehrperson")
            ).getData();
        // ACT
        UntersuchungTriage entity = UntersuchungTriageMapper.toEntity(untersuchung, null, json);
        // ASSERT
        Assertions.assertThat(entity.getTriageSuBemerkungen()).isNull();
        Assertions.assertThat(entity.getTriageMpaBemerkungen()).isNull();
        Assertions.assertThat(entity.getBefundSuBemerkungen()).isNull();
        Assertions.assertThat(entity.getWunschSchuelerBemerkungen()).isNull();
        Assertions.assertThat(entity.getVorinformationLehrpersonBemerkungen()).isNull();
    }
}
