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

package ch.dvbern.oss.hemed.esc.api.testutil.assertj;

import java.io.IOException;
import java.io.InputStream;

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.microsoft.OfficeParserConfig;
import org.apache.tika.sax.BodyContentHandler;
import org.assertj.core.annotation.CanIgnoreReturnValue;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;
import org.xml.sax.SAXException;

public class DocxAssert extends AbstractAssert<DocxAssert, InputStream> {

    private String content = null;

    public DocxAssert(InputStream actual) {
        super(actual, DocxAssert.class);
        try {
            this.content = extractText(actual);
            actual.close();
        } catch (TikaException | SAXException | IOException e) {
            failWithMessage("Failed to parse DOCX content: %s", e.getMessage());
        }
    }

    public static DocxAssert assertThatDocx(InputStream actual) {
        return new DocxAssert(actual);
    }

    /**
     * Extracts the text of a document with Tika.
     * <p>
     * Tika 3.3.2 ported the SAX based OOXML parsers to 3.x and made them the default. The SAX based docx parser
     * formats tables differently than the DOM based one (cells of a row are separated by tabs on a single line
     * instead of one line per cell). To keep the extracted text - and therefore the expected text files - stable,
     * the DOM based docx parser is used explicitly.
     */
    private static String extractText(InputStream actual) throws TikaException, SAXException, IOException {
        OfficeParserConfig officeParserConfig = new OfficeParserConfig();
        officeParserConfig.setUseSAXDocxExtractor(false);

        ParseContext parseContext = new ParseContext();
        parseContext.set(OfficeParserConfig.class, officeParserConfig);

        BodyContentHandler handler = new BodyContentHandler(-1);
        Parser parser = new AutoDetectParser();
        parser.parse(actual, handler, new Metadata(), parseContext);

        return handler.toString();
    }

    @CanIgnoreReturnValue
    public DocxAssert containsExactly(String expected) {
        isNotNull();
        Assertions.assertThat(content).isEqualTo(expected);
        return this;
    }

    @CanIgnoreReturnValue
    public DocxAssert containsText(String expected) {
        isNotNull();
        Assertions.assertThat(content).contains(expected);
        return this;
    }

    @CanIgnoreReturnValue
    public DocxAssert doesNotContainText(String unexpected) {
        isNotNull();
        Assertions.assertThat(content).doesNotContain(unexpected);
        return this;
    }

    // Optionally expose the text content for debug/assertions
    public String getExtractedText() {
        return content;
    }
}
