package ch.dvbern.oss.hemed.esc.api.features.benutzer;

import ch.dvbern.dvbstarter.shared.types.emailaddress.EmailAddress;
import ch.dvbern.oss.hemed.esc.api.shared.auth.Berechtigung;
import ch.dvbern.oss.hemed.esc.api.shared.config.service.ConfigService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BerechtigungenCacheServiceTest {

    @Mock
    private BenutzerService benutzerService;

    @Mock
    private ConfigService configService;

    private BerechtigungenenCacheService cacheService;

    @BeforeEach
    void setUp() {
        cacheService = new BerechtigungenenCacheService(benutzerService, configService);
        Mockito.when(configService.getBenutzerRollenCacheTimeoutMinutes()).thenReturn(2);
        cacheService.init();
    }

    @Test
    void getBerechtigungenForBenutzername_shouldReturnCached() {

        // Arrange
        ch.dvbern.dvbstarter.shared.types.emailaddress.EmailAddress emailParam = EmailAddress.fromString(UUID.randomUUID() + "@example.com");
        Set<Berechtigung> expectedRoles = Set.of(Berechtigung.AUFGABEN_LESEN, Berechtigung.VORLAGEN_LESEN);
        when(benutzerService.findBerechtigungenByEmail(any()))
            .thenReturn(Optional.of(expectedRoles));
        // Act
        Optional<Set<Berechtigung>> result = cacheService.getBerechtigungenForBenutzerByEmail(emailParam);

        // Assert
        assertThat(result).isPresent().hasValue(expectedRoles);
        cacheService.getBerechtigungenForBenutzerByEmail(emailParam);

        // Verify that the service was only called once due to caching
        verify(benutzerService, times(1)).findBerechtigungenByEmail(emailParam);
    }

    @Test
    void flushCache_shouldInvalidateCache() {
        // Arrange
        ch.dvbern.dvbstarter.shared.types.emailaddress.EmailAddress emailParam = EmailAddress.fromString(UUID.randomUUID() + "@example.com");
        Set<Berechtigung> expectedRoles = Set.of(Berechtigung.AUFGABEN_LESEN, Berechtigung.VORLAGEN_LESEN);
        when(benutzerService.findBerechtigungenByEmail(any()))
            .thenReturn(Optional.of(expectedRoles));
        // Act
        Optional<Set<Berechtigung>> result1 = cacheService.getBerechtigungenForBenutzerByEmail(emailParam);
        cacheService.flushCache(emailParam);
        Optional<Set<Berechtigung>> result2 = cacheService.getBerechtigungenForBenutzerByEmail(emailParam);


        // Assert
        // Act

        // Assert
        assertThat(result1).hasValue(expectedRoles);
        assertThat(result2).hasValue(expectedRoles);
        // called 2 times due to flush
        verify(benutzerService, times(2)).findBerechtigungenByEmail(emailParam);
    }
}
