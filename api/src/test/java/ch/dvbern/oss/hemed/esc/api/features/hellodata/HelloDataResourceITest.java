package ch.dvbern.oss.hemed.esc.api.features.hellodata;

import java.net.HttpURLConnection;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.hellodata.json.HelloDataMetaJson;
import ch.dvbern.oss.hemed.esc.api.features.hellodata.json.HelloDataSchulenEnvelopeJson;
import ch.dvbern.oss.hemed.esc.api.features.hellodata.json.HelloDataSentinelJson;
import ch.dvbern.oss.hemed.esc.api.features.schule.Schule;
import ch.dvbern.oss.hemed.esc.api.features.schule.SchuleService;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantEnum;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantProducer;
import ch.dvbern.oss.hemed.esc.api.testutil.AbstractITest;
import ch.dvbern.oss.hemed.esc.api.testutil.FixtureService;
import ch.dvbern.oss.hemed.esc.api.testutil.TestUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.SchuleBuilder;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.not;

@QuarkusTest
@RequiredArgsConstructor
@DisplayNameGeneration(ReplaceUnderscores.class)
class HelloDataResourceITest extends AbstractITest {

    private static final String SCHULEN_PATH = "/hellodata/v1/ndjson/schulen";
    private static final String SCHULEN_JSON_PATH = "/hellodata/v1/json/schulen";

    private final SchuleService schuleService;
    private final FixtureService fixtureService;
    private final TenantProducer beTenantProducer;
    // the CDI bean, not a bare new ObjectMapper(): it carries the application's Jackson
    // configuration, so assertions here see exactly what HelloData receives
    private final ObjectMapper objectMapper;

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
        // AbstractITest sets BS - but HelloData reads exclusively in tenant BE.
        beTenantProducer.setCurrentTenant(TenantEnum.BE);
    }

    /**
     * The most important test: it compares the endpoint response against a direct read on the
     * BE database. If tenant resolution were lost during streaming, {@code HibernateTenantResolver}
     * would silently fall back to its default (BS), we have to make sure this does not happen
     */
    @Test
    @TestAsHelloDataSeedingBE
    void export_returns_exactly_the_schulen_of_tenant_BE() {
        // ARRANGE
        seedSchule();

        Set<String> expectedIds = schuleService.findAll().stream()
            .map(Schule::getId)
            .map(id -> id.getUUID().toString())
            .collect(Collectors.toSet());

        // ACT
        Response response = RestAssured.given()
            .when()
            .get(SCHULEN_PATH)
            .then()
            .assertThat()
            .statusCode(HttpURLConnection.HTTP_OK)
            .extract()
            .response();

        // ASSERT
        assertThat(response.getContentType()).contains(NdjsonWriter.MEDIA_TYPE);

        List<JsonNode> lines = parseNdjson(response.asString());
        Set<String> exportedIds = dataRows(lines).stream()
            .map(row -> row.get("id").asText())
            .collect(Collectors.toSet());

        assertThat(exportedIds)
            .as("the export must match exactly the Schulen in the BE database")
            .isEqualTo(expectedIds);
    }

    @Test
    @TestAsHelloDataSeedingBE
    void export_ends_with_a_meta_line_whose_rowCount_matches() {
        // ARRANGE
        seedSchule();

        // ACT
        String body = RestAssured.given()
            .when()
            .get(SCHULEN_PATH)
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

        assertThat(dataRows(lines))
            .as("only the last line may be the sentinel line")
            .allSatisfy(row -> assertThat(row.has(HelloDataSentinelJson.META_KEY)).isFalse());
    }

    /**
     * The projection maps constructor arguments <b>by position</b>, and five of the exported
     * fields are adjacent Strings. Transposing two of them would still compile, still stream and
     * still satisfy every other test here. Thus this tests really compares the values and catches such errors.
     */
    @Test
    @TestAsHelloDataSeedingBE
    void exported_fields_carry_the_values_of_their_own_column() {
        // ARRANGE
        Schule schule = seedSchule();

        // ACT
        String body = RestAssured.given().when().get(SCHULEN_PATH)
            .then().statusCode(HttpURLConnection.HTTP_OK).extract().asString();

        // ASSERT
        JsonNode row = dataRows(parseNdjson(body)).stream()
            .filter(r -> r.get("id").asText().equals(schule.getId().getUUID().toString()))
            .findFirst()
            .orElseThrow(() -> new AssertionError("seeded Schule missing from the export"));

        assertThat(row.get("schulhaus").asText()).isEqualTo(schule.getSchulhaus());
        assertThat(row.get("schulstandort").asText()).isEqualTo(schule.getSchulstandort());
        assertThat(row.get("schulkreis").asText()).isEqualTo(schule.getSchulkreis());
        assertThat(row.get("schulhausnummer").asText()).isEqualTo(schule.getSchulhausnummer());
        assertThat(schule.getAdresse()).isNotNull();
        assertThat(row.get("plz").asText()).isEqualTo(schule.getAdresse().getPlz());
        assertThat(row.get("ort").asText()).isEqualTo(schule.getAdresse().getOrt());
        assertThat(row.get("aktivStatus").asText()).isEqualTo(schule.getSchuleAktivStatus().name());
        assertThat(row.get("timestampMutiert").asText()).isNotBlank();
    }

    @Test
    @TestAsHelloDataSeedingBE
    void export_contains_neither_contact_details_nor_free_text() {
        // ARRANGE
        seedSchule();

        // ACT
        String body = RestAssured.given().when().get(SCHULEN_PATH)
            .then().statusCode(HttpURLConnection.HTTP_OK).extract().asString();

        // ASSERT
        List<JsonNode> rows = dataRows(parseNdjson(body));

        assertThat(rows)
            .as("without data rows this test proves nothing - the BE test database must "
                + "contain at least one Schule")
            .isNotEmpty()
            .as("contact details and free text must not leave the tenant")
            .allSatisfy(row -> {
                assertThat(row.has("email")).isFalse();
                assertThat(row.has("telefon")).isFalse();
                assertThat(row.has("bemerkungen")).isFalse();
                assertThat(row.has("zustaendigArzt")).isFalse();
                assertThat(row.has("zustaendigMPA")).isFalse();
                assertThat(row.has("strasseUndNr")).isFalse();
            });
    }

    /**
     * Guards against a bug that would not show up functionally: if the cursor were read outside
     * a transaction, pgjdbc buffers the entire result set - the endpoint would still return
     * correct data, just without streaming.
     * <p>
     * For a response larger than the output buffer (~8 KB) the server can only set a
     * Content-Length if it holds the whole response in memory first. Chunked transfer encoding
     * without a Content-Length is therefore the evidence that the response is sent while it is
     * being produced.
     */
    @Test
    @TestAsHelloDataSeedingBE
    void export_is_streamed_and_not_fully_buffered() {
        // ARRANGE - clearly more than the output buffer holds. Seeded in one statement:
        // 300 individual persists cost seconds and this test does not care how they got there.
        fixtureService.createSchulenInBulk(300);

        // ACT
        Response response = RestAssured.given()
            .when()
            .get(SCHULEN_PATH)
            .then()
            .assertThat()
            .statusCode(HttpURLConnection.HTTP_OK)
            .extract()
            .response();

        // ASSERT
        assertThat(response.asString().length())
            .as("test precondition: the response must be larger than the output buffer")
            .isGreaterThan(8192);

        assertThat(response.getHeader("Transfer-Encoding"))
            .as("chunked = the response was sent while produced, not buffered beforehand")
            .isEqualTo("chunked");

        assertThat(response.getHeader("Content-Length"))
            .as("a buffered response would carry a Content-Length")
            .isNull();
    }

    /**
     * The bulkhead that limits the export to one at a time must release again - otherwise the
     * first export of the night would wedge the endpoint for good.
     */
    @Test
    @TestAsHelloDataBE
    void two_sequential_exports_both_succeed() {
        RestAssured.given().when().get(SCHULEN_PATH).then().statusCode(HttpURLConnection.HTTP_OK);
        RestAssured.given().when().get(SCHULEN_PATH).then().statusCode(HttpURLConnection.HTTP_OK);
    }

    /**
     * The Berechtigung is granted from (azp, preferred_username) alone and says nothing about the
     * tenant, so nothing but the hostname would otherwise stop the HelloData client from
     * exporting another canton's Schulen.
     */
    @Test
    @TestAsHelloDataForeignTenant
    void export_refuses_a_tenant_other_than_BE() {
        String body = RestAssured.given()
            .when()
            .get(SCHULEN_PATH)
            .then()
            .assertThat()
            .statusCode(not(HttpURLConnection.HTTP_OK))
            .extract()
            .asString();

        assertThat(body)
            .as("no row of another tenant may reach the client")
            .doesNotContain("schulhaus");
    }

    @Test
    @TestAsSupportBE
    void export_is_forbidden_without_the_hellodata_berechtigung() {
        RestAssured.given()
            .when()
            .get(SCHULEN_PATH)
            .then()
            .assertThat()
            .statusCode(HttpURLConnection.HTTP_FORBIDDEN);
    }

    @Test
    void export_is_unreachable_without_authentication() {
        RestAssured.given()
            .when()
            .get(SCHULEN_PATH)
            .then()
            .assertThat()
            .statusCode(HttpURLConnection.HTTP_UNAUTHORIZED);
    }

    /**
     * The JSON variant exists for consumers that cannot ingest NDJSON. It must carry exactly the
     * same rows - two endpoints returning different data would be worse than only offering one.
     */
    @Test
    @TestAsHelloDataSeedingBE
    @SneakyThrows
    void json_export_contains_the_same_rows_as_the_ndjson_export() {
        // ARRANGE
        seedSchule();

        // ACT
        Response response = RestAssured.given()
            .when()
            .get(SCHULEN_JSON_PATH)
            .then()
            .assertThat()
            .statusCode(HttpURLConnection.HTTP_OK)
            .extract()
            .response();

        String ndjsonBody = RestAssured.given().when().get(SCHULEN_PATH)
            .then().statusCode(HttpURLConnection.HTTP_OK).extract().asString();

        // ASSERT
        assertThat(response.getContentType()).contains(JsonEnvelopeWriter.MEDIA_TYPE);

        String body = response.asString();

        assertThat(body)
            .as("the framing is written by hand so it can stream - pin it")
            .startsWith("{\"" + JsonEnvelopeWriter.DATA_KEY + "\":[")
            .endsWith("}");

        // readTree, not a lenient line-by-line parse: this is the assertion that the hand-written
        // framing (braces, commas, the trailing _meta) actually produces valid JSON.
        JsonNode envelope = objectMapper.readTree(body);

        Set<String> jsonIds = idsOf(envelope.get(JsonEnvelopeWriter.DATA_KEY));
        Set<String> ndjsonIds = dataRows(parseNdjson(ndjsonBody)).stream()
            .map(row -> row.get("id").asText())
            .collect(Collectors.toSet());

        assertThat(jsonIds)
            .as("both formats export the same Schulen")
            .isNotEmpty()
            .isEqualTo(ndjsonIds);
    }

    @Test
    @TestAsHelloDataSeedingBE
    @SneakyThrows
    void json_export_ends_with_a_meta_whose_rowCount_matches() {
        // ARRANGE
        seedSchule();

        // ACT
        String body = RestAssured.given().when().get(SCHULEN_JSON_PATH)
            .then().statusCode(HttpURLConnection.HTTP_OK).extract().asString();

        // ASSERT
        JsonNode envelope = objectMapper.readTree(body);

        JsonNode meta = envelope.get(HelloDataSentinelJson.META_KEY);
        assertThat(meta)
            .as("without _meta HelloData cannot detect a truncated response")
            .isNotNull();
        assertThat(meta.get("status").asText()).isEqualTo(HelloDataMetaJson.STATUS_COMPLETE);
        assertThat(meta.get("exportedAt").asText()).isNotBlank();
        assertThat(meta.get("rowCount").asLong())
            .as("rowCount must match the size of the data array")
            .isEqualTo(envelope.get(JsonEnvelopeWriter.DATA_KEY).size());
    }

    /**
     * {@code HelloDataSchulenEnvelopeJson} describes the response but never produces it - the
     * body is written incrementally so it can stream. Renaming a field on the DTO would
     * therefore change the OpenAPI contract HelloData generates from, without changing a single
     * byte the server sends. This is the test that ties the two together.
     */
    @Test
    @TestAsHelloDataSeedingBE
    @SneakyThrows
    void json_envelope_matches_the_declared_contract_type() {
        // ARRANGE
        seedSchule();

        JsonNode declared = objectMapper.valueToTree(new HelloDataSchulenEnvelopeJson(
            List.of(),
            new HelloDataMetaJson(
                HelloDataMetaJson.STATUS_COMPLETE,
                0,
                // parse instead of now(): AppClock is the app's clock, and this value is never
                // compared - only the field names of the serialized shape matter here.
                OffsetDateTime.parse("2026-01-01T00:00:00Z"))));

        // ACT
        String body = RestAssured.given().when().get(SCHULEN_JSON_PATH)
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

    /**
     * The whole point of the envelope variant is that it is a container object <b>and</b> still
     * streams. Same evidence as for NDJSON: a response larger than the output buffer can only
     * carry a Content-Length if the server held all of it in memory first.
     */
    @Test
    @TestAsHelloDataSeedingBE
    void json_export_is_streamed_and_not_fully_buffered() {
        // ARRANGE
        fixtureService.createSchulenInBulk(300);

        // ACT
        Response response = RestAssured.given()
            .when()
            .get(SCHULEN_JSON_PATH)
            .then()
            .assertThat()
            .statusCode(HttpURLConnection.HTTP_OK)
            .extract()
            .response();

        // ASSERT
        assertThat(response.asString().length())
            .as("test precondition: the response must be larger than the output buffer")
            .isGreaterThan(8192);

        assertThat(response.getHeader("Transfer-Encoding"))
            .as("chunked = the response was sent while produced, not buffered beforehand")
            .isEqualTo("chunked");

        assertThat(response.getHeader("Content-Length"))
            .as("a buffered response would carry a Content-Length")
            .isNull();
    }

    @Test
    @TestAsHelloDataForeignTenant
    void json_export_refuses_a_tenant_other_than_BE() {
        String body = RestAssured.given()
            .when()
            .get(SCHULEN_JSON_PATH)
            .then()
            .assertThat()
            .statusCode(not(HttpURLConnection.HTTP_OK))
            .extract()
            .asString();

        assertThat(body)
            .as("no row of another tenant may reach the client")
            .doesNotContain("schulhaus");
    }

    @Test
    @TestAsSupportBE
    void json_export_is_forbidden_without_the_hellodata_berechtigung() {
        RestAssured.given()
            .when()
            .get(SCHULEN_JSON_PATH)
            .then()
            .assertThat()
            .statusCode(HttpURLConnection.HTTP_FORBIDDEN);
    }

    @Test
    void json_export_is_unreachable_without_authentication() {
        RestAssured.given()
            .when()
            .get(SCHULEN_JSON_PATH)
            .then()
            .assertThat()
            .statusCode(HttpURLConnection.HTTP_UNAUTHORIZED);
    }

    private static Set<String> idsOf(JsonNode dataArray) {
        assertThat(dataArray).as("the envelope must carry a data array").isNotNull();
        Set<String> ids = new HashSet<>();
        dataArray.forEach(row -> ids.add(row.get("id").asText()));
        return ids;
    }

    private static List<String> fieldNamesOf(JsonNode node) {
        assertThat(node).isNotNull();
        List<String> names = new ArrayList<>();
        node.fieldNames().forEachRemaining(names::add);
        return names.stream().sorted().toList();
    }

    private Schule seedSchule() {
        return schuleService.persist(SchuleBuilder.create(builder -> builder
            .withID(ID.random(Schule.class))
            .withSchulhaus(TestUtil.getRandomString("HelloDataSchulhaus"))));
    }

    /**
     * All lines except the trailing sentinel. A missing sentinel means the export was truncated,
     * which must fail as a readable assertion rather than an IndexOutOfBoundsException.
     */
    private static List<JsonNode> dataRows(List<JsonNode> lines) {
        assertThat(lines)
            .as("response must contain at least the sentinel line")
            .isNotEmpty();
        return lines.subList(0, lines.size() - 1);
    }

    @SneakyThrows
    private List<JsonNode> parseNdjson(String body) {
        List<JsonNode> result = new ArrayList<>();
        for (String line : body.split("\n")) {
            if (!line.isBlank()) {
                result.add(objectMapper.readTree(line));
            }
        }
        return result;
    }
}
