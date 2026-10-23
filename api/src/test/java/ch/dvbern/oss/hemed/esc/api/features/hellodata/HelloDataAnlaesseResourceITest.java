package ch.dvbern.oss.hemed.esc.api.features.hellodata;

import java.net.HttpURLConnection;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import ch.dvbern.oss.hemed.esc.api.features.anlass.Anlass;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.AnlassArt;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.AnlassStatus;
import ch.dvbern.oss.hemed.esc.api.features.hellodata.json.HelloDataAnlaesseEnvelopeJson;
import ch.dvbern.oss.hemed.esc.api.features.hellodata.json.HelloDataMetaJson;
import ch.dvbern.oss.hemed.esc.api.features.hellodata.json.HelloDataSentinelJson;
import ch.dvbern.oss.hemed.esc.api.features.organisation.Organisation;
import ch.dvbern.oss.hemed.esc.api.shared.auth.FachRolle;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantEnum;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantContext;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import ch.dvbern.oss.hemed.esc.api.testutil.AbstractITest;
import ch.dvbern.oss.hemed.esc.api.testutil.FixtureService;
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
class HelloDataAnlaesseResourceITest extends AbstractITest {

    private static final String ANLAESSE_PATH = "/hellodata/v1/ndjson/anlaesse";
    private static final String ANLAESSE_JSON_PATH = "/hellodata/v1/json/anlaesse";

    /**
     * The scheduled range of the seeded Anlass. Von and bis differ so the by-position projection
     * cannot swap them unnoticed.
     */
    private static final LocalDate DATUM =  LocalDate.of(2023, 5, 5);

    private final FixtureService fixtureService;
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
    void export_contains_the_seeded_anlass() {
        // ARRANGE
        Anlass anlass = seedAnlass();

        // ACT
        Response response = RestAssured.given()
            .when()
            .get(ANLAESSE_PATH)
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
            .as("the seeded Anlass must appear in the export")
            .contains(anlass.getId().getUUID().toString());
    }

    @Test
    @TestAsHelloDataSeedingBE
    void export_ends_with_a_meta_line_whose_rowCount_matches() {
        // ARRANGE
        seedAnlass();

        // ACT
        String body = RestAssured.given()
            .when()
            .get(ANLAESSE_PATH)
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
            .as("no data row may carry a sentinel")
            .noneMatch(row -> row.has(HelloDataSentinelJson.META_KEY));
    }

    @Test
    @TestAsHelloDataSeedingBE
    void exported_fields_carry_the_values_of_their_own_column() {
        // ARRANGE
        Anlass anlass = seedAnlass();

        // ACT
        String body = RestAssured.given().when().get(ANLAESSE_PATH)
            .then().statusCode(HttpURLConnection.HTTP_OK).extract().asString();

        // ASSERT
        JsonNode row = rowOf(body, anlass);

        assertThat(row.get("titel").asText())
            .isEqualTo(anlass.getTitel());
        assertThat(row.get("schuljahr").asInt())
            .isEqualTo(anlass.getSchuljahr());
        assertThat(LocalDate.parse(row.get("datum").asText()))
            .isEqualTo(DATUM);
        assertThat(row.get("anlassStatus").asText())
            .isEqualTo(anlass.getAnlassStatus().name());
        assertThat(row.get("anlassArt").asText())
            .isEqualTo(anlass.getAnlassArt().name());
        assertThat(anlass.getArzt()).isNotNull();
        assertThat(row.get("arztId").asText())
            .isEqualTo(anlass.getArzt().getId().getUUID().toString());
        assertThat(anlass.getMpa()).isNotNull();
        assertThat(row.get("mpaId").asText())
            .isEqualTo(anlass.getMpa().getId().getUUID().toString());
    }

    @Test
    @TestAsHelloDataSeedingBE
    void anlass_without_mpa_and_zeitraum_stays_in_the_export() {
        // ARRANGE
        Anlass anlass = seedAnlassWithoutMpaAndZeitraum();

        // ACT
        String body = RestAssured.given().when().get(ANLAESSE_PATH)
            .then().statusCode(HttpURLConnection.HTTP_OK).extract().asString();

        // ASSERT
        JsonNode row = rowOf(body, anlass);

        assertThat(row.hasNonNull("mpaId"))
            .as("the leftJoin must keep an Anlass without MPA in the export, carrying null")
            .isFalse();
        assertThat(row.hasNonNull("zeitVon")).isFalse();
        assertThat(row.hasNonNull("zeitBis")).isFalse();
        assertThat(anlass.getArzt()).isNotNull();
        assertThat(row.get("arztId").asText())
            .isEqualTo(anlass.getArzt().getId().getUUID().toString());
    }

    @Test
    @TestAsHelloDataSeedingBE
    void exported_anlass_appears_exactly_once() {
        // ARRANGE
        Anlass anlass = seedAnlass();

        // ACT
        String body = RestAssured.given().when().get(ANLAESSE_PATH)
            .then().statusCode(HttpURLConnection.HTTP_OK).extract().asString();

        // ASSERT
        assertThat(dataRows(parseNdjson(body)).stream()
            .filter(row -> row.get("id").asText().equals(anlass.getId().getUUID().toString()))
            .count())
            .as("the Arzt and MPA joins must not multiply the rows")
            .isEqualTo(1);
    }

    @Test
    @TestAsHelloDataSeedingBE
    void export_never_contains_the_free_text_about_the_staff() {
        // ARRANGE
        seedAnlass();

        // ACT
        String body = RestAssured.given().when().get(ANLAESSE_PATH)
            .then().statusCode(HttpURLConnection.HTTP_OK).extract().asString();

        // ASSERT
        assertThat(body)
            .as("free text about Arzt, MPA and weitere Person may contain personal data")
            .doesNotContain("arztInfo", "mpaInfo", "weiterePerson");
    }

    @Test
    @TestAsHelloDataSeedingBE
    @SneakyThrows
    void json_export_contains_the_same_rows_as_the_ndjson_export() {
        // ARRANGE
        seedAnlass();

        // ACT
        Response response = RestAssured.given()
            .when()
            .get(ANLAESSE_JSON_PATH)
            .then()
            .assertThat()
            .statusCode(HttpURLConnection.HTTP_OK)
            .extract()
            .response();

        String ndjsonBody = RestAssured.given().when().get(ANLAESSE_PATH)
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
            .as("both formats export the same Anlaesse")
            .isNotEmpty()
            .isEqualTo(ndjsonIds);
    }

    @Test
    @TestAsHelloDataSeedingBE
    @SneakyThrows
    void json_envelope_matches_the_declared_contract_type() {
        // ARRANGE
        seedAnlass();

        JsonNode declared = objectMapper.valueToTree(new HelloDataAnlaesseEnvelopeJson(
            List.of(),
            new HelloDataMetaJson(
                HelloDataMetaJson.STATUS_COMPLETE,
                0,
                OffsetDateTime.parse("2026-01-01T00:00:00Z"))));

        // ACT
        String body = RestAssured.given().when().get(ANLAESSE_JSON_PATH)
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
        RestAssured.given().when().get(ANLAESSE_PATH).then().statusCode(HttpURLConnection.HTTP_OK);
        RestAssured.given().when().get(ANLAESSE_PATH).then().statusCode(HttpURLConnection.HTTP_OK);
    }

    @Test
    @TestAsHelloDataForeignTenant
    void export_refuses_a_tenant_other_than_BE() {
        RestAssured.given()
            .when()
            .get(ANLAESSE_PATH)
            .then()
            .assertThat()
            .statusCode(not(HttpURLConnection.HTTP_OK));
    }

    @Test
    @TestAsSupportBE
    void export_is_forbidden_without_the_hellodata_berechtigung() {
        RestAssured.given()
            .when()
            .get(ANLAESSE_PATH)
            .then()
            .assertThat()
            .statusCode(HttpURLConnection.HTTP_FORBIDDEN);
    }

    @Test
    void export_is_unreachable_without_authentication() {
        RestAssured.given()
            .when()
            .get(ANLAESSE_PATH)
            .then()
            .assertThat()
            .statusCode(HttpURLConnection.HTTP_UNAUTHORIZED);
    }

    @Test
    @TestAsSupportBE
    void json_export_is_forbidden_without_the_hellodata_berechtigung() {
        RestAssured.given()
            .when()
            .get(ANLAESSE_JSON_PATH)
            .then()
            .assertThat()
            .statusCode(HttpURLConnection.HTTP_FORBIDDEN);
    }

    @Test
    void json_export_is_unreachable_without_authentication() {
        RestAssured.given()
            .when()
            .get(ANLAESSE_JSON_PATH)
            .then()
            .assertThat()
            .statusCode(HttpURLConnection.HTTP_UNAUTHORIZED);
    }

    @NonNull
    private JsonNode rowOf(@NonNull String ndjsonBody, @NonNull Anlass anlass) {
        return dataRows(parseNdjson(ndjsonBody)).stream()
            .filter(row -> row.get("id").asText().equals(anlass.getId().getUUID().toString()))
            .findFirst()
            .orElseThrow(() -> new AssertionError("seeded Anlass missing from the export"));
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

    /**
     * Tenant BE is dezentral, so an Anlass is only valid with a zustaendiger Arzt (see
     * {@code arztRequiredForDezentraleTenants}), and an MPA has to belong to the same
     * Organisation as the Arzt ({@code CheckArztUndMpaSameOrganisation}).
     */
    @NonNull
    @CanIgnoreReturnValue
    private Anlass seedAnlass() {
        Organisation organisation = fixtureService.createOrganisation();
        Benutzer arzt = fixtureService.createBenutzer(organisation, FachRolle.SCHULAERZTIN);
        Benutzer mpa = fixtureService.createBenutzer(organisation, FachRolle.MPA);
        return fixtureService.createAnlass(
            AnlassStatus.AUFGEBOTEN,
            AnlassArt.EINZEL_ANLASS,
            arzt,
            mpa,
            DATUM,
            null);
    }

    /**
     * The MPA and the Zeitraum are optional columns: an Anlass without them must stay in the
     * export (the associations are joined with a leftJoin) and carry null on the wire.
     */
    @NonNull
    @CanIgnoreReturnValue
    private Anlass seedAnlassWithoutMpaAndZeitraum() {
        Organisation organisation = fixtureService.createOrganisation();
        Benutzer arzt = fixtureService.createBenutzer(organisation, FachRolle.SCHULAERZTIN);
        return fixtureService.createAnlass(
            AnlassStatus.IN_PLANUNG,
            AnlassArt.GRUPPEN_ANLASS,
            arzt,
            null,
            DATUM,
            null);
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
