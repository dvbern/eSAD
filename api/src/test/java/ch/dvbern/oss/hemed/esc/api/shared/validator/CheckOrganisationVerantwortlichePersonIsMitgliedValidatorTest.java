package ch.dvbern.oss.hemed.esc.api.shared.validator;

import java.util.List;
import java.util.Set;

import ch.dvbern.oss.hemed.esc.api.features.organisation.Organisation;
import ch.dvbern.oss.hemed.esc.api.features.organisation.OrganisationRollen;
import ch.dvbern.oss.hemed.esc.api.shared.auth.FachRolle;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.BenutzerBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.OrganisationBuilder;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CheckOrganisationVerantwortlichePersonIsMitgliedValidatorTest {

	private final CheckOrganisationVerantwortlichePersonIsMitgliedValidator validator =
		new CheckOrganisationVerantwortlichePersonIsMitgliedValidator();

	@Test
	void testIsValid_VersionLessThanOne_ReturnsTrue() {
		// ARRANGE
		Organisation organisation = OrganisationBuilder.defaultOrganisation();
		organisation.setVersion(0L);

		// ACT
		boolean result = validator.isEntityValid(organisation);

		// ASSERT
		assertThat(result).isTrue();
	}

	@Test
	void testIsValid_VerantwortlicherArztIsMitglied_ReturnsTrue() {
		// ARRANGE
		Organisation organisation = OrganisationBuilder.defaultOrganisation();
		Benutzer verantwortlicherArzt = BenutzerBuilder.create(builder -> builder
			.withOrganisationRollen(organisation, List.of(FachRolle.SCHULAERZTIN))
		);
		organisation.setVerantwortlicherArzt(verantwortlicherArzt);
		organisation.setMitglieder(Set.copyOf(verantwortlicherArzt.getOrganisationRollen()));

		// ACT
		boolean result = validator.isEntityValid(organisation);

		// ASSERT
		assertThat(result).isTrue();
	}

	@Test
	void testIsValid_VerantwortlicherArztIsNotMitglied_ReturnsFalse() {
		// ARRANGE
		Organisation organisation = OrganisationBuilder.defaultOrganisation();
		Benutzer verantwortlicherArzt = BenutzerBuilder.defaultBenutzer();
		organisation.setVerantwortlicherArzt(verantwortlicherArzt);
		organisation.setMitglieder(Set.of());

		// ACT
		boolean result = validator.isEntityValid(organisation);

		// ASSERT
		assertThat(result).isFalse();
	}

	@Test
	void testIsValid_VerantwortlicherArztIsMitgliedAmongOthers_ReturnsTrue() {
		// ARRANGE
		Organisation organisation = OrganisationBuilder.defaultOrganisation();
		Benutzer verantwortlicherArzt = BenutzerBuilder.create(builder -> builder
			.withOrganisationRollen(organisation, List.of(FachRolle.SCHULAERZTIN))
		);
		Benutzer otherMitglied = BenutzerBuilder.create(builder -> builder
			.withOrganisationRollen(organisation, List.of(FachRolle.MPA))
		);
		organisation.setVerantwortlicherArzt(verantwortlicherArzt);
		OrganisationRollen arztRollen = verantwortlicherArzt.getOrganisationRollen().iterator().next();
		OrganisationRollen otherRollen = otherMitglied.getOrganisationRollen().iterator().next();
		organisation.setMitglieder(Set.of(arztRollen, otherRollen));

		// ACT
		boolean result = validator.isEntityValid(organisation);

		// ASSERT
		assertThat(result).isTrue();
	}

	@Test
	void testIsValid_OtherMitgliedButNotVerantwortlicherArzt_ReturnsFalse() {
		// ARRANGE
		Organisation organisation = OrganisationBuilder.defaultOrganisation();
		Benutzer verantwortlicherArzt = BenutzerBuilder.defaultBenutzer();
		Benutzer otherMitglied = BenutzerBuilder.create(builder -> builder
			.withOrganisationRollen(organisation, List.of(FachRolle.MPA))
		);
		organisation.setVerantwortlicherArzt(verantwortlicherArzt);
		OrganisationRollen otherRollen = otherMitglied.getOrganisationRollen().iterator().next();
		organisation.setMitglieder(Set.of(otherRollen));

		// ACT
		boolean result = validator.isEntityValid(organisation);

		// ASSERT
		assertThat(result).isFalse();
	}
}
