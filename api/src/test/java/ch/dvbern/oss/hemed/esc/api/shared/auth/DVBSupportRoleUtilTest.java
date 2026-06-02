package ch.dvbern.oss.hemed.esc.api.shared.auth;

import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class DVBSupportRoleUtilTest {

    @Test
    void isDVBSupport_shouldReturnTrue_whenMarkerMatches() {
        assertThat(DVBSupportRoleUtil.isDVBSupport(DVBSupportRoleUtil.DVB_SUPPORT_TENANT_MARKER)).isTrue();
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = { "BS", "BE", "WIN", "dvb-support", "", "DVB_SUPPORT" })
    void isDVBSupport_shouldReturnFalse_whenMarkerDoesNotMatch(String tenantClaim) {
        assertThat(DVBSupportRoleUtil.isDVBSupport(tenantClaim)).isFalse();
    }

    @Test
    void assignDVBSupportBerechtigungenForTenant_shouldReturnEmptySet_whenTenantFromUrlIsNull() {
        // ACT
        Set<Berechtigung> result =
            DVBSupportRoleUtil.assignDVBSupportBerechtigungenForTenant("support@dvbern.ch", null);

        // ASSERT
        assertThat(result).isEmpty();
    }

    @Test
    void assignDVBSupportBerechtigungenForTenant_shouldReturnSupportBerechtigungen_whenTenantProvided() {
        // ACT
        Set<Berechtigung> result =
            DVBSupportRoleUtil.assignDVBSupportBerechtigungenForTenant("support@dvbern.ch", TenantEnum.BS);

        // ASSERT
        Set<Berechtigung> expectedBerechtigungen = FachRolle.collectAllBerechtigungen(TenantEnum.BS, FachRolle.SUPPORT);
        assertThat(result).containsExactlyInAnyOrderElementsOf(expectedBerechtigungen);
    }

    @ParameterizedTest
    @ValueSource(strings = { "BS", "BE", "WIN" })
    void assignDVBSupportBerechtigungenForTenant_shouldReturnNonEmptySet_forAllTenants(String tenantName) {
        // ARRANGE
        TenantEnum tenant = TenantEnum.fromString(tenantName);

        // ACT
        Set<Berechtigung> result =
            DVBSupportRoleUtil.assignDVBSupportBerechtigungenForTenant("support@dvbern.ch", tenant);

        // ASSERT
        assertThat(result).isNotEmpty();
    }
}
