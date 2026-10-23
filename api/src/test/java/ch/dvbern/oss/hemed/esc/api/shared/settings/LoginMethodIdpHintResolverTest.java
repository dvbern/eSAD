/*
 * Copyright (C) 2026 DV Bern AG, Switzerland
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.oss.hemed.esc.api.shared.settings;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import ch.dvbern.dvbstarter.infrastructure.deployment.DeploymentEnvironment;
import ch.dvbern.oss.hemed.esc.api.shared.config.service.ConfigService;
import ch.dvbern.oss.hemed.esc.api.shared.types.LoginMethod;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LoginMethodIdpHintResolverTest {

    private final ConfigService configService = mock(ConfigService.class);
    private final LoginMethodIdpHintResolver resolver = new LoginMethodIdpHintResolver(configService);

    @BeforeEach
    void setUp() {
        when(configService.getLoginMethodAlias(ArgumentMatchers.any())).thenReturn(Optional.empty());
    }

    private void withEnvironment(DeploymentEnvironment environment) {
        when(configService.getEnvironment()).thenReturn(environment);
    }

    private void withAliasOverride(LoginMethod method, String alias) {
        when(configService.getLoginMethodAlias(method)).thenReturn(Optional.of(alias));
    }

    @Test
    void resolve_password_is_empty_string() {
        withEnvironment(DeploymentEnvironment.DEV);
        Assertions.assertThat(resolver.resolve(LoginMethod.PASSWORD)).isEmpty();
    }

    @Test
    void resolve_beLogin_uses_belogin_alias_for_dev() {
        withEnvironment(DeploymentEnvironment.DEV);
        Assertions.assertThat(resolver.resolve(LoginMethod.BE_LOGIN)).isEqualTo("belogin-replica");
    }

    @Test
    void resolve_beLogin_uses_belogin_alias_for_local_and_unitTest_like_dev() {
        withEnvironment(DeploymentEnvironment.LOCAL);
        Assertions.assertThat(resolver.resolve(LoginMethod.BE_LOGIN)).isEqualTo("belogin-replica");

        withEnvironment(DeploymentEnvironment.UNIT_TEST);
        Assertions.assertThat(resolver.resolve(LoginMethod.BE_LOGIN)).isEqualTo("belogin-replica");
    }

    @Test
    void resolve_beLogin_uses_belogin_alias_for_uat() {
        withEnvironment(DeploymentEnvironment.UAT);
        Assertions.assertThat(resolver.resolve(LoginMethod.BE_LOGIN)).isEqualTo("belogin-test");
    }

    @Test
    void resolve_beLogin_uses_belogin_alias_for_prod() {
        withEnvironment(DeploymentEnvironment.PROD);
        Assertions.assertThat(resolver.resolve(LoginMethod.BE_LOGIN)).isEqualTo("belogin");
    }

    @Test
    void resolve_agovZh_is_agov_alias_regardless_of_environment() {
        withEnvironment(DeploymentEnvironment.DEV);
        Assertions.assertThat(resolver.resolve(LoginMethod.AGOV_ZH)).isEqualTo("agov-zh-fachapp");

        withEnvironment(DeploymentEnvironment.PROD);
        Assertions.assertThat(resolver.resolve(LoginMethod.AGOV_ZH)).isEqualTo("agov-zh-fachapp");
    }

    @Test
    void resolve_hin_is_hin_idp_alias_regardless_of_environment() {
        withEnvironment(DeploymentEnvironment.DEV);
        Assertions.assertThat(resolver.resolve(LoginMethod.HIN)).isEqualTo("hin-idp");

        withEnvironment(DeploymentEnvironment.PROD);
        Assertions.assertThat(resolver.resolve(LoginMethod.HIN)).isEqualTo("hin-idp");
    }

    @Test
    void resolve_hemedEntra_uses_the_constant_alias() {
        withEnvironment(DeploymentEnvironment.DEV);
        Assertions.assertThat(resolver.resolve(LoginMethod.DVBERN_HEMED_ENTRA))
            .isEqualTo("dvbern-hemed-entra-client");
    }

    @Test
    void resolve_winterthurAdfs_uses_the_constant_alias() {
        withEnvironment(DeploymentEnvironment.DEV);
        Assertions.assertThat(resolver.resolve(LoginMethod.WINTERTHUR_ADFS)).isEqualTo("WinterthurADFS");
    }

    @Test
    void resolve_uses_the_configured_alias_override_instead_of_the_constant() {
        withEnvironment(DeploymentEnvironment.DEV);
        withAliasOverride(LoginMethod.HIN, "hin-idp-custom");

        Assertions.assertThat(resolver.resolve(LoginMethod.HIN)).isEqualTo("hin-idp-custom");
        // the other methods keep their constant
        Assertions.assertThat(resolver.resolve(LoginMethod.AGOV_ZH)).isEqualTo("agov-zh-fachapp");
    }

    @Test
    void resolve_alias_override_also_wins_over_the_environment_dependent_beLogin_alias() {
        withEnvironment(DeploymentEnvironment.PROD);
        withAliasOverride(LoginMethod.BE_LOGIN, "belogin-special");

        Assertions.assertThat(resolver.resolve(LoginMethod.BE_LOGIN)).isEqualTo("belogin-special");
    }

    @Test
    void resolve_alias_override_can_be_set_for_password_too() {
        withEnvironment(DeploymentEnvironment.DEV);
        withAliasOverride(LoginMethod.PASSWORD, "local-users");

        Assertions.assertThat(resolver.resolve(LoginMethod.PASSWORD)).isEqualTo("local-users");
    }

    @Test
    void toOptions_returns_displayOrder_regardless_of_input_order_and_password_is_last() {
        withEnvironment(DeploymentEnvironment.DEV);

        List<LoginOptionJson> options = resolver.toOptions(
            Set.of(
                LoginMethod.PASSWORD,
                LoginMethod.DVBERN_HEMED_ENTRA,
                LoginMethod.HIN,
                LoginMethod.AGOV_ZH,
                LoginMethod.BE_LOGIN
            )
        );

        Assertions.assertThat(options).hasSize(5);
        Assertions.assertThat(options.get(0).method()).isEqualTo(LoginMethod.BE_LOGIN);
        Assertions.assertThat(options.get(1).method()).isEqualTo(LoginMethod.AGOV_ZH);
        Assertions.assertThat(options.get(2).method()).isEqualTo(LoginMethod.HIN);
        Assertions.assertThat(options.get(3).method()).isEqualTo(LoginMethod.DVBERN_HEMED_ENTRA);
        Assertions.assertThat(options.get(4).method()).isEqualTo(LoginMethod.PASSWORD);
        Assertions.assertThat(options.get(4).idpHint()).isEmpty();
    }

    @Test
    void toOptions_with_single_method() {
        withEnvironment(DeploymentEnvironment.DEV);

        List<LoginOptionJson> options = resolver.toOptions(Set.of(LoginMethod.DVBERN_HEMED_ENTRA));

        Assertions.assertThat(options).hasSize(1);
        Assertions.assertThat(options.getFirst().method()).isEqualTo(LoginMethod.DVBERN_HEMED_ENTRA);
        Assertions.assertThat(options.getFirst().idpHint()).isEqualTo("dvbern-hemed-entra-client");
        Assertions.assertThat(options.getFirst().order()).isEqualTo(5);
    }

    @Test
    void toOptions_with_empty_input_returns_empty_list() {
        withEnvironment(DeploymentEnvironment.DEV);
        Assertions.assertThat(resolver.toOptions(Set.of())).isEmpty();
    }
}
