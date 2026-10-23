package ch.dvbern.oss.hemed.esc.api.features.hellodata;

import java.net.HttpURLConnection;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import ch.dvbern.dvbstarter.shared.clock.AppClock;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Anlass;
import ch.dvbern.oss.hemed.esc.api.features.anlass.AnlassService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.AnlassConfig;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.AnlassConfigService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.EinladungService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.Audiogramm;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.UntersuchungHoerenAudiogramm;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.UntersuchungHoerenAudiogrammRepo;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.UntersuchungHoerenAudiogrammService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.types.AudiogrammBeurteilung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.types.AudiogrammSimpel;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.types.Dezibel;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.types.HoerenEmpfehlung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.types.HoerenHoerhilfe;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.types.HoerenZusaetzlicheAngaben;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfstatusKrankheitRepo;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfungen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfungenNurUnvollstaendige;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfungenNurUnvollstaendigeRepo;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfungenNurUnvollstaendigeService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfungenRepo;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.types.ImpfausweisGrundUnvollstaendig;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.types.ImpfausweisVollstaendig;
import ch.dvbern.oss.hemed.esc.api.features.anlass.kind.UntersuchungKindDaten;
import ch.dvbern.oss.hemed.esc.api.features.anlass.kind.UntersuchungKindDatenService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.massnahmen.UntersuchungImpfungenMassnahmen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.massnahmen.UntersuchungImpfungenMassnahmenRepo;
import ch.dvbern.oss.hemed.esc.api.features.anlass.massnahmen.UntersuchungImpfungenMassnahmenService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.massnahmen.types.InformationEltern;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.AnlassTyp;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.UntersuchungRepo;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.UntersuchungService;
import ch.dvbern.oss.hemed.esc.api.features.hellodata.json.HelloDataMetaJson;
import ch.dvbern.oss.hemed.esc.api.features.hellodata.json.HelloDataSchulenEnvelopeJson;
import ch.dvbern.oss.hemed.esc.api.features.hellodata.json.HelloDataSentinelJson;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Dossier;
import ch.dvbern.oss.hemed.esc.api.features.kinder.DossierService;
import ch.dvbern.oss.hemed.esc.api.features.schule.Schule;
import ch.dvbern.oss.hemed.esc.api.features.schule.SchuleService;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantEnum;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantContext;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import ch.dvbern.oss.hemed.esc.api.shared.valueenhanced.MultiValueEnhanced;
import ch.dvbern.oss.hemed.esc.api.shared.valueenhanced.SingleValueEnhanced;
import ch.dvbern.oss.hemed.esc.api.testutil.AbstractITest;
import ch.dvbern.oss.hemed.esc.api.testutil.FixtureService;
import ch.dvbern.oss.hemed.esc.api.testutil.TestUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.AnlassBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.AudiogrammBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.DossierBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.EinladungBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.SchuleBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsHelloDataBE;
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
class HelloDataResourceITest extends AbstractITest {

    private static final String SCHULEN_PATH = "/hellodata/v1/ndjson/schulen";
    private static final String SCHULEN_JSON_PATH = "/hellodata/v1/json/schulen";
    private static final String UNTERSUCHUNGEN_PATH = "/hellodata/v1/ndjson/untersuchungen";
    private static final String UNTERSUCHUNGEN_JSON_PATH = "/hellodata/v1/json/untersuchungen";

    private final SchuleService schuleService;
    private final FixtureService fixtureService;
    private final TenantContext beTenantContext;
    // the CDI bean, not a bare new ObjectMapper(): it carries the application's Jackson
    // configuration, so assertions here see exactly what HelloData receives
    private final ObjectMapper objectMapper;
    private final AnlassService anlassService;
    private final AnlassConfigService anlassConfigService;
    private final EinladungService einladungService;
    private final UntersuchungService untersuchungService;
    private final UntersuchungHoerenAudiogrammRepo untersuchungHoerenAudiogrammRepo;
    private final UntersuchungHoerenAudiogrammService untersuchungHoerenAudiogrammService;
    private final UntersuchungKindDatenService untersuchungKindDatenService;
    private final UntersuchungImpfungenRepo untersuchungImpfungenRepo;
    private final UntersuchungImpfungenNurUnvollstaendigeRepo untersuchungImpfungenNurUnvollstaendigeRepo;
    private final UntersuchungImpfungenNurUnvollstaendigeService untersuchungImpfungenNurUnvollstaendigeService;
    private final UntersuchungImpfungenMassnahmenRepo untersuchungImpfungenMassnahmenRepo;
    private final UntersuchungImpfungenMassnahmenService untersuchungImpfungenMassnahmenService;
    private final UntersuchungImpfstatusKrankheitRepo untersuchungImpfstatusKrankheitRepo;
    private final UntersuchungRepo untersuchungRepo;
    private final DossierService dossierService;
    private final AppClock appClock;

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
        // AbstractITest sets BS - but HelloData reads exclusively in tenant BE.
        beTenantContext.setCurrentTenant(TenantEnum.BE);
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
    @SneakyThrows
    @TestAsHelloDataSeedingBE
    void export_untersuchungen_returns_ndjson_with_meta_and_anonymized_data() {
        // ARRANGE
        Benutzer arzt = fixtureService.readBenutzerWithBerechtigungen(TestAsSchularztBE.BENUTZER_ID_ID);
        AnlassConfig config = anlassConfigService.getByAnlassTypAndStichtag(
            AnlassTyp.KLASSE_8,
            appClock.offsetDateTime().now().toLocalDate());
        Anlass anlass = anlassService.persist(AnlassBuilder.create(builder -> builder
            .withTitle("HelloData Test Anlass NDJSON")
            .withArzt(arzt)
            .withAnlassConfig(config)));
        Dossier dossier = dossierService.persist(DossierBuilder.create(builder -> builder
            .forKind(fixtureService.createKind())
            .withZustaendigArzt(arzt)));
        Einladung einladung = einladungService.persist(EinladungBuilder.create(builder -> builder
            .withAnlass(anlass)
            .withDossier(dossier)));
        Untersuchung untersuchung = untersuchungService.getOrCreateUntersuchung(einladung.getId());
        UntersuchungKindDaten kd = untersuchungKindDatenService.persist(
            UntersuchungKindDaten.ofKind(dossier.getKind()));
        untersuchung.setUntersuchungKindDaten(kd);
        fixtureService.executeSql(
            "UPDATE untersuchung SET untersuchungkinddaten_id = '" + kd.getId().getUUID()
                + "' WHERE id = '" + untersuchung.getId().getUUID() + "'");

        // ACT
        Response response = RestAssured.given()
            .when()
            .get(UNTERSUCHUNGEN_PATH)
            .then()
            .assertThat()
            .statusCode(HttpURLConnection.HTTP_OK)
            .extract()
            .response();

        // ASSERT
        assertThat(response.getContentType()).contains(NdjsonWriter.MEDIA_TYPE);

        String body = response.asString();
        List<JsonNode> lines = parseNdjson(body);
        assertThat(lines).isNotEmpty();

        List<JsonNode> data = dataRows(lines);
        Set<String> exportedIds = data.stream()
            .map(row -> row.get("id").asText())
            .collect(Collectors.toSet());
        assertThat(exportedIds).contains(untersuchung.getId().getUUID().toString());

        // Check metadata sentinel
        JsonNode lastLine = lines.getLast();
        assertThat(lastLine.has(HelloDataSentinelJson.META_KEY)).isTrue();
        JsonNode meta = lastLine.get(HelloDataSentinelJson.META_KEY);
        assertThat(meta.get("status").asText()).isEqualTo(HelloDataMetaJson.STATUS_COMPLETE);
        assertThat(meta.get("rowCount").asLong()).isEqualTo(data.size());

        // Verify anonymization: ensure sensitive PII fields are never emitted
        assertThat(body).doesNotContain("\"vorname\"")
            .doesNotContain("\"nachname\"")
            .doesNotContain("\"geburtsdatum\"")
            .contains("\"geschlecht\"");
    }

    @Test
    @SneakyThrows
    @TestAsHelloDataSeedingBE
    void export_untersuchungen_returns_json_envelope_with_meta_and_anonymized_data() {
        // ARRANGE
        Benutzer arzt = fixtureService.readBenutzerWithBerechtigungen(TestAsSchularztBE.BENUTZER_ID_ID);
        AnlassConfig config = anlassConfigService.getByAnlassTypAndStichtag(
            AnlassTyp.KLASSE_8,
            appClock.offsetDateTime().now().toLocalDate());
        Anlass anlass = anlassService.persist(AnlassBuilder.create(builder -> builder
            .withTitle("HelloData Test Anlass")
            .withArzt(arzt)
            .withAnlassConfig(config)));
        Dossier dossier = dossierService.persist(DossierBuilder.create(builder -> builder
            .forKind(fixtureService.createKind())
            .withZustaendigArzt(arzt)));
        Einladung einladung = einladungService.persist(EinladungBuilder.create(builder -> builder
            .withAnlass(anlass)
            .withDossier(dossier)));
        Untersuchung untersuchung = untersuchungService.getOrCreateUntersuchung(einladung.getId());
        UntersuchungKindDaten kd = untersuchungKindDatenService.persist(
            UntersuchungKindDaten.ofKind(dossier.getKind()));
        untersuchung.setUntersuchungKindDaten(kd);
        fixtureService.executeSql(
            "UPDATE untersuchung SET untersuchungkinddaten_id = '" + kd.getId().getUUID()
                + "' WHERE id = '" + untersuchung.getId().getUUID() + "'");

        // ACT
        Response response = RestAssured.given()
            .when()
            .get(UNTERSUCHUNGEN_JSON_PATH)
            .then()
            .assertThat()
            .statusCode(HttpURLConnection.HTTP_OK)
            .extract()
            .response();

        // ASSERT
        assertThat(response.getContentType()).contains(JsonEnvelopeWriter.MEDIA_TYPE);

        String body = response.asString();
        JsonNode root = objectMapper.readTree(body);

        assertThat(root.has("data")).isTrue();
        JsonNode dataArray = root.get("data");
        assertThat(dataArray.isArray()).isTrue();

        Set<String> exportedIds = idsOf(dataArray);
        assertThat(exportedIds).contains(untersuchung.getId().getUUID().toString());

        // Check metadata sentinel
        assertThat(root.has(HelloDataSentinelJson.META_KEY)).isTrue();
        JsonNode meta = root.get(HelloDataSentinelJson.META_KEY);
        assertThat(meta.get("status").asText()).isEqualTo(HelloDataMetaJson.STATUS_COMPLETE);
        assertThat(meta.get("rowCount").asLong()).isEqualTo(dataArray.size());

        // Verify anonymization: ensure sensitive PII fields are never emitted
        assertThat(body).doesNotContain("\"vorname\"")
            .doesNotContain("\"nachname\"")
            .doesNotContain("\"geburtsdatum\"")
            .contains("\"geschlecht\"");
    }

    @Test
    @SneakyThrows
    @TestAsHelloDataSeedingBE
    void export_untersuchungen_returns_nested_impfungen_massnahmen() {
        // ARRANGE
        Benutzer arzt = fixtureService.readBenutzerWithBerechtigungen(TestAsSchularztBE.BENUTZER_ID_ID);
        AnlassConfig config = anlassConfigService.getByAnlassTypAndStichtag(
            AnlassTyp.KLASSE_8,
            appClock.offsetDateTime().now().toLocalDate());
        Anlass anlass = anlassService.persist(AnlassBuilder.create(builder -> builder
            .withTitle("HelloData Impfungen Anlass")
            .withArzt(arzt)
            .withAnlassConfig(config)));
        Dossier dossier = dossierService.persist(DossierBuilder.create(builder -> builder
            .forKind(fixtureService.createKind())
            .withZustaendigArzt(arzt)));
        Einladung einladung = einladungService.persist(EinladungBuilder.create(builder -> builder
            .withAnlass(anlass)
            .withDossier(dossier)));
        Untersuchung untersuchung = untersuchungService.getOrCreateUntersuchung(einladung.getId());
        UntersuchungKindDaten kd = untersuchungKindDatenService.persist(
            UntersuchungKindDaten.ofKind(dossier.getKind()));
        untersuchung.setUntersuchungKindDaten(kd);

        // Hoeren Audiogramm
        UntersuchungHoerenAudiogramm hoeren = UntersuchungHoerenAudiogramm.forUntersuchung(untersuchung);
        Audiogramm audioR = AudiogrammBuilder.create(b -> b.withDezibelAllHz(Dezibel.DB20));
        audioR.setVersion(0L);
        audioR.disableOptimisticLockingCheck();
        hoeren.setAudiogrammRechts(audioR);
        Audiogramm audioL = AudiogrammBuilder.create(b -> b.withDezibelAllHz(Dezibel.DB20));
        audioL.setVersion(0L);
        audioL.disableOptimisticLockingCheck();
        hoeren.setAudiogrammLinks(audioL);
        hoeren.setAudiogrammSimpel(SingleValueEnhanced.of(AudiogrammSimpel.DB_TIEF, null));
        hoeren.setHoerhilfe(SingleValueEnhanced.of(HoerenHoerhilfe.NEIN, null));
        hoeren.setAudiogrammBeurteilung(MultiValueEnhanced.of(EnumSet.of(AudiogrammBeurteilung.NORMAL), null));
        hoeren.setZusaetzlicheAngaben(MultiValueEnhanced.of(EnumSet.of(HoerenZusaetzlicheAngaben.KEINE), null));
        hoeren.setEmpfehlung(MultiValueEnhanced.of(EnumSet.of(HoerenEmpfehlung.UEBERPRUEFUNG_ARZT), null));
        hoeren.setBemerkungen("Bemerkungen");
        hoeren.disableOptimisticLockingCheck();
        untersuchungHoerenAudiogrammService.persist(hoeren);
        untersuchung.setUntersuchungHoerenAudiogramm(hoeren);
        fixtureService.executeSql("UPDATE untersuchung SET untersuchunghoerenaudiogramm_id = '" + hoeren.getId().getUUID() + "' WHERE id = '" + untersuchung.getId().getUUID() + "'");

        // Impfungen
        UntersuchungImpfungen impfungen = untersuchung.getUntersuchungImpfungen();
        fixtureService.executeSql("UPDATE untersuchungimpfungen SET impfausweisunvollstaendig = true WHERE id = '" + impfungen.getId().getUUID() + "'");

        UntersuchungImpfungenNurUnvollstaendige nurUnv = new UntersuchungImpfungenNurUnvollstaendige();
        nurUnv.setUntersuchungImpfungen(impfungen);
        nurUnv.setImpfausweisVollstaendig(ImpfausweisVollstaendig.NEIN);
        nurUnv.setImpfausweisGrundUnvollstaendig(ImpfausweisGrundUnvollstaendig.IMPFUNG_AUFGESCHOBEN);
        nurUnv.disableOptimisticLockingCheck();
        untersuchungImpfungenNurUnvollstaendigeRepo.persist(nurUnv);
        impfungen.setUntersuchungImpfungenNurUnvollstaendige(nurUnv);
        fixtureService.executeSql("UPDATE untersuchungimpfungen SET untersuchungimpfungennurunvollstaendige_id = '" + nurUnv.getId().getUUID() + "' WHERE id = '" + impfungen.getId().getUUID() + "'");

        UntersuchungImpfungenMassnahmen mass = new UntersuchungImpfungenMassnahmen();
        mass.setUntersuchungImpfungen(impfungen);
        mass.setInformationEltern(InformationEltern.BRIEF);
        mass.setInformationHausarzt(true);
        mass.setRueckfrageHausarzt(true);
        mass.setVormerkenFuerGruppenFolgetermin(false);
        mass.disableOptimisticLockingCheck();
        untersuchungImpfungenMassnahmenRepo.persist(mass);
        impfungen.setUntersuchungImpfungenMassnahmen(mass);
        fixtureService.executeSql("UPDATE untersuchungimpfungen SET untersuchungimpfungenmassnahmen_id = '" + mass.getId().getUUID() + "' WHERE id = '" + impfungen.getId().getUUID() + "'");

        // ACT
        Response response = RestAssured.given()
            .when()
            .get(UNTERSUCHUNGEN_JSON_PATH)
            .then()
            .assertThat()
            .statusCode(HttpURLConnection.HTTP_OK)
            .extract()
            .response();

        // ASSERT
        String body = response.asString();
        JsonNode root = objectMapper.readTree(body);
        JsonNode dataArray = root.get("data");
        assertThat(dataArray.isArray()).isTrue();

        JsonNode row = null;
        for (JsonNode item : dataArray) {
            if (item.get("id").asText().equals(untersuchung.getId().getUUID().toString())) {
                row = item;
                break;
            }
        }
        JsonNode impfungenNode = row.get("impfungen");
        assertThat(impfungenNode.get("id").asText()).isEqualTo(impfungen.getId().getUUID().toString());
        assertThat(impfungenNode.get("impfausweisUnvollstaendig").asBoolean()).isTrue();

        JsonNode massnahmenNode = impfungenNode.get("massnahmen");
        assertThat(massnahmenNode.get("id").asText()).isEqualTo(mass.getId().getUUID().toString());
        assertThat(massnahmenNode.get("vormerkenFuerGruppenFolgetermin").asBoolean()).isFalse();
        assertThat(massnahmenNode.get("informationHausarzt").asBoolean()).isTrue();
        assertThat(massnahmenNode.get("informationEltern").asText()).isEqualTo("BRIEF");
        assertThat(massnahmenNode.get("rueckfrageHausarzt").asBoolean()).isTrue();

        assertThat(impfungenNode.has("nurUnvollstaendige")).isTrue();
        JsonNode nurUnvNode = impfungenNode.get("nurUnvollstaendige");
        assertThat(nurUnvNode.get("id").asText()).isEqualTo(nurUnv.getId().getUUID().toString());
        assertThat(nurUnvNode.get("impfausweisVollstaendig").asText()).isEqualTo("NEIN");
        assertThat(nurUnvNode.get("impfausweisGrundUnvollstaendig").asText()).isEqualTo("IMPFUNG_AUFGESCHOBEN");

        JsonNode hoerenNode = row.get("hoeren");
        assertThat(hoerenNode.get("audiogrammId").asText()).isEqualTo(hoeren.getId().getUUID().toString());
        assertThat(hoerenNode.has("audiogrammRechts")).isTrue();
        JsonNode audioRNode = hoerenNode.get("audiogrammRechts");
        assertThat(audioRNode.get("id").asText()).isEqualTo(audioR.getId().getUUID().toString());
        assertThat(audioRNode.get("dbHz500").asText()).isEqualTo("DB20");
        assertThat(audioRNode.get("dbHz1000").asText()).isEqualTo("DB20");

        JsonNode audioLNode = hoerenNode.get("audiogrammLinks");
        assertThat(audioLNode.get("id").asText()).isEqualTo(audioL.getId().getUUID().toString());
        assertThat(audioLNode.get("dbHz500").asText()).isEqualTo("DB20");
        assertThat(audioLNode.get("dbHz1000").asText()).isEqualTo("DB20");
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

    @Test
    @TestAsHelloDataSeedingBE
    @SneakyThrows
    void json_export_untersuchungen_contains_the_same_rows_as_the_ndjson_export() {
        // ARRANGE
        Benutzer arzt = fixtureService.readBenutzerWithBerechtigungen(TestAsSchularztBE.BENUTZER_ID_ID);
        AnlassConfig config = anlassConfigService.getByAnlassTypAndStichtag(
            AnlassTyp.KLASSE_8,
            appClock.offsetDateTime().now().toLocalDate());
        Anlass anlass = anlassService.persist(AnlassBuilder.create(builder -> builder
            .withTitle("HelloData Compare Anlass")
            .withArzt(arzt)
            .withAnlassConfig(config)));
        Dossier dossier = dossierService.persist(DossierBuilder.create(builder -> builder
            .forKind(fixtureService.createKind())
            .withZustaendigArzt(arzt)));
        Einladung einladung = einladungService.persist(EinladungBuilder.create(builder -> builder
            .withAnlass(anlass)
            .withDossier(dossier)));
        Untersuchung untersuchung = untersuchungService.getOrCreateUntersuchung(einladung.getId());
        UntersuchungKindDaten kd = untersuchungKindDatenService.persist(
            UntersuchungKindDaten.ofKind(dossier.getKind()));
        untersuchung.setUntersuchungKindDaten(kd);

        // ACT
        Response response = RestAssured.given()
            .when()
            .get(UNTERSUCHUNGEN_JSON_PATH)
            .then()
            .assertThat()
            .statusCode(HttpURLConnection.HTTP_OK)
            .extract()
            .response();

        String ndjsonBody = RestAssured.given().when().get(UNTERSUCHUNGEN_PATH)
            .then().statusCode(HttpURLConnection.HTTP_OK).extract().asString();

        // ASSERT
        assertThat(response.getContentType()).contains(JsonEnvelopeWriter.MEDIA_TYPE);

        String body = response.asString();
        JsonNode envelope = objectMapper.readTree(body);

        Set<String> jsonIds = idsOf(envelope.get(JsonEnvelopeWriter.DATA_KEY));
        Set<String> ndjsonIds = dataRows(parseNdjson(ndjsonBody)).stream()
            .map(row -> row.get("id").asText())
            .collect(Collectors.toSet());

        assertThat(jsonIds)
            .as("both formats export the same Untersuchungen")
            .isNotEmpty()
            .isEqualTo(ndjsonIds);
    }

    @Test
    @TestAsHelloDataForeignTenant
    void ndjson_untersuchungen_export_refuses_a_tenant_other_than_BE() {
        String body = RestAssured.given()
            .when()
            .get(UNTERSUCHUNGEN_PATH)
            .then()
            .assertThat()
            .statusCode(not(HttpURLConnection.HTTP_OK))
            .extract()
            .asString();

        assertThat(body)
            .as("no row of another tenant may reach the client")
            .doesNotContain("untersuchungKindDaten");
    }

    @Test
    @TestAsSupportBE
    void ndjson_untersuchungen_export_is_forbidden_without_the_hellodata_berechtigung() {
        RestAssured.given()
            .when()
            .get(UNTERSUCHUNGEN_PATH)
            .then()
            .assertThat()
            .statusCode(HttpURLConnection.HTTP_FORBIDDEN);
    }

    @Test
    void ndjson_untersuchungen_export_is_unreachable_without_authentication() {
        RestAssured.given()
            .when()
            .get(UNTERSUCHUNGEN_PATH)
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
