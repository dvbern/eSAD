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
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.assertj.core.api.AbstractAssert;

import java.io.InputStream;
import org.assertj.core.api.Assertions;
import org.assertj.core.util.CanIgnoreReturnValue;

public class DocxAssert extends AbstractAssert<DocxAssert, InputStream> {

    private String content = null;

    public DocxAssert(InputStream actual) {
        super(actual, DocxAssert.class);
        try {
            Tika tika = new Tika();
            this.content = tika.parseToString(actual);
            actual.close();
        } catch (TikaException | IOException e) {
            failWithMessage("Failed to parse DOCX content: %s", e.getMessage());
        }
    }

    public static DocxAssert assertThatDocx(InputStream actual) {
        return new DocxAssert(actual);
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
