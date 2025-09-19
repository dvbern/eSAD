package ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung;

import java.time.LocalDate;
import java.util.List;

import ch.dvbern.oss.hemed.esc.api.features.anlass.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Bewegungsapparat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Blutdruck;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Einbeinhuepfen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Einbeinstand;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Fingeropposition;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Haut;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Herzauskultation;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.MundAndZaehne;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Sprachfaehigkeit;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungArztBeurteilungBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungBuilder;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class UntersuchungArztBeurteilungMapperTest {


    @Test
    void toEntity_should_keep_bemerkungen_if_required() {
        // ARRANGE
        Untersuchung untersuchung = UntersuchungBuilder.defaultUntersuchung(LocalDate.now());
        UntersuchungArztBeurteilungSaveJson json =
            UntersuchungArztBeurteilungBuilder.createJax(builder -> builder
                .withBlutdruck(Blutdruck.AUFFAELLIG)
                .withHerzauskultation(Herzauskultation.ANDERES, "Herzauskultation Bemerkung")
                .withHaut(Haut.ANDERES, "Haut Bemerkung")
                .withMundAndZaehne(List.of(MundAndZaehne.ANDERES), "MundAndZaehne Bemerkung")
                .withSprachfaehigkeit(List.of(Sprachfaehigkeit.ANDERES), "Sprachfaehigkeit Bemerkung")
                .withBewegungsapparat(List.of(Bewegungsapparat.ANDERES), "Bewegungsapparat Bemerkung")
                .withEinbeinstand(Einbeinstand.ANDERES, "Einbeinstand Bemerkung")
                .withEinbeinhuepfen(Einbeinhuepfen.ANDERES, "Einbeinhuepfen Bemerkung")
                .withFingeropposition(List.of(Fingeropposition.ANDERES), "Fingeropposition Bemerkung")
            ).getData();
        // ACT
        UntersuchungArztBeurteilung entity = UntersuchungArztBeurteilungMapper.toEntity(untersuchung, null, json);
        // ASSERT
        Assertions.assertThat(entity.getHerzauskultationBemerkung()).isEqualTo("Herzauskultation Bemerkung");
        Assertions.assertThat(entity.getHautBemerkung()).isEqualTo("Haut Bemerkung");
        Assertions.assertThat(entity.getMundAndZaehneBemerkung()).isEqualTo("MundAndZaehne Bemerkung");
        Assertions.assertThat(entity.getSprachfaehigkeitBemerkung()).isEqualTo("Sprachfaehigkeit Bemerkung");
        Assertions.assertThat(entity.getBewegungsapparatBemerkung()).isEqualTo("Bewegungsapparat Bemerkung");
        Assertions.assertThat(entity.getEinbeinstandBemerkung()).isEqualTo("Einbeinstand Bemerkung");
        Assertions.assertThat(entity.getEinbeinhuepfenBemerkung()).isEqualTo("Einbeinhuepfen Bemerkung");
        Assertions.assertThat(entity.getFingeroppositionBemerkung()).isEqualTo("Fingeropposition Bemerkung");
    }

    @Test
    void toEntity_should_remove_bemerkungen_if_not_required() {
        // ARRANGE
        Untersuchung untersuchung = UntersuchungBuilder.defaultUntersuchung(LocalDate.now());
        UntersuchungArztBeurteilungSaveJson json =
            UntersuchungArztBeurteilungBuilder.createJax(builder -> builder
                .withBlutdruck(Blutdruck.NORMAL)
                .withHerzauskultation(Herzauskultation.UNAUFFAELLIG, "Herzauskultation Bemerkung")
                .withHaut(Haut.GROBKURSORISCH_UNAUFFAELLIG, "Haut Bemerkung")
                .withMundAndZaehne(List.of(MundAndZaehne.ADENOIDHYPERPLASIE), "MundAndZaehne Bemerkung")
                .withSprachfaehigkeit(List.of(Sprachfaehigkeit.GROBKURSORISCH_NORMAL), "Sprachfaehigkeit Bemerkung")
                .withBewegungsapparat(List.of(Bewegungsapparat.FUSSFEHLSTELLUNG), "Bewegungsapparat Bemerkung")
                .withEinbeinstand(Einbeinstand.SICHER, "Einbeinstand Bemerkung")
                .withEinbeinhuepfen(Einbeinhuepfen.SICHER, "Einbeinhuepfen Bemerkung")
                .withFingeropposition(List.of(Fingeropposition.ALTERSENTSPRECHEND), "Fingeropposition Bemerkung")
            ).getData();
        // ACT
        UntersuchungArztBeurteilung entity = UntersuchungArztBeurteilungMapper.toEntity(untersuchung, null, json);
        // ASSERT
        Assertions.assertThat(entity.getHerzauskultationBemerkung()).isNull();
        Assertions.assertThat(entity.getHautBemerkung()).isNull();
        Assertions.assertThat(entity.getMundAndZaehneBemerkung()).isNull();
        Assertions.assertThat(entity.getSprachfaehigkeitBemerkung()).isNull();
        Assertions.assertThat(entity.getBewegungsapparatBemerkung()).isNull();
        Assertions.assertThat(entity.getEinbeinstandBemerkung()).isNull();
        Assertions.assertThat(entity.getEinbeinhuepfenBemerkung()).isNull();
        Assertions.assertThat(entity.getFingeroppositionBemerkung()).isNull();
    }
}
