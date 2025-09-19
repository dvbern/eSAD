/*
 *
 * Copyright (C) 2025 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.oss.hemed.esc.api.shared.auth;

import java.net.URI;

import org.assertj.core.api.Assertions;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class TenantExtractorFromUrlRequestFilterTest {

    @ParameterizedTest
    @CsvSource({
        "https://local-esc-bs.localhost:4443/	        , BS",
        "https://dev-esc-bs.apps.mercury.ocp.dvbern.ch/	, BS",
        "https://uat-esc-bs.dvbern.ch/kinder	        , BS",
        "https://bs-esad.hemed.ch/                      , BS",
        "https://irgendjemand-esad.hemed.ch/            , ",
        "https://www.dvbern.ch/                         , ",
        "https://uat-api-esc-bs.dvbern.ch               , BS",
        "https://api-bs-esad.hemed.ch                    , BS",
    })
    void getTenantFromUrl(
        @NonNull String url,
        @Nullable String expectedTenantString
    ) {
        TenantEnum expectedTenant = expectedTenantString != null ? TenantEnum.valueOf(expectedTenantString) : null;
        TenantEnum actualTenant = TenantExtractorFromUrlRequestFilter.getTenantFromUrl(URI.create(url));
        Assertions.assertThat(actualTenant).isEqualTo(expectedTenant);
    }
}
