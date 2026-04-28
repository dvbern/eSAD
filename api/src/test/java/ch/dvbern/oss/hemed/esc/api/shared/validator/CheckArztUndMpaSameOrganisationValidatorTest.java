package ch.dvbern.oss.hemed.esc.api.shared.validator;

import java.util.List;

import ch.dvbern.oss.hemed.esc.api.shared.util.HasZustaendigkeit;
import ch.dvbern.oss.hemed.esc.api.features.organisation.Organisation;
import ch.dvbern.oss.hemed.esc.api.shared.auth.FachRolle;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.BenutzerBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.OrganisationBuilder;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CheckArztUndMpaSameOrganisationValidatorTest {

	private final CheckArztUndMpaSameOrganisationValidator validator =
		new CheckArztUndMpaSameOrganisationValidator();

	@Test
	void testIsValid_ArztIsNull_ReturnsTrue() {
		// ARRANGE
		HasZustaendigkeit entity = createHasZustaendigkeit(null, BenutzerBuilder.defaultBenutzer());

		// ACT
		boolean result = validator.isEntityValid(entity);

		// ASSERT
		assertThat(result).isTrue();
	}

	@Test
	void testIsValid_MpaIsNull_ReturnsTrue() {
		// ARRANGE
		HasZustaendigkeit entity = createHasZustaendigkeit(BenutzerBuilder.defaultBenutzer(), null);

		// ACT
		boolean result = validator.isEntityValid(entity);

		// ASSERT
		assertThat(result).isTrue();
	}

	@Test
	void testIsValid_BothArztAndMpaAreNull_ReturnsTrue() {
		// ARRANGE
		HasZustaendigkeit entity = createHasZustaendigkeit(null, null);

		// ACT
		boolean result = validator.isEntityValid(entity);

		// ASSERT
		assertThat(result).isTrue();
	}

	@Test
	void testIsValid_ArztAndMpaSameOrganisation_ReturnsTrue() {
		// ARRANGE
		Organisation organisation = OrganisationBuilder.defaultOrganisation();
		Benutzer arzt = BenutzerBuilder.create(builder -> builder
			.withOrganisationRollen(organisation, List.of(FachRolle.SCHULAERZTIN))
		);
		Benutzer mpa = BenutzerBuilder.create(builder -> builder
			.withOrganisationRollen(organisation, List.of(FachRolle.MPA))
		);
		HasZustaendigkeit entity = createHasZustaendigkeit(arzt, mpa);

		// ACT
		boolean result = validator.isEntityValid(entity);

		// ASSERT
		assertThat(result).isTrue();
	}

	@Test
	void testIsValid_ArztAndMpaDifferentOrganisation_ReturnsFalse() {
		// ARRANGE
		Organisation organisationArzt = OrganisationBuilder.defaultOrganisation();
		Organisation organisationMpa = OrganisationBuilder.defaultOrganisation();
		Benutzer arzt = BenutzerBuilder.create(builder -> builder
			.withOrganisationRollen(organisationArzt, List.of(FachRolle.SCHULAERZTIN))
		);
		Benutzer mpa = BenutzerBuilder.create(builder -> builder
			.withOrganisationRollen(organisationMpa, List.of(FachRolle.MPA))
		);
		HasZustaendigkeit entity = createHasZustaendigkeit(arzt, mpa);

		// ACT
		boolean result = validator.isEntityValid(entity);

		// ASSERT
		assertThat(result).isFalse();
	}

	@Test
	void testIsValid_MpaHasNoOrganisationRollen_ReturnsFalse() {
		// ARRANGE
		Organisation organisation = OrganisationBuilder.defaultOrganisation();
		Benutzer arzt = BenutzerBuilder.create(builder -> builder
			.withOrganisationRollen(organisation, List.of(FachRolle.SCHULAERZTIN))
		);
		Benutzer mpa = BenutzerBuilder.defaultBenutzer();
		HasZustaendigkeit entity = createHasZustaendigkeit(arzt, mpa);

		// ACT
		boolean result = validator.isEntityValid(entity);

		// ASSERT
		assertThat(result).isFalse();
	}

	@Nested
	class GetSingleOrganisation {

		@Test
		void shouldReturnNull_whenArztIsNull() {
			// ARRANGE
			HasZustaendigkeit entity = createHasZustaendigkeit(null, null);

			// ACT
			Organisation result = entity.getSingleOrganisation();

			// ASSERT
			assertThat(result).isNull();
		}

		@Test
		void shouldReturnNull_whenArztHasNoOrganisation() {
			// ARRANGE
			Benutzer arzt = BenutzerBuilder.defaultBenutzer();
			HasZustaendigkeit entity = createHasZustaendigkeit(arzt, null);

			// ACT
			Organisation result = entity.getSingleOrganisation();

			// ASSERT
			assertThat(result).isNull();
		}

		@Test
		void shouldReturnOrganisation_whenArztHasOrganisation() {
			// ARRANGE
			Organisation organisation = OrganisationBuilder.defaultOrganisation();
			Benutzer arzt = BenutzerBuilder.create(builder -> builder
				.withOrganisationRollen(organisation, List.of(FachRolle.SCHULAERZTIN))
			);
			HasZustaendigkeit entity = createHasZustaendigkeit(arzt, null);

			// ACT
			Organisation result = entity.getSingleOrganisation();

			// ASSERT
			assertThat(result).isEqualTo(organisation);
		}
	}

	private static HasZustaendigkeit createHasZustaendigkeit(@Nullable Benutzer arzt, @Nullable Benutzer mpa) {
		return new HasZustaendigkeit() {
			@Override
			public @Nullable Benutzer getZustaendigArzt() {
				return arzt;
			}

			@Override
			public @Nullable Benutzer getZustaendigMPA() {
				return mpa;
			}
		};
	}
}
