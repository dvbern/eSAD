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

package ch.dvbern.oss.hemed.esc.api.features.briefgenerierung.docmerger;

import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.arzt.Arzt;
import ch.dvbern.oss.hemed.esc.api.features.briefvorlagen.briefgenerierung.docmerger.MergeSourceCreatorService;
import ch.dvbern.oss.hemed.esc.api.features.briefvorlagen.briefgenerierung.docmerger.RootMergeSource;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantEnum;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantContext;
import ch.dvbern.oss.hemed.esc.api.testutil.FixtureService;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.ArztBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsAdminBS;
import io.quarkus.test.junit.QuarkusTest;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


@RequiredArgsConstructor
@QuarkusTest
class MergeSourceCreatorServiceITest {

    private final FixtureService fixtureService;
    private final MergeSourceCreatorService mergeSourceCreatorService;
    private final TenantContext tenantContext;

    @BeforeEach
    void setUp() {
        tenantContext.setCurrentTenant(TenantEnum.BS);
    }

    @Test
    @TestAsAdminBS
    void fromAnlass_should_create_a_merge_source() {
        // ARRANGE
        Untersuchung untersuchung = fixtureService.createUntersuchungWithCompleteData();
        var einladung = untersuchung.getEinladungRequired();
        // ACT
        RootMergeSource mergeSource = mergeSourceCreatorService.fromAnlass(einladung, null);
        // ASSERT
        Assertions.assertNotNull(mergeSource);
    }

    @Test
    @TestAsAdminBS
    void fromAnlass_should_create_a_merge_source_with_arzt() {
        // ARRANGE
        Untersuchung untersuchung = fixtureService.createUntersuchungWithCompleteData();
        Arzt arzt = ArztBuilder.create(builder -> builder.withAnrede("Dr. Med.")
            .withBemerkungen("Bemerkungen")
            .withName("Arzt")
            .withVorame("Armin"));
        var einladung = untersuchung.getEinladungRequired();
        // ACT
        RootMergeSource mergeSource = mergeSourceCreatorService.fromAnlass(einladung, arzt);
        // ASSERT
        Assertions.assertNotNull(mergeSource);
    }
}
