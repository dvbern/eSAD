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

import ch.dvbern.oss.hemed.esc.api.features.anlass.UntersuchungSehen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.BrilleLinsenResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.DistanzSehenResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.FarbsehenResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.InspektionKonvergenzResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.MotilitaetResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.RequirementSetting;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.StereotestResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.StrabismusAuftretenResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.StrabismusAuspraegungResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.StrabismusResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.VerwendetTafelResultat;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungSehenBuilder;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class CheckUntersuchungSehenPresentIfReqValidatorTest {

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
        UntersuchungSehen sehen = UntersuchungSehenBuilder.create(
            builder -> builder
                .withBrilleLinsen(brilleLinsen));
        sehen.getUntersuchung().getEinladung().getAnlass().getAnlassConfig().setSehenBrilleLinsen(requirementSetting);
        // ACT
        CheckUntersuchungSehenPresentIfReqValidator validator = new CheckUntersuchungSehenPresentIfReqValidator();
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
        UntersuchungSehen sehen = UntersuchungSehenBuilder.create(
            builder -> builder
                .withFarbsehen(farbsehen));
        sehen.getUntersuchung().getEinladung().getAnlass().getAnlassConfig().setSehenFarbsehen(requirementSetting);
        // ACT
        CheckUntersuchungSehenPresentIfReqValidator validator = new CheckUntersuchungSehenPresentIfReqValidator();
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
        UntersuchungSehen sehen = UntersuchungSehenBuilder.create(
            builder -> builder
                .withVerwendeteTafel(verwendeteTafel));
        sehen.getUntersuchung().getEinladung().getAnlass().getAnlassConfig().setSehenTafel(requirementSetting);
        // ACT
        CheckUntersuchungSehenPresentIfReqValidator validator = new CheckUntersuchungSehenPresentIfReqValidator();
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
        UntersuchungSehen sehen = UntersuchungSehenBuilder.create(
            builder -> builder
                .withFernvisus(distanzSehenResultat)
                .withFernvisusLinks(fernvisusLinks)
                .withFernvisusRechts(fernvisusRechts));
        sehen.getUntersuchung().getEinladung().getAnlass().getAnlassConfig().setSehenFernvisus(requirementSetting);
        // ACT
        CheckUntersuchungSehenPresentIfReqValidator validator = new CheckUntersuchungSehenPresentIfReqValidator();
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
        UntersuchungSehen sehen = UntersuchungSehenBuilder.create(
            builder -> builder
                .withNahvisus(distanzSehenResultat)
                .withNahvisusLinks(nahvisusLinks)
                .withNahvisusRechts(nahvisusRechts));
        sehen.getUntersuchung().getEinladung().getAnlass().getAnlassConfig().setSehenNahvisus(requirementSetting);
        // ACT
        CheckUntersuchungSehenPresentIfReqValidator validator = new CheckUntersuchungSehenPresentIfReqValidator();
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
        UntersuchungSehen sehen = UntersuchungSehenBuilder.create(
            builder -> builder
                .withStereotest(stereotest));
        sehen.getUntersuchung().getEinladung().getAnlass().getAnlassConfig().setSehenStereoTest(requirementSetting);
        // ACT
        CheckUntersuchungSehenPresentIfReqValidator validator = new CheckUntersuchungSehenPresentIfReqValidator();
        boolean result = validator.isEntityValid(sehen);
        // ASSERT
        assertThat(result).isEqualTo(expectedValid);
    }

    @ParameterizedTest
    @CsvSource({
        "REQUIRED   ,                   ,                   ,                   ,false",
        "REQUIRED   ,'KEIN_STRABISMUS'  ,                   ,                   ,false",
        "REQUIRED   ,                   ,'KEIN_STRABISMUS'  ,                   ,false",
        "REQUIRED   ,                   ,                   ,'KEIN_STRABISMUS'  ,false",
        "REQUIRED   ,'KEIN_STRABISMUS'  ,'KEIN_STRABISMUS'  ,                   ,false",
        "REQUIRED   ,'KEIN_STRABISMUS'  ,                   ,'KEIN_STRABISMUS'  ,false",
        "REQUIRED   ,'KEIN_STRABISMUS'  ,'KEIN_STRABISMUS'  ,'KEIN_STRABISMUS'  ,true",
        "OPTIONAL   ,                   ,                   ,                   ,true",
        "OPTIONAL   ,'KEIN_STRABISMUS'  ,'LINKS'            ,'LATENT'           ,true",
        "OPTIONAL   ,'KEIN_STRABISMUS'  ,'KEIN_STRABISMUS'  ,'LATENT'           ,true",
        "OPTIONAL   ,'KEIN_STRABISMUS'  ,'LINKS'            ,'KEIN_STRABISMUS'  ,true",
        "OPTIONAL   ,                   ,'KEIN_STRABISMUS'  ,                   ,true",
        "OPTIONAL   ,                   ,                   ,'KEIN_STRABISMUS'  ,true",
        "OPTIONAL   ,'KEIN_STRABISMUS'  ,'KEIN_STRABISMUS'  ,                   ,true",
        "OPTIONAL   ,'KEIN_STRABISMUS'  ,                   ,'KEIN_STRABISMUS'  ,true",
        "OPTIONAL   ,'KEIN_STRABISMUS'  ,'KEIN_STRABISMUS'  ,'KEIN_STRABISMUS'  ,true",
        "NOT_VISIBLE,                   ,                   ,                   ,true",
        "NOT_VISIBLE,'KEIN_STRABISMUS'  ,                   ,                   ,true",
        "NOT_VISIBLE,                   ,'KEIN_STRABISMUS'  ,                   ,true",
        "NOT_VISIBLE,                   ,                   ,'KEIN_STRABISMUS'  ,true",
        "NOT_VISIBLE,'KEIN_STRABISMUS'  ,'KEIN_STRABISMUS'  ,                   ,true",
        "NOT_VISIBLE,'KEIN_STRABISMUS'  ,                   ,'KEIN_STRABISMUS'  ,true",
        "NOT_VISIBLE,'KEIN_STRABISMUS'  ,'KEIN_STRABISMUS'  ,'KEIN_STRABISMUS'  ,true",
    })
    void validateSehenStrabismus(
        @NonNull RequirementSetting requirementSetting,
        @Nullable String strabismusString,
        @Nullable StrabismusAuftretenResultat strabismusAuftreten,
        @Nullable StrabismusAuspraegungResultat strabismusAuspraegung,
        boolean expectedValid
    ) {
        // ARRANGE
        List<StrabismusResultat> strabismus = strabismusString == null || strabismusString.isBlank()
            ? null
            : Arrays.stream(strabismusString.split(","))
            .map(String::trim)
            .map(StrabismusResultat::valueOf)
            .toList();

        UntersuchungSehen sehen = UntersuchungSehenBuilder.create(
            builder -> builder
                .withStrabismus(strabismus)
                .withStrabismusAuftreten(strabismusAuftreten)
                .withStrabismusAuspraegung(strabismusAuspraegung)

        );
        sehen.getUntersuchung().getEinladung().getAnlass().getAnlassConfig().setSehenStrabismus(requirementSetting);
        // ACT
        CheckUntersuchungSehenPresentIfReqValidator validator = new CheckUntersuchungSehenPresentIfReqValidator();
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
        UntersuchungSehen sehen = UntersuchungSehenBuilder.create(
            builder -> builder
                .withInspektion(inspektion));
        sehen.getUntersuchung().getEinladung().getAnlass().getAnlassConfig().setSehenInspektion(requirementSetting);
        // ACT
        CheckUntersuchungSehenPresentIfReqValidator validator = new CheckUntersuchungSehenPresentIfReqValidator();
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
        UntersuchungSehen sehen = UntersuchungSehenBuilder.create(
            builder -> builder
                .withKonvergenz(konvergenz));
        sehen.getUntersuchung().getEinladung().getAnlass().getAnlassConfig().setSehenKonvergenz(requirementSetting);
        // ACT
        CheckUntersuchungSehenPresentIfReqValidator validator = new CheckUntersuchungSehenPresentIfReqValidator();
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
        UntersuchungSehen sehen = UntersuchungSehenBuilder.create(
            builder -> builder
                .withMotilitaet(motilitaet));
        sehen.getUntersuchung().getEinladung().getAnlass().getAnlassConfig().setSehenMotilitaet(requirementSetting);
        // ACT
        CheckUntersuchungSehenPresentIfReqValidator validator = new CheckUntersuchungSehenPresentIfReqValidator();
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
        UntersuchungSehen sehen = UntersuchungSehenBuilder.create(
            builder -> builder
                .withBemerkung(bemerkung));
        sehen.getUntersuchung().getEinladung().getAnlass().getAnlassConfig().setSehenBemerkungen(requirementSetting);
        // ACT
        CheckUntersuchungSehenPresentIfReqValidator validator = new CheckUntersuchungSehenPresentIfReqValidator();
        boolean result = validator.isEntityValid(sehen);
        // ASSERT
        assertThat(result).isEqualTo(expectedValid);
    }
}
