/*
 * Copyright © 2025 DV Bern AG, Switzerland
 *
 * Das vorliegende Dokument, einschliesslich aller seiner Teile, ist urheberrechtlich
 * geschützt. Jede Verwertung ist ohne Zustimmung der DV Bern AG unzulässig. Dies gilt
 * insbesondere für Vervielfältigungen, die Einspeicherung und Verarbeitung in
 * elektronischer Form. Wird das Dokument einem Kunden im Rahmen der Projektarbeit zur
 * Ansicht übergeben, ist jede weitere Verteilung durch den Kunden an Dritte untersagt.
 */

package ch.dvbern.oss.hemed.esc.api.shared.validator;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.types.RequirementSetting;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.UntersuchungSehenBeurteilung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.BrilleLinsenResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.DistanzSehenAugeEinzelnResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.DistanzSehenResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.FarbsehenResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.InspektionKonvergenzResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.MotilitaetResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.SehenEmpfehlung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.StereotestResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.StrabismusAuftretenResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.StrabismusAuspraegungResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.StrabismusResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.VerwendetTafelResultat;
import ch.dvbern.oss.hemed.esc.api.shared.valueenhanced.SingleValueEnhanced;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungSehenBeurteilungBuilder;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class CheckUntersuchungSehenBeurteilungPresentIfReqValidatorTest {

    @ParameterizedTest
    @CsvSource({
        "REQUIRED   ,           ,false",
        "REQUIRED   ,OHNE_KEINE ,true",
        "OPTIONAL   ,           ,true",
        "OPTIONAL   ,OHNE_KEINE ,true",
        "NOT_VISIBLE,           ,true",
        "NOT_VISIBLE,OHNE_KEINE ,true",
    })
    void validateSehenBrilleLinsen(
        @NonNull RequirementSetting requirementSetting,
        @Nullable BrilleLinsenResultat brilleLinsen,
        boolean expectedValid
    ) {
        // ARRANGE
        UntersuchungSehenBeurteilung sehen = UntersuchungSehenBeurteilungBuilder.create(
            builder -> builder
                .withBrilleLinsen(brilleLinsen, null));
        sehen.getUntersuchung().getEinladung().getAnlass().getAnlassConfig().setSehenBrilleLinsen(requirementSetting);
        // ACT
        CheckUntersuchungSehenBeurteilungPresentIfReqValidator
            validator = new CheckUntersuchungSehenBeurteilungPresentIfReqValidator();
        boolean result = validator.isEntityValid(sehen);
        // ASSERT
        assertThat(result).isEqualTo(expectedValid);
    }

    @ParameterizedTest
    @CsvSource({
        "REQUIRED   ,       ,false",
        "REQUIRED   ,NORMAL ,true",
        "OPTIONAL   ,       ,true",
        "OPTIONAL   ,NORMAL ,true",
        "NOT_VISIBLE,       ,true",
        "NOT_VISIBLE,NORMAL ,true",
    })
    void validateSehenFernvisusLinks(
        @NonNull RequirementSetting requirementSetting,
        @Nullable DistanzSehenAugeEinzelnResultat fernvisusAugeLinks,
        boolean expectedValid
    ) {
        // ARRANGE
        UntersuchungSehenBeurteilung sehen = UntersuchungSehenBeurteilungBuilder.create(builder -> {
        });
        sehen.getUntersuchung()
            .getEinladung()
            .getAnlass()
            .getAnlassConfig()
            .setSehenFernvisusAugeEinzeln(requirementSetting);
        sehen.setFernvisusAugeLinks(SingleValueEnhanced.of(fernvisusAugeLinks, null));
        sehen.setFernvisusAugeRechts(SingleValueEnhanced.of(fernvisusAugeLinks, null));
        // ACT
        CheckUntersuchungSehenBeurteilungPresentIfReqValidator
            validator = new CheckUntersuchungSehenBeurteilungPresentIfReqValidator();
        boolean result = validator.isEntityValid(sehen);
        // ASSERT
        assertThat(result).isEqualTo(expectedValid);
    }

    @ParameterizedTest
    @CsvSource({
        "REQUIRED   ,       ,false",
        "REQUIRED   ,NORMAL ,true",
        "OPTIONAL   ,       ,true",
        "OPTIONAL   ,NORMAL ,true",
        "NOT_VISIBLE,       ,true",
        "NOT_VISIBLE,NORMAL ,true",
    })
    void validateSehenFernvisusRechts(
        @NonNull RequirementSetting requirementSetting,
        @Nullable DistanzSehenAugeEinzelnResultat fernvisusAugeRechts,
        boolean expectedValid
    ) {
        // ARRANGE
        UntersuchungSehenBeurteilung sehen = UntersuchungSehenBeurteilungBuilder.create(builder -> {
        });
        sehen.getUntersuchung()
            .getEinladung()
            .getAnlass()
            .getAnlassConfig()
            .setSehenFernvisusAugeEinzeln(requirementSetting);
        sehen.setFernvisusAugeRechts(SingleValueEnhanced.of(fernvisusAugeRechts, null));
        sehen.setFernvisusAugeLinks(SingleValueEnhanced.of(fernvisusAugeRechts, null));
        // ACT
        CheckUntersuchungSehenBeurteilungPresentIfReqValidator
            validator = new CheckUntersuchungSehenBeurteilungPresentIfReqValidator();
        boolean result = validator.isEntityValid(sehen);
        // ASSERT
        assertThat(result).isEqualTo(expectedValid);
    }

    @ParameterizedTest
    @CsvSource({
        "REQUIRED   ,       ,false",
        "REQUIRED   ,NORMAL ,true",
        "OPTIONAL   ,       ,true",
        "OPTIONAL   ,NORMAL ,true",
        "NOT_VISIBLE,       ,true",
        "NOT_VISIBLE,NORMAL ,true",
    })
    void validateSehenFarbsehen(
        @NonNull RequirementSetting requirementSetting,
        @Nullable FarbsehenResultat farbsehen,
        boolean expectedValid
    ) {
        // ARRANGE
        UntersuchungSehenBeurteilung sehen = UntersuchungSehenBeurteilungBuilder.create(
            builder -> builder
                .withFarbsehen(farbsehen, null));
        sehen.getUntersuchung().getEinladung().getAnlass().getAnlassConfig().setSehenFarbsehen(requirementSetting);
        // ACT
        CheckUntersuchungSehenBeurteilungPresentIfReqValidator
            validator = new CheckUntersuchungSehenBeurteilungPresentIfReqValidator();
        boolean result = validator.isEntityValid(sehen);
        // ASSERT
        assertThat(result).isEqualTo(expectedValid);
    }

    @ParameterizedTest
    @CsvSource({
        "REQUIRED   ,       ,false",
        "REQUIRED   ,E_TAFEL,true",
        "OPTIONAL   ,       ,true",
        "OPTIONAL   ,E_TAFEL,true",
        "NOT_VISIBLE,       ,true",
        "NOT_VISIBLE,E_TAFEL,true",
    })
    void validateSehenVerwendeteTafel(
        @NonNull RequirementSetting requirementSetting,
        @Nullable VerwendetTafelResultat verwendeteTafel,
        boolean expectedValid
    ) {
        // ARRANGE
        UntersuchungSehenBeurteilung sehen = UntersuchungSehenBeurteilungBuilder.create(
            builder -> builder
                .withVerwendeteTafel(verwendeteTafel, null));
        sehen.getUntersuchung().getEinladung().getAnlass().getAnlassConfig().setSehenTafel(requirementSetting);
        // ACT
        CheckUntersuchungSehenBeurteilungPresentIfReqValidator
            validator = new CheckUntersuchungSehenBeurteilungPresentIfReqValidator();
        boolean result = validator.isEntityValid(sehen);
        // ASSERT
        assertThat(result).isEqualTo(expectedValid);
    }

    @ParameterizedTest
    @CsvSource({
        "REQUIRED   ,                       ,   ,   ,false",
        "REQUIRED   ,'NORMAL'               ,   ,   ,false",
        "REQUIRED   ,                       ,1  ,   ,false",
        "REQUIRED   ,                       ,   ,1  ,false",
        "REQUIRED   ,'NORMAL'               ,1  ,   ,false",
        "REQUIRED   ,'NORMAL'               ,   ,1  ,false",
        "REQUIRED   ,                       ,1  ,1  ,false",
        "REQUIRED   ,'NORMAL'               ,1  ,1  ,true",
        "REQUIRED   ,'NICHT_GEPRUEFT'       ,1  ,1  ,true",
        "REQUIRED   ,'NICHT_GEPRUEFT'       ,   ,   ,true",
        "REQUIRED   ,'NORMAL,NICHT_GEPRUEFT',   ,   ,true",
        "REQUIRED   ,'NORMAL,NICHT_GEPRUEFT',   ,1  ,true",
        "REQUIRED   ,'NORMAL,NICHT_GEPRUEFT',1  ,1  ,true",
        "OPTIONAL   ,                       ,   ,   ,true",
        "OPTIONAL   ,'NORMAL'               ,   ,   ,true",
        "OPTIONAL   ,                       ,1  ,   ,true",
        "OPTIONAL   ,                       ,   ,1  ,true",
        "OPTIONAL   ,'NORMAL'               ,1  ,   ,true",
        "OPTIONAL   ,'NORMAL'               ,   ,1  ,true",
        "OPTIONAL   ,                       ,1  ,1  ,true",
        "OPTIONAL   ,'NORMAL'               ,1  ,1  ,true",
        "NOT_VISIBLE,                       ,   ,   ,true",
        "NOT_VISIBLE,'NORMAL'               ,   ,   ,true",
        "NOT_VISIBLE,                       ,1  ,   ,true",
        "NOT_VISIBLE,                       ,   ,1  ,true",
        "NOT_VISIBLE,'NORMAL'               ,1  ,   ,true",
        "NOT_VISIBLE,'NORMAL'               ,   ,1  ,true",
        "NOT_VISIBLE,                       ,1  ,1  ,true",
        "NOT_VISIBLE,'NORMAL'               ,1  ,1  ,true",
    })
    void validateSehenFernvisus(
        @NonNull RequirementSetting requirementSetting,
        @Nullable String distanzSehenResultatString,
        @Nullable BigDecimal fernvisusRechts,
        @Nullable BigDecimal fernvisusLinks,
        boolean expectedValid
    ) {
        // ARRANGE
        List<DistanzSehenResultat> distanzSehenResultat =
            distanzSehenResultatString == null || distanzSehenResultatString.isBlank()
                ? null
                : Arrays.stream(distanzSehenResultatString.split(","))
                .map(String::trim)
                .map(DistanzSehenResultat::valueOf)
                .toList();
        UntersuchungSehenBeurteilung sehen = UntersuchungSehenBeurteilungBuilder.create(
            builder -> builder
                .withFernvisus(distanzSehenResultat, null)
                .withFernvisusLinks(fernvisusLinks)
                .withFernvisusRechts(fernvisusRechts));
        sehen.getUntersuchung().getEinladung().getAnlass().getAnlassConfig().setSehenFernvisus(requirementSetting);
        // ACT
        CheckUntersuchungSehenBeurteilungPresentIfReqValidator
            validator = new CheckUntersuchungSehenBeurteilungPresentIfReqValidator();
        boolean result = validator.isEntityValid(sehen);
        // ASSERT
        assertThat(result).isEqualTo(expectedValid);
    }

    @ParameterizedTest
    @CsvSource({
        "REQUIRED   ,                       ,   ,   ,false",
        "REQUIRED   ,'NORMAL'               ,   ,   ,false",
        "REQUIRED   ,                       ,1  ,   ,false",
        "REQUIRED   ,                       ,   ,1  ,false",
        "REQUIRED   ,'NORMAL'               ,1  ,   ,false",
        "REQUIRED   ,'NORMAL'               ,   ,1  ,false",
        "REQUIRED   ,                       ,1  ,1  ,false",
        "REQUIRED   ,'NORMAL'               ,1  ,1  ,true",
        "REQUIRED   ,'NICHT_GEPRUEFT'       ,1  ,1  ,true",
        "REQUIRED   ,'NICHT_GEPRUEFT'       ,   ,   ,true",
        "REQUIRED   ,'NORMAL,NICHT_GEPRUEFT',   ,   ,true",
        "REQUIRED   ,'NORMAL,NICHT_GEPRUEFT',   ,1  ,true",
        "REQUIRED   ,'NORMAL,NICHT_GEPRUEFT',1  ,1  ,true",
        "OPTIONAL   ,                       ,   ,   ,true",
        "OPTIONAL   ,'NORMAL'               ,   ,   ,true",
        "OPTIONAL   ,                       ,1  ,   ,true",
        "OPTIONAL   ,                       ,   ,1  ,true",
        "OPTIONAL   ,'NORMAL'               ,1  ,   ,true",
        "OPTIONAL   ,'NORMAL'               ,   ,1  ,true",
        "OPTIONAL   ,                       ,1  ,1  ,true",
        "OPTIONAL   ,'NORMAL'               ,1  ,1  ,true",
        "NOT_VISIBLE,                       ,   ,   ,true",
        "NOT_VISIBLE,'NORMAL'               ,   ,   ,true",
        "NOT_VISIBLE,                       ,1  ,   ,true",
        "NOT_VISIBLE,                       ,   ,1  ,true",
        "NOT_VISIBLE,'NORMAL'               ,1  ,   ,true",
        "NOT_VISIBLE,'NORMAL'               ,   ,1  ,true",
        "NOT_VISIBLE,                       ,1  ,1  ,true",
        "NOT_VISIBLE,'NORMAL'               ,1  ,1  ,true",
    })
    void validateSehenNahvisus(
        @NonNull RequirementSetting requirementSetting,
        @Nullable String distanzSehenResultatString,
        @Nullable BigDecimal nahvisusRechts,
        @Nullable BigDecimal nahvisusLinks,
        boolean expectedValid
    ) {
        // ARRANGE
        List<DistanzSehenResultat> distanzSehenResultat =
            distanzSehenResultatString == null || distanzSehenResultatString.isBlank()
                ? null
                : Arrays.stream(distanzSehenResultatString.split(","))
                .map(String::trim)
                .map(DistanzSehenResultat::valueOf)
                .toList();
        UntersuchungSehenBeurteilung sehen = UntersuchungSehenBeurteilungBuilder.create(
            builder -> builder
                .withNahvisus(distanzSehenResultat, null)
                .withNahvisusLinks(nahvisusLinks)
                .withNahvisusRechts(nahvisusRechts));
        sehen.getUntersuchung().getEinladung().getAnlass().getAnlassConfig().setSehenNahvisus(requirementSetting);
        // ACT
        CheckUntersuchungSehenBeurteilungPresentIfReqValidator
            validator = new CheckUntersuchungSehenBeurteilungPresentIfReqValidator();
        boolean result = validator.isEntityValid(sehen);
        // ASSERT
        assertThat(result).isEqualTo(expectedValid);
    }

    @ParameterizedTest
    @CsvSource({
        "REQUIRED   ,       ,false",
        "REQUIRED   ,NORMAL ,true",
        "OPTIONAL   ,       ,true",
        "OPTIONAL   ,NORMAL ,true",
        "NOT_VISIBLE,       ,true",
        "NOT_VISIBLE,NORMAL ,true",
    })
    void validateSehenStereoTest(
        @NonNull RequirementSetting requirementSetting,
        @Nullable StereotestResultat stereotest,
        boolean expectedValid
    ) {
        // ARRANGE
        UntersuchungSehenBeurteilung sehen = UntersuchungSehenBeurteilungBuilder.create(
            builder -> builder
                .withStereotest(stereotest, null));
        sehen.getUntersuchung().getEinladung().getAnlass().getAnlassConfig().setSehenStereoTest(requirementSetting);
        // ACT
        CheckUntersuchungSehenBeurteilungPresentIfReqValidator
            validator = new CheckUntersuchungSehenBeurteilungPresentIfReqValidator();
        boolean result = validator.isEntityValid(sehen);
        // ASSERT
        assertThat(result).isEqualTo(expectedValid);
    }

    @ParameterizedTest
    @CsvSource({
        "REQUIRED   ,                   ,false",
        "REQUIRED   ,'KEIN_STRABISMUS'  ,true",
        "OPTIONAL   ,                   ,true",
        "OPTIONAL   ,'KEIN_STRABISMUS'  ,true",
        "NOT_VISIBLE,                   ,true",
        "NOT_VISIBLE,'KEIN_STRABISMUS'  ,true",
    })
    void validateSehenStrabismus(
        @NonNull RequirementSetting requirementSetting,
        @Nullable String strabismusString,
        boolean expectedValid
    ) {
        // ARRANGE
        List<StrabismusResultat> strabismus = strabismusString == null || strabismusString.isBlank()
            ? null
            : Arrays.stream(strabismusString.split(","))
            .map(String::trim)
            .map(StrabismusResultat::valueOf)
            .toList();

        UntersuchungSehenBeurteilung sehen = UntersuchungSehenBeurteilungBuilder.create(
            builder -> builder
                .withStrabismus(strabismus, null)

        );
        sehen.getUntersuchung().getEinladung().getAnlass().getAnlassConfig().setSehenStrabismus(requirementSetting);
        // ACT
        CheckUntersuchungSehenBeurteilungPresentIfReqValidator
            validator = new CheckUntersuchungSehenBeurteilungPresentIfReqValidator();
        boolean result = validator.isEntityValid(sehen);
        // ASSERT
        assertThat(result).isEqualTo(expectedValid);
    }

    @ParameterizedTest
    @CsvSource({
        "REQUIRED   ,                   ,false",
        "REQUIRED   ,KEIN_STRABISMUS    ,true",
        "OPTIONAL   ,                   ,true",
        "OPTIONAL   ,LINKS              ,true",
        "NOT_VISIBLE,                   ,true",
        "NOT_VISIBLE,RECHTS             ,true",
    })
    void validateSehenStrabismusAuftreten(
        @NonNull RequirementSetting requirementSetting,
        @Nullable StrabismusAuftretenResultat strabismusAuftreten,
        boolean expectedValid
    ) {
        // ARRANGE
        UntersuchungSehenBeurteilung sehen = UntersuchungSehenBeurteilungBuilder.create(
            builder -> builder
                .withStrabismusAuftreten(strabismusAuftreten, null)
        );
        sehen.getUntersuchung()
            .getEinladung()
            .getAnlass()
            .getAnlassConfig()
            .setSehenStrabismusAuftreten(requirementSetting);
        // ACT
        CheckUntersuchungSehenBeurteilungPresentIfReqValidator
            validator = new CheckUntersuchungSehenBeurteilungPresentIfReqValidator();
        boolean result = validator.isEntityValid(sehen);
        // ASSERT
        assertThat(result).isEqualTo(expectedValid);
    }

    @ParameterizedTest
    @CsvSource({
        "REQUIRED   ,                   ,false",
        "REQUIRED   ,'MANIFEST'         ,true",
        "OPTIONAL   ,                   ,true",
        "OPTIONAL   ,'LATENT'           ,true",
        "NOT_VISIBLE,                   ,true",
        "NOT_VISIBLE,'KEIN_STRABISMUS'  ,true",
    })
    void validateSehenStrabismusAuspraegung(
        @NonNull RequirementSetting requirementSetting,
        @Nullable StrabismusAuspraegungResultat strabismusAuspraegung,
        boolean expectedValid
    ) {
        // ARRANGE
        UntersuchungSehenBeurteilung sehen = UntersuchungSehenBeurteilungBuilder.create(
            builder -> builder
                .withStrabismusAuspraegung(strabismusAuspraegung, null)

        );
        sehen.getUntersuchung()
            .getEinladung()
            .getAnlass()
            .getAnlassConfig()
            .setSehenStrabismusAuspraegung(requirementSetting);
        // ACT
        CheckUntersuchungSehenBeurteilungPresentIfReqValidator
            validator = new CheckUntersuchungSehenBeurteilungPresentIfReqValidator();
        boolean result = validator.isEntityValid(sehen);
        // ASSERT
        assertThat(result).isEqualTo(expectedValid);
    }

    @ParameterizedTest
    @CsvSource({
        "REQUIRED   ,               ,false",
        "REQUIRED   ,UNAUFFAELLIG   ,true",
        "OPTIONAL   ,               ,true",
        "OPTIONAL   ,UNAUFFAELLIG   ,true",
        "NOT_VISIBLE,               ,true",
        "NOT_VISIBLE,UNAUFFAELLIG   ,true",
    })
    void validateSehenInspektion(
        @NonNull RequirementSetting requirementSetting,
        @Nullable InspektionKonvergenzResultat inspektion,
        boolean expectedValid
    ) {
        // ARRANGE
        UntersuchungSehenBeurteilung sehen = UntersuchungSehenBeurteilungBuilder.create(
            builder -> builder
                .withInspektion(inspektion, null));
        sehen.getUntersuchung().getEinladung().getAnlass().getAnlassConfig().setSehenInspektion(requirementSetting);
        // ACT
        CheckUntersuchungSehenBeurteilungPresentIfReqValidator
            validator = new CheckUntersuchungSehenBeurteilungPresentIfReqValidator();
        boolean result = validator.isEntityValid(sehen);
        // ASSERT
        assertThat(result).isEqualTo(expectedValid);
    }

    @ParameterizedTest
    @CsvSource({
        "REQUIRED   ,               ,false",
        "REQUIRED   ,UNAUFFAELLIG   ,true",
        "OPTIONAL   ,               ,true",
        "OPTIONAL   ,UNAUFFAELLIG   ,true",
        "NOT_VISIBLE,               ,true",
        "NOT_VISIBLE,UNAUFFAELLIG   ,true",
    })
    void validateSehenKonvergenz(
        @NonNull RequirementSetting requirementSetting,
        @Nullable InspektionKonvergenzResultat konvergenz,
        boolean expectedValid
    ) {
        // ARRANGE
        UntersuchungSehenBeurteilung sehen = UntersuchungSehenBeurteilungBuilder.create(
            builder -> builder
                .withKonvergenz(konvergenz, null));
        sehen.getUntersuchung().getEinladung().getAnlass().getAnlassConfig().setSehenKonvergenz(requirementSetting);
        // ACT
        CheckUntersuchungSehenBeurteilungPresentIfReqValidator
            validator = new CheckUntersuchungSehenBeurteilungPresentIfReqValidator();
        boolean result = validator.isEntityValid(sehen);
        // ASSERT
        assertThat(result).isEqualTo(expectedValid);
    }

    @ParameterizedTest
    @CsvSource({
        "REQUIRED   ,       ,false",
        "REQUIRED   ,NORMAL ,true",
        "OPTIONAL   ,       ,true",
        "OPTIONAL   ,NORMAL ,true",
        "NOT_VISIBLE,       ,true",
        "NOT_VISIBLE,NORMAL ,true",
    })
    void validateSehenMotilitaet(
        @NonNull RequirementSetting requirementSetting,
        @Nullable MotilitaetResultat motilitaet,
        boolean expectedValid
    ) {
        // ARRANGE
        UntersuchungSehenBeurteilung sehen = UntersuchungSehenBeurteilungBuilder.create(
            builder -> builder
                .withMotilitaet(motilitaet, null));
        sehen.getUntersuchung().getEinladung().getAnlass().getAnlassConfig().setSehenMotilitaet(requirementSetting);
        // ACT
        CheckUntersuchungSehenBeurteilungPresentIfReqValidator
            validator = new CheckUntersuchungSehenBeurteilungPresentIfReqValidator();
        boolean result = validator.isEntityValid(sehen);
        // ASSERT
        assertThat(result).isEqualTo(expectedValid);
    }

    @ParameterizedTest
    @CsvSource({
        "REQUIRED   ,                           ,false",
        "REQUIRED   ,UEBERPRUEFUNG_AUGENARZT    ,true",
        "OPTIONAL   ,                           ,true",
        "OPTIONAL   ,UEBERPRUEFUNG_AUGENARZT    ,true",
        "NOT_VISIBLE,                           ,true",
        "NOT_VISIBLE,UEBERPRUEFUNG_AUGENARZT    ,true",
    })
    void validateSehenEmpfehlung(
        @NonNull RequirementSetting requirementSetting,
        @Nullable SehenEmpfehlung empfehlung,
        boolean expectedValid
    ) {
        // ARRANGE
        List<SehenEmpfehlung> empfehlungList = empfehlung == null
            ? null
            : List.of(empfehlung);
        UntersuchungSehenBeurteilung sehen = UntersuchungSehenBeurteilungBuilder.create(
            builder -> builder
                .withEmpfehlung(empfehlungList, null));
        sehen.getUntersuchung().getEinladung().getAnlass().getAnlassConfig().setSehenEmpfehlung(requirementSetting);
        // ACT
        CheckUntersuchungSehenBeurteilungPresentIfReqValidator
            validator = new CheckUntersuchungSehenBeurteilungPresentIfReqValidator();
        boolean result = validator.isEntityValid(sehen);
        // ASSERT
        assertThat(result).isEqualTo(expectedValid);
    }

    @ParameterizedTest
    @CsvSource({
        "REQUIRED   ,           ,false",
        "REQUIRED   ,Bemerkung  ,true",
        "OPTIONAL   ,           ,true",
        "OPTIONAL   ,Bemerkung  ,true",
        "NOT_VISIBLE,           ,true",
        "NOT_VISIBLE,Bemerkung  ,true",
    })
    void validateSehenBemerkungen(
        @NonNull RequirementSetting requirementSetting,
        @Nullable String bemerkung,
        boolean expectedValid
    ) {
        // ARRANGE
        UntersuchungSehenBeurteilung sehen = UntersuchungSehenBeurteilungBuilder.create(
            builder -> builder
                .withBemerkung(bemerkung));
        sehen.getUntersuchung().getEinladung().getAnlass().getAnlassConfig().setSehenBemerkungen(requirementSetting);
        // ACT
        CheckUntersuchungSehenBeurteilungPresentIfReqValidator
            validator = new CheckUntersuchungSehenBeurteilungPresentIfReqValidator();
        boolean result = validator.isEntityValid(sehen);
        // ASSERT
        assertThat(result).isEqualTo(expectedValid);
    }
}
