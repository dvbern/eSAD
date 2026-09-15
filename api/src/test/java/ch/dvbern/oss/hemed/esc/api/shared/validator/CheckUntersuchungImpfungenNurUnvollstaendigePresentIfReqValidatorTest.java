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

import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.types.RequirementSetting;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfungenNurUnvollstaendige;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.types.ImpfausweisGrundUnvollstaendig;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.types.ImpfausweisVollstaendig;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungImpfungenNurUnvollstaendigeBuilder;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class CheckUntersuchungImpfungenNurUnvollstaendigePresentIfReqValidatorTest {

    @ParameterizedTest
    @CsvSource({
        "REQUIRED   ,               ,       ,false",
        "REQUIRED   ,JA             ,       ,true",
        "REQUIRED   ,NEIN           ,       ,false",
        "REQUIRED   ,KEINE_ANGABE   ,       ,true",

        "REQUIRED   ,               ,UNKLAR  ,false",
        "REQUIRED   ,JA             ,UNKLAR  ,true",
        "REQUIRED   ,NEIN           ,UNKLAR  ,true",
        "REQUIRED   ,KEINE_ANGABE   ,UNKLAR  ,true",

        "OPTIONAL   ,               ,       ,true",
        "OPTIONAL   ,JA             ,       ,true",
        "OPTIONAL   ,NEIN           ,       ,true",
        "OPTIONAL   ,KEINE_ANGABE   ,       ,true",

        "OPTIONAL   ,               ,UNKLAR  ,true",
        "OPTIONAL   ,JA             ,UNKLAR  ,true",
        "OPTIONAL   ,NEIN           ,UNKLAR  ,true",
        "OPTIONAL   ,KEINE_ANGABE   ,UNKLAR  ,true",

        "NOT_VISIBLE,               ,       ,true",
        "NOT_VISIBLE,JA             ,       ,true",
        "NOT_VISIBLE,NEIN           ,       ,true",
        "NOT_VISIBLE,KEINE_ANGABE   ,       ,true",

        "NOT_VISIBLE,               ,UNKLAR  ,true",
        "NOT_VISIBLE,JA             ,UNKLAR  ,true",
        "NOT_VISIBLE,NEIN           ,UNKLAR  ,true",
        "NOT_VISIBLE,KEINE_ANGABE   ,UNKLAR  ,true",
    })
    void validateImpfausweisGeprueft(
        @NonNull RequirementSetting requirementSetting,
        @Nullable ImpfausweisVollstaendig impfausweisVollstaendig,
        @Nullable ImpfausweisGrundUnvollstaendig grundUnvollstaendig,
        boolean expectedValid
    ) {
        // ARRANGE
        UntersuchungImpfungenNurUnvollstaendige data = UntersuchungImpfungenNurUnvollstaendigeBuilder.create(
            builder -> builder
                .withImpfausweisVollstaendig(impfausweisVollstaendig)
                .withImpfausweisGrundUnvollstaendig(grundUnvollstaendig)
        );
        data.getUntersuchungImpfungen().getUntersuchung().getAnlass().getAnlassConfig()
            .setImpfungenNurUnvollstaendige(requirementSetting);

        // ACT
        CheckUntersuchungImpfungenNurUnvollstaendigePresentIfReqValidator validator = new CheckUntersuchungImpfungenNurUnvollstaendigePresentIfReqValidator();
        boolean result = validator.isEntityValid(data);

        // ASSERT
        assertThat(result).isEqualTo(expectedValid);
    }
}
