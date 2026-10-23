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

package ch.dvbern.oss.hemed.esc.api.shared.printer.anonymer;

import ch.dvbern.oss.hemed.esc.api.features.anlass.anonymereintrag.AnonymerEintrag;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.UntersuchungService;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantEnum;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantContext;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import ch.dvbern.oss.hemed.esc.api.shared.printer.PrintResultDTO;
import ch.dvbern.oss.hemed.esc.api.testutil.AbstractITest;
import ch.dvbern.oss.hemed.esc.api.testutil.FixtureService;
import ch.dvbern.oss.hemed.esc.api.testutil.restassured.RequestSpecUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsSchularztBE;
import ch.dvbern.oss.hemed.esc.generated.restassured.api.PrinterApiSpec;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.ResponseOptions;
import jakarta.ws.rs.core.Response.Status;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for the dedicated anonymous print pipeline
 * ({@link AnonymerEintragPrintService} -&gt; {@link AnonymerEintragPrinter}).
 * <p>
 * It exercises the full service path against a persisted {@link AnonymerEintrag} (no mock Kind, no
 * bridging Einladung) and asserts that a non-empty PDF with a hash-based filename is produced.
 */
@Slf4j
@QuarkusTest
@RequiredArgsConstructor
class AnonymerEintragPrintServiceITest extends AbstractITest {

    private final PrinterApiSpec api = PrinterApiSpec.printer(RequestSpecUtil.quarkusSpec());

    private final AnonymerEintragPrintService anonymerEintragPrintService;
    private final UntersuchungService untersuchungService;
    private final FixtureService fixtureService;
    private final TenantContext tenantContext;

    @Test
    @TestAsSchularztBE
    void printAnonymerEintrag_withUntersuchung_producesPdfWithHashFilename() {
        // ARRANGE
        tenantContext.setCurrentTenant(TenantEnum.BE);
        Benutzer schularzt = fixtureService.readBenutzerWithBerechtigungen(TestAsSchularztBE.BENUTZER_ID_ID);
        AnonymerEintrag anonymerEintrag = fixtureService.createAnonymerEintragForArzt(schularzt);
        untersuchungService.getOrCreateUntersuchungAnonym(anonymerEintrag.getId(), false);

        // ACT
        PrintResultDTO printResultDTO = anonymerEintragPrintService.printAnonymerEintrag(anonymerEintrag.getId());

        // ASSERT
        assertThat(printResultDTO).isNotNull();
        assertThat(printResultDTO.getContent()).isNotEmpty();
        assertThat(printResultDTO.getFileInfo()).isNotNull();
        String fileName = printResultDTO.getFileInfo().getFileName().getFileName();
        assertThat(fileName)
            .contains("anonym")
            // the entry's hash (whitespace replaced by '-') is part of the filename
            .contains(anonymerEintrag.getHash().replace(' ', '-'))
            .endsWith(".pdf");
        assertThat(printResultDTO.getFileInfo().getFileSize()).isEqualTo(printResultDTO.getContent().length);
    }

    @Test
    @TestAsSchularztBE
    void printerResourceDownloadAnonymerEintrag_returnsPdf() {
        // ARRANGE: the REST endpoint requires an existing Untersuchung on the entry.
        tenantContext.setCurrentTenant(TenantEnum.BE);
        Benutzer schularzt = fixtureService.readBenutzerWithBerechtigungen(TestAsSchularztBE.BENUTZER_ID_ID);
        AnonymerEintrag anonymerEintrag = fixtureService.createAnonymerEintragForArzt(schularzt);
        untersuchungService.getOrCreateUntersuchungAnonym(anonymerEintrag.getId(), false);

        // ACT: call the API via the generated PrinterApiSpec operation.
        byte[] content = api.printerResourceDownloadAnonymerEintrag()
            .anonymerEintragIdPath(anonymerEintrag.getId().getId())
            .execute(ResponseOptions::andReturn)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract()
            .asByteArray();

        // ASSERT
        assertThat(content).isNotNull().hasSizeGreaterThan(0);
    }

    @Test
    @TestAsSchularztBE
    void printAnonymerEintrag_withoutUntersuchung_producesPdf() {
        // ARRANGE: an anonymous entry that has no Untersuchung yet must still print.
        tenantContext.setCurrentTenant(TenantEnum.BE);
        Benutzer schularzt = fixtureService.readBenutzerWithBerechtigungen(TestAsSchularztBE.BENUTZER_ID_ID);
        AnonymerEintrag anonymerEintrag = fixtureService.createAnonymerEintragForArzt(schularzt);

        // ACT
        PrintResultDTO printResultDTO = anonymerEintragPrintService.printAnonymerEintrag(anonymerEintrag.getId());

        // ASSERT
        assertThat(printResultDTO).isNotNull();
        assertThat(printResultDTO.getContent()).isNotEmpty();
        assertThat(printResultDTO.getFileInfo().getFileName().getFileName()).endsWith(".pdf");
    }
}
