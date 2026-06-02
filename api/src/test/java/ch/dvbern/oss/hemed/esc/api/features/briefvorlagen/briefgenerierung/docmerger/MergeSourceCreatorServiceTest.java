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

package ch.dvbern.oss.hemed.esc.api.features.briefvorlagen.briefgenerierung.docmerger;

import ch.dvbern.oss.hemed.esc.api.shared.person.Adresse;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;

class MergeSourceCreatorServiceTest {

    @Test
    void formatAddress_shouldReturnPlaceholder_whenAddressIsNull() {
        // Given
        Adresse address = null;

        // When
        String result = MergeSourceCreatorService.formatAddress(address);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void formatAddress_shouldReturnFormattedAddress_whenAllFieldsArePresent() {
        // Given
        Adresse address = new Adresse();
        address.setStrasseUndNr("Teststrasse 123");
        address.setAdresseZusatz("4. Stock");
        address.setPlz("3000");
        address.setOrt("Bern");

        // When
        String result = MergeSourceCreatorService.formatAddress(address);

        // Then
        assertThat(result).isEqualTo("Teststrasse 123\n4. Stock\n3000 Bern");
    }

    @Test
    void formatAddress_shouldReturnPlaceholder_whenAllFieldsAreEmpty() {
        // Given
        Adresse address = new Adresse();
        address.setStrasseUndNr("");
        address.setAdresseZusatz("");
        address.setPlz("");
        address.setOrt("");

        // When
        String result = MergeSourceCreatorService.formatAddress(address);

        // Then
        assertThat(result).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("providePartialAddresses")
    void formatAddress_shouldCorrectlyFormatPartialAddresses(
        String strasseUndNr, String adresseZusatz, String plz, String ort, String expected) {
        // Given
        Adresse address = new Adresse();
        address.setStrasseUndNr(strasseUndNr);
        address.setAdresseZusatz(adresseZusatz);
        address.setPlz(plz);
        address.setOrt(ort);

        // When
        String result = MergeSourceCreatorService.formatAddress(address);

        // Then
        assertThat(result).isEqualTo(expected);
    }

    private static Stream<Arguments> providePartialAddresses() {
        return Stream.of(
            // Street only
            Arguments.of("Teststrasse 123", "", "", "", "Teststrasse 123"),
            // Street and city
            Arguments.of("Teststrasse 123", "", "", "Bern", "Teststrasse 123\nBern"),
            // Street and ZIP
            Arguments.of("Teststrasse 123", "", "3000", "", "Teststrasse 123\n3000"),
            // ZIP and city only
            Arguments.of("", "", "3000", "Bern", "3000 Bern"),
            // Adresszusatz only
            Arguments.of("", "4. Stock", "", "", "4. Stock"),
            // Various combinations
            Arguments.of("Teststrasse 123", "4. Stock", "", "", "Teststrasse 123\n4. Stock"),
            Arguments.of("", "", "3000", "Bern", "3000 Bern")
        );
    }
}
