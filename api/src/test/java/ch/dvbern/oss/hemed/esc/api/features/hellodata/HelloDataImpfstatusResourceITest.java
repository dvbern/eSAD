package ch.dvbern.oss.hemed.esc.api.features.hellodata;

import java.net.HttpURLConnection;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import ch.dvbern.oss.hemed.esc.api.features.anlass.Anlass;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfungen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.types.ImpfschutzGegeben;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.AnlassTyp;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.hellodata.json.HelloDataImpfstatusEnvelopeJson;
import ch.dvbern.oss.hemed.esc.api.features.hellodata.json.HelloDataImpfstatusKrankheitJson;
import ch.dvbern.oss.hemed.esc.api.features.hellodata.json.HelloDataMetaJson;
import ch.dvbern.oss.hemed.esc.api.features.hellodata.json.HelloDataSentinelJson;
import ch.dvbern.oss.hemed.esc.api.features.impfstoff.types.Krankheit;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Dossier;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantEnum;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantContext;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import ch.dvbern.oss.hemed.esc.api.testutil.AbstractITest;
import ch.dvbern.oss.hemed.esc.api.testutil.FixtureService;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsHelloDataForeignTenant;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsHelloDataSeedingBE;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsSchularztBE;
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
class HelloDataImpfstatusResourceITest extends AbstractITest {

    private static final String IMPFSTATUS_PATH = "/hellodata/v1/ndjson/impfstatuskrankheiten";
    private static final String IMPFSTATUS_JSON_PATH = "/hellodata/v1/json/impfstatuskrankheiten";

    private final FixtureService fixtureService;
    private final TenantContext beTenantContext;
    private final ObjectMapper objectMapper;

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
        beTenantContext.setCurrentTenant(TenantEnum.BE);
    }

    @Test
    @TestAsHelloDataSeedingBE
    void impfstatus_export_returns_rows_of_tenant_BE() {
        // ARRANGE
        Untersuchung untersuchung = seedUntersuchungWithImpfstatus();
        UUID expectedUntersuchungId = untersuchung.getId().getUUID();
        UUID expectedImpfungenId = untersuchung.getUntersuchungImpfungen().getId().getUUID();

        // ACT
        Response response = RestAssured.given()
            .when()
            .get(IMPFSTATUS_PATH)
            .then()
            .assertThat()
            .statusCode(HttpURLConnection.HTTP_OK)
            .extract()
            .response();

        // ASSERT
        assertThat(response.getContentType()).contains(NdjsonWriter.MEDIA_TYPE);

        List<JsonNode> lines = parseNdjson(response.asString());
        List<JsonNode> rows = dataRows(lines);
        assertThat(rows).isNotEmpty();

        List<JsonNode> matchingRows = rows.stream()
            .filter(r -> r.get("untersuchungId").asText().equals(expectedUntersuchungId.toString()))
            .toList();

        assertThat(matchingRows).isNotEmpty();
        assertThat(matchingRows.getFirst().get("untersuchungImpfungenId").asText())
            .isEqualTo(expectedImpfungenId.toString());
    }

    @Test
    @TestAsHelloDataSeedingBE
    void impfstatus_export_ends_with_a_meta_line_whose_rowCount_matches() {
        // ARRANGE
        seedUntersuchungWithImpfstatus();

        // ACT
        String body = RestAssured.given()
            .when()
            .get(IMPFSTATUS_PATH)
            .then()
            .assertThat()
            .statusCode(HttpURLConnection.HTTP_OK)
            .extract()
            .asString();

        // ASSERT
        List<JsonNode> lines = parseNdjson(body);
        assertThat(lines).isNotEmpty();

        JsonNode lastLine = lines.getLast();
        assertThat(lastLine.has(HelloDataSentinelJson.META_KEY)).isTrue();

        JsonNode meta = lastLine.get(HelloDataSentinelJson.META_KEY);
        assertThat(meta.get("status").asText()).isEqualTo(HelloDataMetaJson.STATUS_COMPLETE);
        assertThat(meta.get("exportedAt").asText()).isNotBlank();
        assertThat(meta.get("rowCount").asLong())
            .isEqualTo(dataRows(lines).size());
    }

    @Test
    @TestAsHelloDataSeedingBE
    void exported_impfstatus_fields_carry_the_values_of_their_own_column() {
        // ARRANGE
        Untersuchung untersuchung = seedUntersuchungWithImpfstatus();
        UUID expectedUntersuchungId = untersuchung.getId().getUUID();
        UUID expectedImpfungenId = untersuchung.getUntersuchungImpfungen().getId().getUUID();

        // ACT
        String body = RestAssured.given().when().get(IMPFSTATUS_PATH)
            .then().statusCode(HttpURLConnection.HTTP_OK).extract().asString();

        // ASSERT
        JsonNode row = dataRows(parseNdjson(body)).stream()
            .filter(r -> r.get("untersuchungId").asText().equals(expectedUntersuchungId.toString())
                && Krankheit.MASERN.name().equals(r.get("krankheit").asText()))
            .findFirst()
            .orElseThrow(() -> new AssertionError("seeded Untersuchung missing from the export"));

        assertThat(row.get("id").asText()).isNotBlank();
        assertThat(row.get("untersuchungImpfungenId").asText()).isEqualTo(expectedImpfungenId.toString());
        assertThat(row.get("krankheit").asText()).isEqualTo(Krankheit.MASERN.name());
        assertThat(row.get("impfschutzGegeben").asText()).isEqualTo(ImpfschutzGegeben.JA.name());
        assertThat(row.get("datumLetzteImpfung").asText()).isEqualTo("2025-05-10");
    }

    @Test
    @TestAsHelloDataSeedingBE
    void exported_impfstatus_without_explicit_krankheiten_has_default_entries() {
        // ARRANGE
        Untersuchung untersuchung = seedUntersuchungWithoutImpfstatus();
        UUID expectedUntersuchungId = untersuchung.getId().getUUID();
        UUID expectedImpfungenId = untersuchung.getUntersuchungImpfungen().getId().getUUID();

        // ACT
        String body = RestAssured.given().when().get(IMPFSTATUS_PATH)
            .then().statusCode(HttpURLConnection.HTTP_OK).extract().asString();

        // ASSERT
        List<JsonNode> rows = dataRows(parseNdjson(body)).stream()
            .filter(r -> r.get("untersuchungId").asText().equals(expectedUntersuchungId.toString()))
            .toList();

        assertThat(rows).hasSize(Krankheit.values().length)
            .allMatch(r -> r.get("untersuchungImpfungenId").asText().equals(expectedImpfungenId.toString()))
            .allMatch(r -> r.get("impfschutzGegeben").asText().equals(ImpfschutzGegeben.KEINE_ANGABE.name()));
    }

    @Test
    @TestAsHelloDataForeignTenant
    void impfstatus_export_refuses_a_tenant_other_than_BE() {
        RestAssured.given()
            .when()
            .get(IMPFSTATUS_PATH)
            .then()
            .assertThat()
            .statusCode(not(HttpURLConnection.HTTP_OK));
    }

    @Test
    @TestAsSupportBE
    void impfstatus_export_is_forbidden_without_the_hellodata_berechtigung() {
        RestAssured.given()
            .when()
            .get(IMPFSTATUS_PATH)
            .then()
            .assertThat()
            .statusCode(HttpURLConnection.HTTP_FORBIDDEN);
    }

    @Test
    void impfstatus_export_is_unreachable_without_authentication() {
        RestAssured.given()
            .when()
            .get(IMPFSTATUS_PATH)
            .then()
            .assertThat()
            .statusCode(HttpURLConnection.HTTP_UNAUTHORIZED);
    }

    @Test
    @TestAsHelloDataSeedingBE
    void json_impfstatus_export_carries_the_published_structure() throws Exception {
        // ARRANGE
        seedUntersuchungWithImpfstatus();

        JsonNode declared = objectMapper.readTree(objectMapper.writeValueAsString(
            new HelloDataImpfstatusEnvelopeJson(
                List.of(new HelloDataImpfstatusKrankheitJson(
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    Krankheit.MASERN,
                    1,
                    LocalDate.of(2025, 1, 1),
                    ImpfschutzGegeben.JA,
                    false,
                    false
                )),
                new HelloDataMetaJson(
                    HelloDataMetaJson.STATUS_COMPLETE,
                    1L,
                    OffsetDateTime.parse("2026-01-01T00:00:00Z")
                )
            )
        ));

        // ACT
        String body = RestAssured.given().when().get(IMPFSTATUS_JSON_PATH)
            .then().statusCode(HttpURLConnection.HTTP_OK).extract().asString();

        // ASSERT
        JsonNode actual = objectMapper.readTree(body);

        assertThat(fieldNamesOf(actual))
            .as("the response must have exactly the members the published contract declares")
            .isEqualTo(fieldNamesOf(declared));

        assertThat(fieldNamesOf(actual.get(HelloDataSentinelJson.META_KEY)))
            .as("the _meta payload must match HelloDataMetaJson")
            .isEqualTo(fieldNamesOf(declared.get(HelloDataSentinelJson.META_KEY)));

        assertThat(actual.get("data").isArray()).isTrue();
        assertThat(actual.get("data").size()).isGreaterThanOrEqualTo(1);
    }

    @Test
    @TestAsHelloDataForeignTenant
    void json_impfstatus_export_refuses_a_tenant_other_than_BE() {
        RestAssured.given()
            .when()
            .get(IMPFSTATUS_JSON_PATH)
            .then()
            .assertThat()
            .statusCode(not(HttpURLConnection.HTTP_OK));
    }

    @Test
    @TestAsSupportBE
    void json_impfstatus_export_is_forbidden_without_the_hellodata_berechtigung() {
        RestAssured.given()
            .when()
            .get(IMPFSTATUS_JSON_PATH)
            .then()
            .assertThat()
            .statusCode(HttpURLConnection.HTTP_FORBIDDEN);
    }

    @Test
    void json_impfstatus_export_is_unreachable_without_authentication() {
        RestAssured.given()
            .when()
            .get(IMPFSTATUS_JSON_PATH)
            .then()
            .assertThat()
            .statusCode(HttpURLConnection.HTTP_UNAUTHORIZED);
    }

    private static List<String> fieldNamesOf(JsonNode node) {
        assertThat(node).isNotNull();
        List<String> names = new ArrayList<>();
        node.fieldNames().forEachRemaining(names::add);
        return names.stream().sorted().toList();
    }

    private Untersuchung seedUntersuchungWithImpfstatus() {
        Benutzer arzt = fixtureService.readBenutzerWithBerechtigungen(TestAsSchularztBE.BENUTZER_ID_ID);
        Anlass anlass = fixtureService.createAnlass(AnlassTyp.KLASSE_4, arzt);
        Dossier dossier = fixtureService.createDossier(arzt);
        Einladung einladung = fixtureService.createEinladung(anlass, dossier);
        Untersuchung untersuchung = fixtureService.createUntersuchungWithImpfungen(einladung);
        UntersuchungImpfungen impfungen = untersuchung.getUntersuchungImpfungen();
        assertThat(impfungen).isNotNull();
        fixtureService.createOrUpdateUntersuchungImpfstatusKrankheit(
            impfungen,
            Krankheit.MASERN,
            true,
            LocalDate.of(2025, 5, 10)
        );
        return untersuchung;
    }

    private Untersuchung seedUntersuchungWithoutImpfstatus() {
        Benutzer arzt = fixtureService.readBenutzerWithBerechtigungen(TestAsSchularztBE.BENUTZER_ID_ID);
        Anlass anlass = fixtureService.createAnlass(AnlassTyp.KLASSE_4, arzt);
        Dossier dossier = fixtureService.createDossier(arzt);
        Einladung einladung = fixtureService.createEinladung(anlass, dossier);
        return fixtureService.createUntersuchungWithImpfungen(einladung);
    }

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
