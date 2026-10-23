package ch.dvbern.oss.hemed.esc.api.features.hellodata;

import java.net.HttpURLConnection;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import ch.dvbern.oss.hemed.esc.api.features.anlass.Anlass;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfungIntern;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfungInternService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfungen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.types.ImpfStatus;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.AnlassTyp;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.hellodata.json.HelloDataImpfungInternJson;
import ch.dvbern.oss.hemed.esc.api.features.hellodata.json.HelloDataImpfungInternKrankheitImmunitaetJson;
import ch.dvbern.oss.hemed.esc.api.features.hellodata.json.HelloDataImpfungenInternEnvelopeJson;
import ch.dvbern.oss.hemed.esc.api.features.hellodata.json.HelloDataMetaJson;
import ch.dvbern.oss.hemed.esc.api.features.hellodata.json.HelloDataSentinelJson;
import ch.dvbern.oss.hemed.esc.api.features.impfstoff.Impfstoff;
import ch.dvbern.oss.hemed.esc.api.features.impfstoff.types.Krankheit;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Dossier;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantEnum;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantContext;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import ch.dvbern.oss.hemed.esc.api.testutil.AbstractITest;
import ch.dvbern.oss.hemed.esc.api.testutil.FixtureService;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungImpfungInternBuilder;
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
class HelloDataImpfungenInternResourceITest extends AbstractITest {

    private static final String IMPFUNGEN_INTERN_PATH = "/hellodata/v1/ndjson/impfungenintern";
    private static final String IMPFUNGEN_INTERN_JSON_PATH = "/hellodata/v1/json/impfungenintern";

    private final UntersuchungImpfungInternService untersuchungImpfungInternService;
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
    void export_returns_the_impfungenintern_of_tenant_BE() {
        // ARRANGE
        UntersuchungImpfungIntern impfungIntern = seedUntersuchungWithImpfungIntern();
        UUID expectedId = impfungIntern.getId().getUUID();

        // ACT
        Response response = RestAssured.given()
            .when()
            .get(IMPFUNGEN_INTERN_PATH)
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
            .contains(expectedId.toString());
    }

    @Test
    @TestAsHelloDataSeedingBE
    void export_ends_with_a_meta_line_whose_rowCount_matches() {
        // ARRANGE
        seedUntersuchungWithImpfungIntern();

        // ACT
        String body = RestAssured.given()
            .when()
            .get(IMPFUNGEN_INTERN_PATH)
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
    void exported_impfungintern_fields_carry_the_values_of_their_own_column() {
        // ARRANGE
        UntersuchungImpfungIntern impfungIntern = seedUntersuchungWithImpfungIntern();
        UUID expectedId = impfungIntern.getId().getUUID();
        UUID expectedUntersuchungId = impfungIntern.getUntersuchungImpfungen().getUntersuchung().getId().getUUID();
        UUID expectedImpfungenId = impfungIntern.getUntersuchungImpfungen().getId().getUUID();
        UUID expectedImpfstoffId = impfungIntern.getImpfstoff().getId().getUUID();
        UUID expectedAnlassErfasstId = impfungIntern.getAnlassImpfungErfasst().getId().getUUID();

        // ACT
        String body = RestAssured.given().when().get(IMPFUNGEN_INTERN_PATH)
            .then().statusCode(HttpURLConnection.HTTP_OK).extract().asString();

        // ASSERT
        JsonNode row = dataRows(parseNdjson(body)).stream()
            .filter(r -> r.get("id").asText().equals(expectedId.toString()))
            .findFirst()
            .orElseThrow(() -> new AssertionError("seeded UntersuchungImpfungIntern missing from the export"));

        assertThat(row.get("untersuchungId").asText()).isEqualTo(expectedUntersuchungId.toString());
        assertThat(row.get("untersuchungImpfungenId").asText()).isEqualTo(expectedImpfungenId.toString());
        assertThat(row.get("impfstoffId").asText()).isEqualTo(expectedImpfstoffId.toString());
        assertThat(row.get("impfStatus").asText()).isEqualTo(impfungIntern.getImpfStatus().name());
        assertThat(row.get("anlassImpfungErfasstId").asText()).isEqualTo(expectedAnlassErfasstId.toString());

        JsonNode immunitaeten = row.get("krankheitenImmunitaeten");
        assertThat(immunitaeten).isNotNull();
        assertThat(immunitaeten.isArray()).isTrue();
        assertThat(immunitaeten.size()).isGreaterThanOrEqualTo(1);

        JsonNode hepB = null;
        for (JsonNode k : immunitaeten) {
            if (Krankheit.HEPATITIS_B.name().equals(k.get("krankheit").asText())) {
                hepB = k;
                break;
            }
        }
        assertThat(hepB).isNotNull();
        assertThat(hepB.get("immunitaetGenuegend").asBoolean()).isTrue();
    }

    @Test
    @TestAsHelloDataSeedingBE
    void exported_impfungintern_aggregates_multiple_diseases_correctly() {
        // ARRANGE
        Benutzer arzt = fixtureService.readBenutzerWithBerechtigungen(TestAsSchularztBE.BENUTZER_ID_ID);
        Anlass anlass = fixtureService.createAnlass(AnlassTyp.KLASSE_4, arzt);
        Dossier dossier = fixtureService.createDossier(arzt);
        Einladung einladung = fixtureService.createEinladung(anlass, dossier);
        Untersuchung untersuchung = fixtureService.createUntersuchungWithImpfungen(einladung);
        UntersuchungImpfungen impfungen = untersuchung.getUntersuchungImpfungen();
        assertThat(impfungen).isNotNull();

        Impfstoff impfstoff = fixtureService.createImpfstoff(Krankheit.MASERN, Krankheit.MUMPS, Krankheit.ROETELN);
        UntersuchungImpfungIntern entity = UntersuchungImpfungInternBuilder.create(builder -> builder
            .forUntersuchungImpfungen(impfungen)
            .withImpfstoff(impfstoff)
            .withImmunitaetForKrankheit(Krankheit.MASERN, true)
            .withImmunitaetForKrankheit(Krankheit.MUMPS, false)
            .withImmunitaetForKrankheit(Krankheit.ROETELN, true)
            .withUserErfasst(arzt)
            .withImpfStatus(ImpfStatus.DURCHGEFUEHRT));
        UntersuchungImpfungIntern saved = untersuchungImpfungInternService.persist(entity);

        // ACT
        String body = RestAssured.given().when().get(IMPFUNGEN_INTERN_PATH)
            .then().statusCode(HttpURLConnection.HTTP_OK).extract().asString();

        // ASSERT
        JsonNode row = dataRows(parseNdjson(body)).stream()
            .filter(r -> r.get("id").asText().equals(saved.getId().getUUID().toString()))
            .findFirst()
            .orElseThrow(() -> new AssertionError("seeded UntersuchungImpfungIntern missing from the export"));

        JsonNode immunitaeten = row.get("krankheitenImmunitaeten");
        assertThat(immunitaeten).isNotNull();
        assertThat(immunitaeten.isArray()).isTrue();
        assertThat(immunitaeten).hasSize(3);

        assertThat(immunitaeten.get(0).get("krankheit").asText()).isEqualTo(Krankheit.MASERN.name());
        assertThat(immunitaeten.get(0).get("immunitaetGenuegend").asBoolean()).isTrue();

        assertThat(immunitaeten.get(1).get("krankheit").asText()).isEqualTo(Krankheit.MUMPS.name());
        assertThat(immunitaeten.get(1).get("immunitaetGenuegend").asBoolean()).isFalse();

        assertThat(immunitaeten.get(2).get("krankheit").asText()).isEqualTo(Krankheit.ROETELN.name());
        assertThat(immunitaeten.get(2).get("immunitaetGenuegend").asBoolean()).isTrue();
    }

    @Test
    @TestAsHelloDataForeignTenant
    void impfungenintern_export_refuses_a_tenant_other_than_BE() {
        RestAssured.given()
            .when()
            .get(IMPFUNGEN_INTERN_PATH)
            .then()
            .assertThat()
            .statusCode(not(HttpURLConnection.HTTP_OK));
    }

    @Test
    @TestAsSupportBE
    void impfungenintern_export_is_forbidden_without_the_hellodata_berechtigung() {
        RestAssured.given()
            .when()
            .get(IMPFUNGEN_INTERN_PATH)
            .then()
            .assertThat()
            .statusCode(HttpURLConnection.HTTP_FORBIDDEN);
    }

    @Test
    void impfungenintern_export_is_unreachable_without_authentication() {
        RestAssured.given()
            .when()
            .get(IMPFUNGEN_INTERN_PATH)
            .then()
            .assertThat()
            .statusCode(HttpURLConnection.HTTP_UNAUTHORIZED);
    }

    @Test
    @TestAsHelloDataSeedingBE
    void json_impfungenintern_export_carries_the_published_structure() throws Exception {
        // ARRANGE
        seedUntersuchungWithImpfungIntern();

        JsonNode declared = objectMapper.readTree(objectMapper.writeValueAsString(
            new HelloDataImpfungenInternEnvelopeJson(
                List.of(new HelloDataImpfungInternJson(
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    null,
                    LocalDate.of(2025, 1, 1),
                    ImpfStatus.DURCHGEFUEHRT,
                    UUID.randomUUID(),
                    null,
                    List.of(new HelloDataImpfungInternKrankheitImmunitaetJson(
                        Krankheit.HEPATITIS_B,
                        true
                    ))
                )),
                new HelloDataMetaJson(
                    HelloDataMetaJson.STATUS_COMPLETE,
                    1L,
                    OffsetDateTime.parse("2026-01-01T00:00:00Z")
                )
            )
        ));

        // ACT
        String body = RestAssured.given().when().get(IMPFUNGEN_INTERN_JSON_PATH)
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
    void json_impfungenintern_export_refuses_a_tenant_other_than_BE() {
        RestAssured.given()
            .when()
            .get(IMPFUNGEN_INTERN_JSON_PATH)
            .then()
            .assertThat()
            .statusCode(not(HttpURLConnection.HTTP_OK));
    }

    @Test
    @TestAsSupportBE
    void json_impfungenintern_export_is_forbidden_without_the_hellodata_berechtigung() {
        RestAssured.given()
            .when()
            .get(IMPFUNGEN_INTERN_JSON_PATH)
            .then()
            .assertThat()
            .statusCode(HttpURLConnection.HTTP_FORBIDDEN);
    }

    @Test
    void json_impfungenintern_export_is_unreachable_without_authentication() {
        RestAssured.given()
            .when()
            .get(IMPFUNGEN_INTERN_JSON_PATH)
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

    private UntersuchungImpfungIntern seedUntersuchungWithImpfungIntern() {
        Benutzer arzt = fixtureService.readBenutzerWithBerechtigungen(TestAsSchularztBE.BENUTZER_ID_ID);
        Anlass anlass = fixtureService.createAnlass(AnlassTyp.KLASSE_4, arzt);
        Dossier dossier = fixtureService.createDossier(arzt);
        Einladung einladung = fixtureService.createEinladung(anlass, dossier);
        Untersuchung untersuchung = fixtureService.createUntersuchungWithImpfungen(einladung);
        UntersuchungImpfungen impfungen = untersuchung.getUntersuchungImpfungen();
        assertThat(impfungen).isNotNull();
        Impfstoff impfstoff = fixtureService.createImpfstoff(Krankheit.HEPATITIS_B);
        UntersuchungImpfungIntern entity = UntersuchungImpfungInternBuilder.create(builder -> builder
            .forUntersuchungImpfungen(impfungen)
            .withImpfstoff(impfstoff)
            .withImmunitaetForKrankheit(Krankheit.HEPATITIS_B, true)
            .withUserErfasst(arzt)
            .withImpfStatus(ImpfStatus.DURCHGEFUEHRT));
        return untersuchungImpfungInternService.persist(entity);
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
