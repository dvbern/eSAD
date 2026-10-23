package ch.dvbern.oss.hemed.esc.api.features.hellodata;

import java.net.HttpURLConnection;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import ch.dvbern.oss.hemed.esc.api.features.hellodata.json.HelloDataDossiersEnvelopeJson;
import ch.dvbern.oss.hemed.esc.api.features.hellodata.json.HelloDataMetaJson;
import ch.dvbern.oss.hemed.esc.api.features.hellodata.json.HelloDataSentinelJson;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Dossier;
import ch.dvbern.oss.hemed.esc.api.features.kinder.DossierService;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import ch.dvbern.oss.hemed.esc.api.features.kinder.KindService;
import ch.dvbern.oss.hemed.esc.api.features.kinder.types.PapierakteStatus;
import ch.dvbern.oss.hemed.esc.api.features.organisation.Organisation;
import ch.dvbern.oss.hemed.esc.api.features.schule.Schule;
import ch.dvbern.oss.hemed.esc.api.features.schule.SchuleService;
import ch.dvbern.oss.hemed.esc.api.features.schule.klasse.Klasse;
import ch.dvbern.oss.hemed.esc.api.features.schule.klasse.KlasseService;
import ch.dvbern.oss.hemed.esc.api.shared.auth.FachRolle;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantContext;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantEnum;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import ch.dvbern.oss.hemed.esc.api.shared.person.Geschlecht;
import ch.dvbern.oss.hemed.esc.api.shared.person.Land;
import ch.dvbern.oss.hemed.esc.api.shared.types.EntityAktivStatus;
import ch.dvbern.oss.hemed.esc.api.testutil.AbstractITest;
import ch.dvbern.oss.hemed.esc.api.testutil.FixtureService;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.DossierBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.KindBuilder;
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
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.not;

@QuarkusTest
@RequiredArgsConstructor
@DisplayNameGeneration(ReplaceUnderscores.class)
class HelloDataDossiersResourceITest extends AbstractITest {

    private static final String DOSSIERS_PATH = "/hellodata/v1/ndjson/dossiers";
    private static final String DOSSIERS_JSON_PATH = "/hellodata/v1/json/dossiers";

    /**
     * Free text of the seeded Dossier's andereZusatzinformation. It must never show up in the
     * export.
     */
    private static final String ANDERE_ZUSATZINFORMATION = "HelloData Zusatzinformation mit Personendaten";

    private final KindService kindService;
    private final DossierService dossierService;
    private final SchuleService schuleService;
    private final KlasseService klasseService;
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
    void export_contains_the_seeded_dossier() {
        // ARRANGE
        Dossier dossier = seedDossier(null, null);

        // ACT
        Response response = RestAssured.given()
            .when()
            .get(DOSSIERS_PATH)
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
            .as("the seeded Dossier must appear in the export")
            .contains(dossier.getId().getUUID().toString());
    }

    @Test
    @TestAsHelloDataSeedingBE
    void export_ends_with_a_meta_line_whose_rowCount_matches() {
        // ARRANGE
        seedDossier(null, null);

        // ACT
        String body = RestAssured.given()
            .when()
            .get(DOSSIERS_PATH)
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
        Schule seededSchule = schuleService.persist(SchuleBuilder.create(builder -> { }));
        Klasse klasse = klasseService.persist(KlasseBuilder.create(builder -> builder.forSchule(seededSchule)));
        Dossier dossier = seedDossier(klasse.getSchule(), klasse);

        // ACT
        String body = RestAssured.given().when().get(DOSSIERS_PATH)
            .then().statusCode(HttpURLConnection.HTTP_OK).extract().asString();

        // ASSERT
        JsonNode row = rowOf(body, dossier);

        Kind kind = dossier.getKind();
        assertThat(kind.getNationalitaet()).isNotNull();
        assertThat(row.get("kindId").asText()).isEqualTo(kind.getId().getUUID().toString());
        assertThat(row.get("geschlecht").asText()).isEqualTo(kind.getGeschlecht().name());
        assertThat(row.get("geburtsjahr").asInt()).isEqualTo(kind.getGeburtsdatum().getYear());
        assertThat(row.get("nationalitaet").asText()).isEqualTo(kind.getNationalitaet().name());
        assertThat(row.get("erstsprache").asText()).isEqualTo(kind.getErstsprache());
        assertThat(row.get("wohntyp").asText()).isEqualTo(kind.getWohntyp());
        assertThat(row.get("schuleId").asText()).isEqualTo(klasse.getSchule().getId().getUUID().toString());
        assertThat(row.get("klasseId").asText()).isEqualTo(klasse.getId().getUUID().toString());
        assertThat(row.get("kindAktivStatus").asText()).isEqualTo(kind.getKindAktivStatus().name());
        assertThat(row.get("kinderschutzfall").asBoolean()).isEqualTo(dossier.isKinderschutzfall());
        assertThat(row.get("keineAuskunftAnDrittpersonen").asBoolean())
            .isEqualTo(dossier.isKeineAuskunftAnDrittpersonen());
        assertThat(row.get("sansPapier").asBoolean()).isEqualTo(dossier.isSansPapier());
        assertThat(row.get("immunitaetMasern").asBoolean()).isEqualTo(dossier.isImmunitaetMasern());
        assertThat(row.get("immunitaetVarizellen").asBoolean()).isEqualTo(dossier.isImmunitaetVarizellen());
        assertThat(row.get("papierakteStatus").asText()).isEqualTo(dossier.getPapierakteStatus().name());
        assertThat(row.get("hashValue").asText()).isEqualTo(kind.getHashValue());
    }

    @Test
    @TestAsHelloDataSeedingBE
    void schuleId_and_klasseId_are_null_when_the_kind_has_neither() {
        // ARRANGE
        Dossier dossier = seedDossier(null, null);

        // ACT
        String body = RestAssured.given().when().get(DOSSIERS_PATH)
            .then().statusCode(HttpURLConnection.HTTP_OK).extract().asString();

        // ASSERT
        JsonNode row = rowOf(body, dossier);

        assertThat(row.get("schuleId").isNull()).isTrue();
        assertThat(row.get("klasseId").isNull()).isTrue();
    }

    @Test
    @TestAsHelloDataSeedingBE
    void schuleId_is_set_without_a_klasse_when_only_the_schule_is_known() {
        // ARRANGE
        Schule schule = schuleService.persist(SchuleBuilder.create(builder -> { }));
        Dossier dossier = seedDossier(schule, null);

        // ACT
        String body = RestAssured.given().when().get(DOSSIERS_PATH)
            .then().statusCode(HttpURLConnection.HTTP_OK).extract().asString();

        // ASSERT
        JsonNode row = rowOf(body, dossier);

        assertThat(row.get("schuleId").asText()).isEqualTo(schule.getId().getUUID().toString());
        assertThat(row.get("klasseId").isNull()).isTrue();
    }

    @Test
    @TestAsHelloDataSeedingBE
    void exported_row_has_exactly_the_fields_of_the_contract() {
        // ARRANGE - every column filled in, so the check does not depend on how nulls are written
        Schule seededSchule = schuleService.persist(SchuleBuilder.create(builder -> { }));
        Klasse klasse = klasseService.persist(KlasseBuilder.create(builder -> builder.forSchule(seededSchule)));
        Dossier dossier = seedDossier(klasse.getSchule(), klasse);

        // ACT
        String body = RestAssured.given()
            .when()
            .get(DOSSIERS_PATH)
            .then()
            .assertThat()
            .statusCode(HttpURLConnection.HTTP_OK)
            .extract()
            .asString();

        // ASSERT
        assertThat(fieldNamesOf(rowOf(body, dossier)))
            .as("a row carries exactly the fields of HelloDataDossierJson - any other field is "
                + "either a leak or needs a deliberate change of the contract towards HelloData")
            .containsExactlyInAnyOrder(
                "id",
                "kindId",
                "geschlecht",
                "geburtsjahr",
                "nationalitaet",
                "erstsprache",
                "wohntyp",
                "schuleId",
                "klasseId",
                "kindAktivStatus",
                "kinderschutzfall",
                "keineAuskunftAnDrittpersonen",
                "sansPapier",
                "immunitaetMasern",
                "immunitaetVarizellen",
                "papierakteStatus",
                "hashValue"
            );
    }

    @Test
    @TestAsHelloDataSeedingBE
    void export_never_contains_personal_data_of_the_kind_or_the_staff() {
        // ARRANGE
        Dossier dossier = seedDossier(null, null);

        // ACT
        String body = RestAssured.given()
            .when()
            .get(DOSSIERS_PATH)
            .then()
            .assertThat()
            .statusCode(HttpURLConnection.HTTP_OK)
            .extract()
            .asString();

        // ASSERT
        assertThat(body)
            .as("without the seeded Dossier in the export this test proves nothing")
            .contains(dossier.getId().getUUID().toString());

        Kind kind = dossier.getKind();
        assertThat(kind.getAdresse())
            .isNotNull();
        assertThat(kind.getEmail())
            .isNotNull();
        assertThat(body)
            .as("name, vorname, Adresse and email of the Kind are personal data")
            .doesNotContain(
                kind.getName(),
                kind.getVorname(),
                kind.getAdresse().getStrasseUndNr(),
                kind.getEmail().getAddress()
            )
            .as("the free text of the andereZusatzinformation may contain personal data")
            .doesNotContain(ANDERE_ZUSATZINFORMATION);

        Benutzer arzt = dossier.getZustaendigArzt();
        Benutzer mpa = dossier.getZustaendigMPA();
        assertThat(arzt)
            .isNotNull();
        assertThat(mpa)
            .isNotNull();
        assertThat(body)
            .as("zustaendigArzt and zustaendigMPA are personal data of staff")
            .doesNotContain(
                arzt.getId().getUUID().toString(),
                arzt.getNachname(),
                mpa.getId().getUUID().toString(),
                mpa.getNachname()
            );
    }

    @Test
    @TestAsHelloDataSeedingBE
    @SneakyThrows
    void json_export_contains_the_same_rows_as_the_ndjson_export() {
        // ARRANGE
        seedDossier(null, null);

        // ACT
        Response response = RestAssured.given()
            .when()
            .get(DOSSIERS_JSON_PATH)
            .then()
            .assertThat()
            .statusCode(HttpURLConnection.HTTP_OK)
            .extract()
            .response();

        String ndjsonBody = RestAssured.given().when().get(DOSSIERS_PATH)
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
            .as("both formats export the same Dossiers")
            .isNotEmpty()
            .isEqualTo(ndjsonIds);
    }

    @Test
    @TestAsHelloDataSeedingBE
    @SneakyThrows
    void json_envelope_matches_the_declared_contract_type() {
        // ARRANGE
        seedDossier(null, null);

        JsonNode declared = objectMapper.valueToTree(new HelloDataDossiersEnvelopeJson(
            List.of(),
            new HelloDataMetaJson(
                HelloDataMetaJson.STATUS_COMPLETE,
                0,
                OffsetDateTime.parse("2026-01-01T00:00:00Z"))));

        // ACT
        String body = RestAssured.given().when().get(DOSSIERS_JSON_PATH)
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
        RestAssured.given().when().get(DOSSIERS_PATH).then().statusCode(HttpURLConnection.HTTP_OK);
        RestAssured.given().when().get(DOSSIERS_PATH).then().statusCode(HttpURLConnection.HTTP_OK);
    }

    @Test
    @TestAsHelloDataForeignTenant
    void export_refuses_a_tenant_other_than_BE() {
        RestAssured.given()
            .when()
            .get(DOSSIERS_PATH)
            .then()
            .assertThat()
            .statusCode(not(HttpURLConnection.HTTP_OK));
    }

    @Test
    @TestAsSupportBE
    void export_is_forbidden_without_the_hellodata_berechtigung() {
        RestAssured.given()
            .when()
            .get(DOSSIERS_PATH)
            .then()
            .assertThat()
            .statusCode(HttpURLConnection.HTTP_FORBIDDEN);
    }

    @Test
    void export_is_unreachable_without_authentication() {
        RestAssured.given()
            .when()
            .get(DOSSIERS_PATH)
            .then()
            .assertThat()
            .statusCode(HttpURLConnection.HTTP_UNAUTHORIZED);
    }

    @Test
    @TestAsSupportBE
    void json_export_is_forbidden_without_the_hellodata_berechtigung() {
        RestAssured.given()
            .when()
            .get(DOSSIERS_JSON_PATH)
            .then()
            .assertThat()
            .statusCode(HttpURLConnection.HTTP_FORBIDDEN);
    }

    @Test
    void json_export_is_unreachable_without_authentication() {
        RestAssured.given()
            .when()
            .get(DOSSIERS_JSON_PATH)
            .then()
            .assertThat()
            .statusCode(HttpURLConnection.HTTP_UNAUTHORIZED);
    }

    @NonNull
    private JsonNode rowOf(@NonNull String ndjsonBody, @NonNull Dossier dossier) {
        return dataRows(parseNdjson(ndjsonBody)).stream()
            .filter(row -> row.get("id").asText().equals(dossier.getId().getUUID().toString()))
            .findFirst()
            .orElseThrow(() -> new AssertionError("seeded Dossier missing from the export"));
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
     * Tenant BE is dezentral, so a Dossier is only valid with a zustaendiger Arzt
     * ({@code CheckArztRequiredForDezentraleTenants}), and an MPA has to belong to the same
     * Organisation as the Arzt ({@code CheckArztUndMpaSameOrganisation}).
     * <p>
     * Every field the contract deliberately leaves out is filled in - name, vorname, Adresse,
     * email and telefon of the Kind, the andereZusatzinformation and both zustaendige Personen -
     * so that a leak of any of them shows up in the export.
     */
    @NonNull
    @CanIgnoreReturnValue
    private Dossier seedDossier(@Nullable Schule schule, @Nullable Klasse klasse) {
        Kind kind = KindBuilder.create(builder -> builder
            .withUniqueName("HelloDataDossier", "Kind")
            .withGeburtsdatum(LocalDate.of(2015, 6, 15))
            .withGeschlecht(Geschlecht.WEIBLICH)
            .withSchule(schule)
            .withKlasse(klasse)
            .withKindAktivStatus(EntityAktivStatus.AKTIV));
        kind.setNationalitaet(Land.SCHWEIZ);
        kind = kindService.persistWithoutDuplicateCheck(kind);

        Kind persistedKind = kind;
        Organisation organisation = fixtureService.createOrganisation();
        Benutzer arzt = fixtureService.createBenutzer(organisation, FachRolle.SCHULAERZTIN);
        Benutzer mpa = fixtureService.createBenutzer(organisation, FachRolle.MPA);
        Dossier dossier = DossierBuilder.create(builder -> builder
            .forKind(persistedKind)
            .withZustaendigArzt(arzt)
            .withZustaendigMPA(mpa)
            .withAndereZusatzinformation(true, ANDERE_ZUSATZINFORMATION)
            .withImmunitaetMasern(true)
            .withImmunitaetVarizellen(false));
        dossier.setPapierakteStatus(PapierakteStatus.VORHANDEN);
        return dossierService.persist(dossier);
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
