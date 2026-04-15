package ch.dvbern.oss.hemed.esc.api.shared.validator;

import java.util.List;

import ch.dvbern.oss.hemed.esc.api.features.organisation.Organisation;
import ch.dvbern.oss.hemed.esc.api.shared.auth.FachRolle;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.BenutzerBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.OrganisationBuilder;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class CheckBenutzerFachRollenValidatorTest {

    @ParameterizedTest
    @CsvSource({
        "       ,           ,true",
        "MPA    ,           ,true",
        "       ,SUPPORT    ,true",
        "MPA    ,SUPPORT    ,false",
    })
    void validateBenutzerFachRollen(
        @Nullable FachRolle organisationsRolle,
        @Nullable FachRolle systemRolle,
        boolean expectedValid
    ) {
        // ARRANGE
        Organisation organisation = OrganisationBuilder.defaultOrganisation();
        Benutzer benutzer =  BenutzerBuilder.defaultBenutzer();

        if (organisationsRolle != null && systemRolle == null) {
            benutzer = BenutzerBuilder.create(builder -> builder
                .withOrganisationRollen(organisation, List.of(organisationsRolle))
            );
        } else if (organisationsRolle == null && systemRolle != null) {
            benutzer = BenutzerBuilder.create(builder -> builder
                .withSystemRolle(List.of(systemRolle))
            );
        } else if (organisationsRolle != null) {
            benutzer = BenutzerBuilder.create(builder -> builder
                .withOrganisationRollen(organisation, List.of(organisationsRolle))
                .withSystemRolle(List.of(systemRolle))
            );
        }

        // ACT
        CheckBenutzerFachRollenValidator
            validator = new CheckBenutzerFachRollenValidator();
        boolean result = validator.isEntityValid(benutzer);
        // ASSERT
        assertThat(result).isEqualTo(expectedValid);
    }
}
