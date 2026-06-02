package ch.dvbern.oss.hemed.esc.api.shared.validator;

import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import ch.dvbern.oss.hemed.esc.api.shared.settings.TenantSettingsService;
import ch.dvbern.oss.hemed.esc.api.shared.util.HasZustaendigkeit;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.BenutzerBuilder;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CheckArztRequiredForDezentraleTenantsValidatorTest {

	@Mock
	private TenantSettingsService tenantSettingsService;

	@InjectMocks
	private CheckArztRequiredForDezentraleTenantsValidator validator;

	@Test
	void isEntityValid_zentraleOrganisation_withArzt_returnsTrue() {
		// ARRANGE
		when(tenantSettingsService.isZentraleOrganisation()).thenReturn(true);
		HasZustaendigkeit entity = createHasZustaendigkeit(BenutzerBuilder.defaultBenutzer());

		// ACT
		boolean result = validator.isEntityValid(entity);

		// ASSERT
		assertThat(result).isTrue();
	}

	@Test
	void isEntityValid_zentraleOrganisation_withoutArzt_returnsTrue() {
		// ARRANGE
		when(tenantSettingsService.isZentraleOrganisation()).thenReturn(true);
		HasZustaendigkeit entity = createHasZustaendigkeit(null);

		// ACT
		boolean result = validator.isEntityValid(entity);

		// ASSERT
		assertThat(result).isTrue();
	}

	@Test
	void isEntityValid_dezentraleOrganisation_withArzt_returnsTrue() {
		// ARRANGE
		when(tenantSettingsService.isZentraleOrganisation()).thenReturn(false);
		HasZustaendigkeit entity = createHasZustaendigkeit(BenutzerBuilder.defaultBenutzer());

		// ACT
		boolean result = validator.isEntityValid(entity);

		// ASSERT
		assertThat(result).isTrue();
	}

	@Test
	void isEntityValid_dezentraleOrganisation_withoutArzt_returnsFalse() {
		// ARRANGE
		when(tenantSettingsService.isZentraleOrganisation()).thenReturn(false);
		HasZustaendigkeit entity = createHasZustaendigkeit(null);

		// ACT
		boolean result = validator.isEntityValid(entity);

		// ASSERT
		assertThat(result).isFalse();
	}

	private static HasZustaendigkeit createHasZustaendigkeit(@Nullable Benutzer arzt) {
		return new HasZustaendigkeit() {
			@Override
			public @Nullable Benutzer getZustaendigArzt() {
				return arzt;
			}

			@Override
			public @Nullable Benutzer getZustaendigMPA() {
				return null;
			}
		};
	}
}
