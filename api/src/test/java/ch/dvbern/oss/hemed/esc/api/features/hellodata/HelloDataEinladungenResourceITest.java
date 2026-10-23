/*
 * Copyright (C) 2026 DV Bern AG, Switzerland
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.oss.hemed.esc.api.features.hellodata;

import java.net.HttpURLConnection;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Anlass;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anonymereintrag.AnonymerEintrag;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anonymereintrag.AnonymerEintragService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.EinladungService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.types.ArtAbmeldung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.types.EinladungStatus;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.types.GrundAbmeldung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.AnlassTyp;
import ch.dvbern.oss.hemed.esc.api.features.hellodata.json.HelloDataEinladungenEnvelopeJson;
import ch.dvbern.oss.hemed.esc.api.features.hellodata.json.HelloDataMetaJson;
import ch.dvbern.oss.hemed.esc.api.features.hellodata.json.HelloDataSentinelJson;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Dossier;
import ch.dvbern.oss.hemed.esc.api.features.notizen.Notiz;
import ch.dvbern.oss.hemed.esc.api.features.notizen.NotizTyp;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantContext;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantEnum;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileData;
import ch.dvbern.oss.hemed.esc.api.testutil.AbstractITest;
import ch.dvbern.oss.hemed.esc.api.testutil.FixtureService;
import ch.dvbern.oss.hemed.esc.api.testutil.TestUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.AnonymerEintragBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.EinladungBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.FileDataBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.NotizBuilder;
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
class HelloDataEinladungenResourceITest extends AbstractITest {

    private static final String EINLADUNGEN_PATH = "/hellodata/v1/ndjson/einladungen";
    private static final String EINLADUNGEN_JSON_PATH = "/hellodata/v1/json/einladungen";

    private final FixtureService fixtureService;
    private final EinladungService einladungService;
    private final AnonymerEintragService anonymerEintragService;
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
    void export_contains_the_seeded_einladung() {
        // ARRANGE
        Einladung einladung = seedEinladung(true);

        // ACT
        Response response = RestAssured.given()
            .when()
            .get(EINLADUNGEN_PATH)
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
            .as("the seeded Einladung must appear in the export")
            .contains(einladung.getId().getUUID().toString());
    }

    @Test
    @TestAsHelloDataSeedingBE
    void export_ends_with_a_meta_line_whose_rowCount_matches() {
        // ARRANGE
        seedEinladung(true);

        // ACT
        String body = RestAssured.given()
            .when()
            .get(EINLADUNGEN_PATH)
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
        Einladung einladung = seedEinladung(true);

        // ACT
        String body = RestAssured.given().when().get(EINLADUNGEN_PATH)
            .then().statusCode(HttpURLConnection.HTTP_OK).extract().asString();

        // ASSERT
        JsonNode row = rowOf(body, einladung);

        assertThat(row.get("anlassId").asText())
            .isEqualTo(einladung.getAnlass().getId().getUUID().toString());
        assertThat(row.get("dossierId").asText())
            .isEqualTo(einladung.getDossier().getId().getUUID().toString());
        assertThat(row.get("einladungStatus").asText())
            .isEqualTo(einladung.getEinladungStatus().name());
        assertThat(row.get("grundAbmeldung").asText())
            .isEqualTo(GrundAbmeldung.KONTROLLE_KINDER_PRIVATARZT.name());
        assertThat(row.get("artAbmeldung").asText())
            .isEqualTo(ArtAbmeldung.INFO_LEHRPERSON.name());

        assertThat(row.hasNonNull("untersuchungId"))
            .as("the seeded Einladung has no Untersuchung - and must survive the leftJoin anyway")
            .isFalse();
    }

    /**
     * Regular Einladungen have a Dossier and an EinladungStatus, with hash absent (null).
     * Anonymous entries have no Dossier and no EinladungStatus, but carry the pseudonym hash.
     */
    @Test
    @TestAsHelloDataSeedingBE
    void regular_einladung_carries_dossierId_and_no_hash_while_anonymer_eintrag_carries_hash_and_no_dossierId() {
        // ARRANGE
        Einladung einladung = seedEinladung(true);
        AnonymerEintrag anonymerEintrag = seedAnonymerEintrag("test-anon-hash-12345");

        // ACT
        String body = RestAssured.given()
            .when()
            .get(EINLADUNGEN_PATH)
            .then()
            .statusCode(HttpURLConnection.HTTP_OK)
            .extract()
            .asString();

        // ASSERT
        JsonNode einladungRow = rowOf(body, einladung);
        assertThat(einladungRow.hasNonNull("dossierId"))
            .as("dossierId is present on regular Einladungen")
            .isTrue();
        assertThat(einladungRow.get("dossierId").asText())
            .isEqualTo(einladung.getDossier().getId().getUUID().toString());
        assertThat(einladungRow.hasNonNull("einladungStatus"))
            .as("einladungStatus is present on regular Einladungen")
            .isTrue();
        assertThat(einladungRow.hasNonNull("hash"))
            .as("hash is absent/null for regular Einladungen")
            .isFalse();

        JsonNode anonymerRow = rowOf(body, anonymerEintrag);
        assertThat(anonymerRow.hasNonNull("dossierId"))
            .as("dossierId is absent/null for anonymous entries")
            .isFalse();
        assertThat(anonymerRow.hasNonNull("einladungStatus"))
            .as("einladungStatus is absent/null for anonymous entries")
            .isFalse();
        assertThat(anonymerRow.hasNonNull("hash"))
            .as("hash is present on anonymous entries")
            .isTrue();
        assertThat(anonymerRow.get("hash").asText())
            .isEqualTo("test-anon-hash-12345");
        assertThat(anonymerRow.get("anlassId").asText())
            .isEqualTo(anonymerEintrag.getAnlass().getId().getUUID().toString());
    }

    /**
     * "Vorhanden" means uploaded: a Notiz of the typ without an attached file is not an upload.
     * Guards the two {@code exists} subqueries against degenerating into "a Notiz exists".
     */
    @Test
    @TestAsHelloDataSeedingBE
    void vorhanden_flags_are_true_only_when_the_notiz_carries_a_file() {
        // ARRANGE
        Einladung withFiles = seedEinladung(true);
        Einladung withoutFiles = seedEinladung(false);

        // ACT
        String body = RestAssured.given().when().get(EINLADUNGEN_PATH)
            .then().statusCode(HttpURLConnection.HTTP_OK).extract().asString();

        // ASSERT
        JsonNode rowWithFiles = rowOf(body, withFiles);
        assertThat(rowWithFiles.get("impfausweisVorhanden").asBoolean()).isTrue();
        assertThat(rowWithFiles.get("fragebogenVorhanden").asBoolean()).isTrue();

        JsonNode rowWithoutFiles = rowOf(body, withoutFiles);
        assertThat(rowWithoutFiles.get("impfausweisVorhanden").asBoolean())
            .as("a Notiz without a file is not an upload")
            .isFalse();
        assertThat(rowWithoutFiles.get("fragebogenVorhanden").asBoolean())
            .as("a Notiz without a file is not an upload")
            .isFalse();
    }

    @Test
    @TestAsHelloDataSeedingBE
    void export_contains_neither_free_text_nor_internal_workflow_state() {
        // ARRANGE
        seedEinladung(true);

        // ACT
        String body = RestAssured.given().when().get(EINLADUNGEN_PATH)
            .then().statusCode(HttpURLConnection.HTTP_OK).extract().asString();

        // ASSERT
        List<JsonNode> rows = dataRows(parseNdjson(body));

        assertThat(rows)
            .as("without data rows this test proves nothing")
            .isNotEmpty()
            .allSatisfy(row -> {
                assertThat(row.has("bemerkungAbmeldung")).isFalse();
                assertThat(row.has("notizen")).isFalse();
                assertThat(row.has("spssExported")).isFalse();
                assertThat(row.has("naechsteStation")).isFalse();
                assertThat(row.has("naechsteStationSeit")).isFalse();
            });
    }

    @Test
    @TestAsHelloDataSeedingBE
    @SneakyThrows
    void json_export_contains_the_same_rows_as_the_ndjson_export() {
        // ARRANGE
        seedEinladung(true);

        // ACT
        Response response = RestAssured.given()
            .when()
            .get(EINLADUNGEN_JSON_PATH)
            .then()
            .assertThat()
            .statusCode(HttpURLConnection.HTTP_OK)
            .extract()
            .response();

        String ndjsonBody = RestAssured.given().when().get(EINLADUNGEN_PATH)
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
            .as("both formats export the same Einladungen")
            .isNotEmpty()
            .isEqualTo(ndjsonIds);
    }

    @Test
    @TestAsHelloDataSeedingBE
    @SneakyThrows
    void json_export_ends_with_the_meta_sentinel_and_matches_rowCount() {
        // ARRANGE
        seedEinladung(true);

        // ACT
        String body = RestAssured.given()
            .when()
            .get(EINLADUNGEN_JSON_PATH)
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
     * Guards against drift: HelloDataEinladungenEnvelopeJson is never used to produce a response
     * (doing so would buffer the whole export in memory), it only exists to describe the shape
     * in the OpenAPI contract. This test proves that the streaming implementation really produces
     * that shape.
     */
    @Test
    @TestAsHelloDataSeedingBE
    @SneakyThrows
    void json_envelope_matches_the_declared_contract_type() {
        // ARRANGE
        seedEinladung(true);

        JsonNode declared = objectMapper.valueToTree(new HelloDataEinladungenEnvelopeJson(
            List.of(),
            new HelloDataMetaJson(
                HelloDataMetaJson.STATUS_COMPLETE,
                0,
                OffsetDateTime.parse("2026-01-01T00:00:00Z"))));

        // ACT
        String body = RestAssured.given().when().get(EINLADUNGEN_JSON_PATH)
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
        RestAssured.given().when().get(EINLADUNGEN_PATH).then().statusCode(HttpURLConnection.HTTP_OK);
        RestAssured.given().when().get(EINLADUNGEN_PATH).then().statusCode(HttpURLConnection.HTTP_OK);
    }

    @Test
    @TestAsHelloDataForeignTenant
    void export_is_refused_for_non_be_tenants() {
        RestAssured.given()
            .when()
            .get(EINLADUNGEN_PATH)
            .then()
            .assertThat()
            .statusCode(HttpURLConnection.HTTP_INTERNAL_ERROR);
    }

    @Test
    @TestAsSupportBE
    void export_is_forbidden_without_the_hellodata_berechtigung() {
        RestAssured.given()
            .when()
            .get(EINLADUNGEN_PATH)
            .then()
            .assertThat()
            .statusCode(HttpURLConnection.HTTP_FORBIDDEN);
    }

    @Test
    void export_is_unreachable_without_authentication() {
        RestAssured.given()
            .when()
            .get(EINLADUNGEN_PATH)
            .then()
            .assertThat()
            .statusCode(HttpURLConnection.HTTP_UNAUTHORIZED);
    }

    @Test
    @TestAsHelloDataForeignTenant
    void json_export_is_refused_for_non_be_tenants() {
        RestAssured.given()
            .when()
            .get(EINLADUNGEN_JSON_PATH)
            .then()
            .assertThat()
            .statusCode(HttpURLConnection.HTTP_INTERNAL_ERROR);
    }

    @Test
    @TestAsSupportBE
    void json_export_is_forbidden_without_the_hellodata_berechtigung() {
        RestAssured.given()
            .when()
            .get(EINLADUNGEN_JSON_PATH)
            .then()
            .assertThat()
            .statusCode(HttpURLConnection.HTTP_FORBIDDEN);
    }

    @Test
    void json_export_is_unreachable_without_authentication() {
        RestAssured.given()
            .when()
            .get(EINLADUNGEN_JSON_PATH)
            .then()
            .assertThat()
            .statusCode(HttpURLConnection.HTTP_UNAUTHORIZED);
    }

    @NonNull
    private JsonNode rowOf(@NonNull String ndjsonBody, @NonNull Einladung einladung) {
        String id = einladung.getId().getUUID().toString();
        return dataRows(parseNdjson(ndjsonBody)).stream()
            .filter(row -> row.get("id").asText().equals(id))
            .findFirst()
            .orElseThrow(() -> new AssertionError("seeded Einladung missing from the export: " + id));
    }

    @NonNull
    private JsonNode rowOf(@NonNull String ndjsonBody, @NonNull AnonymerEintrag anonymerEintrag) {
        String id = anonymerEintrag.getId().getUUID().toString();
        return dataRows(parseNdjson(ndjsonBody)).stream()
            .filter(row -> row.get("id").asText().equals(id))
            .findFirst()
            .orElseThrow(() -> new AssertionError("seeded AnonymerEintrag missing from the export: " + id));
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
    private AnonymerEintrag seedAnonymerEintrag(@NonNull String hash) {
        Benutzer arzt = fixtureService.createBenutzerWithOrganisation();
        Anlass anlass = fixtureService.createAnlassWithArzt(AnlassTyp.KLASSE_4_ANONYM, arzt);

        return anonymerEintragService.persist(
            AnonymerEintragBuilder.create(builder -> builder
                .withID(ID.random(AnonymerEintrag.class))
                .withAnlass(anlass)
                .withHash(hash))
        );
    }

    /**
     * @param withUploadedFiles whether the Impfausweis- and Fragebogen-Notiz carry a file. The
     * Notizen exist either way, so the flags in the export can only differ because of the files.
     */
    @NonNull
    @CanIgnoreReturnValue
    private Einladung seedEinladung(boolean withUploadedFiles) {
        // createBenutzerWithOrganisation and not createBenutzerFuerZentraleOrganisation: the
        // latter looks up "the" zentrale Organisation, and tenant BE has more than one.
        Benutzer verantwortlicher = fixtureService.createBenutzerWithOrganisation();
        // createAnlassWithArzt and not createAnlass: BE is a dezentraler Tenant, where
        // CheckArztRequiredForDezentraleTenants rejects an Anlass without a zustaendiger Arzt.
        Anlass anlass = fixtureService.createAnlassWithArzt(AnlassTyp.KLASSE_4, verantwortlicher);
        // same reason: in tenant BE a Dossier without a zustaendiger Arzt does not validate
        Dossier dossier = fixtureService.createDossier(verantwortlicher);

        Notiz impfausweis = buildNotiz(
            dossier,
            verantwortlicher,
            NotizTyp.IMPFAUSWEIS,
            withUploadedFiles ? List.of(FileDataBuilder.defaultImgFileData()) : List.of());
        Notiz fragebogen = buildNotiz(
            dossier,
            verantwortlicher,
            NotizTyp.FRAGEBOGEN,
            withUploadedFiles ? List.of(FileDataBuilder.defaultPdfFileData()) : List.of());

        return einladungService.persist(EinladungBuilder.create(builder -> builder
            .withID(ID.random(Einladung.class))
            .withAnlass(anlass)
            .withDossier(dossier)
            .withStatus(EinladungStatus.TEILNAHME_ABGESAGT)
            .withGrundAbmeldung(GrundAbmeldung.KONTROLLE_KINDER_PRIVATARZT)
            .withArtAbmeldung(ArtAbmeldung.INFO_LEHRPERSON)
            .withNotiz(impfausweis)
            .withNotiz(fragebogen)));
    }

    /**
     * Built, not persisted: {@code Einladung.notizen} cascades ALL, so a Notiz persisted here
     * would be inserted a second time by the cascade (duplicate key on {@code notizPK}).
     */
    @NonNull
    private static Notiz buildNotiz(
        @NonNull Dossier dossier,
        @NonNull Benutzer verantwortlicher,
        @NonNull NotizTyp notizTyp,
        @NonNull List<FileData> files
    ) {
        return NotizBuilder.create(builder -> builder
            .withID(ID.random(Notiz.class))
            .withTitel(TestUtil.getRandomString("HelloDataNotiz"))
            .withNotizTyp(notizTyp)
            .withVerantwortlicher(verantwortlicher)
            .withDossier(dossier)
            .withFiles(files));
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
