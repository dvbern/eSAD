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

package ch.dvbern.oss.hemed.esc.api.shared.fileio.s3;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Slf4j
class S3EncryptionKeyGeneratorTest {

    /**
     * This tetst can be used to generate a new encryption key for S3. It may serve as a template on what the
     * keys should look like*
     */
    @Test
    @Disabled("This test is disabled because it is not a real test. It is used to generate an example of an encryption key.")
    void generateS3EncryptionKey() {
        // ACT
        String base64Key = S3EncryptionKeyGenerator.generateKey();
        // ASSERT
        Assertions.assertThat(base64Key).isNotNull();
        LOG.info("Base64 Encoded Key looks like this \n{}", base64Key);
    }
}
