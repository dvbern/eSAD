package ch.dvbern.oss.hemed.esc.api.shared.auth;

import java.util.Set;
import jakarta.inject.Inject;

import ch.dvbern.oss.hemed.esc.api.features.anlass.AnlassService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.EinladungService;
import ch.dvbern.oss.hemed.esc.api.features.archivierung.ArchivdokumentService;
import ch.dvbern.oss.hemed.esc.api.features.benutzer.BenutzerService;
import ch.dvbern.oss.hemed.esc.api.features.kinder.DossierService;
import ch.dvbern.oss.hemed.esc.api.features.kinder.KindService;
import ch.dvbern.oss.hemed.esc.api.features.organisation.Organisation;
import ch.dvbern.oss.hemed.esc.api.features.organisation.OrganisationService;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import ch.dvbern.oss.hemed.esc.api.shared.config.service.ConfigService;
import ch.dvbern.oss.hemed.esc.api.shared.documentqueue.DocumentQueueService;
import ch.dvbern.oss.hemed.esc.api.shared.settings.TenantSettingsService;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsSupportBS;

import io.quarkus.arc.ClientProxy;
import io.quarkus.security.identity.CurrentIdentityAssociation;
import io.quarkus.security.runtime.QuarkusPrincipal;
import io.quarkus.security.runtime.QuarkusSecurityIdentity;
import io.quarkus.test.junit.QuarkusMock;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@QuarkusTest
class AuthorizerServiceSafeCacheTest {

    @Inject
    protected AuthorizerService authorizerService;

    @Inject
    protected TenantContext tenantContext;

    @Inject
    protected CurrentIdentityAssociation association;

    BenutzerService benutzerService;

    OrganisationService organisationService;

    TenantSettingsService tenantSettingsService;

    // Other mocks to avoid any DB calls
    AnlassService anlassService;
    DocumentQueueService documentQueueService;
    KindService kindService;
    ArchivdokumentService archivdokumentService;
    ConfigService configService;
    EinladungService einladungService;
    DossierService dossierService;

    @BeforeEach
    void setupMocks() {
        benutzerService = mock(BenutzerService.class);
        organisationService = mock(OrganisationService.class);
        tenantSettingsService = mock(TenantSettingsService.class);
        anlassService = mock(AnlassService.class);
        documentQueueService = mock(DocumentQueueService.class);
        kindService = mock(KindService.class);
        archivdokumentService = mock(ArchivdokumentService.class);
        configService = mock(ConfigService.class);
        einladungService = mock(EinladungService.class);
        dossierService = mock(DossierService.class);

        QuarkusMock.installMockForType(benutzerService, BenutzerService.class);
        QuarkusMock.installMockForType(organisationService, OrganisationService.class);
        QuarkusMock.installMockForType(tenantSettingsService, TenantSettingsService.class);
        QuarkusMock.installMockForType(anlassService, AnlassService.class);
        QuarkusMock.installMockForType(documentQueueService, DocumentQueueService.class);
        QuarkusMock.installMockForType(kindService, KindService.class);
        QuarkusMock.installMockForType(archivdokumentService, ArchivdokumentService.class);
        QuarkusMock.installMockForType(configService, ConfigService.class);
        QuarkusMock.installMockForType(einladungService, EinladungService.class);
        QuarkusMock.installMockForType(dossierService, DossierService.class);
    }

    @Test
    @TestAsSupportBS
    void testGetCachedTenantCacheIsSafe() throws Exception {
        // ARRANGE
        when(benutzerService.getCurrentBenutzerOrThrowException()).thenReturn(new Benutzer());

        // ACT 1: Initial call to populate cache
        authorizerService.hasReadAuthorizationOrganisation(new Organisation());

        // Verify it's cached as BS
        AuthorizerService realService = ClientProxy.unwrap(authorizerService);
        var field = AuthorizerService.class.getDeclaredField("cachedTenant");
        field.setAccessible(true);
        assertThat(field.get(realService)).isEqualTo(TenantEnum.BS);

        // ACT 2: Change tenant inside the same request context (not allowed, see PerTenantRunnerService)
        tenantContext.setCurrentTenant(TenantEnum.WIN);

        // ACT 3 + ASSERT: the cache of BS must never be served for WIN - fail loudly instead
        Organisation emptyOrganisation = new Organisation();
        assertThatThrownBy(() -> authorizerService.hasReadAuthorizationOrganisation(emptyOrganisation))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("AuthorizerService");
    }

    @Test
    @TestAsSupportBS
    void testUserCacheIsSafe() throws Exception {
        // ARRANGE
        Benutzer user1 = new Benutzer();
        Benutzer user2 = new Benutzer();
        when(benutzerService.getCurrentBenutzerOrThrowException()).thenReturn(user1, user2);

        // ACT 1: Initial call to populate cache
        authorizerService.hasReadAuthorizationOrganisation(new Organisation());

        // Verify user is cached
        AuthorizerService realService = ClientProxy.unwrap(authorizerService);
        var field = AuthorizerService.class.getDeclaredField("cachedCurrentBenutzer");
        field.setAccessible(true);
        var cachedUser = (Benutzer) field.get(realService);
        assertThat(cachedUser).isSameAs(user1);

        // ACT 2: Change user identity manually (simulating @RunAs)
        var identity = QuarkusSecurityIdentity.builder()
            .setPrincipal(new QuarkusPrincipal("other-user@dvbern.ch"))
            .addRoles(Set.of(BerechtigungName.SYSTEM_ADMIN))
            .build();
        association.setIdentity(identity);

        // ACT 3: Check if it still uses the old user
        authorizerService.hasReadAuthorizationOrganisation(new Organisation());

        assertThat(field.get(realService))
            .as("The cache should have been invalidated or refreshed for the new user")
            .isNotSameAs(cachedUser)
            .isSameAs(user2);
    }
}
