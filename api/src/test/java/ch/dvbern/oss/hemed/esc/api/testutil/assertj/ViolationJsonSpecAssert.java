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

import java.util.Objects;

import ch.dvbern.oss.hemed.esc.generated.restassured.dto.ViolationJsonSpec;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.annotation.CanIgnoreReturnValue;

@SuppressWarnings({ "UnusedReturnValue", "unused" })
public class ViolationJsonSpecAssert extends AbstractAssert<ViolationJsonSpecAssert, ViolationJsonSpec> {

    private ViolationJsonSpecAssert(ViolationJsonSpec actual) {
        super(actual, ViolationJsonSpecAssert.class);
    }

    public static ViolationJsonSpecAssert assertThatViolationJsonSpec(ViolationJsonSpec actual) {
        return new ViolationJsonSpecAssert(actual);
    }

    @CanIgnoreReturnValue
    public ViolationJsonSpecAssert hasClientKey() {
        isNotNull();
        if (StringUtils.isBlank(actual.getKey())) {
            failWithMessage(
                "Expected ViolationJsonSpec to have a non-blank clientKey but clientKey was: %s",
                actual.getKey()
            );
        }
        return this;
    }

    @CanIgnoreReturnValue
    public ViolationJsonSpecAssert hasClientKey(String expectedClientKey) {
        isNotNull();
        if (!Objects.equals(actual.getKey(), expectedClientKey)) {
            failWithActualExpectedAndMessage(
                actual.getKey(),
                expectedClientKey,
                "Expected ViolationJsonSpec to have clientKey >%s< but was >%s<",
                expectedClientKey,
                actual.getKey()
            );
        }
        return this;
    }

    @CanIgnoreReturnValue
    public ViolationJsonSpecAssert hasRawMessage(String expectedMessage) {
        isNotNull();
        if (!Objects.equals(actual.getMessage(), expectedMessage)) {
            failWithActualExpectedAndMessage(
                actual.getMessage(),
                expectedMessage,
                "Expected ViolationJsonSpec to have message >%s< but was >%s<",
                expectedMessage, actual.getMessage());
        }
        return this;
    }

    @CanIgnoreReturnValue
    public ViolationJsonSpecAssert hasRawMessageContaining(String expectedMessage) {
        isNotNull();
        if (!actual.getMessage().contains(expectedMessage)) {
            failWithActualExpectedAndMessage(
                actual.getMessage(),
                expectedMessage,
                "Expected ViolationJsonSpec to have message containing >%s< but was >%s<",
                expectedMessage, actual.getMessage());
        }
        return this;
    }

}
