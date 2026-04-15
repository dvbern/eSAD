package ch.dvbern.oss.hemed.esc.api.shared.plz;

import java.net.HttpURLConnection;
import java.util.List;

import ch.dvbern.oss.hemed.esc.api.testutil.restassured.RequestSpecUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsAdminBS;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsSupportBS;
import ch.dvbern.oss.hemed.esc.generated.restassured.api.PlzApiSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.PLZDataJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.PLZDataListJsonSpec;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.ResponseBody;
import lombok.RequiredArgsConstructor;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;

@QuarkusTest
@TestAsAdminBS
@DisplayNameGeneration(ReplaceUnderscores.class)
@RequiredArgsConstructor
class PLZResourceTest {

    private final PlzApiSpec api = PlzApiSpec.plz(RequestSpecUtil.quarkusSpec());

    @Test
    @TestAsSupportBS
    void importPlzFromCsv() {

        // ACT
        var success = api.pLZResourceImportPlzFromCsv()
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(HttpURLConnection.HTTP_NO_CONTENT)
            .extract().asString();

        // ASSERT
        Assertions.assertThat(success).isNotNull();
    }

    @Test
    @TestAsSupportBS
    void findPlzData() {
        // Import Plz Data
        api.pLZResourceImportPlzFromCsv()
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(HttpURLConnection.HTTP_NO_CONTENT)
            .extract().asString();

        // ACT
        PLZDataListJsonSpec plzDataListJson = api.pLZResourceFindPLZData()
            .plzdataQuery("3000")
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(HttpURLConnection.HTTP_OK)
            .extract().as(PLZDataListJsonSpec.class);

        // ASSERT
        Assertions.assertThat(plzDataListJson).isNotNull();
        List<PLZDataJsonSpec> plzDataList = plzDataListJson.getPlzDataList();
        Assertions.assertThat(plzDataList)
            .isNotNull()
            .hasSize(1);
        Assertions.assertThat(plzDataList.getFirst().getOrtsBezeichnung()).isEqualTo("Bern");
    }
}
