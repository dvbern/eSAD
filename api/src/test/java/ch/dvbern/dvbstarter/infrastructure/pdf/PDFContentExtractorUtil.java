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

import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureException;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureMessage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class PDFContentExtractorUtil {

    public record PdfInfo(String textcontent, int pages) {
    }

    private PDFContentExtractorUtil() {
    }

    @NonNull
    public static PdfInfo extractPdfInfo(byte[] content) {
        try (PDDocument document = Loader.loadPDF(content)) {
            PDFTextStripper pdfStripper = new PDFTextStripper();
            return new PdfInfo(pdfStripper.getText(document), document.getNumberOfPages());
        } catch (IOException e) {
            throw new AppFailureException(
                AppFailureMessage.internalError("Error while extracting text from PDF"), e);
        }
    }

    @NonNull
    public static PdfInfo extractPdfInfo(@NonNull String path) {
        byte[] bytes = null;
        try {
            bytes = Files.readAllBytes(Path.of(path));
        } catch (IOException e) {
            throw new AppFailureException(
                AppFailureMessage.internalError("Error while loading file"), e);
        }
        return extractPdfInfo(bytes);
    }
}
