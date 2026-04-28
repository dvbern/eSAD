package ch.dvbern.oss.hemed.esc.api.shared.benutzer;

import java.util.EnumSet;

import ch.dvbern.oss.hemed.esc.api.features.benutzer.BenutzerFixtureBuilder;
import ch.dvbern.oss.hemed.esc.api.features.organisation.Organisation;
import ch.dvbern.oss.hemed.esc.api.features.organisation.OrganisationRollen;
import ch.dvbern.oss.hemed.esc.api.shared.auth.FachRolle;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.OrganisationBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.OrganisationRollenBuilder;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BenutzerTest {
    @Test
    void fach_rollen_can_be_empty() {
        Benutzer benutzer = BenutzerFixtureBuilder.validBenutzer().build();

        Organisation organisation = OrganisationBuilder.defaultOrganisation();
        OrganisationRollen organisationRollen = OrganisationRollenBuilder.create(builder ->
            builder.withBenutzer(benutzer)
                .withOrganisation(organisation)
                .withFachrollen(EnumSet.noneOf(FachRolle.class))
        );
        benutzer.addOrganisation(organisationRollen);

        assertTrue(benutzer.getFachRollenCombined().isEmpty());
    }

    @Test
    void system_rollen_should_be_read_and_writable() {
        Benutzer benutzer = BenutzerFixtureBuilder.validBenutzer().build();

        Organisation organisation = OrganisationBuilder.defaultOrganisation();
        OrganisationRollen organisationRollen = OrganisationRollenBuilder.create(builder ->
            builder.withBenutzer(benutzer)
                .withOrganisation(organisation)
                .withFachrollen(EnumSet.of(FachRolle.SUPPORT))
        );
        benutzer.addOrganisation(organisationRollen);

        assertEquals(1, benutzer.getFachRollenCombined().size());
        assertEquals(FachRolle.SUPPORT, benutzer.getFachRollenCombined().stream().findFirst().orElseThrow());
    }

    @Test
    void fach_rollen_should_be_read_and_writable() {
        Benutzer benutzer = BenutzerFixtureBuilder.validBenutzer().build();

        Organisation organisation = OrganisationBuilder.defaultOrganisation();
        OrganisationRollen organisationRollen = OrganisationRollenBuilder.create(builder ->
            builder.withBenutzer(benutzer)
                .withOrganisation(organisation)
                .withFachrollen(EnumSet.of(FachRolle.ADMIN))
        );
        benutzer.addOrganisation(organisationRollen);

        assertEquals(1, benutzer.getFachRollenCombined().size());
        assertEquals(FachRolle.ADMIN, benutzer.getFachRollenCombined().stream().findFirst().orElseThrow());
    }

    @Test
    void fach_rollen_are_returned_in_ordinal_order() {
        Benutzer benutzer = BenutzerFixtureBuilder.validBenutzer().build();

        Organisation organisation = OrganisationBuilder.defaultOrganisation();
        OrganisationRollen organisationRollen = OrganisationRollenBuilder.create(builder ->
            builder.withBenutzer(benutzer)
                .withOrganisation(organisation)
                .withFachrollen(EnumSet.of(
                    FachRolle.SCHULAERZTIN,
                    FachRolle.MPA,
                    FachRolle.ADMIN,
                    FachRolle.ORTHOPISTIN,
                    FachRolle.EMPFANG))
        );
        benutzer.addOrganisation(organisationRollen);

        Assertions.assertThat(benutzer.getFachRollenCombined())
            .containsExactly(
                FachRolle.EMPFANG,
                FachRolle.MPA,
                FachRolle.ORTHOPISTIN,
                FachRolle.SCHULAERZTIN,
                FachRolle.ADMIN);
    }
}
