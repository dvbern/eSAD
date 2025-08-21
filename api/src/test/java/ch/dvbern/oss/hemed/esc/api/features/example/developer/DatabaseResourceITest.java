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

package ch.dvbern.oss.hemed.esc.api.features.example.developer;

import ch.dvbern.oss.hemed.esc.api.testutil.restassured.RequestSpecUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsSupport;
import ch.dvbern.oss.hemed.esc.generated.restassured.api.DeveloperApiSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.DBInfoJsonSpec;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.ResponseBody;
import lombok.RequiredArgsConstructor;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

@QuarkusTest
@RequiredArgsConstructor
class DatabaseResourceITest {

    DeveloperApiSpec developerApiSpec = DeveloperApiSpec.developer(RequestSpecUtil.quarkusSpec());

    @Test
    @TestAsSupport
    void testResetAndReinitDatabasePreservingHibernateSequenceValue() {
        var result = developerApiSpec
            .databaseResourceReturnDbInfo()
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(200)
            .extract().as(DBInfoJsonSpec.class);

        Assertions.assertThat(result.getVersion()).isNotNull()
            .contains("PostgreSQL");
    }
}
