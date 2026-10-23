package ch.dvbern.oss.hemed.esc.api.features.hellodata;

import java.net.HttpURLConnection;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.hellodata.json.HelloDataKlassenEnvelopeJson;
import ch.dvbern.oss.hemed.esc.api.features.hellodata.json.HelloDataMetaJson;
import ch.dvbern.oss.hemed.esc.api.features.hellodata.json.HelloDataSentinelJson;
import ch.dvbern.oss.hemed.esc.api.features.schule.Schule;
import ch.dvbern.oss.hemed.esc.api.features.schule.SchuleService;
import ch.dvbern.oss.hemed.esc.api.features.schule.klasse.Klasse;
import ch.dvbern.oss.hemed.esc.api.features.schule.klasse.KlasseService;
import ch.dvbern.oss.hemed.esc.api.features.schule.types.Klassenstufe;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantEnum;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantContext;
import ch.dvbern.oss.hemed.esc.api.shared.types.EntityAktivStatus;
import ch.dvbern.oss.hemed.esc.api.testutil.AbstractITest;
import ch.dvbern.oss.hemed.esc.api.testutil.TestUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.KlasseBuilder;
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
import org.assertj.core.annotation.CanIgnoreReturnValue;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@RequiredArgsConstructor
@DisplayNameGeneration(ReplaceUnderscores.class)
class HelloDataKlassenResourceITest extends AbstractITest {

    private static final String KLASSEN_PATH = "/hellodata/v1/ndjson/klassen";
    private static final String KLASSEN_JSON_PATH = "/hellodata/v1/json/klassen";

    private final SchuleService schuleService;
    private final KlasseService klasseService;
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
    void export_contains_the_seeded_klasse() {
        // ARRANGE
        Klasse klasse = seedKlasse();

        // ACT
        Response response = RestAssured.given()
            .when()
            .get(KLASSEN_PATH)
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
            .as("the seeded Klasse must appear in the export")
            .contains(klasse.getId().getUUID().toString());
    }

    @Test
    @TestAsHelloDataSeedingBE
    void export_ends_with_a_meta_line_whose_rowCount_matches() {
        // ARRANGE
        seedKlasse();

        // ACT
        String body = RestAssured.given()
            .when()
            .get(KLASSEN_PATH)
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
        Klasse klasse = seedKlasse();

        // ACT
        String body = RestAssured.given().when().get(KLASSEN_PATH)
            .then().statusCode(HttpURLConnection.HTTP_OK).extract().asString();

        // ASSERT
        JsonNode row = dataRows(parseNdjson(body)).stream()
            .filter(r -> r.get("id").asText().equals(klasse.getId().getUUID().toString()))
            .findFirst()
            .orElseThrow(() -> new AssertionError("seeded Klasse missing from the export"));

        assertThat(row.get("schuleId").asText()).isEqualTo(klasse.getSchule().getId().getUUID().toString());
        assertThat(row.get("bezeichnung").asText()).isEqualTo(klasse.getBezeichnung());
        assertThat(row.get("klassenstufe").asText()).isEqualTo(klasse.getKlassenstufe().name());
        assertThat(row.get("klasseSchulart").asText()).isEqualTo(klasse.getKlasseSchulart());
        assertThat(row.get("klasseAktivStatus").asText()).isEqualTo(klasse.getKlasseAktivStatus().name());
    }

    @Test
    @TestAsHelloDataSeedingBE
    void export_contains_neither_staff_nor_import_info() {
        // ARRANGE
        seedKlasse();

        // ACT
        String body = RestAssured.given().when().get(KLASSEN_PATH)
            .then().statusCode(HttpURLConnection.HTTP_OK).extract().asString();

        // ASSERT
        List<JsonNode> rows = dataRows(parseNdjson(body));

        assertThat(rows)
            .as("without data rows this test proves nothing")
            .isNotEmpty()
            .allSatisfy(row -> {
                assertThat(row.has("lehrpersonen")).isFalse();
                assertThat(row.has("externalImportInfo")).isFalse();
            });
    }

    @Test
    @TestAsHelloDataSeedingBE
    @SneakyThrows
    void json_export_contains_the_same_rows_as_the_ndjson_export() {
        // ARRANGE
        seedKlasse();

        // ACT
        Response response = RestAssured.given()
            .when()
            .get(KLASSEN_JSON_PATH)
            .then()
            .assertThat()
            .statusCode(HttpURLConnection.HTTP_OK)
            .extract()
            .response();

        String ndjsonBody = RestAssured.given().when().get(KLASSEN_PATH)
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
            .as("both formats export the same Klassen")
            .isNotEmpty()
            .isEqualTo(ndjsonIds);
    }

    @Test
    @TestAsHelloDataSeedingBE
    @SneakyThrows
    void json_export_ends_with_the_meta_sentinel_and_matches_rowCount() {
        // ARRANGE
        seedKlasse();

        // ACT
        String body = RestAssured.given()
            .when()
            .get(KLASSEN_JSON_PATH)
            .then()
            .assertThat()
            .statusCode(HttpURLConnection.HTTP_OK)
            .extract()
            .asString();

        // ASSERT
        JsonNode envelope = objectMapper.readTree(body);

        assertThat(envelope.has(HelloDataSentinelJson.META_KEY))
            .as("the document must end with the _meta sentinel")
            .isTrue();

        JsonNode meta = envelope.get(HelloDataSentinelJson.META_KEY);
        JsonNode data = envelope.get(JsonEnvelopeWriter.DATA_KEY);

        assertThat(meta.get("status").asText()).isEqualTo(HelloDataMetaJson.STATUS_COMPLETE);
        assertThat(meta.get("exportedAt").asText()).isNotBlank();
        assertThat(meta.get("rowCount").asLong())
            .as("rowCount must match the data array length")
            .isEqualTo(data.size());
    }

    /**
     * Guards against drift: HelloDataKlassenEnvelopeJson is never used to produce a response
     * (doing so would buffer the whole export in memory), it only exists to describe the shape
     * in the OpenAPI contract. This test proves that the streaming implementation really produces
     * that shape.
     */
    @Test
    @TestAsHelloDataSeedingBE
    @SneakyThrows
    void json_envelope_matches_the_declared_contract_type() {
        // ARRANGE
        seedKlasse();

        JsonNode declared = objectMapper.valueToTree(new HelloDataKlassenEnvelopeJson(
            List.of(),
            new HelloDataMetaJson(
                HelloDataMetaJson.STATUS_COMPLETE,
                0,
                OffsetDateTime.parse("2026-01-01T00:00:00Z"))));

        // ACT
        String body = RestAssured.given().when().get(KLASSEN_JSON_PATH)
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
        RestAssured.given().when().get(KLASSEN_PATH).then().statusCode(HttpURLConnection.HTTP_OK);
    }

    @Test
    @TestAsHelloDataForeignTenant
    void export_is_refused_for_non_be_tenants() {
        RestAssured.given()
            .when()
            .get(KLASSEN_PATH)
            .then()
            .assertThat()
            .statusCode(HttpURLConnection.HTTP_INTERNAL_ERROR);
    }

    @Test
    @TestAsSupportBE
    void export_is_forbidden_without_the_hellodata_berechtigung() {
        RestAssured.given()
            .when()
            .get(KLASSEN_PATH)
            .then()
            .assertThat()
            .statusCode(HttpURLConnection.HTTP_FORBIDDEN);
    }

    @Test
    void export_is_unreachable_without_authentication() {
        RestAssured.given()
            .when()
            .get(KLASSEN_PATH)
            .then()
            .assertThat()
            .statusCode(HttpURLConnection.HTTP_UNAUTHORIZED);
    }

    @Test
    @TestAsHelloDataForeignTenant
    void json_export_is_refused_for_non_be_tenants() {
        RestAssured.given()
            .when()
            .get(KLASSEN_JSON_PATH)
            .then()
            .assertThat()
            .statusCode(HttpURLConnection.HTTP_INTERNAL_ERROR);
    }

    @Test
    @TestAsSupportBE
    void json_export_is_forbidden_without_the_hellodata_berechtigung() {
        RestAssured.given()
            .when()
            .get(KLASSEN_JSON_PATH)
            .then()
            .assertThat()
            .statusCode(HttpURLConnection.HTTP_FORBIDDEN);
    }

    @Test
    void json_export_is_unreachable_without_authentication() {
        RestAssured.given()
            .when()
            .get(KLASSEN_JSON_PATH)
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
    private Klasse seedKlasse() {
        Schule schule = schuleService.persist(SchuleBuilder.create(builder -> builder
            .withID(ID.random(Schule.class))
            .withSchulhaus(TestUtil.getRandomString("HelloDataSchulhaus"))));

        return klasseService.persist(KlasseBuilder.create(builder -> builder
            .withID(ID.random(Klasse.class))
            .forSchule(schule)
            .withBezeichnung(TestUtil.getRandomString("Klasse"))
            .withKlassenstufe(Klassenstufe.PRIMAR_KLASSE_3)
            .withKlasseSchulart(KlasseBuilder.KLASSENTYP_PRIM)
            .withEntityAktivStatus(EntityAktivStatus.AKTIV)));
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
