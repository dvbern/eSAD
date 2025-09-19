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

package ch.dvbern.oss.hemed.esc.api.shared.printer;

import lombok.experimental.UtilityClass;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.regex.Pattern;

@UtilityClass
public class PDFAPrintUtil {

    private static final String WHITESPACE_CHARS_REGEX = "[\\t\\f]+";
    private static final String NEWLINE_CHARS_REGEX = "[\\n\\r\u2028]+";
    private static final String CONTROL_CHARS_REGEX = "\\p{Cntrl}&&[^\\n\\r]";
    private static final Pattern PATTERN_TAB = Pattern.compile(WHITESPACE_CHARS_REGEX);
    private static final Pattern PATTERN_NEWLINE = Pattern.compile(NEWLINE_CHARS_REGEX);
    private static final Pattern PATTERN_CTRL_CHAR = Pattern.compile(CONTROL_CHARS_REGEX);


    public static String sanitizeAndTrimToEmpty(@Nullable String str) {
        return str == null ? "" : sanitizeForPDFA(str.trim());
    }

    /**
     * Cleans text so that it won't cause PdfAConformanceException in iText.
     * (com.itextpdf.pdfa.exceptions.PdfAConformanceException: Embedded fonts shall define all glyphs referenced for rendering within the conforming file.)
     * - Replaces tabs with space
     * - Removes other control characters
     */
    @NonNull
    public static String sanitizeForPDFA(String input) {
        // Step 1: replace tabs with a space
        String withNormalizedWhitespace = PATTERN_TAB.matcher(input).replaceAll(" ");
        // Step 2: preserve newlines, might be problematic but currently seems to work
        String withPreservedNewlines = PATTERN_NEWLINE.matcher(withNormalizedWhitespace).replaceAll("\n");
        // Step 3: remove any other ASCII control characters (0x00–0x1F, 0x7F) except newlines
        return PATTERN_CTRL_CHAR.matcher(withPreservedNewlines).replaceAll("");
        // Step 4: should we remove non breaking spaces or other exotics like  NO-BREAK SPACE (U+00A0), EN SPACE
        // (U+2002), EM SPACE (U+2003), ZERO-WIDTH SPACE (U+200B), etc. ?

    }
}
