package ch.dvbern.oss.hemed.esc.api.shared.validator;

import java.util.List;

import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.UntersuchungArztBeurteilung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Bewegungsapparat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Blutdruck;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Einbeinhuepfen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Einbeinstand;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Fingeropposition;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Haut;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Herzauskultation;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Motorik;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.MundAndZaehne;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Sprachfaehigkeit;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Verhalten;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.types.RequirementSetting;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungArztBeurteilungBuilder;
import org.assertj.core.api.Assertions;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class CheckUntersuchungArztBeurteilungPresentIfReqValidatorTest {

    @ParameterizedTest
    @CsvSource({
        "REQUIRED    ,              , false" ,
        "REQUIRED    , GANGSTOERUNG , true"  ,
        "OPTIONAL    ,              , true"  ,
        "OPTIONAL    , GANGSTOERUNG , true"  ,
        "NOT_VISIBLE ,              , true"  ,
        "NOT_VISIBLE , GANGSTOERUNG , true"  ,

    })
    void validateBewegungsapparat(
        @NonNull RequirementSetting requirementSetting,
        @Nullable Bewegungsapparat bewegungsapparat,
        boolean expectedValid
    ) {
        // ARRANGE
        List<Bewegungsapparat> bewegungsapparatList = bewegungsapparat != null
            ? List.of(bewegungsapparat)
            : null;
        UntersuchungArztBeurteilung entity = UntersuchungArztBeurteilungBuilder.create(
            builder -> builder
                .withBewegungsapparat(bewegungsapparatList));
        entity.getUntersuchung().getEinladung().getAnlass().getAnlassConfig().setUntersuchungBewegungsapparat(requirementSetting);
        // ACT
        CheckUntersuchungArztBeurteilungPresentIfReqValidator
            validator = new CheckUntersuchungArztBeurteilungPresentIfReqValidator();
        var result = validator.isEntityValid(entity);
        // ASSERT
        Assertions.assertThat(result).isEqualTo(expectedValid);
    }

    @ParameterizedTest
    @CsvSource({
        "REQUIRED	,	    ,false",
        "REQUIRED	,SICHER	,true",
        "OPTIONAL	,       ,true",
        "OPTIONAL	,SICHER	,true",
        "NOT_VISIBLE,       ,true",
        "NOT_VISIBLE,SICHER	,true",
    })
    void validateEinbeinstand(
        @NonNull RequirementSetting requirementSetting,
        @Nullable Einbeinstand einbeinstand,
        boolean expectedValid
    ) {
        // ARRANGE
        UntersuchungArztBeurteilung entity = UntersuchungArztBeurteilungBuilder.create(
            builder -> builder
                .withEinbeinstand(einbeinstand));
        entity.getUntersuchung().getEinladung().getAnlass().getAnlassConfig().setUntersuchungEinbeinstand(requirementSetting);
        // ACT
        CheckUntersuchungArztBeurteilungPresentIfReqValidator
            validator = new CheckUntersuchungArztBeurteilungPresentIfReqValidator();
        var result = validator.isEntityValid(entity);
        // ASSERT
        Assertions.assertThat(result).isEqualTo(expectedValid);
    }

    @ParameterizedTest
    @CsvSource({
        "REQUIRED	,	    ,false",
        "REQUIRED	,SICHER	,true",
        "OPTIONAL	,       ,true",
        "OPTIONAL	,SICHER	,true",
        "NOT_VISIBLE,       ,true",
        "NOT_VISIBLE,SICHER	,true",
    })
    void validateEinbeinhuepfen(
        @NonNull RequirementSetting requirementSetting,
        @Nullable Einbeinhuepfen einbeinhuepfen,
        boolean expectedValid
    ) {
        // ARRANGE
        UntersuchungArztBeurteilung entity = UntersuchungArztBeurteilungBuilder.create(
            builder -> builder
                .withEinbeinhuepfen(einbeinhuepfen));
        entity.getUntersuchung().getEinladung().getAnlass().getAnlassConfig().setUntersuchungEinbeinhuepfen(requirementSetting);
        // ACT
        CheckUntersuchungArztBeurteilungPresentIfReqValidator
            validator = new CheckUntersuchungArztBeurteilungPresentIfReqValidator();
        var result = validator.isEntityValid(entity);
        // ASSERT
        Assertions.assertThat(result).isEqualTo(expectedValid);
    }

    @ParameterizedTest
    @CsvSource({
        "REQUIRED    ,                    , false" ,
        "REQUIRED    , ALTERSENTSPRECHEND , true"  ,
        "OPTIONAL    ,                    , true"  ,
        "OPTIONAL    , ALTERSENTSPRECHEND , true"  ,
        "NOT_VISIBLE ,                    , true"  ,
        "NOT_VISIBLE , ALTERSENTSPRECHEND , true"  ,

    })
    void validateFingeropposition(
        @NonNull RequirementSetting requirementSetting,
        @Nullable Fingeropposition fingeropposition,
        boolean expectedValid
    ) {
        // ARRANGE
        List<Fingeropposition> fingeroppositionList = fingeropposition != null
            ? List.of(fingeropposition)
            : null;
        UntersuchungArztBeurteilung entity = UntersuchungArztBeurteilungBuilder.create(
            builder -> builder
                .withFingeropposition(fingeroppositionList));
        entity.getUntersuchung().getEinladung().getAnlass().getAnlassConfig().setUntersuchungFingeropposition(requirementSetting);
        // ACT
        CheckUntersuchungArztBeurteilungPresentIfReqValidator
            validator = new CheckUntersuchungArztBeurteilungPresentIfReqValidator();
        var result = validator.isEntityValid(entity);
        // ASSERT
        Assertions.assertThat(result).isEqualTo(expectedValid);
    }

    @ParameterizedTest
    @CsvSource({
        "REQUIRED	,	    ,false",
        "REQUIRED	,NORMAL	,true",
        "OPTIONAL	,       ,true",
        "OPTIONAL	,NORMAL	,true",
        "NOT_VISIBLE,       ,true",
        "NOT_VISIBLE,NORMAL	,true",
    })
    void validateMotorik(
        @NonNull RequirementSetting requirementSetting,
        @Nullable Motorik motorik,
        boolean expectedValid
    ) {
        // ARRANGE
        UntersuchungArztBeurteilung entity = UntersuchungArztBeurteilungBuilder.create(
            builder -> builder
                .withMotorik(motorik));
        entity.getUntersuchung().getEinladung().getAnlass().getAnlassConfig().setUntersuchungMotorik(requirementSetting);
        // ACT
        CheckUntersuchungArztBeurteilungPresentIfReqValidator
            validator = new CheckUntersuchungArztBeurteilungPresentIfReqValidator();
        var result = validator.isEntityValid(entity);
        // ASSERT
        Assertions.assertThat(result).isEqualTo(expectedValid);
    }

    @ParameterizedTest
    @CsvSource({
        "REQUIRED    ,                       , false" ,
        "REQUIRED    , GROBKURSORISCH_NORMAL , true"  ,
        "OPTIONAL    ,                       , true"  ,
        "OPTIONAL    , GROBKURSORISCH_NORMAL , true"  ,
        "NOT_VISIBLE ,                       , true"  ,
        "NOT_VISIBLE , GROBKURSORISCH_NORMAL , true"  ,

    })
    void validateSprachfaehigkeit(
        @NonNull RequirementSetting requirementSetting,
        @Nullable Sprachfaehigkeit sprachfaehigkeit,
        boolean expectedValid
    ) {
        // ARRANGE
        List<Sprachfaehigkeit> sprachfaehigkeitList = sprachfaehigkeit != null
            ? List.of(sprachfaehigkeit)
            : null;
        UntersuchungArztBeurteilung entity = UntersuchungArztBeurteilungBuilder.create(
            builder -> builder
                .withSprachfaehigkeit(sprachfaehigkeitList));
        entity.getUntersuchung().getEinladung().getAnlass().getAnlassConfig().setUntersuchungSprache(requirementSetting);
        // ACT
        CheckUntersuchungArztBeurteilungPresentIfReqValidator
            validator = new CheckUntersuchungArztBeurteilungPresentIfReqValidator();
        var result = validator.isEntityValid(entity);
        // ASSERT
        Assertions.assertThat(result).isEqualTo(expectedValid);
    }

    @ParameterizedTest
    @CsvSource({
        "REQUIRED    ,              , false" ,
        "REQUIRED    , UNAUFFAELLIG , true"  ,
        "OPTIONAL    ,              , true"  ,
        "OPTIONAL    , UNAUFFAELLIG , true"  ,
        "NOT_VISIBLE ,              , true"  ,
        "NOT_VISIBLE , UNAUFFAELLIG , true"  ,

    })
    void validateMundAndZaehne(
        @NonNull RequirementSetting requirementSetting,
        @Nullable MundAndZaehne mundAndZaehne,
        boolean expectedValid
    ) {
        // ARRANGE
        List<MundAndZaehne> mundAndZaehneList = mundAndZaehne != null
            ? List.of(mundAndZaehne)
            : null;
        UntersuchungArztBeurteilung entity = UntersuchungArztBeurteilungBuilder.create(
            builder -> builder
                .withMundAndZaehne(mundAndZaehneList));
        entity.getUntersuchung().getEinladung().getAnlass().getAnlassConfig().setUntersuchungMundZaehne(requirementSetting);
        // ACT
        CheckUntersuchungArztBeurteilungPresentIfReqValidator
            validator = new CheckUntersuchungArztBeurteilungPresentIfReqValidator();
        var result = validator.isEntityValid(entity);
        // ASSERT
        Assertions.assertThat(result).isEqualTo(expectedValid);
    }

    @ParameterizedTest
    @CsvSource({
        "REQUIRED    ,                             , false" ,
        "REQUIRED    , GROBKURSORISCH_UNAUFFAELLIG , true"  ,
        "OPTIONAL    ,                             , true"  ,
        "OPTIONAL    , GROBKURSORISCH_UNAUFFAELLIG , true"  ,
        "NOT_VISIBLE ,                             , true"  ,
        "NOT_VISIBLE , GROBKURSORISCH_UNAUFFAELLIG , true"  ,

    })
    void validateHaut(
        @NonNull RequirementSetting requirementSetting,
        @Nullable Haut haut,
        boolean expectedValid
    ) {
        // ARRANGE
        UntersuchungArztBeurteilung entity = UntersuchungArztBeurteilungBuilder.create(
            builder -> builder
                .withHaut(haut));
        entity.getUntersuchung().getEinladung().getAnlass().getAnlassConfig().setUntersuchungHaut(requirementSetting);
        // ACT
        CheckUntersuchungArztBeurteilungPresentIfReqValidator
            validator = new CheckUntersuchungArztBeurteilungPresentIfReqValidator();
        var result = validator.isEntityValid(entity);
        // ASSERT
        Assertions.assertThat(result).isEqualTo(expectedValid);
    }

    @ParameterizedTest
    @CsvSource({
        "REQUIRED    ,              , false" ,
       "REQUIRED    , UNAUFFAELLIG , true"  ,
       "OPTIONAL    ,              , true"  ,
       "OPTIONAL    , UNAUFFAELLIG , true"  ,
       "NOT_VISIBLE ,              , true"  ,
       "NOT_VISIBLE , UNAUFFAELLIG , true"  ,

    })
    void validateHerzauskultation(
        @NonNull RequirementSetting requirementSetting,
        @Nullable Herzauskultation herzauskultation,
        boolean expectedValid
    ) {
        // ARRANGE
        UntersuchungArztBeurteilung entity = UntersuchungArztBeurteilungBuilder.create(
            builder -> builder
                .withHerzauskultation(herzauskultation));
        entity.getUntersuchung().getEinladung().getAnlass().getAnlassConfig().setUntersuchungHerz(requirementSetting);
        // ACT
        CheckUntersuchungArztBeurteilungPresentIfReqValidator
            validator = new CheckUntersuchungArztBeurteilungPresentIfReqValidator();
        var result = validator.isEntityValid(entity);
        // ASSERT
        Assertions.assertThat(result).isEqualTo(expectedValid);
    }

    @ParameterizedTest
    @CsvSource({
        "REQUIRED	,	    ,false",
        "REQUIRED	,NORMAL	,true",
        "OPTIONAL	,       ,true",
        "OPTIONAL	,NORMAL	,true",
        "NOT_VISIBLE,       ,true",
        "NOT_VISIBLE,NORMAL	,true",
    })
    void validateBlutdruck(
        @NonNull RequirementSetting requirementSetting,
        @Nullable Blutdruck blutdruck,
        boolean expectedValid
    ) {
        // ARRANGE
        UntersuchungArztBeurteilung entity = UntersuchungArztBeurteilungBuilder.create(
            builder -> builder
                .withBlutdruck(blutdruck));
        entity.getUntersuchung().getEinladung().getAnlass().getAnlassConfig().setUntersuchungBlutdruck(requirementSetting);
        // ACT
        CheckUntersuchungArztBeurteilungPresentIfReqValidator
            validator = new CheckUntersuchungArztBeurteilungPresentIfReqValidator();
        var result = validator.isEntityValid(entity);
        // ASSERT
        Assertions.assertThat(result).isEqualTo(expectedValid);
    }

    @ParameterizedTest
    @CsvSource({
        "REQUIRED    ,              , false" ,
        "REQUIRED    , UNAUFFAELLIG , true"  ,
        "OPTIONAL    ,              , true"  ,
        "OPTIONAL    , UNAUFFAELLIG , true"  ,
        "NOT_VISIBLE ,              , true"  ,
        "NOT_VISIBLE , UNAUFFAELLIG , true"  ,

    })
    void validateVerhalten(
        @NonNull RequirementSetting requirementSetting,
        @Nullable Verhalten verhalten,
        boolean expectedValid
    ) {
        // ARRANGE
        UntersuchungArztBeurteilung entity = UntersuchungArztBeurteilungBuilder.create(
            builder -> builder
                .withVerhalten(verhalten));
        entity.getUntersuchung().getEinladung().getAnlass().getAnlassConfig().setUntersuchungVerhalten(requirementSetting);
        // ACT
        CheckUntersuchungArztBeurteilungPresentIfReqValidator
            validator = new CheckUntersuchungArztBeurteilungPresentIfReqValidator();
        var result = validator.isEntityValid(entity);
        // ASSERT
        Assertions.assertThat(result).isEqualTo(expectedValid);
    }
}
