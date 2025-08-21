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

package ch.dvbern.dvbstarter.infrastructure.pdf;

import ch.dvbern.oss.hemed.esc.api.testutil.TestUtil;
import java.io.File;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PDFContentExtractorUtilTest {

    @Test
    void extractPdfInfo() {
        // ARRANGE
        File testfile = TestUtil.loadTestPDF();

        // ACT
        PDFContentExtractorUtil.PdfInfo pdfInfo = PDFContentExtractorUtil.extractPdfInfo(testfile.getPath());

        // ASSERT
        assertNotNull(pdfInfo);
        assertTrue(pdfInfo.textcontent().contains("A Simple PDF File"));
    }

}
