package ch.dvbern.oss.hemed.esc.api.shared.validator;

import java.util.List;

import ch.dvbern.oss.hemed.esc.api.features.organisation.Organisation;
import ch.dvbern.oss.hemed.esc.api.shared.auth.FachRolle;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.BenutzerBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.OrganisationBuilder;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CheckOrganisationVerantwortlichePersonIsSchularztValidatorTest {

    private final CheckOrganisationVerantwortlichePersonIsSchularztValidator validator =
        new CheckOrganisationVerantwortlichePersonIsSchularztValidator();

    @Test
    void testIsValid_VerantwortlicherArztIsSchularzt_ReturnsTrue() {
        // ARRANGE
        Organisation organisation = OrganisationBuilder.defaultOrganisation();
        Benutzer schularzt = BenutzerBuilder.create(builder -> builder
            .withOrganisationRollen(organisation, List.of(FachRolle.SCHULAERZTIN))
        );
        organisation.setVerantwortlicherArzt(schularzt);

        // ACT
        boolean result = validator.isEntityValid(organisation);

        // ASSERT
        assertThat(result).isTrue();
    }

    @Test
    void testIsValid_VerantwortlicherArztIsNoSchularzt_ReturnsFalse() {
        // ARRANGE
        Organisation organisation = OrganisationBuilder.defaultOrganisation();
        Benutzer mpa = BenutzerBuilder.create(builder -> builder
            .withOrganisationRollen(organisation, List.of(FachRolle.MPA))
        );
        organisation.setVerantwortlicherArzt(mpa);

        // ACT
        boolean result = validator.isEntityValid(organisation);

        // ASSERT
        assertThat(result).isFalse();
    }

    @Test
    void testIsValid_VerantwortlicherArztHasNoRollenInOrganisation_ReturnsFalse() {
        // ARRANGE
        Organisation organisation = OrganisationBuilder.defaultOrganisation();
        Organisation otherOrganisation = OrganisationBuilder.defaultOrganisation();

        Benutzer schularztInOtherOrg = BenutzerBuilder.create(builder -> builder
            .withOrganisationRollen(otherOrganisation, List.of(FachRolle.SCHULAERZTIN))
        );
        organisation.setVerantwortlicherArzt(schularztInOtherOrg);

        // ACT
        boolean result = validator.isEntityValid(organisation);

        // ASSERT
        assertThat(result).isFalse();
    }

    @Test
    void testIsValid_VerantwortlicherArztIsNotAMember_ReturnsFalse() {
        // ARRANGE
        Organisation organisation = OrganisationBuilder.defaultOrganisation();
        Benutzer benutzer = BenutzerBuilder.defaultBenutzer();
        organisation.setVerantwortlicherArzt(benutzer);

        // ACT
        boolean result = validator.isEntityValid(organisation);

        // ASSERT
        assertThat(result).isFalse();
    }
}
