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

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import ch.dvbern.oss.hemed.esc.api.shared.config.service.ConfigService;
import ch.dvbern.oss.hemed.esc.api.shared.types.LoginMethod;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Derives the {@code kc_idp_hint} query parameter value and the display order for each {@link LoginMethod}.
 * The hint is derived here, not stored, so it can be adjusted per environment without a DB migration.
 * Each alias can additionally be overridden per environment via {@code esc.login-method.<NAME>.alias}
 * (see {@link ConfigService#getLoginMethodAlias(LoginMethod)}); the constants below are the fallback.
 */
@ApplicationScoped
@RequiredArgsConstructor
public class LoginMethodIdpHintResolver {

    // Deliberate display order for the landing page - NOT the enum declaration / EnumSet ordinal order.
    private static final List<LoginMethod> DISPLAY_ORDER = List.of(
        LoginMethod.BE_LOGIN,
        LoginMethod.AGOV_ZH,
        LoginMethod.BASEL_ENTRA,
        LoginMethod.WINTERTHUR_ADFS,
        LoginMethod.HIN,
        LoginMethod.DVBERN_HEMED_ENTRA,
        LoginMethod.PASSWORD
    );

    // Alias of the HIN broker IdP in the ESC realm - always the same, in every environment.
    private static final String HIN_ALIAS = "hin-idp";

    // Alias of the AGOV broker IdP used by Zurich - the same in every environment so far.
    private static final String AGOV_ZH_ALIAS = "agov-zh-fachapp";

    private static final String WINTERTHUR_ADFS_ALIAS = "WinterthurADFS";
    private static final String BASEL_ENTRA_ALIAS = "basel-entra";
    private static final String DVBERN_HEMED_ENTRA_ALIAS = "dvbern-hemed-entra-client";

    private final ConfigService configService;

    @NonNull
    public String resolve(@NonNull LoginMethod method) {
        return configService.getLoginMethodAlias(method)
            .orElseGet(() -> resolveDefaultAlias(method));
    }

    @NonNull
    private String resolveDefaultAlias(@NonNull LoginMethod method) {
        return switch (method) {
            case PASSWORD -> "";
            case AGOV_ZH -> AGOV_ZH_ALIAS;
            case WINTERTHUR_ADFS -> WINTERTHUR_ADFS_ALIAS;
            case BASEL_ENTRA -> BASEL_ENTRA_ALIAS;
            case BE_LOGIN -> resolveBeLoginAlias();
            case HIN -> HIN_ALIAS;
            case DVBERN_HEMED_ENTRA -> DVBERN_HEMED_ENTRA_ALIAS;
        };
    }

    // BeLogin (Bern's cantonal IAM) uses a different broker alias per stage, following
    // REALM_IDP_BELOGIN_ALIAS in esc-keycloak: belogin-replica (DEV), belogin-test (UAT), belogin (PROD).
    @NonNull
    private String resolveBeLoginAlias() {
        return switch (configService.getEnvironment()) {
            case PROD -> "belogin";
            case UAT -> "belogin-test";
            case DEV, LOCAL, UNIT_TEST -> "belogin-replica";
        };
    }

    public int order(@NonNull LoginMethod method) {
        int index = DISPLAY_ORDER.indexOf(method);
        return index >= 0 ? index : DISPLAY_ORDER.size();
    }

    @NonNull
    public List<LoginOptionJson> toOptions(@NonNull Collection<LoginMethod> methods) {
        return methods.stream()
            .sorted(Comparator.comparingInt(this::order))
            .map(method -> new LoginOptionJson(method, resolve(method), order(method)))
            .toList();
    }
}
