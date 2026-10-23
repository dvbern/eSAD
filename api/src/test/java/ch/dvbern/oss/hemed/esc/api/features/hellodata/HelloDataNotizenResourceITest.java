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
import ch.dvbern.oss.hemed.esc.api.features.hellodata.json.HelloDataNotizenEnvelopeJson;
import ch.dvbern.oss.hemed.esc.api.features.hellodata.json.HelloDataSentinelJson;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Dossier;
import ch.dvbern.oss.hemed.esc.api.features.labels.Label;
import ch.dvbern.oss.hemed.esc.api.features.notizen.Notiz;
import ch.dvbern.oss.hemed.esc.api.features.notizen.NotizTyp;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantEnum;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantContext;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import ch.dvbern.oss.hemed.esc.api.testutil.AbstractITest;
import ch.dvbern.oss.hemed.esc.api.testutil.FixtureService;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.FileDataBuilder;
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
class HelloDataNotizenResourceITest extends AbstractITest {

    private static final String NOTIZEN_PATH = "/hellodata/v1/ndjson/notizen";
    private static final String NOTIZEN_JSON_PATH = "/hellodata/v1/json/notizen";

    /**
     * Free text of the seeded Notiz. It must never show up in the export.
     */
    private static final String BEMERKUNG = "HelloData Bemerkung mit Personendaten";

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
    void export_contains_the_seeded_notiz() {
        // ARRANGE
        Notiz notiz = seedNotiz();

        // ACT
        Response response = RestAssured.given()
            .when()
            .get(NOTIZEN_PATH)
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
            .as("the seeded Notiz must appear in the export")
            .contains(notiz.getId().getUUID().toString());
    }

    @Test
    @TestAsHelloDataSeedingBE
    void export_ends_with_a_meta_line_whose_rowCount_matches() {
        // ARRANGE
        seedNotiz();

        // ACT
        String body = RestAssured.given()
            .when()
            .get(NOTIZEN_PATH)
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
        Notiz notiz = seedNotiz();

        // ACT
        String body = RestAssured.given().when().get(NOTIZEN_PATH)
            .then().statusCode(HttpURLConnection.HTTP_OK).extract().asString();

        // ASSERT
        JsonNode row = rowOf(body, notiz);

        assertThat(row.get("dossierId").asText())
            .isEqualTo(notiz.getDossier().getId().getUUID().toString());
        assertThat(row.get("verantwortlichePersonId").asText())
            .isEqualTo(notiz.getVerantwortlicher().getId().getUUID().toString());
        assertThat(row.get("status").asText())
            .isEqualTo(notiz.getStatus().name());
        assertThat(row.get("hatFileanhang").asBoolean())
            .as("the seeded Notiz has no file attached")
            .isFalse();
        assertThat(row.get("labels").asText())
            .as("a Notiz without Labels must carry an empty string, not null")
            .isEmpty();
    }

    @Test
    @TestAsHelloDataSeedingBE
    void exported_labels_carry_the_comma_separated_label_uuids() {
        // ARRANGE
        Notiz notiz = seedNotizWithLabels(Set.of(Label.ID_LABEL_IMPFAUSWEIS, Label.ID_LABEL_FRAGEBOGEN));
        String expectedLabels = Set.of(Label.ID_LABEL_IMPFAUSWEIS, Label.ID_LABEL_FRAGEBOGEN).stream()
            .map(labelID -> labelID.getUUID().toString())
            .sorted()
            .collect(Collectors.joining(","));

        // ACT
        String body = RestAssured.given().when().get(NOTIZEN_PATH)
            .then().statusCode(HttpURLConnection.HTTP_OK).extract().asString();

        // ASSERT
        assertThat(rowOf(body, notiz).get("labels").asText())
            .isEqualTo(expectedLabels);
    }

    @Test
    @TestAsHelloDataSeedingBE
    void exported_notiz_with_labels_appears_exactly_once() {
        // ARRANGE
        Notiz notiz = seedNotizWithLabels(Set.of(Label.ID_LABEL_IMPFAUSWEIS, Label.ID_LABEL_FRAGEBOGEN));

        // ACT
        String body = RestAssured.given().when().get(NOTIZEN_PATH)
            .then().statusCode(HttpURLConnection.HTTP_OK).extract().asString();

        // ASSERT
        assertThat(dataRows(parseNdjson(body)).stream()
            .filter(row -> row.get("id").asText().equals(notiz.getId().getUUID().toString()))
            .count())
            .as("the Label join must not multiply the rows")
            .isEqualTo(1);
    }

    @Test
    @TestAsHelloDataSeedingBE
    void hatFileanhang_is_true_once_a_file_has_been_uploaded() {
        // ARRANGE
        Notiz notiz = seedNotizWithFile();

        // ACT
        String body = RestAssured.given().when().get(NOTIZEN_PATH)
            .then().statusCode(HttpURLConnection.HTTP_OK).extract().asString();

        // ASSERT
        assertThat(rowOf(body, notiz).get("hatFileanhang").asBoolean())
            .isTrue();
    }

    @Test
    @TestAsHelloDataSeedingBE
    void export_never_contains_free_text_of_the_notiz() {
        // ARRANGE
        seedNotiz();

        // ACT
        String body = RestAssured.given().when().get(NOTIZEN_PATH)
            .then().statusCode(HttpURLConnection.HTTP_OK).extract().asString();

        // ASSERT
        assertThat(body)
            .as("titel and bemerkungen may contain personal data and are not exported")
            .doesNotContain("titel", "bemerkungen", "Base Notiz", BEMERKUNG);
    }

    @Test
    @TestAsHelloDataSeedingBE
    @SneakyThrows
    void json_export_contains_the_same_rows_as_the_ndjson_export() {
        // ARRANGE
        seedNotiz();

        // ACT
        Response response = RestAssured.given()
            .when()
            .get(NOTIZEN_JSON_PATH)
            .then()
            .assertThat()
            .statusCode(HttpURLConnection.HTTP_OK)
            .extract()
            .response();

        String ndjsonBody = RestAssured.given().when().get(NOTIZEN_PATH)
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
            .as("both formats export the same Notizen")
            .isNotEmpty()
            .isEqualTo(ndjsonIds);
    }

    @Test
    @TestAsHelloDataSeedingBE
    @SneakyThrows
    void json_envelope_matches_the_declared_contract_type() {
        // ARRANGE
        seedNotiz();

        JsonNode declared = objectMapper.valueToTree(new HelloDataNotizenEnvelopeJson(
            List.of(),
            new HelloDataMetaJson(
                HelloDataMetaJson.STATUS_COMPLETE,
                0,
                OffsetDateTime.parse("2026-01-01T00:00:00Z"))));

        // ACT
        String body = RestAssured.given().when().get(NOTIZEN_JSON_PATH)
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
        RestAssured.given().when().get(NOTIZEN_PATH).then().statusCode(HttpURLConnection.HTTP_OK);
        RestAssured.given().when().get(NOTIZEN_PATH).then().statusCode(HttpURLConnection.HTTP_OK);
    }

    @Test
    @TestAsHelloDataForeignTenant
    void export_refuses_a_tenant_other_than_BE() {
        RestAssured.given()
            .when()
            .get(NOTIZEN_PATH)
            .then()
            .assertThat()
            .statusCode(not(HttpURLConnection.HTTP_OK));
    }

    @Test
    @TestAsSupportBE
    void export_is_forbidden_without_the_hellodata_berechtigung() {
        RestAssured.given()
            .when()
            .get(NOTIZEN_PATH)
            .then()
            .assertThat()
            .statusCode(HttpURLConnection.HTTP_FORBIDDEN);
    }

    @Test
    void export_is_unreachable_without_authentication() {
        RestAssured.given()
            .when()
            .get(NOTIZEN_PATH)
            .then()
            .assertThat()
            .statusCode(HttpURLConnection.HTTP_UNAUTHORIZED);
    }

    @Test
    @TestAsSupportBE
    void json_export_is_forbidden_without_the_hellodata_berechtigung() {
        RestAssured.given()
            .when()
            .get(NOTIZEN_JSON_PATH)
            .then()
            .assertThat()
            .statusCode(HttpURLConnection.HTTP_FORBIDDEN);
    }

    @Test
    void json_export_is_unreachable_without_authentication() {
        RestAssured.given()
            .when()
            .get(NOTIZEN_JSON_PATH)
            .then()
            .assertThat()
            .statusCode(HttpURLConnection.HTTP_UNAUTHORIZED);
    }

    @NonNull
    private JsonNode rowOf(@NonNull String ndjsonBody, @NonNull Notiz notiz) {
        return dataRows(parseNdjson(ndjsonBody)).stream()
            .filter(row -> row.get("id").asText().equals(notiz.getId().getUUID().toString()))
            .findFirst()
            .orElseThrow(() -> new AssertionError("seeded Notiz missing from the export"));
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
    private Notiz seedNotiz() {
        return seedNotizWithLabels(Set.of());
    }

    /**
     * Tenant BE is dezentral, so a Dossier is only valid with a zustaendiger Arzt (see
     * {@code arztRequiredForDezentraleTenants}). The Benutzer is created without any Rolle
     * because BE has more than one Organisation, which rules out the fixtures relying on the
     * single zentrale Organisation.
     */
    @NonNull
    @CanIgnoreReturnValue
    private Notiz seedNotizWithLabels(@NonNull Set<ID<Label>> labelIDs) {
        Benutzer arzt = fixtureService.createBenutzerNoRoles();
        Dossier dossier = fixtureService.createDossier(arzt);
        return fixtureService.createNotiz(dossier, labelIDs, NotizTyp.DEFAULT, BEMERKUNG);
    }

    /**
     * The export only reports whether there is at least one file, so a FileData row attached to
     * the Notiz is enough - no upload through the file storage is needed.
     */
    @NonNull
    @CanIgnoreReturnValue
    private Notiz seedNotizWithFile() {
        Notiz notiz = seedNotiz();
        return fixtureService.addFileToNotiz(notiz.getId(), FileDataBuilder.defaultPdfFileData());
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
