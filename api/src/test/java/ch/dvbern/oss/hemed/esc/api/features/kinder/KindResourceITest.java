package ch.dvbern.oss.hemed.esc.api.features.kinder;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;

import ch.dvbern.dvbstarter.shared.EdokidIdTestGenerator;
import ch.dvbern.dvbstarter.shared.clock.AppClock;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.dvbstarter.shared.types.sozialversicherungsnummer.SozialversicherungsNummerTestGenerator;
import ch.dvbern.oss.hemed.esc.api.externalimportinfojson.ImportTyp;
import ch.dvbern.oss.hemed.esc.api.features.archivierung.Archivdokument;
import ch.dvbern.oss.hemed.esc.api.features.archivierung.ArchivdokumentService;
import ch.dvbern.oss.hemed.esc.api.features.kinder.edokid_id.EdokidId;
import ch.dvbern.oss.hemed.esc.api.features.kinder.edokid_id.EdokidIdGeneratorService;
import ch.dvbern.oss.hemed.esc.api.features.kinder.kontakt_person_kind.KontaktPersonKind;
import ch.dvbern.oss.hemed.esc.api.features.kinder.kontakt_person_kind.KontaktPersonKindService;
import ch.dvbern.oss.hemed.esc.api.features.kinder.types.KontaktPersonKindBeziehung;
import ch.dvbern.oss.hemed.esc.api.features.organisation.Organisation;
import ch.dvbern.oss.hemed.esc.api.features.schule.Schule;
import ch.dvbern.oss.hemed.esc.api.features.schule.SchuleService;
import ch.dvbern.oss.hemed.esc.api.features.schule.klasse.Klasse;
import ch.dvbern.oss.hemed.esc.api.features.schule.klasse.KlasseService;
import ch.dvbern.oss.hemed.esc.api.shared.auth.FachRolle;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantEnum;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantProducer;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import ch.dvbern.oss.hemed.esc.api.shared.person.Geschlecht;
import ch.dvbern.oss.hemed.esc.api.shared.types.EntityAktivStatus;
import ch.dvbern.oss.hemed.esc.api.shared.util.HashValueGeneratorUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.AbstractITest;
import ch.dvbern.oss.hemed.esc.api.testutil.FixtureService;
import ch.dvbern.oss.hemed.esc.api.testutil.TestUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.assertj.AppValidationErrorJsonSpecAssert;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.DossierBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.KindBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.KlasseBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.KontaktPersonKindBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.SchuleBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.restassured.RequestSpecUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsAdminBE;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsAdminBS;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsSchularztBE;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsSupportBE;
import ch.dvbern.oss.hemed.esc.generated.restassured.api.KinderApiSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.AppFailureErrorJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.AppValidationErrorJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.DossierJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.EntityAktivStatusSaveJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.EntityAktivStatusSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.EsadIdJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.FailureTypeSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.KindJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.KindListJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.KindSaveJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.KindSaveModeDuplikatSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.KindSaveResultJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.KindSchuleAndKlasseSaveJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.KlasseOptionJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.KontaktPersonKindListJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.SchuleOptionJsonSpec;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.ResponseBody;
import jakarta.ws.rs.core.Response.Status;
import lombok.RequiredArgsConstructor;
import org.assertj.core.api.Assertions;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static ch.dvbern.dvbstarter.shared.EdokidIdTestGenerator.EDOKID_ID_NO_SCHOOL_REGEX_PATTERN;
import static ch.dvbern.dvbstarter.shared.EdokidIdTestGenerator.EDOKID_ID_REGEX_PATTERN;
import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@RequiredArgsConstructor
class KindResourceITest extends AbstractITest {

    private final KinderApiSpec api = KinderApiSpec.kinder(RequestSpecUtil.quarkusSpec());

    private final FixtureService fixtureService;
    private final KindService kindService;
    private final KontaktPersonKindService kontaktPersonKindService;
    private final SchuleService schuleService;
    private final KlasseService klasseService;
    private final DossierService dossierService;
    private final AppClock appClock;
    private final TenantProducer tenantProducer;
    private final ArchivdokumentService archivdokumentService;

    // Without this, the test has to be adapted every year on 01. August
    private final LocalDate currentDate = this.getAppClock().localDateTime().now().toLocalDate();
    private final int currentEintrittsjahr = EdokidIdGeneratorService.calculateEintrittsJahr(currentDate);

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
    }

    @Test
    @TestAsAdminBS
    void findAllKinder() {
        // ARRANGE
        Kind kind = KindBuilder.create(
            builder -> builder
                .withID(ID.random(Kind.class))
                .withSozialversicherungsNummer(SozialversicherungsNummerTestGenerator.INSTANCE.getNextSozialversicherungsnummer())
                .withName("Gerber", "Emma"));
        kindService.persistWithoutDuplicateCheck(kind);
        // ACT
        KindListJsonSpec result = api
            .kindResourceFindAllKinder()
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(KindListJsonSpec.class);
        // ASSERT
        assertThat(result.getKinderList())
            .isNotEmpty()
            .anyMatch(kindJsonSpec -> kind.getId().getId().equals(kindJsonSpec.getId())
                && "Gerber".equals(kindJsonSpec.getData().getName())
                && "Emma".equals(kindJsonSpec.getData().getVorname()));
    }

    @Test
    @TestAsAdminBS
    void getKind() {
        // ARRANGE
        ID<Kind> kindId = ID.random(Kind.class);
        Schule schule = SchuleBuilder.defaultSchule();
        Klasse klasse = KlasseBuilder.create(builder -> builder.forSchule(schule));
        EdokidId edokidId = EdokidIdTestGenerator.getNextEdokidId(schule.getSchulhausnummer());
        Kind kind = KindBuilder.create(
            builder -> builder
                .withID(kindId)
                .withEsadId(edokidId.toFormattedString())
                .withKlasse(klasse)
                .withWohntyp("Heim")
                .withName("Meier", "Fridolin")
                .withGeburtsdatum(LocalDate.of(2010, 10, 10))
                .withGeschlecht(Geschlecht.MAENNLICH));
        schuleService.persist(schule);
        klasseService.persist(klasse);
        kindService.persistWithoutDuplicateCheck(kind);
        // ACT
        KindJsonSpec result = api
            .kindResourceGetKind()
            .kindIdPath(kind.getId().getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(KindJsonSpec.class);
        // ASSERT
        assertThat(result).isNotNull();
        assertThat(result.getExternalImportInfoJson()).isNull();

        assertThat(result.getEsadId()).isNotNull().hasSize(12)
            .matches(EDOKID_ID_REGEX_PATTERN);
        assertThat(result.getEsadId()).isEqualTo(edokidId.toFormattedString());
        assertThat(result.getId()).isEqualTo(kind.getId().getId());
        assertThat(result.getData().getName()).isEqualTo("Meier");
        assertThat(result.getData().getVorname()).isEqualTo("Fridolin");
        assertThat(result.getData().getGeburtsdatum()).isEqualTo(LocalDate.of(2010, 10, 10));
        assertThat(result.getData().getGeschlecht().toString()).hasToString(Geschlecht.MAENNLICH.toString());
        assertThat(result.getHashValue()).isEqualTo("d6b39de838740b4ac6c664dbf54147a435a6e4f499914f1495146397453efd33");
        assertThat(result.getData().getKlasse()).isNotNull();
        assertThat(result.getData().getSchule()).isNotNull();
        assertThat(result.getData().getKlasse().getId()).isEqualTo(klasse.getId().getId());
        assertThat(result.getData().getSchule().getId()).isEqualTo(schule.getId().getId());
        assertThat(result.getData().getWohntyp()).isEqualTo("Heim");
    }

    @Test
    @TestAsAdminBS
    void getKind_withExternalImportInfo() {
        // ARRANGE
        ID<Kind> kindId = ID.random(Kind.class);
        String kindExternalId = UUID.randomUUID().toString();
        Schule schule = SchuleBuilder.defaultSchule();
        Klasse klasse = KlasseBuilder.create(builder -> builder.forSchule(schule));
        EdokidId edokidId = EdokidIdTestGenerator.getNextEdokidId(schule.getSchulhausnummer());
        Kind kind = KindBuilder.create(
            builder -> builder
                .withID(kindId)
                .withEsadId(edokidId.toFormattedString())
                .withKlasse(klasse)
                .withWohntyp("Heim")
                .withName("Meier", "Fridolin")
                .withExternalImportInfo(kindExternalId, true, getAppClock().offsetDateTime().now(), ImportTyp.ESCADA)
        );
        schuleService.persist(schule);
        klasseService.persist(klasse);
        kindService.persistWithoutDuplicateCheck(kind);
        // ACT
        KindJsonSpec result = api
            .kindResourceGetKind()
            .kindIdPath(kind.getId().getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(KindJsonSpec.class);
        // ASSERT
        assertThat(result).isNotNull();
        assertThat(result.getExternalImportInfoJson()).isNotNull();
        assertThat(result.getExternalImportInfoJson().getIdInExternalSystem()).isNotNull().isEqualTo(kindExternalId);
        assertThat(result.getExternalImportInfoJson().getAktivInExternalSystem()).isTrue();
        assertThat(result.getExternalImportInfoJson().getTimestampFirstImport()).isNotNull();
        assertThat(result.getExternalImportInfoJson().getTimestampLatestImport()).isNotNull();
        assertThat(result.getEsadId()).isNotNull().hasSize(12)
            .matches(EDOKID_ID_REGEX_PATTERN);
        assertThat(result.getEsadId()).isEqualTo(edokidId.toFormattedString());
        assertThat(result.getId()).isEqualTo(kind.getId().getId());
        assertThat(result.getData().getName()).isEqualTo("Meier");
        assertThat(result.getData().getVorname()).isEqualTo("Fridolin");
        assertThat(result.getData().getKlasse()).isNotNull();
        assertThat(result.getData().getSchule()).isNotNull();
        assertThat(result.getData().getKlasse().getId()).isEqualTo(klasse.getId().getId());
        assertThat(result.getData().getSchule().getId()).isEqualTo(schule.getId().getId());
        assertThat(result.getData().getWohntyp()).isEqualTo("Heim");

    }

    @Test
    @TestAsAdminBS
    void getKindWithSchuleButNoKlasse() {
        // ARRANGE
        ID<Kind> kindId = ID.random(Kind.class);
        Schule schule = SchuleBuilder.defaultSchule();
        Klasse klasse = KlasseBuilder.create(builder -> builder.forSchule(schule));

        EdokidId edokidId = EdokidIdTestGenerator.getNextEdokidId(schule.getSchulhausnummer());
        Kind kind = KindBuilder.create(
            builder -> builder
                .withID(kindId)
                .withEsadId(edokidId.toFormattedString())
                .withSchule(schule)
                .withKlasse(null)
                .withWohntyp("Heim")
                .withName("Meier", "Fridolin"));
        schuleService.persist(schule);
        klasseService.persist(klasse);
        kindService.persistWithoutDuplicateCheck(kind);
        // ACT
        KindJsonSpec result = api
            .kindResourceGetKind()
            .kindIdPath(kind.getId().getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(KindJsonSpec.class);
        // ASSERT
        assertThat(result).isNotNull();
        assertThat(result.getExternalImportInfoJson()).isNull();
        assertThat(result.getEsadId()).isNotNull().hasSize(12)
            .matches(EDOKID_ID_REGEX_PATTERN);
        assertThat(result.getEsadId()).isEqualTo(edokidId.toFormattedString());
        assertThat(result.getId()).isEqualTo(kind.getId().getId());
        assertThat(result.getData().getName()).isEqualTo("Meier");
        assertThat(result.getData().getVorname()).isEqualTo("Fridolin");
        assertThat(result.getData().getKlasse()).isNull();
        assertThat(result.getData().getSchule()).isNotNull();
        assertThat(result.getData().getSchule().getId()).isEqualTo(schule.getId().getId());
        assertThat(result.getData().getWohntyp()).isEqualTo("Heim");
    }

    @Test
    @TestAsAdminBS
    void findKindKontaktPersonen() {
        // ARRANGE
        Kind kind1 = kindService.persistWithoutDuplicateCheck(KindBuilder.defaultKind());
        KontaktPersonKind mutter1 = KontaktPersonKindBuilder.create(
            builder -> builder
                .forKind(kind1)
                .withBeziehung(KontaktPersonKindBeziehung.MUTTER)
                .withName("Meier", "Miranda"));
        KontaktPersonKind vater1 = KontaktPersonKindBuilder.create(
            builder -> builder
                .forKind(kind1)
                .withBeziehung(KontaktPersonKindBeziehung.VATER)
                .withName("Meier", "Toni"));

        kontaktPersonKindService.persist(mutter1);
        kontaktPersonKindService.persist(vater1);
        Kind kind2 = KindBuilder.defaultKind();
        kindService.persistWithoutDuplicateCheck(kind2);
        KontaktPersonKind mutter2 = KontaktPersonKindBuilder.create(
            builder -> builder
                .forKind(kind2)
                .withBeziehung(KontaktPersonKindBeziehung.MUTTER)
                .withName("Wirth", "Anna"));
        kontaktPersonKindService.persist(mutter2);
        // ACT
        KontaktPersonKindListJsonSpec result = api
            .kindResourceFindAktivKindKontaktPersonen()
            .kindIdPath(kind1.getId().getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(KontaktPersonKindListJsonSpec.class);
        // ASSERT
        assertThat(result).isNotNull();
        assertThat(result.getKontaktPersonenList())
            .hasSize(2)
            .anyMatch(kp -> Objects.equals(mutter1.getId().getId(), kp.getId())
                && Objects.equals(mutter1.getName(), kp.getData().getName())
                && Objects.equals(mutter1.getVorname(), kp.getData().getVorname()))
            .anyMatch(kp -> Objects.equals(vater1.getId().getId(), kp.getId())
                && Objects.equals(vater1.getName(), kp.getData().getName())
                && Objects.equals(vater1.getVorname(), kp.getData().getVorname()))
            .noneMatch(kp -> Objects.equals(mutter2.getId().getId(), kp.getId())
                && Objects.equals(mutter2.getName(), kp.getData().getName())
                && Objects.equals(mutter2.getVorname(), kp.getData().getVorname()));
    }

    @Test
    @TestAsAdminBS
    void findKindKontaktPersonen_shouldNot_find_inaktiv() {
        // ARRANGE
        Kind kind = fixtureService.createKind();
        KontaktPersonKind kontaktPersonKindAktiv = KontaktPersonKindBuilder.create(
            builder -> builder.forKind(kind)
        );
        KontaktPersonKind kontaktPersonKindInaktiv = KontaktPersonKindBuilder.create(
            builder -> builder.forKind(kind)
                .withExternalImportInfo(
                    UUID.randomUUID().toString(),
                    false,
                    getAppClock().offsetDateTime().now(),
                    ImportTyp.ESCADA)
        );

        kontaktPersonKindService.persist(kontaktPersonKindAktiv);
        kontaktPersonKindService.persist(kontaktPersonKindInaktiv);

        // ACT
        KontaktPersonKindListJsonSpec result = api
            .kindResourceFindAktivKindKontaktPersonen()
            .kindIdPath(kind.getId().getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(KontaktPersonKindListJsonSpec.class);

        // ASSERT
        assertThat(result).isNotNull();
        assertThat(result.getKontaktPersonenList()).anyMatch(
            kp -> kontaktPersonKindAktiv.getId().getId().equals(kp.getId())
        ).noneMatch(kp -> kontaktPersonKindInaktiv.getId().getId().equals(kp.getId()));
    }

    @Test
    @TestAsSupportBE
    void createKindWithVerantwortliche() {
        // ARRANGE
        Benutzer arzt = fixtureService.createBenutzerWithOrganisation();
        Organisation organisation = arzt.getSingleOrganisation();
        assertThat(organisation).isNotNull();
        Benutzer mpa = fixtureService.createBenutzer(organisation, FachRolle.MPA);

        KindSaveJsonSpec saveKindJson = KindBuilder.createJaxSpec(
            builder -> builder
                .withName("Verantwortliche", "Test")
                .withGeschlecht(Geschlecht.MAENNLICH)
                .withGeburtsdatum(LocalDate.of(2012, 12, 12)),
            appClock.offsetDateTime().now().toLocalDate()).getData();

        // ACT
        KindSaveResultJsonSpec createdWithVerantwortliche = api
            .kindResourceCreateKindAndDossier()
            .body(saveKindJson)
            .kindSaveModeQuery(KindSaveModeDuplikatSpec.NO_CHECK)
            .arztIdQuery(arzt.getId().getId())
            .mpaIdQuery(mpa.getId().getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.CREATED.getStatusCode())
            .extract().as(KindSaveResultJsonSpec.class);

        DossierJsonSpec dossierJson = createdWithVerantwortliche.getDossierJson();
        assertThat(dossierJson).isNotNull();
        assertThat(dossierJson.getDataNichtMedizinisch().getZustaendigArzt()).isNotNull();
        assertThat(dossierJson.getDataNichtMedizinisch().getZustaendigArzt().getId()).isEqualTo(arzt.getId().getId());
        assertThat(dossierJson.getDataNichtMedizinisch().getZustaendigMPA()).isNotNull();
        assertThat(dossierJson.getDataNichtMedizinisch().getZustaendigMPA().getId()).isEqualTo(mpa.getId().getId());
    }

    @Test
    @TestAsAdminBS
    void createKind() {
        // ARRANGE
        String name = "Create";
        String vorname = "Camila";
        Geschlecht geschlecht = Geschlecht.WEIBLICH;
        LocalDate geburtsdatum = LocalDate.of(2010, 10, 10);
        KindSaveJsonSpec saveKindJson = KindBuilder.createJaxSpec(
            builder -> builder
                .withName(name, vorname)
                .withGeschlecht(geschlecht)
                .withGeburtsdatum(geburtsdatum)
                .withEsadId(EdokidIdTestGenerator.getNextEdokidId(null).toFormattedString()),
            appClock.offsetDateTime().now().toLocalDate()).getData();
        String expeectedHash =
            HashValueGeneratorUtil.generateHashValueForKindValues(name, vorname, geschlecht, geburtsdatum);

        // ACT
        KindSaveResultJsonSpec result = api
            .kindResourceCreateKindAndDossier()
            .body(saveKindJson)
            .kindSaveModeQuery(KindSaveModeDuplikatSpec.CHECK)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.CREATED.getStatusCode())
            .extract().as(KindSaveResultJsonSpec.class);

        // ASSERT
        assertThat(result).isNotNull();
        KindJsonSpec kindResult = result.getKindJson();
        assertThat(kindResult).isNotNull();
        assertThat(kindResult.getId()).isNotNull();
        assertThat(kindResult.getExternalImportInfoJson()).isNull();
        assertThat(kindResult.getEsadId()).isNull();
        assertThat(kindResult.getData().getName()).isEqualTo(name);
        assertThat(kindResult.getData().getVorname()).isEqualTo(vorname);
        assertThat(kindResult.getData().getGeschlecht().toString()).hasToString(geschlecht.toString());
        assertThat(kindResult.getData().getGeburtsdatum()).isEqualTo(geburtsdatum);
        assertThat(kindResult.getHashValue()).isEqualTo(expeectedHash);
    }

    @Test
    @TestAsAdminBS
    void createKind_schuleDifferentFromSchuleInKlasse_shouldUseSchuleInKlasse() {
        // ARRANGE
        Schule schule = SchuleBuilder.create(builder -> builder
            .withSchulhaus("TestSchulhaus"));
        Klasse klasse = KlasseBuilder.defaultKlasse();
        schuleService.persist(klasse.getSchule());
        klasseService.persist(klasse);
        KindSaveJsonSpec saveKindJson = KindBuilder.createJaxSpec(
            builder -> builder
                .withName("Create", "Colin")
                .withKlasse(klasse)
                .withSchule(schule)
                .withEsadId(EdokidIdTestGenerator.getNextEdokidId(klasse.getSchule().getSchulhausnummer())
                    .toFormattedString()),
            appClock.offsetDateTime().now().toLocalDate()).getData();

        // ACT
        KindSaveResultJsonSpec result = api
            .kindResourceCreateKindAndDossier()
            .body(saveKindJson)
            .kindSaveModeQuery(KindSaveModeDuplikatSpec.NO_CHECK)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.CREATED.getStatusCode())
            .extract().as(KindSaveResultJsonSpec.class);

        // ASSERT
        assertThat(result).isNotNull();
        KindJsonSpec kindResult = result.getKindJson();
        assertThat(kindResult).isNotNull();
        assertThat(kindResult.getId()).isNotNull();
        assertThat(kindResult.getEsadId()).isNull();
        assertThat(kindResult.getData().getName()).isEqualTo("Create");
        assertThat(kindResult.getData().getVorname()).isEqualTo("Colin");
        assertThat(kindResult.getData().getSchule()).isNotNull();
        assertThat(kindResult.getData().getSchule().getBezeichnung()).isNotEqualTo(schule.getSchulhaus());
        assertThat(kindResult.getData().getSchule().getBezeichnung()).isEqualTo(klasse.getSchule().getSchulhaus());
    }

    @Test
    @TestAsAdminBS
    void createKind_withSchuleWhichHasSchulhausnummer_EdokidId_contains_schulhausnummer() {
        // ARRANGE
        String schulhausnummer = "263";
        Schule schule =
            schuleService.persist(SchuleBuilder.create(builder -> builder.withSchulhausnummer(schulhausnummer)));
        Klasse klasse = klasseService.persist(KlasseBuilder.create(builder -> builder.forSchule(schule)));
        KindSaveJsonSpec saveKindJson = KindBuilder.createJaxSpec(
            builder -> builder
                .withName("Muster", "Max")
                .withKlasse(klasse),
            appClock.offsetDateTime().now().toLocalDate()).getData();

        // ACT
        KindSaveResultJsonSpec result = api
            .kindResourceCreateKindAndDossier()
            .body(saveKindJson)
            .kindSaveModeQuery(KindSaveModeDuplikatSpec.CHECK)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.CREATED.getStatusCode())
            .extract().as(KindSaveResultJsonSpec.class);

        // ASSERT
        assertThat(result).isNotNull();
        KindJsonSpec kindResult = result.getKindJson();
        assertThat(kindResult).isNotNull();
        assertThat(kindResult.getId()).isNotNull();
        assertThat(kindResult.getEsadId()).isNull();
        assertThat(kindResult.getData().getName()).isEqualTo("Muster");
        assertThat(kindResult.getData().getVorname()).isEqualTo("Max");
    }

    @Test
    @TestAsAdminBS
    void createKind_throws_if_svnr_is_not_unique() {
        // ARRANGE
        KindSaveJsonSpec saveKindJson = KindBuilder.createJaxSpec(
            builder -> builder
                .withName("Duplicate", "Daniel")
                .withSozialversicherungsNummer(SozialversicherungsNummerTestGenerator.INSTANCE.getNextSozialversicherungsnummer())
                .withEsadId(EdokidIdTestGenerator.getNextEdokidId("728").toFormattedString()),
            appClock.offsetDateTime().now().toLocalDate()
        ).getData();

        // save first instance
        KindSaveResultJsonSpec result1 = api
            .kindResourceCreateKindAndDossier()
            .body(saveKindJson)
            .kindSaveModeQuery(KindSaveModeDuplikatSpec.CHECK)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.CREATED.getStatusCode())
            .extract().as(KindSaveResultJsonSpec.class);

        assertThat(result1).isNotNull();

        // ACT
        AppValidationErrorJsonSpec errResult = api
            .kindResourceCreateKindAndDossier()
            .body(saveKindJson)
            .kindSaveModeQuery(KindSaveModeDuplikatSpec.CHECK)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.BAD_REQUEST.getStatusCode())
            .extract().as(AppValidationErrorJsonSpec.class);

        // ASSERT
        assertThat(errResult).isNotNull();
        assertThat(errResult.getType()).isEqualTo(FailureTypeSpec.VALIDATION);
        assertThat(errResult.getViolations()).hasSize(1);
        assertThat(errResult.getViolations().getFirst().getKey())
            .isEqualTo("GenericMessage");
        assertThat(errResult.getViolations().getFirst().getMessage())
            .isEqualTo("Die Sozialversicherungsnummer muss einzigartig sein");
    }

    @Test
    @TestAsAdminBS
    void updateKind() {
        // ARRANGE
        Kind existingKind = kindService.persistWithoutDuplicateCheck(KindBuilder.defaultKind());
        String updatedName = "Updated";
        String updatedVorname = "Person";
        Geschlecht geschlecht = existingKind.getGeschlecht();
        LocalDate geburtsdatum = existingKind.getGeburtsdatum();

        assertThat(existingKind).isNotNull()
            .returns("Emma", Kind::getVorname)
            .returns("Gerber", Kind::getName)
            .returns(null, Kind::getExternalSystemIdOrNull);
        String existingHash = HashValueGeneratorUtil.generateHashValueForKindValues(
            existingKind.getName(),
            existingKind.getVorname(),
            geschlecht,
            geburtsdatum);

        KindJsonSpec updatedKindJson = KindBuilder.createJaxSpec(
            builder -> builder
                .withID(existingKind.getId())
                .withName(updatedName, updatedVorname)
                .withSozialversicherungsNummer(SozialversicherungsNummerTestGenerator.INSTANCE.getNextSozialversicherungsnummer()),
            appClock.offsetDateTime().now().toLocalDate());
        String expectedHash =
            HashValueGeneratorUtil.generateHashValueForKindValues(
                updatedName,
                updatedVorname,
                geschlecht,
                geburtsdatum);

        // ACT
        KindSaveResultJsonSpec result = api
            .kindResourceUpdateKindAndVerantwortlichePersonen()
            .kindIdPath(existingKind.getId().getId())
            .body(updatedKindJson.getData())
            .kindSaveModeQuery(KindSaveModeDuplikatSpec.CHECK)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(KindSaveResultJsonSpec.class);

        // ASSERT
        assertThat(result).isNotNull();
        assertThat(result.getIdsOfPotentialDuplicates()).isNull();
        KindJsonSpec kindResult = result.getKindJson();
        assertThat(kindResult).isNotNull();
        assertThat(kindResult.getId()).isEqualTo(existingKind.getId().getId());
        assertThat(kindResult.getData().getName()).isEqualTo(updatedName);
        assertThat(kindResult.getData().getVorname()).isEqualTo(updatedVorname);
        assertThat(kindResult.getData().getGeschlecht().toString()).hasToString(geschlecht.toString());
        assertThat(kindResult.getData().getGeburtsdatum()).isEqualTo(geburtsdatum);
        assertThat(kindResult.getHashValue()).isEqualTo(expectedHash).isNotEqualTo(existingHash);
        assertThat(kindResult.getEsadId()).isNull();
    }

    @Test
    @TestAsAdminBS
    void updateKind_throws_if_kindIsInactive() {
        // ARRANGE
        Kind existingKind = fixtureService.createInactiveKind();
        String updatedName = "Updated";
        String updatedVorname = "Person";

        assertThat(existingKind).isNotNull()
            .returns("Emma", Kind::getVorname)
            .returns("Gerber", Kind::getName)
            .returns(null, Kind::getExternalSystemIdOrNull);

        KindJsonSpec updatedKindJson = KindBuilder.createJaxSpec(
            builder -> builder
                .withID(existingKind.getId())
                .withName(updatedName, updatedVorname)
                .withSozialversicherungsNummer(SozialversicherungsNummerTestGenerator.INSTANCE.getNextSozialversicherungsnummer()),
            appClock.offsetDateTime().now().toLocalDate());

        // ACT
        AppValidationErrorJsonSpec error = api
            .kindResourceUpdateKindAndVerantwortlichePersonen()
            .kindIdPath(existingKind.getId().getId())
            .body(updatedKindJson.getData())
            .kindSaveModeQuery(KindSaveModeDuplikatSpec.CHECK)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.BAD_REQUEST.getStatusCode())
            .extract().as(AppValidationErrorJsonSpec.class);

        // ASSERT
        AppValidationErrorJsonSpecAssert.assertThatAppValidationErrorJsonSpec(error)
            .extractingExactlyOneViolation()
            .hasRawMessageContaining("Das Kind "
                + existingKind.getNameVorname()
                + " "
                + existingKind.getId()
                + " ist inaktiv. Es können keine Änderungen vorgenommen werden.");
    }

    @Test
    @TestAsAdminBS
    void changeKindAktivStatus_should_work() {
        // ARRANGE
        Kind existingKind = fixtureService.createKind();
        ID<Kind> existingKindId = existingKind.getId();
        EntityAktivStatusSpec desiredStatus = EntityAktivStatusSpec.INAKTIV;
        EntityAktivStatusSaveJsonSpec saveJson = new EntityAktivStatusSaveJsonSpec(EntityAktivStatusSpec.INAKTIV);

        // ACT
        KindJsonSpec result = api.kindResourceUpdateKindAktivStatus()
            .kindIdPath(existingKindId.getId())
            .body(saveJson)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract()
            .as(KindJsonSpec.class);

        assertThat(result.getData().getKindAktivStatus()).isEqualTo(EntityAktivStatusSpec.INAKTIV);

        // ASSERT
        KindJsonSpec jsonSpec = api.kindResourceGetKind()
            .kindIdPath(existingKindId.getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(KindJsonSpec.class);

        assertThat(jsonSpec.getData().getKindAktivStatus()).isEqualTo(desiredStatus);
    }

    @Test
    @TestAsAdminBS
    void removeKind() {
        // ARRANGE
        Kind kindToDelete = kindService.persistWithoutDuplicateCheck(KindBuilder.defaultKind());

        // ACT
        api.kindResourceRemoveKind()
            .kindIdPath(kindToDelete.getId().getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.NO_CONTENT.getStatusCode());

        // ASSERT
        // Verify the kind no longer exists by attempting to fetch it
        AppFailureErrorJsonSpec fail = api.kindResourceGetKind()
            .kindIdPath(kindToDelete.getId().getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode())
            .extract()
            .as(AppFailureErrorJsonSpec.class);
        assertThat(fail.getSummary()).contains("Entity not found");
    }

    @Test
    @TestAsAdminBS
    void findKinderBySchule() {
        // ARRANGE
        Schule schule1 = schuleService.persist(SchuleBuilder.defaultSchule());
        Schule schule2 = schuleService.persist(SchuleBuilder.defaultSchule());
        Klasse klasse1OfSchule1 = klasseService.persist(KlasseBuilder.create(builder -> builder.forSchule(schule1)));
        Klasse klasse2OfSchule1 = klasseService.persist(KlasseBuilder.create(builder -> builder.forSchule(schule1)));
        Klasse klasseOfSchule2 = klasseService.persist(KlasseBuilder.create(builder -> builder.forSchule(schule2)));
        Kind kindSchule1Klasse1 = kindService.persistWithoutDuplicateCheck(KindBuilder.create(builder -> builder
            .withKlasse(klasse1OfSchule1)));
        Kind kindSchule1Klasse2 = kindService.persistWithoutDuplicateCheck(KindBuilder.create(builder -> builder
            .withKlasse(klasse2OfSchule1)));
        Kind kindSchule2 = kindService.persistWithoutDuplicateCheck(KindBuilder.create(builder -> builder
            .withKlasse(klasseOfSchule2)));
        // ACT / ASSERT
        // Kinder von Schule 1
        KindListJsonSpec resultSchule1 = api
            .kindResourceFindKinderBySchuleOrKlasse()
            .schuleIdQuery(schule1.getId().getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(KindListJsonSpec.class);
        assertThat(resultSchule1.getKinderList())
            .isNotEmpty()
            .hasSize(2)
            .anyMatch(kindJsonSpec -> kindSchule1Klasse1.getId().getId().equals(kindJsonSpec.getId()))
            .anyMatch(kindJsonSpec -> kindSchule1Klasse2.getId().getId().equals(kindJsonSpec.getId()));
        // Kinder von Schule 2
        KindListJsonSpec resultSchule2 = api
            .kindResourceFindKinderBySchuleOrKlasse()
            .schuleIdQuery(schule2.getId().getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(KindListJsonSpec.class);
        assertThat(resultSchule2.getKinderList())
            .isNotEmpty()
            .hasSize(1)
            .anyMatch(kindJsonSpec -> kindSchule2.getId().getId().equals(kindJsonSpec.getId()));
        // Kinder von Klasse 1 / Schule 1
        KindListJsonSpec resultKlasse1 = api
            .kindResourceFindKinderBySchuleOrKlasse()
            .schuleIdQuery(schule1.getId().getId())
            .klasseIdQuery(klasse1OfSchule1.getId().getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(KindListJsonSpec.class);
        assertThat(resultKlasse1.getKinderList())
            .isNotEmpty()
            .hasSize(1)
            .anyMatch(kindJsonSpec -> kindSchule1Klasse1.getId().getId().equals(kindJsonSpec.getId()));
        // Kinder von Klasse 2 / Schule 1
        KindListJsonSpec resultKlasse2 = api
            .kindResourceFindKinderBySchuleOrKlasse()
            .klasseIdQuery(klasse2OfSchule1.getId().getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(KindListJsonSpec.class);
        assertThat(resultKlasse2.getKinderList())
            .isNotEmpty()
            .hasSize(1)
            .anyMatch(kindJsonSpec -> kindSchule1Klasse2.getId().getId().equals(kindJsonSpec.getId()));
        // Kinder von Klasse 1 Schule 2
        KindListJsonSpec resultKlasseSchule2 = api
            .kindResourceFindKinderBySchuleOrKlasse()
            .klasseIdQuery(klasseOfSchule2.getId().getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(KindListJsonSpec.class);
        assertThat(resultKlasseSchule2.getKinderList())
            .isNotEmpty()
            .hasSize(1)
            .anyMatch(kindJsonSpec -> kindSchule2.getId().getId().equals(kindJsonSpec.getId()));
    }

    @Test
    @TestAsAdminBS
    void testCreationOfEdokidId() {
        // ARRANGE
        Kind kind1 = kindService.persistWithoutDuplicateCheck(KindBuilder.defaultKind());
        // ACT
        var result = api.kindResourceGenerateEsadIdForKind()
            .kindIdPath(kind1.getId().getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(KindJsonSpec.class);
        // ASSERT
        Assertions.assertThat(result.getEsadId()).isNotNull()
            .matches(Pattern.compile(EDOKID_ID_NO_SCHOOL_REGEX_PATTERN));
    }

    @Test
    @TestAsAdminBS
    void testCreationOfEdokidId_throws_if_kind_is_inactive() {
        // ARRANGE
        Kind kind = kindService.persistWithoutDuplicateCheck(
            KindBuilder.create(builder -> builder
                .withKindAktivStatus(EntityAktivStatus.INAKTIV)
            ));

        String expectedValidationErrorMessage = String.format(
            "Das Kind %s %s ist inaktiv. Es können keine Änderungen vorgenommen werden.",
            kind.getNameVorname(),
            kind.getId()
        );

        // ACT
        AppValidationErrorJsonSpec validationError = api.kindResourceGenerateEsadIdForKind()
            .kindIdPath(kind.getId().getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.BAD_REQUEST.getStatusCode())
            .extract().as(AppValidationErrorJsonSpec.class);

        assertThat(validationError.getViolations()).hasSize(1);
        assertThat(validationError.getViolations().getFirst().getKey())
            .isEqualTo("CannotEditInactiveKind");
        assertThat(validationError.getViolations().getFirst().getMessage())
            .isEqualTo(expectedValidationErrorMessage);
    }

    @Test
    @TestAsAdminBS
    void updateKind_set_esadId() {
        // ARRANGE
        Kind kind1 = kindService.persistWithoutDuplicateCheck(KindBuilder.defaultKind());
        String newEsadId = "A4 32 154 32";

        // ACT
        EsadIdJsonSpec updateEsadId = new EsadIdJsonSpec(newEsadId);
        KindJsonSpec result = api
            .kindResourceSetEsadIdForKind()
            .kindIdPath(kind1.getId().getId())
            .body(updateEsadId)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(KindJsonSpec.class);

        // ASSERT
        assertThat(result).isNotNull();
        assertThat(result.getEsadId()).isEqualTo(newEsadId);
    }

    @Test
    @TestAsAdminBS
    void updateKind_set_esadId_throws_if_esadId_is_not_unique() {
        // ARRANGE
        Kind kind1 = kindService.persistWithoutDuplicateCheck(KindBuilder.defaultKind());
        Kind kind2 = kindService.persistWithoutDuplicateCheck(KindBuilder.defaultKind());
        String newEsadId = "A5 45 123 78";

        EsadIdJsonSpec updateEsadId = new EsadIdJsonSpec(newEsadId);
        KindJsonSpec result = api
            .kindResourceSetEsadIdForKind()
            .kindIdPath(kind1.getId().getId())
            .body(updateEsadId)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(KindJsonSpec.class);
        assertThat(result).isNotNull();
        assertThat(result.getEsadId()).isEqualTo(newEsadId);

        // ACT
        AppValidationErrorJsonSpec errResult = api
            .kindResourceSetEsadIdForKind()
            .kindIdPath(kind2.getId().getId())
            .body(updateEsadId)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.BAD_REQUEST.getStatusCode())
            .extract()
            .as(AppValidationErrorJsonSpec.class);

        // ASSERT
        assertThat(errResult).isNotNull();
        assertThat(errResult.getType()).isEqualTo(FailureTypeSpec.VALIDATION);
        assertThat(errResult.getViolations()).hasSize(1);
        assertThat(errResult.getViolations().getFirst().getKey())
            .isEqualTo("GenericMessage");
        assertThat(errResult.getViolations().getFirst().getMessage())
            .isEqualTo("ID Nr. muss einzigartig sein");
    }

    @Test
    @TestAsAdminBS
    void updateKind_set_esadId_throws_if_kind_is_inactive() {
        // ARRANGE
        Kind kind = fixtureService.createInactiveKind();

        String newEsadId = "A54512378";

        EsadIdJsonSpec updateEsadId = new EsadIdJsonSpec(newEsadId);

        String expectedValidationErrorMessage = String.format(
            "Das Kind %s %s ist inaktiv. Es können keine Änderungen vorgenommen werden.",
            kind.getNameVorname(),
            kind.getId());

        // ACT
        AppValidationErrorJsonSpec validationError = api
            .kindResourceSetEsadIdForKind()
            .kindIdPath(kind.getId().getId())
            .body(updateEsadId)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.BAD_REQUEST.getStatusCode())
            .extract()
            .as(AppValidationErrorJsonSpec.class);

        assertThat(validationError.getViolations()).hasSize(1);
        assertThat(validationError.getViolations().getFirst().getKey())
            .isEqualTo("CannotEditInactiveKind");
        assertThat(validationError.getViolations().getFirst().getMessage())
            .isEqualTo(expectedValidationErrorMessage);
    }

    @Test
    @TestAsAdminBS
    void createKind_duplikat_force_false() {
        // ARRANGE
        String name = TestUtil.getRandomString("Muster");
        LocalDate geburtsdatum = LocalDate.of(2010, Month.JANUARY, 31);
        Geschlecht geschlecht = Geschlecht.MAENNLICH;
        // Prepare Kind that we want to identify as duplicate later
        Kind existingKind = fixtureService.createKind(name, name, geburtsdatum, geschlecht);
        // Wir erstellen ein neues Kind mit den gleichen Daten, aber ohne forceSave
        KindSaveJsonSpec saveKindJson = KindBuilder.createJaxSpec(
            builder -> builder
                .withName(name, name)
                .withGeburtsdatum(geburtsdatum)
                .withGeschlecht(geschlecht),
            appClock.offsetDateTime().now().toLocalDate()).getData();

        // ACT
        KindSaveResultJsonSpec result = api
            .kindResourceCreateKindAndDossier()
            .body(saveKindJson)
            .kindSaveModeQuery(KindSaveModeDuplikatSpec.CHECK)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.CREATED.getStatusCode())
            .extract().as(KindSaveResultJsonSpec.class);

        // ASSERT
        assertThat(result).isNotNull();
        assertThat(result.getKindJson()).isNull();
        assertThat(result.getIdsOfPotentialDuplicates()).isNotNull()
            .isNotEmpty()
            .containsExactly(existingKind.getId().getId());
    }

    @Test
    @TestAsAdminBS
    void createKind_duplikat_force_true() {
        // ARRANGE
        String name = TestUtil.getRandomString("Muster");
        LocalDate geburtsdatum = LocalDate.of(2010, Month.JANUARY, 31);
        Geschlecht geschlecht = Geschlecht.MAENNLICH;
        // Prepare Kind that we want to identify as duplicate later
        Kind existingKind = fixtureService.createKind(name, name, geburtsdatum, geschlecht);
        // Wir erstellen ein neues Kind mit den gleichen Daten, aber ohne forceSave
        KindSaveJsonSpec saveKindJson = KindBuilder.createJaxSpec(
            builder -> builder
                .withName(name, name)
                .withGeburtsdatum(geburtsdatum)
                .withGeschlecht(geschlecht),
            appClock.offsetDateTime().now().toLocalDate()).getData();

        // ACT
        KindSaveResultJsonSpec result = api
            .kindResourceCreateKindAndDossier()
            .body(saveKindJson)
            .kindSaveModeQuery(KindSaveModeDuplikatSpec.MARK_AS_FALSE_POSITIVE)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.CREATED.getStatusCode())
            .extract().as(KindSaveResultJsonSpec.class);

        // ASSERT
        assertThat(result).isNotNull();
        assertThat(result.getKindJson()).isNotNull();
        assertThat(result.getKindJson().getId()).isNotEqualTo(existingKind.getId().getId());
        assertThat(result.getKindJson().getData().getName()).isEqualTo(existingKind.getName());
        assertThat(result.getIdsOfPotentialDuplicates()).isNull();
    }

    @Test
    @TestAsAdminBS
    void updateKind_duplikat_force_false() {
        // ARRANGE
        String name = TestUtil.getRandomString("Muster");
        LocalDate geburtsdatum = LocalDate.of(2010, Month.JANUARY, 31);
        Geschlecht geschlecht = Geschlecht.MAENNLICH;
        // Prepare Kind that we want to identify as duplicate later
        Kind existingKind = fixtureService.createKind(name, name, geburtsdatum, geschlecht);
        Kind kind = kindService.persistWithoutDuplicateCheck(KindBuilder.defaultKind());

        // ACT
        KindJsonSpec updatedKindJson = KindBuilder.createJaxSpec(
            builder -> builder
                .withID(kind.getId())
                .withName(name, name)
                .withGeburtsdatum(geburtsdatum)
                .withGeschlecht(geschlecht),
            appClock.offsetDateTime().now().toLocalDate());
        KindSaveResultJsonSpec result = api
            .kindResourceUpdateKindAndVerantwortlichePersonen()
            .kindIdPath(kind.getId().getId())
            .body(updatedKindJson.getData())
            .kindSaveModeQuery(KindSaveModeDuplikatSpec.CHECK)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(KindSaveResultJsonSpec.class);

        // ASSERT
        assertThat(result).isNotNull();
        assertThat(result.getKindJson()).isNull();
        assertThat(result.getIdsOfPotentialDuplicates()).isNotNull()
            .isNotEmpty()
            .containsExactly(existingKind.getId().getId());
    }

    @Test
    @TestAsAdminBS
    void updateKind_duplikat_force_true() {
        // ARRANGE
        String name = TestUtil.getRandomString("Muster");
        LocalDate geburtsdatum = LocalDate.of(2010, Month.JANUARY, 31);
        Geschlecht geschlecht = Geschlecht.MAENNLICH;
        // Prepare Kind that we want to identify as duplicate later
        Kind existingKind = fixtureService.createKind(name, name, geburtsdatum, geschlecht);
        Kind kind = kindService.persistWithoutDuplicateCheck(KindBuilder.defaultKind());

        // ACT
        KindJsonSpec updatedKindJson = KindBuilder.createJaxSpec(
            builder -> builder
                .withID(kind.getId())
                .withName(name, name)
                .withGeburtsdatum(geburtsdatum)
                .withGeschlecht(geschlecht),
            appClock.offsetDateTime().now().toLocalDate());
        KindSaveResultJsonSpec result = api
            .kindResourceUpdateKindAndVerantwortlichePersonen()
            .kindIdPath(kind.getId().getId())
            .body(updatedKindJson.getData())
            .kindSaveModeQuery(KindSaveModeDuplikatSpec.MARK_AS_FALSE_POSITIVE)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(KindSaveResultJsonSpec.class);

        // ASSERT
        assertThat(result).isNotNull();
        assertThat(result.getKindJson()).isNotNull();
        assertThat(result.getKindJson().getId()).isNotEqualTo(existingKind.getId().getId());
        assertThat(result.getKindJson().getData().getName()).isEqualTo(existingKind.getName());
        assertThat(result.getIdsOfPotentialDuplicates()).isNull();
    }

    @Test
    @TestAsAdminBE
    void updateSchuleAndKlasse_setsSchuleAndKlasseOnKindWithoutDossier() {
        // ARRANGE
        tenantProducer.setCurrentTenant(TenantEnum.BE);

        Kind kind = kindService.persistWithoutDuplicateCheck(KindBuilder.defaultKind());
        Schule schule = schuleService.persist(SchuleBuilder.defaultSchule());
        Klasse klasse = klasseService.persist(KlasseBuilder.create(builder -> builder.forSchule(schule)));

        KindSchuleAndKlasseSaveJsonSpec body = buildSchuleAndKlasseBody(schule, klasse, kind.getVersion());

        // ACT
        KindSaveResultJsonSpec result  = api
            .kindResourceUpdateSchuleAndKlasse()
            .kindIdPath(kind.getId().getId())
            .body(body)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(KindSaveResultJsonSpec.class);

        // ASSERT
        assertThat(result).isNotNull();
        assertThat(result.getKindJson()).isNotNull();
        assertThat(result.getKindJson().getData().getSchule()).isNotNull();
        assertThat(result.getKindJson().getData().getSchule().getId()).isEqualTo(schule.getId().getId());
        assertThat(result.getKindJson().getData().getKlasse()).isNotNull();
        assertThat(result.getKindJson().getData().getKlasse().getId()).isEqualTo(klasse.getId().getId());
        assertThat(result.getIdsOfPotentialDuplicates()).isNull();
    }

    @Test
    @TestAsAdminBE
    void updateSchuleAndKlasse_clearsSchuleAndKlasse() {
        // ARRANGE
        tenantProducer.setCurrentTenant(TenantEnum.BE);

        Schule schule = schuleService.persist(SchuleBuilder.defaultSchule());
        Klasse klasse = klasseService.persist(KlasseBuilder.create(builder -> builder.forSchule(schule)));
        Kind kind = kindService.persistWithoutDuplicateCheck(
            KindBuilder.create(builder -> builder
                .withSchule(schule)
                .withKlasse(klasse)));

        KindSchuleAndKlasseSaveJsonSpec body = new KindSchuleAndKlasseSaveJsonSpec();
        body.setSchule(null);
        body.setKlasse(null);
        body.setVersion(kind.getVersion());

        // ACT
        KindSaveResultJsonSpec result = api
            .kindResourceUpdateSchuleAndKlasse()
            .kindIdPath(kind.getId().getId())
            .body(body)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(KindSaveResultJsonSpec.class);

        // ASSERT
        assertThat(result).isNotNull();
        assertThat(result.getKindJson()).isNotNull();
        assertThat(result.getKindJson().getData().getSchule()).isNull();
        assertThat(result.getKindJson().getData().getKlasse()).isNull();
    }

    @Test
    @TestAsAdminBE
    void updateSchuleAndKlasse_schuleDifferentFromSchuleInKlasse_shouldUseSchuleFromKlasse() {
        // ARRANGE
        tenantProducer.setCurrentTenant(TenantEnum.BE);

        Kind kind = kindService.persistWithoutDuplicateCheck(KindBuilder.defaultKind());
        Schule otherSchule = schuleService.persist(SchuleBuilder.create(builder -> builder
            .withSchulhaus("OtherSchulhaus")));
        Schule schuleOfKlasse = schuleService.persist(SchuleBuilder.create(builder -> builder
            .withSchulhaus("SchulhausOfKlasse")));
        Klasse klasse = klasseService.persist(KlasseBuilder.create(builder -> builder.forSchule(schuleOfKlasse)));

        KindSchuleAndKlasseSaveJsonSpec body = buildSchuleAndKlasseBody(otherSchule, klasse, kind.getVersion());

        // ACT
        KindSaveResultJsonSpec result = api
            .kindResourceUpdateSchuleAndKlasse()
            .kindIdPath(kind.getId().getId())
            .body(body)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(KindSaveResultJsonSpec.class);

        // ASSERT
        // Wenn die uebergebene Schule nicht zur Klasse passt, gewinnt die Schule der Klasse
        assertThat(result).isNotNull();
        assertThat(result.getKindJson()).isNotNull();
        assertThat(result.getKindJson().getData().getSchule()).isNotNull();
        assertThat(result.getKindJson().getData().getSchule().getId()).isEqualTo(schuleOfKlasse.getId().getId());
        assertThat(result.getKindJson().getData().getKlasse()).isNotNull();
        assertThat(result.getKindJson().getData().getKlasse().getId()).isEqualTo(klasse.getId().getId());
    }

    @Test
    @TestAsAdminBE
    void updateSchuleAndKlasse_throws_if_kind_is_inactive() {
        // ARRANGE
        tenantProducer.setCurrentTenant(TenantEnum.BE);

        Kind kind = fixtureService.createInactiveKind();
        Schule schule = schuleService.persist(SchuleBuilder.defaultSchule());
        Klasse klasse = klasseService.persist(KlasseBuilder.create(builder -> builder.forSchule(schule)));

        KindSchuleAndKlasseSaveJsonSpec body = buildSchuleAndKlasseBody(schule, klasse, kind.getVersion());

        // ACT
        AppValidationErrorJsonSpec validationError = api
            .kindResourceUpdateSchuleAndKlasse()
            .kindIdPath(kind.getId().getId())
            .body(body)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.BAD_REQUEST.getStatusCode())
            .extract().as(AppValidationErrorJsonSpec.class);

        // ASSERT
        assertThat(validationError.getViolations()).hasSize(1);
        assertThat(validationError.getViolations().getFirst().getKey())
            .isEqualTo("CannotEditInactiveKind");
    }

    @Test
    @TestAsAdminBE
    void updateSchuleAndKlasse_withDossier_noSchuleChange_doesNotResetVerantwortliche() {
        // ARRANGE
        tenantProducer.setCurrentTenant(TenantEnum.BE);

        Schule schule = schuleService.persist(SchuleBuilder.defaultSchule());
        Klasse oldKlasse = klasseService.persist(KlasseBuilder.create(builder -> builder.forSchule(schule)));
        Klasse newKlasse = klasseService.persist(KlasseBuilder.create(builder -> builder.forSchule(schule)));
        Kind kind = kindService.persistWithoutDuplicateCheck(
            KindBuilder.create(builder -> builder
                .withSchule(schule)
                .withKlasse(oldKlasse)));
        Benutzer arzt = fixtureService.createBenutzerWithOrganisation();
        dossierService.persist(DossierBuilder.create(builder -> builder
            .forKind(kind)
            .withZustaendigArzt(arzt)));

        KindSchuleAndKlasseSaveJsonSpec body = buildSchuleAndKlasseBody(schule, newKlasse, kind.getVersion());

        // ACT
        api
            .kindResourceUpdateSchuleAndKlasse()
            .kindIdPath(kind.getId().getId())
            .body(body)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(KindSaveResultJsonSpec.class);

        // ASSERT - Schule unveraendert => Verantwortliche bleiben bestehen
        Dossier reloaded = dossierService.findByKind(kind.getId()).orElseThrow();
        assertThat(reloaded.getZustaendigArzt()).isNotNull();
        assertThat(reloaded.getZustaendigArzt().getId()).isEqualTo(arzt.getId());
        Kind reloadedKind = kindService.getById(kind.getId());
        assertThat(reloadedKind.getKlasse()).isNotNull();
        assertThat(reloadedKind.getKlasse().getId()).isEqualTo(newKlasse.getId());
        assertThat(reloadedKind.getSchule()).isNotNull();
        assertThat(reloadedKind.getSchule().getId()).isEqualTo(schule.getId());
    }

    @Test
    @TestAsSchularztBE
    void updateSchuleAndKlasse_withDossier_schuleChanged_triggersHandlingOfVerantwortliche() {
        // ARRANGE
        tenantProducer.setCurrentTenant(TenantEnum.BE);
        Benutzer arzt = fixtureService.readBenutzerWithBerechtigungen(TestAsSchularztBE.BENUTZER_ID_ID);
        Benutzer newArzt = fixtureService.createBenutzerWithOrganisation();

        Schule oldSchule = schuleService.persist(SchuleBuilder.create(builder -> builder
            .withSchulhaus("AlteSchule").withZustaendigArzt(arzt)));
        Schule newSchule = schuleService.persist(SchuleBuilder.create(builder -> builder
            .withSchulhaus("NeueSchule").withZustaendigArzt(newArzt)));
        Klasse newKlasse = klasseService.persist(KlasseBuilder.create(builder -> builder.forSchule(newSchule)));

        Kind kind = kindService.persistWithoutDuplicateCheck(
            KindBuilder.create(builder -> builder
                .withSchule(oldSchule)));

        Dossier dossier = fixtureService.createDossier(kind, arzt);

        KindSchuleAndKlasseSaveJsonSpec body = buildSchuleAndKlasseBody(newSchule, newKlasse, kind.getVersion());

        // ACT
        KindSaveResultJsonSpec result = api
            .kindResourceUpdateSchuleAndKlasse()
            .kindIdPath(kind.getId().getId())
            .body(body)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(KindSaveResultJsonSpec.class);

        // ASSERT
        assertThat(result).isNotNull();
        assertThat(result.getKindJson()).isNotNull();
        assertThat(result.getKindJson().getData().getSchule()).isNotNull();
        assertThat(result.getKindJson().getData().getSchule().getId()).isEqualTo(newSchule.getId().getId());

        List<Archivdokument> archivdokumente = archivdokumentService.findAll();
        assertThat(archivdokumente.stream().anyMatch(archiv -> archiv.getDossierID().equals(dossier.getId().getUUID()))).isTrue();
    }

    @Test
    @TestAsSchularztBE
    void updateSchuleAndKlasse_throws_if_user_not_zustaendig() {
        // ARRANGE
        tenantProducer.setCurrentTenant(TenantEnum.BE);

        // Dossier wird einem anderen Arzt einer anderen Organisation zugewiesen,
        // der aktuelle Benutzer (TestAsSupportBE) ist nicht zustaendig
        Benutzer otherArzt = fixtureService.createBenutzerWithOrganisation();
        Schule schule = schuleService.persist(SchuleBuilder.defaultSchule());
        Klasse klasse = klasseService.persist(KlasseBuilder.create(builder -> builder.forSchule(schule)));
        Kind kind = kindService.persistWithoutDuplicateCheck(KindBuilder.defaultKind());
        dossierService.persist(DossierBuilder.create(builder -> builder
            .forKind(kind)
            .withZustaendigArzt(otherArzt)));

        KindSchuleAndKlasseSaveJsonSpec body = buildSchuleAndKlasseBody(schule, klasse, kind.getVersion());

        // ACT
        AppValidationErrorJsonSpec validationError = api
            .kindResourceUpdateSchuleAndKlasse()
            .kindIdPath(kind.getId().getId())
            .body(body)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.BAD_REQUEST.getStatusCode())
            .extract().as(AppValidationErrorJsonSpec.class);

        // ASSERT
        assertThat(validationError.getViolations()).isNotEmpty();
        assertThat(validationError.getViolations().getFirst().getKey())
            .isEqualTo("ZustaendigkeitCanOnlyBeChangedByZustaendiger");
    }

    private KindSchuleAndKlasseSaveJsonSpec buildSchuleAndKlasseBody(
        @Nullable Schule schule,
        @Nullable Klasse klasse,
        long version
    ) {
        KindSchuleAndKlasseSaveJsonSpec body = new KindSchuleAndKlasseSaveJsonSpec();
        if (schule != null) {
            body.setSchule(toSchuleOptionSpec(schule));
        }
        if (klasse != null) {
            body.setKlasse(toKlasseOptionSpec(klasse));
        }
        body.setVersion(version);
        return body;
    }

    private SchuleOptionJsonSpec toSchuleOptionSpec(@NonNull Schule schule) {
        SchuleOptionJsonSpec spec = new SchuleOptionJsonSpec();
        spec.setId(schule.getId().getId());
        spec.setBezeichnung(schule.getSchulhaus());
        spec.setAktivStatus(EntityAktivStatusSpec.valueOf(schule.getSchuleAktivStatus().name()));
        return spec;
    }

    private KlasseOptionJsonSpec toKlasseOptionSpec(@NonNull Klasse klasse) {
        KlasseOptionJsonSpec spec = new KlasseOptionJsonSpec();
        spec.setId(klasse.getId().getId());
        spec.setBezeichnung(klasse.getBezeichnung());
        spec.setAktivStatus(EntityAktivStatusSpec.valueOf(klasse.getKlasseAktivStatus().name()));
        return spec;
    }
}
