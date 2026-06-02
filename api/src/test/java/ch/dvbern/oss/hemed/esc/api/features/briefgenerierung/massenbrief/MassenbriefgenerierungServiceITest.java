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

package ch.dvbern.oss.hemed.esc.api.features.briefgenerierung.massenbrief;

import java.nio.file.Path;
import java.util.List;

import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureException;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppValidationException;
import ch.dvbern.dvbstarter.shared.types.cleanfilename.CleanFileName;
import ch.dvbern.oss.hemed.esc.api.features.arzt.Arzt;
import ch.dvbern.oss.hemed.esc.api.features.benutzer.BenutzerService;
import ch.dvbern.oss.hemed.esc.api.features.briefvorlagen.Briefvorlage;
import ch.dvbern.oss.hemed.esc.api.features.briefvorlagen.BriefvorlageType;
import ch.dvbern.oss.hemed.esc.api.features.briefvorlagen.briefgenerierung.MassenbriefGeneratorService;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import ch.dvbern.oss.hemed.esc.api.shared.documentqueue.json.DocumentQueueMassenbriefSaveJson;
import ch.dvbern.oss.hemed.esc.api.testutil.AbstractITest;
import ch.dvbern.oss.hemed.esc.api.testutil.FixtureService;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsAdminBS;
import io.quarkus.test.junit.QuarkusTest;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@RequiredArgsConstructor
@QuarkusTest
class MassenbriefgenerierungServiceITest extends AbstractITest {
    private final MassenbriefGeneratorService massenbriefGeneratorService;
    private final FixtureService fixtureService;
    private final BenutzerService benutzerService;

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
    }

    @Test
    @TestAsAdminBS
    void generateMassenbriefZip_should_generate() {
        // ARRANGE
        String filename = "Test.zip";
        Kind kind1 = fixtureService.createKind("Muster", "Max");
        Kind kind2 = fixtureService.createKind("Muster", "Moritz");
        Briefvorlage briefvorlage = fixtureService.createBriefvorlage(BriefvorlageType.KIND);
        DocumentQueueMassenbriefSaveJson saveJson = new DocumentQueueMassenbriefSaveJson(
            briefvorlage.getId(),
            null,
            List.of(kind1.getId(), kind2.getId())
        );
        Benutzer benutzer =
            fixtureService.readBenutzerWithBerechtigungen(benutzerService.getCurrentBenutzerOrThrowException().getId());

        // ACT
        Path result =
            massenbriefGeneratorService.generateMassenbriefZip(saveJson, benutzer, CleanFileName.parse(filename));

        // ASSERT
        assertThat(result).isNotNull();
        assertThat(result.getFileName().toFile()).hasName(filename);

    }

    @Test
    @TestAsAdminBS
    void generateMassenbriefZip_forBriefvorlageTypeAnlass_should_throw() {
        // ARRANGE
        Kind kind1 = fixtureService.createKind("Meier", "Hans");
        Kind kind2 = fixtureService.createKind("Tell", "Willhelm");
        Arzt arzt = fixtureService.createArzt();
        Briefvorlage briefvorlage = fixtureService.createBriefvorlage(BriefvorlageType.ANLASS);
        DocumentQueueMassenbriefSaveJson saveJson = new DocumentQueueMassenbriefSaveJson(
            briefvorlage.getId(),
            arzt.getId(),
            List.of(kind1.getId(), kind2.getId())
        );
        Benutzer benutzer = benutzerService.getCurrentBenutzerOrThrowException();
        CleanFileName cleanFileName = CleanFileName.parse("Test-wrong-type-should-throw.zip");

        // ACT & ASSERT
        assertThatThrownBy(() -> massenbriefGeneratorService.generateMassenbriefZip(
            saveJson,
            benutzer,
            cleanFileName))
            .isInstanceOf(AppValidationException.class)
            .hasMessageContaining("Massenbriefgenerierung for BriefvorlageTyp.ANLASS is not supported.");
    }

    @Test
    @TestAsAdminBS
    void generateMassenbriefZip_withNoKinderIds_should_throw() {
        // ARRANGE
        Arzt arzt = fixtureService.createArzt();
        Briefvorlage briefvorlage = fixtureService.createBriefvorlage(BriefvorlageType.KIND);
        DocumentQueueMassenbriefSaveJson saveJson = new DocumentQueueMassenbriefSaveJson(
            briefvorlage.getId(),
            arzt.getId(),
            List.of()
        );
        Benutzer benutzer = benutzerService.getCurrentBenutzerOrThrowException();
        CleanFileName cleanFileName = CleanFileName.parse("Test-no-kids-should-throw.zip");

        // ACT & ASSERT
        assertThatThrownBy(() -> massenbriefGeneratorService.generateMassenbriefZip(
            saveJson,
            benutzer,
            cleanFileName))
            .isInstanceOf(AppFailureException.class)
            .hasMessageContaining("AppFailure.INTERNAL_ERROR, args: message=Error while generating Massenbrief files:");
    }
}
