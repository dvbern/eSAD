package ch.dvbern.oss.hemed.esc.api.features.hellodata;

import java.net.HttpURLConnection;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Anlass;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.AnlassStatus;
import ch.dvbern.oss.hemed.esc.api.features.hellodata.json.HelloDataAufgabenEnvelopeJson;
import ch.dvbern.oss.hemed.esc.api.features.hellodata.json.HelloDataMetaJson;
import ch.dvbern.oss.hemed.esc.api.features.hellodata.json.HelloDataSentinelJson;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import ch.dvbern.oss.hemed.esc.api.features.labels.Label;
import ch.dvbern.oss.hemed.esc.api.shared.aufgabe.Aufgabe;
import ch.dvbern.oss.hemed.esc.api.shared.aufgabe.types.AufgabeStatus;
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
class HelloDataAufgabenResourceITest extends AbstractITest {

    private static final String AUFGABEN_PATH = "/hellodata/v1/ndjson/aufgaben";
    private static final String AUFGABEN_JSON_PATH = "/hellodata/v1/json/aufgaben";

    /**
     * Free text of the seeded Aufgabe. It must never show up in the export.
     */
    private static final String BEMERKUNG = "HelloData Bemerkung mit Personendaten";

    private static final LocalDate FAELLIG_BIS = LocalDate.of(2026, 1, 31);

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
    void export_contains_the_seeded_aufgabe() {
        // ARRANGE
        Aufgabe aufgabe = seedAufgabe();

        // ACT
        Response response = RestAssured.given()
            .when()
            .get(AUFGABEN_PATH)
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
            .as("the seeded Aufgabe must appear in the export")
            .contains(aufgabe.getId().getUUID().toString());
    }

    @Test
    @TestAsHelloDataSeedingBE
    void export_ends_with_a_meta_line_whose_rowCount_matches() {
        // ARRANGE
        seedAufgabe();

        // ACT
        String body = RestAssured.given()
            .when()
            .get(AUFGABEN_PATH)
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
        Aufgabe aufgabe = seedAufgabeWithKindAndAnlass();

        // ACT
        String body = RestAssured.given().when().get(AUFGABEN_PATH)
            .then().statusCode(HttpURLConnection.HTTP_OK).extract().asString();

        // ASSERT
        JsonNode row = rowOf(body, aufgabe);

        assertThat(row.get("aufgabeStatus").asText())
            .isEqualTo(AufgabeStatus.IN_BEARBEITUNG.name());
        assertThat(row.get("faelligBis").asText())
            .isEqualTo(FAELLIG_BIS.toString());
        assertThat(row.get("userErfasst").asText())
            .isEqualTo(aufgabe.getUserErfasst().getId().getUUID().toString());
        assertThat(row.get("empfaenger").asText())
            .isEqualTo(aufgabe.getEmpfaenger().getId().getUUID().toString());
        assertThat(row.get("kindId").asText())
            .isEqualTo(requireNonNullKind(aufgabe).getId().getUUID().toString());
        assertThat(row.get("anlassId").asText())
            .isEqualTo(requireNonNullAnlass(aufgabe).getId().getUUID().toString());
    }

    @Test
    @TestAsHelloDataSeedingBE
    void exported_aufgabe_without_kind_and_anlass_carries_null_in_both_columns() {
        // ARRANGE
        Aufgabe aufgabe = seedAufgabe();

        // ACT
        String body = RestAssured.given().when().get(AUFGABEN_PATH)
            .then().statusCode(HttpURLConnection.HTTP_OK).extract().asString();

        // ASSERT
        JsonNode row = rowOf(body, aufgabe);

        assertThat(row.get("kindId").isNull())
            .as("Kind is optional - the Aufgabe must stay in the export with a null kindId")
            .isTrue();
        assertThat(row.get("anlassId").isNull())
            .as("Anlass is optional - the Aufgabe must stay in the export with a null anlassId")
            .isTrue();
        assertThat(row.get("labels").asText())
            .as("an Aufgabe without Labels must carry an empty string, not null")
            .isEmpty();
    }

    @Test
    @TestAsHelloDataSeedingBE
    void exported_labels_carry_the_comma_separated_label_uuids() {
        // ARRANGE
        Aufgabe aufgabe = seedAufgabeWithLabels(
            Set.of(Label.ID_LABEL_IMPFAUSWEIS, Label.ID_LABEL_FRAGEBOGEN));
        String expectedLabels = Set.of(Label.ID_LABEL_IMPFAUSWEIS, Label.ID_LABEL_FRAGEBOGEN).stream()
            .map(labelID -> labelID.getUUID().toString())
            .sorted()
            .collect(Collectors.joining(","));

        // ACT
        String body = RestAssured.given().when().get(AUFGABEN_PATH)
            .then().statusCode(HttpURLConnection.HTTP_OK).extract().asString();

        // ASSERT
        assertThat(rowOf(body, aufgabe).get("labels").asText())
            .isEqualTo(expectedLabels);
    }

    @Test
    @TestAsHelloDataSeedingBE
    void exported_aufgabe_with_labels_appears_exactly_once() {
        // ARRANGE
        Aufgabe aufgabe = seedAufgabeWithLabels(
            Set.of(Label.ID_LABEL_IMPFAUSWEIS, Label.ID_LABEL_FRAGEBOGEN));

        // ACT
        String body = RestAssured.given().when().get(AUFGABEN_PATH)
            .then().statusCode(HttpURLConnection.HTTP_OK).extract().asString();

        // ASSERT
        assertThat(dataRows(parseNdjson(body)).stream()
            .filter(row -> row.get("id").asText().equals(aufgabe.getId().getUUID().toString()))
            .count())
            .as("the Label join must not multiply the rows")
            .isEqualTo(1);
    }

    @Test
    @TestAsHelloDataSeedingBE
    void export_never_contains_free_text_of_the_aufgabe() {
        // ARRANGE
        seedAufgabe();

        // ACT
        String body = RestAssured.given().when().get(AUFGABEN_PATH)
            .then().statusCode(HttpURLConnection.HTTP_OK).extract().asString();

        // ASSERT
        assertThat(body)
            .as("titel and bemerkung may contain personal data and are not exported")
            .doesNotContain("titel", "bemerkung", "Basis Aufgabe", BEMERKUNG);
    }

    @Test
    @TestAsHelloDataSeedingBE
    @SneakyThrows
    void json_export_contains_the_same_rows_as_the_ndjson_export() {
        // ARRANGE
        seedAufgabe();

        // ACT
        Response response = RestAssured.given()
            .when()
            .get(AUFGABEN_JSON_PATH)
            .then()
            .assertThat()
            .statusCode(HttpURLConnection.HTTP_OK)
            .extract()
            .response();

        String ndjsonBody = RestAssured.given().when().get(AUFGABEN_PATH)
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
            .as("both formats export the same Aufgaben")
            .isNotEmpty()
            .isEqualTo(ndjsonIds);
    }

    @Test
    @TestAsHelloDataSeedingBE
    @SneakyThrows
    void json_envelope_matches_the_declared_contract_type() {
        // ARRANGE
        seedAufgabe();

        JsonNode declared = objectMapper.valueToTree(new HelloDataAufgabenEnvelopeJson(
            List.of(),
            new HelloDataMetaJson(
                HelloDataMetaJson.STATUS_COMPLETE,
                0,
                OffsetDateTime.parse("2026-01-01T00:00:00Z"))));

        // ACT
        String body = RestAssured.given().when().get(AUFGABEN_JSON_PATH)
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
        RestAssured.given().when().get(AUFGABEN_PATH).then().statusCode(HttpURLConnection.HTTP_OK);
        RestAssured.given().when().get(AUFGABEN_PATH).then().statusCode(HttpURLConnection.HTTP_OK);
    }

    @Test
    @TestAsHelloDataForeignTenant
    void export_refuses_a_tenant_other_than_BE() {
        RestAssured.given()
            .when()
            .get(AUFGABEN_PATH)
            .then()
            .assertThat()
            .statusCode(not(HttpURLConnection.HTTP_OK));
    }

    @Test
    @TestAsSupportBE
    void export_is_forbidden_without_the_hellodata_berechtigung() {
        RestAssured.given()
            .when()
            .get(AUFGABEN_PATH)
            .then()
            .assertThat()
            .statusCode(HttpURLConnection.HTTP_FORBIDDEN);
    }

    @Test
    void export_is_unreachable_without_authentication() {
        RestAssured.given()
            .when()
            .get(AUFGABEN_PATH)
            .then()
            .assertThat()
            .statusCode(HttpURLConnection.HTTP_UNAUTHORIZED);
    }

    @Test
    @TestAsSupportBE
    void json_export_is_forbidden_without_the_hellodata_berechtigung() {
        RestAssured.given()
            .when()
            .get(AUFGABEN_JSON_PATH)
            .then()
            .assertThat()
            .statusCode(HttpURLConnection.HTTP_FORBIDDEN);
    }

    @Test
    void json_export_is_unreachable_without_authentication() {
        RestAssured.given()
            .when()
            .get(AUFGABEN_JSON_PATH)
            .then()
            .assertThat()
            .statusCode(HttpURLConnection.HTTP_UNAUTHORIZED);
    }

    @NonNull
    private JsonNode rowOf(@NonNull String ndjsonBody, @NonNull Aufgabe aufgabe) {
        return dataRows(parseNdjson(ndjsonBody)).stream()
            .filter(row -> row.get("id").asText().equals(aufgabe.getId().getUUID().toString()))
            .findFirst()
            .orElseThrow(() -> new AssertionError("seeded Aufgabe missing from the export"));
    }

    @NonNull
    private static Kind requireNonNullKind(@NonNull Aufgabe aufgabe) {
        Kind kind = aufgabe.getKind();
        assertThat(kind).as("the seeded Aufgabe must have a Kind").isNotNull();
        return kind;
    }

    @NonNull
    private static Anlass requireNonNullAnlass(@NonNull Aufgabe aufgabe) {
        Anlass anlass = aufgabe.getAnlass();
        assertThat(anlass).as("the seeded Aufgabe must have an Anlass").isNotNull();
        return anlass;
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
     * The Benutzer is created without any Rolle because BE has more than one Organisation, which
     * rules out the fixtures relying on the single zentrale Organisation.
     */
    @NonNull
    @CanIgnoreReturnValue
    private Aufgabe seedAufgabe() {
        return seedAufgabeWithLabels(Collections.emptySet());
    }

    @NonNull
    @CanIgnoreReturnValue
    private Aufgabe seedAufgabeWithLabels(@NonNull Set<ID<Label>> labelIDs) {
        Benutzer benutzer = fixtureService.createBenutzerNoRoles();
        return fixtureService.createAufgabe(
            benutzer,
            null,
            null,
            labelIDs,
            AufgabeStatus.IN_BEARBEITUNG,
            FAELLIG_BIS,
            BEMERKUNG);
    }

    /**
     * Tenant BE is dezentral, so an Anlass is only valid with a zustaendiger Arzt (see
     * {@code arztRequiredForDezentraleTenants}) - hence the Benutzer is handed to the Anlass as
     * well.
     */
    @NonNull
    @CanIgnoreReturnValue
    private Aufgabe seedAufgabeWithKindAndAnlass() {
        Benutzer benutzer = fixtureService.createBenutzerNoRoles();
        Kind kind = fixtureService.createKind();
        Anlass anlass = fixtureService.createAnlass(AnlassStatus.IN_PLANUNG, benutzer);
        return fixtureService.createAufgabe(
            benutzer,
            kind,
            anlass,
            Collections.emptySet(),
            AufgabeStatus.IN_BEARBEITUNG,
            FAELLIG_BIS,
            BEMERKUNG);
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
