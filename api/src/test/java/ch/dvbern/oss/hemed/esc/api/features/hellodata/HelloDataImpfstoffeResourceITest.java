package ch.dvbern.oss.hemed.esc.api.features.hellodata;

import java.net.HttpURLConnection;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.hellodata.json.HelloDataImpfstoffeEnvelopeJson;
import ch.dvbern.oss.hemed.esc.api.features.hellodata.json.HelloDataMetaJson;
import ch.dvbern.oss.hemed.esc.api.features.hellodata.json.HelloDataSentinelJson;
import ch.dvbern.oss.hemed.esc.api.features.impfstoff.Impfstoff;
import ch.dvbern.oss.hemed.esc.api.features.impfstoff.ImpfstoffService;
import ch.dvbern.oss.hemed.esc.api.features.impfstoff.types.Krankheit;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantEnum;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantContext;
import ch.dvbern.oss.hemed.esc.api.shared.util.EnumSetUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.AbstractITest;
import ch.dvbern.oss.hemed.esc.api.testutil.TestUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.ImpfstoffBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsHelloDataBE;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsHelloDataForeignTenant;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsHelloDataSeedingBE;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsSupportBE;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.assertj.core.annotation.CanIgnoreReturnValue;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.not;

@QuarkusTest
@RequiredArgsConstructor
@DisplayNameGeneration(ReplaceUnderscores.class)
class HelloDataImpfstoffeResourceITest extends AbstractITest {

    private static final String IMPFSTOFFE_PATH = "/hellodata/v1/ndjson/impfstoffe";
    private static final String IMPFSTOFFE_JSON_PATH = "/hellodata/v1/json/impfstoffe";

    private final ImpfstoffService impfstoffService;
    private final TenantContext beTenantContext;
    // the CDI bean, not a bare new ObjectMapper(): it carries the application's Jackson
    // configuration, so assertions here see exactly what HelloData receives
    private final ObjectMapper objectMapper;

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
        // AbstractITest sets BS - but HelloData reads exclusively in tenant BE.
        beTenantContext.setCurrentTenant(TenantEnum.BE);
    }

    @Test
    @TestAsHelloDataSeedingBE
    void export_contains_the_seeded_impfstoff() {
        // ARRANGE
        Impfstoff impfstoff = seedImpfstoff();

        // ACT
        Response response = RestAssured.given()
            .when()
            .get(IMPFSTOFFE_PATH)
            .then()
            .assertThat()
            .statusCode(HttpURLConnection.HTTP_OK)
            .extract()
            .response();

        // ASSERT
        assertThat(response.getContentType()).contains(NdjsonWriter.MEDIA_TYPE);

        Set<String> exportedIds = dataRows(parseNdjson(response.asString())).stream()
            .map(row -> row.get("id").asText())
            .collect(Collectors.toSet());

        assertThat(exportedIds)
            .as("the seeded Impfstoff must appear in the export")
            .contains(impfstoff.getId().getUUID().toString());
    }

    @Test
    @TestAsHelloDataSeedingBE
    void export_ends_with_a_meta_line_whose_rowCount_matches() {
        // ARRANGE
        seedImpfstoff();

        // ACT
        String body = RestAssured.given()
            .when()
            .get(IMPFSTOFFE_PATH)
            .then()
            .assertThat()
            .statusCode(HttpURLConnection.HTTP_OK)
            .extract()
            .asString();

        // ASSERT
        List<JsonNode> lines = parseNdjson(body);
        assertThat(lines).isNotEmpty();

        JsonNode lastLine = lines.getLast();
        assertThat(lastLine.has(HelloDataSentinelJson.META_KEY))
            .as("without the sentinel line HelloData cannot detect a truncated stream")
            .isTrue();

        JsonNode meta = lastLine.get(HelloDataSentinelJson.META_KEY);
        assertThat(meta.get("status").asText()).isEqualTo(HelloDataMetaJson.STATUS_COMPLETE);
        assertThat(meta.get("exportedAt").asText()).isNotBlank();
        assertThat(meta.get("rowCount").asLong())
            .as("rowCount must match the number of preceding data rows")
            .isEqualTo(dataRows(lines).size());
    }

    @Test
    @TestAsHelloDataSeedingBE
    void exported_fields_carry_the_values_of_their_own_column() {
        // ARRANGE
        Impfstoff impfstoff = seedImpfstoff();

        // ACT
        String body = RestAssured.given().when().get(IMPFSTOFFE_PATH)
            .then().statusCode(HttpURLConnection.HTTP_OK).extract().asString();

        // ASSERT
        JsonNode row = dataRows(parseNdjson(body)).stream()
            .filter(r -> r.get("id").asText().equals(impfstoff.getId().getUUID().toString()))
            .findFirst()
            .orElseThrow(() -> new AssertionError("seeded Impfstoff missing from the export"));

        assertThat(row.get("name").asText()).isEqualTo(impfstoff.getName());
        // krankheiten is the raw comma-separated column - the export hands it on unchanged
        assertThat(row.get("krankheiten").asText())
            .isEqualTo(EnumSetUtil.toString(List.copyOf(impfstoff.getKrankheiten())));
    }

    @Test
    @TestAsHelloDataSeedingBE
    @SneakyThrows
    void json_export_contains_the_same_rows_as_the_ndjson_export() {
        // ARRANGE
        seedImpfstoff();

        // ACT
        Response response = RestAssured.given()
            .when()
            .get(IMPFSTOFFE_JSON_PATH)
            .then()
            .assertThat()
            .statusCode(HttpURLConnection.HTTP_OK)
            .extract()
            .response();

        String ndjsonBody = RestAssured.given().when().get(IMPFSTOFFE_PATH)
            .then().statusCode(HttpURLConnection.HTTP_OK).extract().asString();

        // ASSERT
        assertThat(response.getContentType()).contains(JsonEnvelopeWriter.MEDIA_TYPE);

        String jsonBody = response.asString();

        assertThat(jsonBody)
            .as("the framing is written by hand so it can stream - pin it")
            .startsWith("{\"" + JsonEnvelopeWriter.DATA_KEY + "\":[")
            .endsWith("}");

        JsonNode envelope = objectMapper.readTree(jsonBody);

        Set<String> jsonIds = idsOf(envelope.get(JsonEnvelopeWriter.DATA_KEY));
        Set<String> ndjsonIds = dataRows(parseNdjson(ndjsonBody)).stream()
            .map(row -> row.get("id").asText())
            .collect(Collectors.toSet());

        assertThat(jsonIds)
            .as("both formats export the same Impfstoffe")
            .isNotEmpty()
            .isEqualTo(ndjsonIds);
    }

    @Test
    @TestAsHelloDataSeedingBE
    @SneakyThrows
    void json_envelope_matches_the_declared_contract_type() {
        // ARRANGE
        seedImpfstoff();

        JsonNode declared = objectMapper.valueToTree(new HelloDataImpfstoffeEnvelopeJson(
            List.of(),
            new HelloDataMetaJson(
                HelloDataMetaJson.STATUS_COMPLETE,
                0,
                OffsetDateTime.parse("2026-01-01T00:00:00Z"))));

        // ACT
        String body = RestAssured.given().when().get(IMPFSTOFFE_JSON_PATH)
            .then().statusCode(HttpURLConnection.HTTP_OK).extract().asString();

        // ASSERT
        JsonNode actual = objectMapper.readTree(body);

        assertThat(fieldNamesOf(actual))
            .as("the response must have exactly the members the published contract declares")
            .isEqualTo(fieldNamesOf(declared));

        assertThat(fieldNamesOf(actual.get(HelloDataSentinelJson.META_KEY)))
            .as("the _meta payload must match HelloDataMetaJson")
            .isEqualTo(fieldNamesOf(declared.get(HelloDataSentinelJson.META_KEY)));
    }

    @Test
    @TestAsHelloDataBE
    void two_sequential_exports_both_succeed() {
        RestAssured.given().when().get(IMPFSTOFFE_PATH).then().statusCode(HttpURLConnection.HTTP_OK);
        RestAssured.given().when().get(IMPFSTOFFE_PATH).then().statusCode(HttpURLConnection.HTTP_OK);
    }

    @Test
    @TestAsHelloDataForeignTenant
    void export_refuses_a_tenant_other_than_BE() {
        RestAssured.given()
            .when()
            .get(IMPFSTOFFE_PATH)
            .then()
            .assertThat()
            .statusCode(not(HttpURLConnection.HTTP_OK));
    }

    @Test
    @TestAsSupportBE
    void export_is_forbidden_without_the_hellodata_berechtigung() {
        RestAssured.given()
            .when()
            .get(IMPFSTOFFE_PATH)
            .then()
            .assertThat()
            .statusCode(HttpURLConnection.HTTP_FORBIDDEN);
    }

    @Test
    void export_is_unreachable_without_authentication() {
        RestAssured.given()
            .when()
            .get(IMPFSTOFFE_PATH)
            .then()
            .assertThat()
            .statusCode(HttpURLConnection.HTTP_UNAUTHORIZED);
    }

    @Test
    @TestAsSupportBE
    void json_export_is_forbidden_without_the_hellodata_berechtigung() {
        RestAssured.given()
            .when()
            .get(IMPFSTOFFE_JSON_PATH)
            .then()
            .assertThat()
            .statusCode(HttpURLConnection.HTTP_FORBIDDEN);
    }

    @Test
    void json_export_is_unreachable_without_authentication() {
        RestAssured.given()
            .when()
            .get(IMPFSTOFFE_JSON_PATH)
            .then()
            .assertThat()
            .statusCode(HttpURLConnection.HTTP_UNAUTHORIZED);
    }

    @NonNull
    private static Set<String> idsOf(@NonNull JsonNode dataArray) {
        assertThat(dataArray).as("the envelope must carry a data array").isNotNull();
        Set<String> ids = new HashSet<>();
        dataArray.forEach(row -> ids.add(row.get("id").asText()));
        return ids;
    }

    @NonNull
    private static List<String> fieldNamesOf(@NonNull JsonNode node) {
        assertThat(node).isNotNull();
        List<String> names = new ArrayList<>();
        node.fieldNames().forEachRemaining(names::add);
        return names.stream().sorted().toList();
    }

    @NonNull
    @CanIgnoreReturnValue
    private Impfstoff seedImpfstoff() {
        return impfstoffService.persist(ImpfstoffBuilder.create(builder -> builder
            .withID(ID.random(Impfstoff.class))
            .withName(TestUtil.getRandomString("HelloDataImpfstoff"))
            .forKrankheiten(Krankheit.MASERN, Krankheit.MUMPS, Krankheit.ROETELN)));
    }

    /**
     * All lines except the trailing sentinel. A missing sentinel means the export was truncated,
     * which must fail as a readable assertion rather than an IndexOutOfBoundsException.
     */
    @NonNull
    private static List<JsonNode> dataRows(@NonNull List<JsonNode> lines) {
        assertThat(lines)
            .as("response must contain at least the sentinel line")
            .isNotEmpty();
        return lines.subList(0, lines.size() - 1);
    }

    @NonNull
    @SneakyThrows
    private List<JsonNode> parseNdjson(@NonNull String body) {
        List<JsonNode> result = new ArrayList<>();
        for (String line : body.split("\n")) {
            if (!line.isBlank()) {
                result.add(objectMapper.readTree(line));
            }
        }
        return result;
    }
}
