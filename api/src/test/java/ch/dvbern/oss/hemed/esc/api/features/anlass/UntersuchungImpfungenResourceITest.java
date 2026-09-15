package ch.dvbern.oss.hemed.esc.api.features.anlass;

import ch.dvbern.dvbstarter.shared.clock.AppClock;
import ch.dvbern.oss.hemed.esc.api.Util;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.AnlassConfig;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.AnlassConfigRepo;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.types.RequirementSetting;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anonymereintrag.AnonymerEintrag;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfstatusKrankheitMapper;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfungIntern;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfungInternService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfungen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfungenService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.types.Immunsuppression;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.types.ImpfStatus;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.types.ImpfStatusMinimal;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.types.ImpfausweisGeprueft;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.types.ImpfausweisGrundUnvollstaendig;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.types.ImpfausweisVollstaendig;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.types.ImpfempfehlungMinimal;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.types.ImpfschutzGegeben;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.types.ImpfungDurchgefuehrtMinimal;
import ch.dvbern.oss.hemed.esc.api.features.anlass.massnahmen.UntersuchungImpfungenMassnahmen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.massnahmen.types.InformationEltern;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.AnlassTyp;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.UntersuchungService;
import ch.dvbern.oss.hemed.esc.api.features.impfstoff.Impfstoff;
import ch.dvbern.oss.hemed.esc.api.features.impfstoff.types.Krankheit;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Dossier;
import ch.dvbern.oss.hemed.esc.api.features.kinder.DossierService;
import ch.dvbern.oss.hemed.esc.api.shared.aufgabe.Aufgabe;
import ch.dvbern.oss.hemed.esc.api.shared.aufgabe.AufgabeService;
import ch.dvbern.oss.hemed.esc.api.shared.aufgabe.types.AufgabeStatus;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantEnum;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantProducer;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import ch.dvbern.oss.hemed.esc.api.testutil.AbstractITest;
import ch.dvbern.oss.hemed.esc.api.testutil.FixtureService;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.AnlassConfigBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungImpfstatusKrankheitBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungImpfungInternBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungImpfungenBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungImpfungenDataMinimalBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungImpfungenMassnahmenBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungImpfungenNurUnvollstaendigeBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.restassured.RequestSpecUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsAdminBS;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsSchularztBE;
import ch.dvbern.oss.hemed.esc.generated.restassured.api.UntersuchungImpfungenApiSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.AppFailureErrorJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.BenutzerOptionJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.ImpfStatusSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.ImpfschutzGegebenSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.InformationElternSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.KrankheitSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.UntersuchungImpfstatusKrankheitSaveJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.UntersuchungImpfstatusListSaveJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.UntersuchungImpfungInternJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.UntersuchungImpfungInternListJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.UntersuchungImpfungInternSaveJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.UntersuchungImpfungenDataJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.UntersuchungImpfungenDataMinimalJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.UntersuchungImpfungenJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.UntersuchungImpfungenMassnahmenJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.UntersuchungImpfungenNurUnvollstaendigeJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.UntersuchungImpfungenSaveJsonSpec;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.ResponseBody;
import jakarta.ws.rs.core.Response.Status;
import lombok.RequiredArgsConstructor;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static ch.dvbern.oss.hemed.esc.api.testutil.restassured.RequestSpecUtil.toHasAnonymerEintragIdWrapper;
import static ch.dvbern.oss.hemed.esc.api.testutil.restassured.RequestSpecUtil.toHasUntersuchungIdWrapper;

@QuarkusTest
@RequiredArgsConstructor
class UntersuchungImpfungenResourceITest extends AbstractITest {

	private final UntersuchungImpfungenApiSpec api =
			UntersuchungImpfungenApiSpec.untersuchungImpfungen(RequestSpecUtil.quarkusSpec());

	private final UntersuchungService untersuchungService;
	private final UntersuchungImpfungenService untersuchungImpfungenService;
	private final UntersuchungImpfstatusKrankheitMapper untersuchungImpfstatusKrankheitMapper;
	private final UntersuchungImpfungInternService untersuchungImpfungInternService;
    private final AnlassConfigRepo anlassConfigRepo;
	private final FixtureService fixtureService;
	private final AufgabeService aufgabeService;
	private final DossierService dossierService;
	private final AppClock appClock;
    private final TenantProducer tenantProducer;

	@Override
	@BeforeEach
	public void setUp() {
		super.setUp();
	}

	@Test
	@TestAsAdminBS
	void getUntersuchungImpfungen_Data_non_existing_should_create_data() {
		// ARRANGE
		Einladung einladung = fixtureService.createEinladung();
		untersuchungService.getOrCreateUntersuchung(einladung.getId());
		// ACT / ASSERT
		UntersuchungImpfungenDataJsonSpec data = api.untersuchungImpfungenResourceGetUntersuchungImpfungenData()
				.anlassIdPath(einladung.getAnlass().getId().getId())
				.einladungIdPath(toHasUntersuchungIdWrapper(einladung.getId()))
				.execute(ResponseBody::prettyPeek)
				.then()
				.assertThat()
				.statusCode(Status.OK.getStatusCode())
				.extract()
				.as(UntersuchungImpfungenDataJsonSpec.class);
		// ASSERT
		Assertions.assertThat(data).isNotNull();
		Assertions.assertThat(data.getUntersuchungImpfungen()).isNotNull();
	}

	@Test
	@TestAsAdminBS
	void getUntersuchungImpfungen_Data_existing_should_return_existing() {
		// ARRANGE
		Untersuchung untersuchung = fixtureService.createUntersuchungWithImpfungen();
		Assertions.assertThat(untersuchung.getUntersuchungImpfungen()).isNotNull();
		UntersuchungImpfungen existingImpfungen =
				untersuchungImpfungenService.getById(untersuchung.getUntersuchungImpfungen().getId());
		Assertions.assertThat(existingImpfungen).isNotNull();

		// ACT
		UntersuchungImpfungenDataJsonSpec data = api.untersuchungImpfungenResourceGetUntersuchungImpfungenData()
				.anlassIdPath(untersuchung.getAnlass().getId().getId())
				.einladungIdPath(toHasUntersuchungIdWrapper(untersuchung.getEinladungRequired().getId()))
				.execute(ResponseBody::prettyPeek)
				.then()
				.assertThat()
				.statusCode(Status.OK.getStatusCode())
				.extract()
				.as(UntersuchungImpfungenDataJsonSpec.class);
		UntersuchungImpfungenJsonSpec result = data.getUntersuchungImpfungen();
		// ASSERT
		Assertions.assertThat(result).isNotNull();
		Assertions.assertThat(result.getId()).isEqualTo(existingImpfungen.getId().getId());
	}

	@Test
	@TestAsAdminBS
	void createUntersuchungImpfungen() {
		// ARRANGE
		Einladung einladung = fixtureService.createEinladung();
		Benutzer benutzer = fixtureService.createBenutzerFuerZentraleOrganisation();

		UntersuchungImpfungenSaveJsonSpec saveJsonSpec =
            UntersuchungImpfungenBuilder.createJaxSpec(builder -> builder
                .withBemerkungen("Test createUntersuchungImpfungen")
                .withVisum1(benutzer, appClock.offsetDateTime().now().toLocalDate())
            ).getData();
        saveJsonSpec.setVersion(saveJsonSpec.getVersion() - 1);

		// ACT
		UntersuchungImpfungenJsonSpec result = api.untersuchungImpfungenResourceSaveUntersuchungImpfungen()
				.anlassIdPath(einladung.getAnlass().getId().getId())
				.einladungIdPath(toHasUntersuchungIdWrapper(einladung.getId()))
				.body(saveJsonSpec)
				.execute(ResponseBody::prettyPeek)
				.then()
				.assertThat()
				.statusCode(Status.OK.getStatusCode())
				.extract()
				.as(UntersuchungImpfungenJsonSpec.class);

		// ASSERT
		Assertions.assertThat(result).isNotNull();
		Assertions.assertThat(result.getId()).isNotNull();
		Assertions.assertThat(result.getData().getCheckliste().getBemerkungen())
				.isNotNull()
				.isEqualTo("Test createUntersuchungImpfungen");
		Assertions.assertThat(result.getData().getDoppelkontrolle().getVisumPerson1())
				.isNotNull()
				.extracting(BenutzerOptionJsonSpec::getId)
				.isEqualTo(benutzer.getId().getId());
	}

	@Test
	@TestAsAdminBS
	void updateUntersuchungImpfungen() {
		// ARRANGE
		Benutzer anotherBenutzer = fixtureService.createBenutzerFuerZentraleOrganisation();
		Untersuchung untersuchung = fixtureService.createUntersuchungWithImpfungen();
		Assertions.assertThat(untersuchung.getUntersuchungImpfungen()).isNotNull();
		UntersuchungImpfungen existingImpfungen =
				untersuchungImpfungenService.getById(untersuchung.getUntersuchungImpfungen().getId());
		Assertions.assertThat(existingImpfungen).isNotNull();

		// ACT
		UntersuchungImpfungenSaveJsonSpec saveJsonSpec =
				UntersuchungImpfungenBuilder.createJaxSpec(builder -> builder.forUntersuchungImpfungen(existingImpfungen)
						.withBemerkungen("Test updateUntersuchungImpfungen")
						.withVisum1(anotherBenutzer, appClock.offsetDateTime().now().toLocalDate())).getData();
		UntersuchungImpfungenJsonSpec result = api.untersuchungImpfungenResourceSaveUntersuchungImpfungen()
				.anlassIdPath(untersuchung.getAnlass().getId().getId())
				.einladungIdPath(toHasUntersuchungIdWrapper(untersuchung.getEinladungRequired().getId()))
				.body(saveJsonSpec)
				.execute(ResponseBody::prettyPeek)
				.then()
				.assertThat()
				.statusCode(Status.OK.getStatusCode())
				.extract()
				.as(UntersuchungImpfungenJsonSpec.class);

		// ASSERT
		Assertions.assertThat(result).isNotNull();
		Assertions.assertThat(result.getId()).isNotNull();
		Assertions.assertThat(result.getData().getCheckliste().getBemerkungen())
				.isNotNull()
				.isEqualTo("Test updateUntersuchungImpfungen");
		Assertions.assertThat(result.getData().getDoppelkontrolle().getVisumPerson1())
				.isNotNull()
				.extracting(BenutzerOptionJsonSpec::getId)
				.isEqualTo(anotherBenutzer.getId().getId());
	}

	@Test
	@TestAsAdminBS
	void visierenDoppelkontrolleVisum1() {
		// ARRANGE
		Untersuchung untersuchung = fixtureService.createUntersuchungWithImpfungen();
		Assertions.assertThat(untersuchung.getUntersuchungImpfungen()).isNotNull();
		UntersuchungImpfungen existingImpfungen =
				untersuchungImpfungenService.getById(untersuchung.getUntersuchungImpfungen().getId());
		Assertions.assertThat(existingImpfungen).isNotNull();
		// ACT
        UntersuchungImpfungenSaveJsonSpec payload = UntersuchungImpfungenBuilder.createJaxSpec(
            untersuchungImpfungenBuilder -> untersuchungImpfungenBuilder
                .forUntersuchungImpfungen(existingImpfungen)).getData();
		UntersuchungImpfungenJsonSpec result = api.untersuchungImpfungenResourceVisierenDoppelkontrolleVisum1()
				.anlassIdPath(untersuchung.getAnlass().getId().getId())
				.einladungIdPath(untersuchung.getEinladungRequired().getId().getId())
                .body(payload)
				.execute(ResponseBody::prettyPeek)
				.then()
				.assertThat()
				.statusCode(Status.OK.getStatusCode())
				.extract()
				.as(UntersuchungImpfungenJsonSpec.class);
		// ASSERT
		Assertions.assertThat(result).isNotNull();
		Assertions.assertThat(result.getData().getDoppelkontrolle().getVisumPerson1())
				.isNotNull()
				.extracting(BenutzerOptionJsonSpec::getId)
				.isEqualTo(TestAsAdminBS.BENUTZER_ID);
		Assertions.assertThat(result.getData().getDoppelkontrolle().getVisumDatum1())
				.isNotNull()
				.isEqualTo(appClock.offsetDateTime().now().toLocalDate());
	}

	@Test
	@TestAsAdminBS
	void anfordernDoppelkontrolleVisum2() {
		// ARRANGE
		Benutzer anotherBenutzer = fixtureService.createBenutzerFuerZentraleOrganisation();
		Untersuchung untersuchung = fixtureService.createUntersuchungWithImpfungen();
		Assertions.assertThat(untersuchung.getUntersuchungImpfungen()).isNotNull();
		UntersuchungImpfungen existingImpfungen =
				untersuchungImpfungenService.getById(untersuchung.getUntersuchungImpfungen().getId());
		Assertions.assertThat(existingImpfungen).isNotNull();
		// ACT
        UntersuchungImpfungenSaveJsonSpec payload = UntersuchungImpfungenBuilder.createJaxSpec(
            untersuchungImpfungenBuilder -> untersuchungImpfungenBuilder
                .forUntersuchungImpfungen(existingImpfungen)
                .withVisum2(anotherBenutzer, LocalDate.now())).getData();
		UntersuchungImpfungenJsonSpec result = api.untersuchungImpfungenResourceAnfordernDoppelkontrolleVisum2()
				.anlassIdPath(untersuchung.getAnlass().getId().getId())
				.einladungIdPath(untersuchung.getEinladungRequired().getId().getId())
				.body(payload)
				.execute(ResponseBody::prettyPeek)
				.then()
				.assertThat()
				.statusCode(Status.OK.getStatusCode())
				.extract()
				.as(UntersuchungImpfungenJsonSpec.class);
		// ASSERT
		Assertions.assertThat(result).isNotNull();
		Assertions.assertThat(result.getData().getDoppelkontrolle().getVisumPerson2())
				.isNotNull()
				.extracting(BenutzerOptionJsonSpec::getId)
				.isEqualTo(anotherBenutzer.getId().getId());
		Assertions.assertThat(result.getData().getDoppelkontrolle().getVisumDatum2()).isNull();
		Set<Aufgabe> aufgabenOfAnotherBenutzer = aufgabeService.findAll()
				.stream()
				.filter(aufgabe -> aufgabe.getEmpfaenger().getId().equals(anotherBenutzer.getId()))
				.collect(Collectors.toSet());
		Assertions.assertThat(aufgabenOfAnotherBenutzer).isNotEmpty().hasSize(1);

		for (Aufgabe aufgabe : aufgabenOfAnotherBenutzer) {
			Assertions.assertThat(aufgabe.getLink()).isNotNull();
			Assertions.assertThat(aufgabe.getLink())
					.isEqualTo(Util.parseURL("https://local-esc-bs.localhost:4443/anlass/"
							+ untersuchung.getEinladungRequired()
							.getAnlass()
							.getId()
							.getId()
							+ "/untersuchung/einladung/"
							+ untersuchung.getEinladungRequired().getId().getId()
							+ "/impfungen?einladungIds="
							+ untersuchung.getEinladungRequired().getId().getId()));
		}
	}

	@Test
	@TestAsAdminBS
	void visierenDoppelkontrolleVisum2() {
		// ARRANGE
		Untersuchung untersuchung = fixtureService.createUntersuchungWithImpfungen();
		Assertions.assertThat(untersuchung.getUntersuchungImpfungen()).isNotNull();
		UntersuchungImpfungen existingImpfungen =
				untersuchungImpfungenService.getById(untersuchung.getUntersuchungImpfungen().getId());
		Assertions.assertThat(existingImpfungen).isNotNull();
		// ACT
        UntersuchungImpfungenSaveJsonSpec payload = UntersuchungImpfungenBuilder.createJaxSpec(
            untersuchungImpfungenBuilder -> untersuchungImpfungenBuilder
                .forUntersuchungImpfungen(existingImpfungen)).getData();
		UntersuchungImpfungenJsonSpec result = api.untersuchungImpfungenResourceVisierenDoppelkontrolleVisum2()
				.anlassIdPath(untersuchung.getAnlass().getId().getId())
				.einladungIdPath(untersuchung.getEinladungRequired().getId().getId())
                .body(payload)
				.execute(ResponseBody::prettyPeek)
				.then()
				.assertThat()
				.statusCode(Status.OK.getStatusCode())
				.extract()
				.as(UntersuchungImpfungenJsonSpec.class);
		// ASSERT
		Assertions.assertThat(result).isNotNull();
		Assertions.assertThat(result.getData().getDoppelkontrolle().getVisumPerson2())
				.isNotNull()
				.extracting(BenutzerOptionJsonSpec::getId)
				.isEqualTo(TestAsAdminBS.BENUTZER_ID);
		Assertions.assertThat(result.getData().getDoppelkontrolle().getVisumDatum2())
				.isNotNull()
				.isEqualTo(appClock.offsetDateTime().now().toLocalDate());
	}

	@Test
	@TestAsAdminBS
	void resetDoppelkontrolle() {
		// ARRANGE
		Untersuchung untersuchung = fixtureService.createUntersuchungWithImpfungen();
		Assertions.assertThat(untersuchung.getUntersuchungImpfungen()).isNotNull();
		UntersuchungImpfungen existingImpfungen =
				untersuchungImpfungenService.getById(untersuchung.getUntersuchungImpfungen().getId());
		Assertions.assertThat(existingImpfungen).isNotNull();

        UntersuchungImpfungenSaveJsonSpec payloadSetVisum1 = UntersuchungImpfungenBuilder.createJaxSpec(
            untersuchungImpfungenBuilder -> untersuchungImpfungenBuilder
                .forUntersuchungImpfungen(existingImpfungen)).getData();
        UntersuchungImpfungenJsonSpec resultVisum = api.untersuchungImpfungenResourceVisierenDoppelkontrolleVisum1()
            .anlassIdPath(untersuchung.getAnlass().getId().getId())
            .einladungIdPath(untersuchung.getEinladungRequired().getId().getId())
            .body(payloadSetVisum1)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract()
            .as(UntersuchungImpfungenJsonSpec.class);

        Assertions.assertThat(resultVisum.getData().getDoppelkontrolle().getVisumDatum1()).isNotNull();
		Assertions.assertThat(resultVisum.getData().getDoppelkontrolle().getVisumPerson1()).isNotNull();

        existingImpfungen.setVersion(existingImpfungen.getVersion() + 1);

		// ACT
        UntersuchungImpfungenSaveJsonSpec payload = UntersuchungImpfungenBuilder.createJaxSpec(
            untersuchungImpfungenBuilder -> untersuchungImpfungenBuilder
                .forUntersuchungImpfungen(existingImpfungen)).getData();
		UntersuchungImpfungenJsonSpec result = api.untersuchungImpfungenResourceResetDoppelkontrolle()
				.anlassIdPath(untersuchung.getAnlass().getId().getId())
				.einladungIdPath(untersuchung.getEinladungRequired().getId().getId())
                .body(payload)
				.execute(ResponseBody::prettyPeek)
				.then()
				.assertThat()
				.statusCode(Status.OK.getStatusCode())
				.extract()
				.as(UntersuchungImpfungenJsonSpec.class);
		// ASSERT
		Assertions.assertThat(result).isNotNull();
		Assertions.assertThat(result.getData().getDoppelkontrolle().getVisumPerson1()).isNull();
		Assertions.assertThat(result.getData().getDoppelkontrolle().getVisumDatum1()).isNull();
		Assertions.assertThat(result.getData().getDoppelkontrolle().getVisumPerson2()).isNull();
		Assertions.assertThat(result.getData().getDoppelkontrolle().getVisumDatum2()).isNull();
	}

	@Test
	@TestAsAdminBS
	void getUntersuchungImpfungenDataImpfstatus_should_return_empty_items_if_nothing_exists() {
		// ARRANGE
		Einladung einladung = fixtureService.createEinladung();
		// ACT
		UntersuchungImpfungenDataJsonSpec data = api.untersuchungImpfungenResourceGetUntersuchungImpfungenData()
				.anlassIdPath(einladung.getAnlass().getId().getId())
				.einladungIdPath(toHasUntersuchungIdWrapper(einladung.getId()))
				.execute(ResponseBody::prettyPeek)
				.then()
				.assertThat()
				.statusCode(Status.OK.getStatusCode())
				.extract()
				.as(UntersuchungImpfungenDataJsonSpec.class);
		UntersuchungImpfstatusListSaveJsonSpec result = data.getImpfstatusList();
		// ASSERT
		Assertions.assertThat(result.getImpfstatusList())
				.isNotNull()
				.hasSize(Krankheit.values().length)
				.allMatch(impfstatus -> impfstatus.getAnzahlImpfungen() == null);
		// Es sollte ein UntersuchungImpfungen erstellt worden sein
		Untersuchung untersuchung = untersuchungService.getOrCreateUntersuchung(einladung.getId());
		Assertions.assertThat(untersuchung.getUntersuchungImpfungen()).isNotNull();
	}

	@Test
	@TestAsAdminBS
	void getUntersuchungImpfungenDataImpfstatus_should_return_existing_items() {
		// ARRANGE
		Untersuchung untersuchung = fixtureService.createUntersuchungWithImpfungen();
		Assertions.assertThat(untersuchung.getUntersuchungImpfungen()).isNotNull();

		// ACT
		UntersuchungImpfungenDataJsonSpec data = api.untersuchungImpfungenResourceGetUntersuchungImpfungenData()
				.anlassIdPath(untersuchung.getAnlass().getId().getId())
				.einladungIdPath(toHasUntersuchungIdWrapper(untersuchung.getEinladungRequired().getId()))
				.execute(ResponseBody::prettyPeek)
				.then()
				.assertThat()
				.statusCode(Status.OK.getStatusCode())
				.extract()
				.as(UntersuchungImpfungenDataJsonSpec.class);
		UntersuchungImpfstatusListSaveJsonSpec result = data.getImpfstatusList();
		// ASSERT
		// Es gibt fuer jede Krankheit einen Impfstatus
		Assertions.assertThat(result.getImpfstatusList()).isNotNull().hasSize(Krankheit.values().length);

		// Alle anderen Krankheiten sollten Impfstatus keine Impfungen haben
		List<UntersuchungImpfstatusKrankheitSaveJsonSpec> allOtherKrankheitenJson = result.getImpfstatusList()
				.stream()
				.filter(impfstatus -> impfstatus.getKrankheit() != KrankheitSpec.MASERN)
				.toList();
		Assertions.assertThat(allOtherKrankheitenJson)
				.hasSize(Krankheit.values().length - 1)
				.allMatch(impfstatus -> impfstatus.getAnzahlImpfungen() == null);
	}

	@Test
	@TestAsAdminBS
	void saveUntersuchungImpfungenImpfstatus_create_new_item() {
		// ARRANGE
		Untersuchung untersuchung = fixtureService.createUntersuchungWithImpfungen();
        UntersuchungImpfungen untersuchungImpfungen = untersuchung.getUntersuchungImpfungen();
        Assertions.assertThat(untersuchungImpfungen).isNotNull();
		// ACT
		UntersuchungImpfstatusKrankheitSaveJsonSpec createJsonSpec =
				UntersuchungImpfstatusKrankheitBuilder.createSaveJaxSpec(builder -> builder.forUntersuchungImpfungen(
                    untersuchungImpfungen).forKrankheit(Krankheit.MASERN).withAnzahlImpfungen(4));
		UntersuchungImpfstatusListSaveJsonSpec listSpec =
				new UntersuchungImpfstatusListSaveJsonSpec(List.of(createJsonSpec));
		UntersuchungImpfstatusListSaveJsonSpec result =
				api.untersuchungImpfungenResourceSaveUntersuchungImpfungenImpfstatus()
						.anlassIdPath(untersuchung.getAnlass().getId().getId())
						.einladungIdPath(toHasUntersuchungIdWrapper(untersuchung.getEinladungRequired().getId()))
						.body(listSpec)
						.execute(ResponseBody::prettyPeek)
						.then()
						.assertThat()
						.statusCode(Status.OK.getStatusCode())
						.extract()
						.as(UntersuchungImpfstatusListSaveJsonSpec.class);
		// ASSERT
		// Es gibt fuer jede Krankheit einen Impfstatus
		Assertions.assertThat(result.getImpfstatusList()).isNotNull().hasSize(Krankheit.values().length);
		// Der Impfstatus fuer Masern sollte 2 Impfungen haben
		UntersuchungImpfstatusKrankheitSaveJsonSpec masernJson = result.getImpfstatusList()
				.stream()
				.filter(impfstatus -> impfstatus.getKrankheit() == KrankheitSpec.MASERN)
				.findFirst()
				.orElseThrow();
		Assertions.assertThat(masernJson.getAnzahlImpfungen()).isEqualTo(4);
		// Alle anderen Krankheiten sollten Impfstatus keine Impfungen haben
		List<UntersuchungImpfstatusKrankheitSaveJsonSpec> allOtherKrankheitenJson = result.getImpfstatusList()
				.stream()
				.filter(impfstatus -> impfstatus.getKrankheit() != KrankheitSpec.MASERN)
				.toList();
		Assertions.assertThat(allOtherKrankheitenJson)
				.hasSize(Krankheit.values().length - 1)
				.allMatch(impfstatus -> impfstatus.getAnzahlImpfungen() == null);
	}

	@Test
	@TestAsAdminBS
	void saveUntersuchungImpfungenImpfstatus_set_immunitaet_on_dossier_for_masern_varizellen() {
		// ARRANGE
		Untersuchung untersuchung = fixtureService.createUntersuchungWithImpfungen();
        UntersuchungImpfungen untersuchungImpfungen = untersuchung.getUntersuchungImpfungen();
        Assertions.assertThat(untersuchungImpfungen).isNotNull();

		// ACT
		UntersuchungImpfstatusKrankheitSaveJsonSpec createMasernJsonSpec =
				UntersuchungImpfstatusKrankheitBuilder.createSaveJaxSpec(builder -> builder
						.forUntersuchungImpfungen(untersuchungImpfungen)
						.forKrankheit(Krankheit.MASERN)
						.withImpfschutzGegeben(ImpfschutzGegeben.JA));

		UntersuchungImpfstatusKrankheitSaveJsonSpec createVarizellenJsonSpec =
				UntersuchungImpfstatusKrankheitBuilder.createSaveJaxSpec(builder -> builder
						.forUntersuchungImpfungen(untersuchungImpfungen)
						.forKrankheit(Krankheit.VARIZELLEN)
						.withImpfschutzGegeben(ImpfschutzGegeben.JA));

		UntersuchungImpfstatusListSaveJsonSpec listSpec =
				new UntersuchungImpfstatusListSaveJsonSpec(List.of(
						createMasernJsonSpec,
						createVarizellenJsonSpec));
		UntersuchungImpfstatusListSaveJsonSpec result =
				api.untersuchungImpfungenResourceSaveUntersuchungImpfungenImpfstatus()
						.anlassIdPath(untersuchung.getAnlass().getId().getId())
						.einladungIdPath(toHasUntersuchungIdWrapper(untersuchung.getEinladungRequired().getId()))
						.body(listSpec)
						.execute(ResponseBody::prettyPeek)
						.then()
						.assertThat()
						.statusCode(Status.OK.getStatusCode())
						.extract()
						.as(UntersuchungImpfstatusListSaveJsonSpec.class);
		// ASSERT
		// Es gibt fuer jede Krankheit einen Impfstatus
		Dossier dossier = dossierService.getById(untersuchung.getEinladungRequired().getDossier().getId());
		Assertions.assertThat(result.getImpfstatusList()).isNotNull().hasSize(Krankheit.values().length);
		Assertions.assertThat(dossier.isImmunitaetMasern()).isTrue();
		Assertions.assertThat(dossier.isImmunitaetVarizellen()).isTrue();
	}

	@Test
	@TestAsAdminBS
	void saveUntersuchungImpfungenImpfstatus_update_existing_item() {
		// ARRANGE
		Untersuchung untersuchung = fixtureService.createUntersuchungWithImpfungen();
        UntersuchungImpfungen untersuchungImpfungen = untersuchung.getUntersuchungImpfungen();
        Assertions.assertThat(untersuchungImpfungen).isNotNull();
		// Damit werden alle Krankheiten erstellt
		untersuchungImpfstatusKrankheitMapper.getByUntersuchungImpfungen(untersuchungImpfungen);
        UntersuchungImpfstatusKrankheitSaveJsonSpec createSpec =
				UntersuchungImpfstatusKrankheitBuilder.createSaveJaxSpec(builder -> builder.forUntersuchungImpfungen(
                    untersuchungImpfungen).forKrankheit(Krankheit.MASERN));
        UntersuchungImpfstatusListSaveJsonSpec createListSpec =
            new UntersuchungImpfstatusListSaveJsonSpec(List.of(createSpec));
        api.untersuchungImpfungenResourceSaveUntersuchungImpfungenImpfstatus()
						.anlassIdPath(untersuchung.getAnlass().getId().getId())
						.einladungIdPath(toHasUntersuchungIdWrapper(untersuchung.getEinladungRequired().getId()))
            .body(createListSpec)
						.execute(ResponseBody::prettyPeek)
						.then()
						.assertThat()
						.statusCode(Status.OK.getStatusCode())
						.extract()
						.as(UntersuchungImpfstatusListSaveJsonSpec.class);

        // ACT
        UntersuchungImpfstatusKrankheitSaveJsonSpec updateSpec =
            UntersuchungImpfstatusKrankheitBuilder.createSaveJaxSpec(builder -> builder.forUntersuchungImpfungen(
                untersuchungImpfungen).forKrankheit(Krankheit.MASERN).withAnzahlImpfungen(4));
        UntersuchungImpfstatusListSaveJsonSpec updateListSpec =
            new UntersuchungImpfstatusListSaveJsonSpec(List.of(updateSpec));
        UntersuchungImpfstatusListSaveJsonSpec result =
            api.untersuchungImpfungenResourceSaveUntersuchungImpfungenImpfstatus()
                .anlassIdPath(untersuchung.getAnlass().getId().getId())
                .einladungIdPath(toHasUntersuchungIdWrapper(untersuchung.getEinladungRequired().getId()))
                .body(updateListSpec)
                .execute(ResponseBody::prettyPeek)
                .then()
                .assertThat()
                .statusCode(Status.OK.getStatusCode())
                .extract()
                .as(UntersuchungImpfstatusListSaveJsonSpec.class);

        // ASSERT
		// Es gibt fuer jede Krankheit einen Impfstatus
		Assertions.assertThat(result.getImpfstatusList()).isNotNull().hasSize(Krankheit.values().length);
		// Der Impfstatus fuer Masern sollte 2 Impfungen haben
		UntersuchungImpfstatusKrankheitSaveJsonSpec masernJson = result.getImpfstatusList()
				.stream()
				.filter(impfstatus -> impfstatus.getKrankheit() == KrankheitSpec.MASERN)
				.findFirst()
				.orElseThrow();
		Assertions.assertThat(masernJson.getAnzahlImpfungen()).isEqualTo(4);
		// Alle anderen Krankheiten sollten Impfstatus keine Impfungen haben
		List<UntersuchungImpfstatusKrankheitSaveJsonSpec> allOtherKrankheitenJson = result.getImpfstatusList()
				.stream()
				.filter(impfstatus -> impfstatus.getKrankheit() != KrankheitSpec.MASERN)
				.toList();
		Assertions.assertThat(allOtherKrankheitenJson)
				.hasSize(Krankheit.values().length - 1)
				.allMatch(impfstatus -> impfstatus.getAnzahlImpfungen() == null);
	}

	@Test
	@TestAsAdminBS
	void getUntersuchungImpfungenDataImpfstatus_should_return_data_from_previous_impfstatus_if_impfschutz_is_gegeben() {
		// ARRANGE
		Dossier dossier = fixtureService.createDossier();

		Anlass previousAnlass = fixtureService.createAnlass(LocalDate.of(2021, 1, 1));
		Einladung previousEinladung = fixtureService.createEinladung(previousAnlass, dossier);
		Untersuchung previousUntersuchung = fixtureService.createUntersuchungWithImpfungen(previousEinladung);
        UntersuchungImpfungen untersuchungImpfungen = previousUntersuchung.getUntersuchungImpfungen();
        Assertions.assertThat(untersuchungImpfungen).isNotNull();

        UntersuchungImpfstatusKrankheitSaveJsonSpec createJsonSpecMasern =
            UntersuchungImpfstatusKrankheitBuilder.createSaveJaxSpec(builder -> builder
                .forUntersuchungImpfungen(untersuchungImpfungen)
                .forKrankheit(Krankheit.MASERN)
                .withAnzahlImpfungen(2)
                .withImpfschutzGegeben(ImpfschutzGegeben.JA)
                .withDatumLetzteImpfung(LocalDate.of(2022, 2, 3)));

        UntersuchungImpfstatusKrankheitSaveJsonSpec createJsonSpecPolio =
            UntersuchungImpfstatusKrankheitBuilder.createSaveJaxSpec(builder -> builder.forUntersuchungImpfungen(
                untersuchungImpfungen).forKrankheit(Krankheit.POLIO).withAnzahlImpfungen(2));

        UntersuchungImpfstatusListSaveJsonSpec listSpec =
            new UntersuchungImpfstatusListSaveJsonSpec(List.of(createJsonSpecMasern, createJsonSpecPolio));
        UntersuchungImpfstatusListSaveJsonSpec resultKrankheiten =
            api.untersuchungImpfungenResourceSaveUntersuchungImpfungenImpfstatus()
                .anlassIdPath(previousUntersuchung.getAnlass().getId().getId())
                .einladungIdPath(toHasUntersuchungIdWrapper(previousUntersuchung.getEinladungRequired().getId()))
                .body(listSpec)
                .execute(ResponseBody::prettyPeek)
                .then()
                .assertThat()
                .statusCode(Status.OK.getStatusCode())
                .extract()
                .as(UntersuchungImpfstatusListSaveJsonSpec.class);

        Assertions.assertThat(resultKrankheiten.getImpfstatusList().stream()
            .filter(k -> k.getKrankheit() == KrankheitSpec.MASERN)
            .findFirst().orElseThrow()
            .getImpfschutzGegeben()).isEqualTo(ImpfschutzGegebenSpec.JA);

        Anlass newAnlass = fixtureService.createAnlass(LocalDate.of(2024, 1, 1));
		Einladung newEinladung = fixtureService.createEinladung(newAnlass, dossier);
		Untersuchung newUntersuchung = fixtureService.createUntersuchungWithImpfungen(newEinladung);
		Assertions.assertThat(newUntersuchung.getUntersuchungImpfungen()).isNotNull();
		untersuchungImpfstatusKrankheitMapper.getByUntersuchungImpfungen(newUntersuchung.getUntersuchungImpfungen());

		// ACT
		UntersuchungImpfungenDataJsonSpec data = api.untersuchungImpfungenResourceGetUntersuchungImpfungenData()
				.anlassIdPath(newEinladung.getAnlass().getId().getId())
				.einladungIdPath(toHasUntersuchungIdWrapper(newEinladung.getId()))
				.execute(ResponseBody::prettyPeek)
				.then()
				.assertThat()
				.statusCode(Status.OK.getStatusCode())
				.extract()
				.as(UntersuchungImpfungenDataJsonSpec.class);
		UntersuchungImpfstatusListSaveJsonSpec result = data.getImpfstatusList();

		// ASSERT
		Assertions.assertThat(result.getImpfstatusList()).isNotNull().hasSize(Krankheit.values().length);
		Assertions.assertThat(result.getImpfstatusList()
				.stream()
				.anyMatch(impfstatus -> impfstatus.getKrankheit() == KrankheitSpec.MASERN
						&& impfstatus.getImpfschutzGegeben() == ImpfschutzGegebenSpec.JA
						&& Integer.valueOf(2).equals(impfstatus.getAnzahlImpfungen())
						&& Objects.equals(impfstatus.getDatumLetzteImpfung(), LocalDate.of(2022, 2, 3)))).isTrue();

		Assertions.assertThat(result.getImpfstatusList()
				.stream()
				.noneMatch(impfstatus ->
                    impfstatus.getKrankheit() == KrankheitSpec.POLIO
                        && impfstatus.getImpfschutzGegeben() == ImpfschutzGegebenSpec.JA)).isTrue();
	}

	@Test
	@TestAsAdminBS
	void getUntersuchungImpfungenDataImpfstatus_should_return_data_from_previous_impfliste_if_impfschutz_is_gegeben() {
		// ARRANGE
		Dossier dossier = fixtureService.createDossier();

		Anlass previousAnlass = fixtureService.createAnlass(LocalDate.of(2021, 1, 1));
		Einladung previousEinladung = fixtureService.createEinladung(previousAnlass, dossier);
		Untersuchung previousUntersuchung = fixtureService.createUntersuchungWithImpfungen(previousEinladung);
		Impfstoff impfstoff = fixtureService.createImpfstoff();
        UntersuchungImpfungen untersuchungImpfungen = previousUntersuchung.getUntersuchungImpfungen();
        Assertions.assertThat(untersuchungImpfungen).isNotNull();

        UntersuchungImpfstatusKrankheitSaveJsonSpec createJsonSpecMasern =
            UntersuchungImpfstatusKrankheitBuilder.createSaveJaxSpec(builder -> builder.forUntersuchungImpfungen(
                untersuchungImpfungen).forKrankheit(Krankheit.MASERN).withAnzahlImpfungen(2));

        UntersuchungImpfstatusListSaveJsonSpec listSpec =
            new UntersuchungImpfstatusListSaveJsonSpec(List.of(createJsonSpecMasern));
        UntersuchungImpfstatusListSaveJsonSpec resultKrankheiten =
            api.untersuchungImpfungenResourceSaveUntersuchungImpfungenImpfstatus()
                .anlassIdPath(previousUntersuchung.getAnlass().getId().getId())
                .einladungIdPath(toHasUntersuchungIdWrapper(previousUntersuchung.getEinladungRequired().getId()))
                .body(listSpec)
                .execute(ResponseBody::prettyPeek)
                .then()
                .assertThat()
                .statusCode(Status.OK.getStatusCode())
                .extract()
                .as(UntersuchungImpfstatusListSaveJsonSpec.class);

        Assertions.assertThat(resultKrankheiten.getImpfstatusList().stream().filter(k -> k.getKrankheit() == KrankheitSpec.MASERN).findFirst().get().getAnzahlImpfungen()).isEqualTo(2);

		untersuchungImpfungInternService.persist(UntersuchungImpfungInternBuilder.create(builder -> builder.forUntersuchungImpfungen(
                untersuchungImpfungen)
				.withUserErfasst(fixtureService.createBenutzerFuerZentraleOrganisation())
				.withImpfstoff(impfstoff)
				.withImmunitaetForKrankheit(Krankheit.MASERN, true)
				.withDatumImpfung(LocalDate.of(2023, 1, 27))));

		untersuchungImpfungInternService.persist(UntersuchungImpfungInternBuilder.create(builder -> builder.forUntersuchungImpfungen(
                untersuchungImpfungen)
				.withUserErfasst(fixtureService.createBenutzerFuerZentraleOrganisation())
				.withImpfstoff(impfstoff)
				.withImmunitaetForKrankheit(Krankheit.ROETELN, false)
				.withDatumImpfung(LocalDate.of(2023, 6, 21))));

		Anlass newAnlass = fixtureService.createAnlass(LocalDate.of(2024, 1, 1));
		Einladung newEinladung = fixtureService.createEinladung(newAnlass, dossier);
		Untersuchung newUntersuchung = fixtureService.createUntersuchungWithImpfungen(newEinladung);
		Assertions.assertThat(newUntersuchung.getUntersuchungImpfungen()).isNotNull();
		untersuchungImpfstatusKrankheitMapper.getByUntersuchungImpfungen(newUntersuchung.getUntersuchungImpfungen());

		// ACT
		UntersuchungImpfungenDataJsonSpec data = api.untersuchungImpfungenResourceGetUntersuchungImpfungenData()
				.anlassIdPath(newEinladung.getAnlass().getId().getId())
				.einladungIdPath(toHasUntersuchungIdWrapper(newEinladung.getId()))
				.execute(ResponseBody::prettyPeek)
				.then()
				.assertThat()
				.statusCode(Status.OK.getStatusCode())
				.extract()
				.as(UntersuchungImpfungenDataJsonSpec.class);
		UntersuchungImpfstatusListSaveJsonSpec result = data.getImpfstatusList();

		// ASSERT
		Assertions.assertThat(result.getImpfstatusList()).isNotNull().hasSize(Krankheit.values().length);
		Assertions.assertThat(result.getImpfstatusList()
				.stream()
				.anyMatch(impfstatus -> impfstatus.getKrankheit() == KrankheitSpec.MASERN
						&& impfstatus.getImpfschutzGegeben() == ImpfschutzGegebenSpec.JA
						&& Integer.valueOf(3).equals(impfstatus.getAnzahlImpfungen())
						&& Objects.equals(impfstatus.getDatumLetzteImpfung(), LocalDate.of(2023, 1, 27)))).isTrue();

		Assertions.assertThat(result.getImpfstatusList()
				.stream()
				.noneMatch(impfstatus ->
                    impfstatus.getKrankheit() == KrankheitSpec.ROETELN
                        && impfstatus.getImpfschutzGegeben() == ImpfschutzGegebenSpec.JA)).isTrue();

	}

    @Test
    @TestAsAdminBS
    void getUntersuchungImpfungenDataImpfstatus_should_return_data_from_previous_impfliste_if_impfschutz_is_gegeben_and_anlass_durchgefuhert_in_previous_anlass() {
        // ARRANGE
        Dossier dossier = fixtureService.createDossier();

        Anlass previousAnlass = fixtureService.createAnlass(LocalDate.of(2021, 1, 1));
        Einladung previousEinladung = fixtureService.createEinladung(previousAnlass, dossier);
        Untersuchung previousUntersuchung = fixtureService.createUntersuchungWithImpfungen(previousEinladung);
        Impfstoff impfstoff = fixtureService.createImpfstoff();
        UntersuchungImpfungen untersuchungImpfungen = previousUntersuchung.getUntersuchungImpfungen();
        Assertions.assertThat(untersuchungImpfungen).isNotNull();

        Anlass differentAnlass = fixtureService.createAnlass(LocalDate.of(2021, 1, 1));

        UntersuchungImpfstatusKrankheitSaveJsonSpec createJsonSpecMasern =
            UntersuchungImpfstatusKrankheitBuilder.createSaveJaxSpec(builder -> builder
                .forUntersuchungImpfungen(untersuchungImpfungen)
                .forKrankheit(Krankheit.MASERN)
                .withImpfschutzGegeben(ImpfschutzGegeben.JA)
                .withDatumLetzteImpfung(LocalDate.of(2022, 2, 3))
                .withAnzahlImpfungen(2));

        UntersuchungImpfstatusListSaveJsonSpec listSpec =
            new UntersuchungImpfstatusListSaveJsonSpec(List.of(createJsonSpecMasern));
        UntersuchungImpfstatusListSaveJsonSpec resultKrankheiten =
            api.untersuchungImpfungenResourceSaveUntersuchungImpfungenImpfstatus()
                .anlassIdPath(previousUntersuchung.getAnlass().getId().getId())
                .einladungIdPath(toHasUntersuchungIdWrapper(previousUntersuchung.getEinladungRequired().getId()))
                .body(listSpec)
                .execute(ResponseBody::prettyPeek)
                .then()
                .assertThat()
                .statusCode(Status.OK.getStatusCode())
                .extract()
                .as(UntersuchungImpfstatusListSaveJsonSpec.class);

        Assertions.assertThat(resultKrankheiten.getImpfstatusList().stream().filter(k -> k.getKrankheit() == KrankheitSpec.MASERN).findFirst().get().getAnzahlImpfungen()).isEqualTo(2);

        untersuchungImpfungInternService.persist(UntersuchungImpfungInternBuilder.create(builder -> builder.forUntersuchungImpfungen(
                untersuchungImpfungen)
            .withUserErfasst(fixtureService.createBenutzerFuerZentraleOrganisation())
            .withImpfstoff(impfstoff)
            .withImmunitaetForKrankheit(Krankheit.MASERN, true)
            .withDatumImpfung(LocalDate.of(2023, 1, 27))
            .withAnlassImpfungDurchgefuehrt(differentAnlass)));

        Anlass newAnlass = fixtureService.createAnlass(LocalDate.of(2024, 1, 1));
        Einladung newEinladung = fixtureService.createEinladung(newAnlass, dossier);
        Untersuchung newUntersuchung = fixtureService.createUntersuchungWithImpfungen(newEinladung);
        Assertions.assertThat(newUntersuchung.getUntersuchungImpfungen()).isNotNull();
        untersuchungImpfstatusKrankheitMapper.getByUntersuchungImpfungen(newUntersuchung.getUntersuchungImpfungen());

        // ACT
        UntersuchungImpfungenDataJsonSpec data = api.untersuchungImpfungenResourceGetUntersuchungImpfungenData()
            .anlassIdPath(newEinladung.getAnlass().getId().getId())
            .einladungIdPath(toHasUntersuchungIdWrapper(newEinladung.getId()))
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract()
            .as(UntersuchungImpfungenDataJsonSpec.class);
        UntersuchungImpfstatusListSaveJsonSpec result = data.getImpfstatusList();

        // ASSERT
        Assertions.assertThat(result.getImpfstatusList()).isNotNull().hasSize(Krankheit.values().length);
        Assertions.assertThat(result.getImpfstatusList()
            .stream()
            .anyMatch(impfstatus -> impfstatus.getKrankheit() == KrankheitSpec.MASERN
                && impfstatus.getImpfschutzGegeben() == ImpfschutzGegebenSpec.JA
                && Integer.valueOf(2).equals(impfstatus.getAnzahlImpfungen())
            )).isTrue();
    }

    @Test
    @TestAsAdminBS
    void getUntersuchungImpfungenDataImpfstatus_should_return_data_from_previous_impfstatus_if_impfschutz_is_gegeben_and_krankheit_is_available() {
        // ARRANGE
        Dossier dossier = fixtureService.createDossier();

        Anlass previousAnlass = fixtureService.createAnlass(LocalDate.of(2021, 1, 1));
        Einladung previousEinladung = fixtureService.createEinladung(previousAnlass, dossier);
        Untersuchung previousUntersuchung = fixtureService.createUntersuchungWithImpfungen(previousEinladung);
        UntersuchungImpfungen untersuchungImpfungen = previousUntersuchung.getUntersuchungImpfungen();
        Assertions.assertThat(untersuchungImpfungen).isNotNull();

        UntersuchungImpfstatusKrankheitSaveJsonSpec createJsonSpecMasern =
            UntersuchungImpfstatusKrankheitBuilder.createSaveJaxSpec(builder -> builder
                .forUntersuchungImpfungen(untersuchungImpfungen)
                .forKrankheit(Krankheit.MASERN)
                .withAnzahlImpfungen(2)
                .withImpfschutzGegeben(ImpfschutzGegeben.JA)
                .withDatumLetzteImpfung(LocalDate.of(2022, 2, 3)));

        UntersuchungImpfstatusKrankheitSaveJsonSpec createJsonSpecPolio =
            UntersuchungImpfstatusKrankheitBuilder.createSaveJaxSpec(builder -> builder
                .forUntersuchungImpfungen(untersuchungImpfungen)
                .forKrankheit(Krankheit.POLIO)
                .withAnzahlImpfungen(2)
                .withImpfschutzGegeben(ImpfschutzGegeben.JA));

        UntersuchungImpfstatusListSaveJsonSpec listSpec =
            new UntersuchungImpfstatusListSaveJsonSpec(List.of(createJsonSpecMasern, createJsonSpecPolio));
        UntersuchungImpfstatusListSaveJsonSpec resultKrankheiten =
            api.untersuchungImpfungenResourceSaveUntersuchungImpfungenImpfstatus()
                .anlassIdPath(previousUntersuchung.getAnlass().getId().getId())
                .einladungIdPath(toHasUntersuchungIdWrapper(previousUntersuchung.getEinladungRequired().getId()))
                .body(listSpec)
                .execute(ResponseBody::prettyPeek)
                .then()
                .assertThat()
                .statusCode(Status.OK.getStatusCode())
                .extract()
                .as(UntersuchungImpfstatusListSaveJsonSpec.class);

        Assertions.assertThat(resultKrankheiten.getImpfstatusList().stream().filter(k -> k.getKrankheit() == KrankheitSpec.MASERN).findFirst().orElseThrow().getImpfschutzGegeben()).isEqualTo(ImpfschutzGegebenSpec.JA);
        Assertions.assertThat(resultKrankheiten.getImpfstatusList().stream().filter(k -> k.getKrankheit() == KrankheitSpec.POLIO).findFirst().orElseThrow().getImpfschutzGegeben()).isEqualTo(ImpfschutzGegebenSpec.JA);

        AnlassConfig anlassConfig =  AnlassConfigBuilder.create(builder -> builder
            .withGueltigkeitsZeitraum(LocalDate.of(1999, 1, 1), LocalDate.of(1999, 12, 31))
            .withAnlassTyp(AnlassTyp.KLASSE_4)
            .withKrankheitenLinks(EnumSet.noneOf(Krankheit.class))
            .withKrankheitenRechts(EnumSet.of(Krankheit.MASERN)));
        anlassConfigRepo.persist(anlassConfig);

        Anlass newAnlass = fixtureService.createAnlass(anlassConfig);
        Einladung newEinladung = fixtureService.createEinladung(newAnlass, dossier);
        Untersuchung newUntersuchung = fixtureService.createUntersuchungWithImpfungen(newEinladung);
        Assertions.assertThat(newUntersuchung.getUntersuchungImpfungen()).isNotNull();
        untersuchungImpfstatusKrankheitMapper.getByUntersuchungImpfungen(newUntersuchung.getUntersuchungImpfungen());

        // ACT
        UntersuchungImpfungenDataJsonSpec data = api.untersuchungImpfungenResourceGetUntersuchungImpfungenData()
            .anlassIdPath(newEinladung.getAnlass().getId().getId())
            .einladungIdPath(toHasUntersuchungIdWrapper(newEinladung.getId()))
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract()
            .as(UntersuchungImpfungenDataJsonSpec.class);
        UntersuchungImpfstatusListSaveJsonSpec result = data.getImpfstatusList();

        // ASSERT
        Assertions.assertThat(result.getImpfstatusList()).isNotNull().hasSize(1);
        Assertions.assertThat(result.getImpfstatusList()
            .stream()
            .anyMatch(impfstatus -> impfstatus.getKrankheit() == KrankheitSpec.MASERN
                && impfstatus.getImpfschutzGegeben() == ImpfschutzGegebenSpec.JA
                && Integer.valueOf(2).equals(impfstatus.getAnzahlImpfungen())
                && Objects.equals(impfstatus.getDatumLetzteImpfung(), LocalDate.of(2022, 2, 3)))).isTrue();

        Assertions.assertThat(result.getImpfstatusList()
            .stream()
            .noneMatch(impfstatus -> impfstatus.getKrankheit() == KrankheitSpec.POLIO)).isTrue();
    }


    @Test
	@TestAsAdminBS
	void getUntersuchungImpfungenDataIntern_noneExist() {
		// ARRANGE
		Untersuchung untersuchung = fixtureService.createUntersuchungWithImpfungen();
		Assertions.assertThat(untersuchung.getUntersuchungImpfungen()).isNotNull();
		// ACT
		UntersuchungImpfungenDataJsonSpec data = api.untersuchungImpfungenResourceGetUntersuchungImpfungenData()
				.anlassIdPath(untersuchung.getAnlass().getId().getId())
				.einladungIdPath(toHasUntersuchungIdWrapper(untersuchung.getEinladungRequired().getId()))
				.execute(ResponseBody::prettyPeek)
				.then()
				.assertThat()
				.statusCode(Status.OK.getStatusCode())
				.extract()
				.as(UntersuchungImpfungenDataJsonSpec.class);
		UntersuchungImpfungInternListJsonSpec result = data.getImpfungInternList();
		// ASSERT
		Assertions.assertThat(result.getImpfungenInternList()).isNotNull().isEmpty();
	}

	@Test
	@TestAsAdminBS
	void getUntersuchungImpfungenDataIntern_returnExisting() {
		// ARRANGE
		Untersuchung untersuchung = fixtureService.createUntersuchungWithImpfungen();
        UntersuchungImpfungen untersuchungImpfungen = untersuchung.getUntersuchungImpfungen();
        Assertions.assertThat(untersuchungImpfungen).isNotNull();
		Impfstoff impfstoff = fixtureService.createImpfstoff();

		UntersuchungImpfungIntern existingImpfung =
				untersuchungImpfungInternService.persist(UntersuchungImpfungInternBuilder.create(builder -> builder.forUntersuchungImpfungen(
                        untersuchungImpfungen)
						.withUserErfasst(fixtureService.createBenutzerFuerZentraleOrganisation())
						.withImpfstoff(impfstoff)));

		// ACT
		UntersuchungImpfungenDataJsonSpec data = api.untersuchungImpfungenResourceGetUntersuchungImpfungenData()
				.anlassIdPath(untersuchung.getAnlass().getId().getId())
				.einladungIdPath(toHasUntersuchungIdWrapper(untersuchung.getEinladungRequired().getId()))
				.execute(ResponseBody::prettyPeek)
				.then()
				.assertThat()
				.statusCode(Status.OK.getStatusCode())
				.extract()
				.as(UntersuchungImpfungenDataJsonSpec.class);
		UntersuchungImpfungInternListJsonSpec result = data.getImpfungInternList();
		// ASSERT
		Assertions.assertThat(result.getImpfungenInternList())
				.isNotNull()
				.isNotEmpty()
				.hasSize(1)
				.flatExtracting(UntersuchungImpfungInternJsonSpec::getId)
				.containsExactly(existingImpfung.getId().getId());
	}

	@Test
	@TestAsAdminBS
	void getUntersuchungImpfungenDataIntern_return_from_previous_anlass_if_existing() {
		// ARRANGE
		Dossier dossier = fixtureService.createDossier();
		Anlass previousAnlass = fixtureService.createAnlass(LocalDate.of(2021, 1, 1));
		Einladung previousEinladung = fixtureService.createEinladung(previousAnlass, dossier);
		Untersuchung previousUntersuchung = fixtureService.createUntersuchungWithImpfungen(previousEinladung);
		Impfstoff impfstoff = fixtureService.createImpfstoff();

        UntersuchungImpfungen untersuchungImpfungen = previousUntersuchung.getUntersuchungImpfungen();
        Assertions.assertThat(untersuchungImpfungen).isNotNull();
		UntersuchungImpfungIntern existingImpfung =
				untersuchungImpfungInternService.persist(UntersuchungImpfungInternBuilder.create(builder -> builder
                    .forUntersuchungImpfungen(untersuchungImpfungen)
                    .withUserErfasst(fixtureService.createBenutzerFuerZentraleOrganisation())
					.withImpfstoff(impfstoff)
					.withImmunitaetForKrankheit(Krankheit.MASERN, true)));

		Anlass anlass = fixtureService.createAnlass(LocalDate.of(2024, 1, 1));
		Einladung einladung = fixtureService.createEinladung(anlass, dossier);

		Untersuchung untersuchung = fixtureService.createUntersuchungWithImpfungen(einladung);
		Assertions.assertThat(untersuchung.getUntersuchungImpfungen()).isNotNull();

		// ACT
		UntersuchungImpfungenDataJsonSpec data = api.untersuchungImpfungenResourceGetUntersuchungImpfungenData()
				.anlassIdPath(untersuchung.getAnlass().getId().getId())
				.einladungIdPath(toHasUntersuchungIdWrapper(untersuchung.getEinladungRequired().getId()))
				.execute(ResponseBody::prettyPeek)
				.then()
				.assertThat()
				.statusCode(Status.OK.getStatusCode())
				.extract()
				.as(UntersuchungImpfungenDataJsonSpec.class);
		UntersuchungImpfungInternListJsonSpec result = data.getImpfungInternList();
		// ASSERT
		Assertions.assertThat(result.getImpfungenInternList())
				.isNotNull()
				.isNotEmpty()
				.hasSize(1);
		Assertions.assertThat(result.getImpfungenInternList().getFirst())
				.extracting(impfungen -> impfungen.getData().getImpfstoff().getId())
				.isEqualTo(existingImpfung.getImpfstoff().getId().getId());

		Assertions.assertThat(result.getImpfungenInternList().getFirst().getData().getKrankheitenImmunitaeten())
				.anyMatch(i -> i.getKrankheit() == KrankheitSpec.MASERN && i.getImmunitaetGenuegend());
        List<UntersuchungImpfungIntern> impfungenBefore = untersuchungImpfungInternService.findByUntersuchung(previousUntersuchung.getId());
        List<UntersuchungImpfungIntern> interneImpfungCopy = untersuchungImpfungInternService.findByUntersuchung(untersuchung.getId());
        Assertions.assertThat(interneImpfungCopy).hasSameSizeAs(impfungenBefore).hasSize(1);

        // compare the copy of the same impfung, they should have the same data
        UntersuchungImpfungIntern impfungCopy = interneImpfungCopy.getFirst();
        UntersuchungImpfungIntern impfungBefore = impfungenBefore.getFirst();
        Assertions.assertThat(impfungCopy.getDatumImpfung()).isEqualTo(impfungBefore.getDatumImpfung());
        Assertions.assertThat(impfungCopy.getUserErfasst()).isEqualTo(impfungBefore.getUserErfasst());
        Assertions.assertThat(impfungCopy.getImpfstoff()).isEqualTo(impfungBefore.getImpfstoff());

        // make sure each impfung only contains at most one immunitaet for a specific krankheit (bug where we copied wrong)
        checkEachImpfungAtMostImmunizesOnceForAnyGivenKrankheit(impfungCopy);
        checkEachImpfungAtMostImmunizesOnceForAnyGivenKrankheit(impfungBefore);
        Assertions.assertThat(impfungCopy.getKrankheitenImmunitaeten()).hasSameSizeAs(impfungBefore.getKrankheitenImmunitaeten());
    }

    private void checkEachImpfungAtMostImmunizesOnceForAnyGivenKrankheit(UntersuchungImpfungIntern impfungIntern) {
        for(Krankheit krankheit : Krankheit.values()) {
            long countForKrankheit = impfungIntern.getKrankheitenImmunitaeten().stream()
                .filter(i -> i.getKrankheit() == krankheit)
                .count();
            Assertions.assertThat(countForKrankheit).as("Duplicate immunitaet found for krankheit " + krankheit)
                .isLessThanOrEqualTo(1);
        }
    }

    @Test
	@TestAsAdminBS
	void createUntersuchungImpfungIntern() {
		// ARRANGE
		Untersuchung untersuchung = fixtureService.createUntersuchungWithImpfungen();
        UntersuchungImpfungen untersuchungImpfungen = untersuchung.getUntersuchungImpfungen();
        Assertions.assertThat(untersuchungImpfungen).isNotNull();
		Impfstoff impfstoff = fixtureService.createImpfstoff();
		// ACT
		UntersuchungImpfungInternSaveJsonSpec payload =
				UntersuchungImpfungInternBuilder.createJaxSpec(builder -> builder.forUntersuchungImpfungen(
                        untersuchungImpfungen)
						.withImpfstoff(impfstoff)).getData();
		UntersuchungImpfungInternJsonSpec result = api.untersuchungImpfungenResourceCreateUntersuchungImpfungIntern()
				.anlassIdPath(untersuchung.getAnlass().getId().getId())
				.einladungIdPath(toHasUntersuchungIdWrapper(untersuchung.getEinladungRequired().getId()))
				.body(payload)
				.execute(ResponseBody::prettyPeek)
				.then()
				.assertThat()
				.statusCode(Status.CREATED.getStatusCode())
				.extract()
				.as(UntersuchungImpfungInternJsonSpec.class);
		// ASSERT
		Assertions.assertThat(result)
				.isNotNull()
				.extracting(json -> json.getData().getImpfstoff().getId())
				.isEqualTo(impfstoff.getId().getId());
	}

	@Test
	@TestAsAdminBS
	void updateUntersuchungImpfungenIntern_should_set_immunitaet_on_dossier_for_masern_varizellen() {
		// ARRANGE
		Untersuchung untersuchung = fixtureService.createUntersuchungWithImpfungen();
        UntersuchungImpfungen untersuchungImpfungen = untersuchung.getUntersuchungImpfungen();
        Assertions.assertThat(untersuchungImpfungen).isNotNull();
		Impfstoff impfstoff = fixtureService.createImpfstoff(Krankheit.MASERN, Krankheit.VARIZELLEN);
		// ACT
		UntersuchungImpfungInternSaveJsonSpec payload =
				UntersuchungImpfungInternBuilder.createJaxSpec(builder -> builder
                    .forUntersuchungImpfungen(untersuchungImpfungen)
                    .withImpfstoff(impfstoff)
					.withImmunitaetForKrankheit(Krankheit.MASERN, true)
					.withImmunitaetForKrankheit(Krankheit.VARIZELLEN, true)).getData();

		api.untersuchungImpfungenResourceCreateUntersuchungImpfungIntern()
				.anlassIdPath(untersuchung.getAnlass().getId().getId())
				.einladungIdPath(toHasUntersuchungIdWrapper(untersuchung.getEinladungRequired().getId()))
				.body(payload)
				.execute(ResponseBody::prettyPeek)
				.then()
				.assertThat()
				.statusCode(Status.CREATED.getStatusCode())
				.extract()
				.as(UntersuchungImpfungInternJsonSpec.class);
		// ASSERT
		Dossier dossier = dossierService.getById(untersuchung.getEinladungRequired().getDossier().getId());
		Assertions.assertThat(dossier.isImmunitaetVarizellen()).isTrue();
		Assertions.assertThat(dossier.isImmunitaetMasern()).isTrue();
	}

	@Test
	@TestAsAdminBS
	void updateUntersuchungImpfungenIntern() {
		// ARRANGE
		Untersuchung untersuchung = fixtureService.createUntersuchungWithImpfungen();
        UntersuchungImpfungen untersuchungImpfungen = untersuchung.getUntersuchungImpfungen();
        Assertions.assertThat(untersuchungImpfungen).isNotNull();
		Impfstoff impfstoff = fixtureService.createImpfstoff();

		UntersuchungImpfungIntern existingImpfung =
				untersuchungImpfungInternService.persist(UntersuchungImpfungInternBuilder.create(builder -> builder.forUntersuchungImpfungen(
                        untersuchungImpfungen)
						.withUserErfasst(fixtureService.createBenutzerFuerZentraleOrganisation())
						.withDatumImpfung(null)
						.withImpfstoff(impfstoff)));

		// ACT
		UntersuchungImpfungInternSaveJsonSpec payload =
				UntersuchungImpfungInternBuilder.createJaxSpec(builder -> builder.forImpfungIntern(existingImpfung)
						.withImpfstoff(fixtureService.createImpfstoff())
						.withImmunitaetForKrankheit(Krankheit.MASERN, true)
						.withDatumImpfung(appClock.offsetDateTime().now().toLocalDate())).getData();
		UntersuchungImpfungInternJsonSpec result = api.untersuchungImpfungenResourceUpdateUntersuchungImpfungenIntern()
				.anlassIdPath(untersuchung.getAnlass().getId().getId())
				.einladungIdPath(toHasUntersuchungIdWrapper(untersuchung.getEinladungRequired().getId()))
				.impfungInternIdPath(existingImpfung.getId().getId())
				.body(payload)
				.execute(ResponseBody::prettyPeek)
				.then()
				.assertThat()
				.statusCode(Status.OK.getStatusCode())
				.extract()
				.as(UntersuchungImpfungInternJsonSpec.class);

		// ASSERT
		Assertions.assertThat(result).isNotNull();
		Assertions.assertThat(result.getData().getDatumImpfung())
				.isEqualTo(appClock.offsetDateTime().now().toLocalDate());
		Assertions.assertThat(result.getData().getKrankheitenImmunitaeten())
				.anyMatch(json -> json.getKrankheit() == KrankheitSpec.MASERN && json.getImmunitaetGenuegend());
	}

    @Test
    @TestAsAdminBS
    void updateUntersuchungImpfungenIntern_durchfuehren_setzt_anlass_durchgefuehrt() {
        // ARRANGE
        Untersuchung untersuchung = fixtureService.createUntersuchungWithImpfungen();
        UntersuchungImpfungen untersuchungImpfungen = untersuchung.getUntersuchungImpfungen();
        Assertions.assertThat(untersuchungImpfungen).isNotNull();
        Impfstoff impfstoff = fixtureService.createImpfstoff();

        UntersuchungImpfungIntern existingImpfung =
            untersuchungImpfungInternService.persist(UntersuchungImpfungInternBuilder.create(builder -> builder.forUntersuchungImpfungen(
                    untersuchungImpfungen)
                .withUserErfasst(fixtureService.createBenutzerFuerZentraleOrganisation())
                .withDatumImpfung(null)
                .withImpfstoff(impfstoff)));
        Assertions.assertThat(existingImpfung.getImpfStatus()).isEqualTo(ImpfStatus.GEPLANT);

        // ACT
        UntersuchungImpfungInternSaveJsonSpec payload =
            UntersuchungImpfungInternBuilder.createJaxSpec(builder -> builder.forImpfungIntern(existingImpfung)
                .withImpfstoff(fixtureService.createImpfstoff())
                .withImpfStatus(ImpfStatus.DURCHGEFUEHRT)
                .withImmunitaetForKrankheit(Krankheit.MASERN, true)
                .withDatumImpfung(appClock.offsetDateTime().now().toLocalDate())).getData();

        UntersuchungImpfungInternJsonSpec result = api.untersuchungImpfungenResourceUpdateUntersuchungImpfungenIntern()
            .anlassIdPath(untersuchung.getAnlass().getId().getId())
            .einladungIdPath(toHasUntersuchungIdWrapper(untersuchung.getEinladungRequired().getId()))
            .impfungInternIdPath(existingImpfung.getId().getId())
            .body(payload)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract()
            .as(UntersuchungImpfungInternJsonSpec.class);

        // ASSERT
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getData().getImpfStatus())
            .isEqualTo(ImpfStatusSpec.DURCHGEFUEHRT);
        Assertions.assertThat(result.getAnlassImpfungDurchgefuehrtId())
            .isEqualTo(untersuchung.getAnlass().getId().getId());
    }

    @Test
    @TestAsAdminBS
    void getUntersuchungImpfungenDataIntern_setzt_durchgefuehr_bei_neuem_anlass() {
        // ARRANGE
        Dossier dossier = fixtureService.createDossier();
        Anlass previousAnlass = fixtureService.createAnlass(LocalDate.of(2021, 1, 1));
        Einladung previousEinladung = fixtureService.createEinladung(previousAnlass, dossier);
        Untersuchung previousUntersuchung = fixtureService.createUntersuchungWithImpfungen(previousEinladung);
        Impfstoff impfstoff = fixtureService.createImpfstoff();

        UntersuchungImpfungen untersuchungImpfungen = previousUntersuchung.getUntersuchungImpfungen();
        Assertions.assertThat(untersuchungImpfungen).isNotNull();
        UntersuchungImpfungIntern existingImpfung =
            untersuchungImpfungInternService.persist(UntersuchungImpfungInternBuilder.create(builder -> builder
                .forUntersuchungImpfungen(untersuchungImpfungen)
                .withUserErfasst(fixtureService.createBenutzerFuerZentraleOrganisation())
                .withImpfstoff(impfstoff)
                .withImmunitaetForKrankheit(Krankheit.MASERN, true)));

        Anlass anlass = fixtureService.createAnlass(LocalDate.of(2024, 1, 1));
        Einladung einladung = fixtureService.createEinladung(anlass, dossier);

        Untersuchung untersuchung = fixtureService.createUntersuchungWithImpfungen(einladung);
        Assertions.assertThat(untersuchung.getUntersuchungImpfungen()).isNotNull();

        // ACT
        UntersuchungImpfungInternSaveJsonSpec payload =
            UntersuchungImpfungInternBuilder.createJaxSpec(builder -> builder.forImpfungIntern(existingImpfung)
                .withImpfstoff(fixtureService.createImpfstoff())
                .withImpfStatus(ImpfStatus.DURCHGEFUEHRT)
                .withImmunitaetForKrankheit(Krankheit.MASERN, true)
                .withDatumImpfung(appClock.offsetDateTime().now().toLocalDate())).getData();

        UntersuchungImpfungInternJsonSpec result = api.untersuchungImpfungenResourceUpdateUntersuchungImpfungenIntern()
            .anlassIdPath(untersuchung.getAnlass().getId().getId())
            .einladungIdPath(toHasUntersuchungIdWrapper(untersuchung.getEinladungRequired().getId()))
            .impfungInternIdPath(existingImpfung.getId().getId())
            .body(payload)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract()
            .as(UntersuchungImpfungInternJsonSpec.class);

        // ASSERT
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getData().getImpfStatus())
            .isEqualTo(ImpfStatusSpec.DURCHGEFUEHRT);
        Assertions.assertThat(result.getAnlassImpfungDurchgefuehrtId())
            .isEqualTo(untersuchung.getAnlass().getId().getId());
    }

    @Test
	@TestAsAdminBS
	void removeUntersuchungImpfungenIntern() {
		// ARRANGE
		Untersuchung untersuchung = fixtureService.createUntersuchungWithImpfungen();
        UntersuchungImpfungen untersuchungImpfungen = untersuchung.getUntersuchungImpfungen();
        Assertions.assertThat(untersuchungImpfungen).isNotNull();
		Impfstoff impfstoff = fixtureService.createImpfstoff();

		UntersuchungImpfungIntern existingImpfung =
				untersuchungImpfungInternService.persist(UntersuchungImpfungInternBuilder.create(builder -> builder.forUntersuchungImpfungen(
                        untersuchungImpfungen)
						.withUserErfasst(fixtureService.createBenutzerFuerZentraleOrganisation())
						.withDatumImpfung(null)
						.withImpfstoff(impfstoff)));
		List<UntersuchungImpfungIntern> impfungenVorher =
				untersuchungImpfungInternService.findByUntersuchung(untersuchung.getId());
		Assertions.assertThat(impfungenVorher).isNotEmpty();
		// ACT
		api.untersuchungImpfungenResourceRemoveUntersuchungImpfungenIntern()
				.anlassIdPath(untersuchung.getAnlass().getId().getId())
				.einladungIdPath(toHasUntersuchungIdWrapper(untersuchung.getEinladungRequired().getId()))
				.impfungInternIdPath(existingImpfung.getId().getId())
				.execute(ResponseBody::prettyPeek)
				.then()
				.assertThat()
				.statusCode(Status.NO_CONTENT.getStatusCode());
		// ASSERT
		List<UntersuchungImpfungIntern> impfungenNachher =
				untersuchungImpfungInternService.findByUntersuchung(untersuchung.getId());
		Assertions.assertThat(impfungenNachher).isEmpty();
	}

	@Test
	@TestAsAdminBS
	void removeUntersuchungImpfungenIntern_not_allowed_if_impfung_from_another_anlass() {
		// ARRANGE
		Anlass anotherAnlass = fixtureService.createAnlass();
		Untersuchung untersuchung = fixtureService.createUntersuchungWithImpfungen();
        UntersuchungImpfungen untersuchungImpfungen = untersuchung.getUntersuchungImpfungen();
        Assertions.assertThat(untersuchungImpfungen).isNotNull();
		Impfstoff impfstoff = fixtureService.createImpfstoff();

		UntersuchungImpfungIntern existingImpfung =
				untersuchungImpfungInternService.persist(UntersuchungImpfungInternBuilder.create(builder -> builder.forUntersuchungImpfungen(
                        untersuchungImpfungen)
						.withAnlassImpfungErfasst(anotherAnlass)
						.withUserErfasst(fixtureService.createBenutzerFuerZentraleOrganisation())
						.withImpfstoff(impfstoff)));
		List<UntersuchungImpfungIntern> impfungenVorher =
				untersuchungImpfungInternService.findByUntersuchung(untersuchung.getId());
		Assertions.assertThat(impfungenVorher).isNotEmpty();
		// ACT
		AppFailureErrorJsonSpec error = api.untersuchungImpfungenResourceRemoveUntersuchungImpfungenIntern()
				.anlassIdPath(untersuchung.getAnlass().getId().getId())
				.einladungIdPath(toHasUntersuchungIdWrapper(untersuchung.getEinladungRequired().getId()))
				.impfungInternIdPath(existingImpfung.getId().getId())
				.execute(ResponseBody::prettyPeek)
				.then()
				.assertThat()
				.statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode())
				.extract()
				.as(AppFailureErrorJsonSpec.class);
		// ASSERT
		Assertions.assertThat(error).isNotNull();
		Assertions.assertThat(error.getSummary()).contains("Die Impfung kann nicht gelöscht werden");
		List<UntersuchungImpfungIntern> impfungenNachher =
				untersuchungImpfungInternService.findByUntersuchung(untersuchung.getId());
		Assertions.assertThat(impfungenNachher).isNotEmpty();
	}

    @Test
    @TestAsAdminBS
    void getUntersuchungImpfungen_should_get_massnahmen_achtung_from_previous_anlass() {
        // ARRANGE
        String achtungString = "Kind ist auf etwas allergisch";

        Dossier dossier = fixtureService.createDossier();

        Anlass previousAnlass = fixtureService.createAnlass(LocalDate.of(2021, 1, 1));
        Einladung previousEinladung = fixtureService.createEinladung(previousAnlass, dossier);
        Untersuchung previousUntersuchung = fixtureService.createUntersuchungWithImpfungenMassnahmenAchtung(previousEinladung, achtungString);

        Assertions.assertThat(previousUntersuchung.getUntersuchungImpfungen()).isNotNull();
        Assertions.assertThat(previousUntersuchung.getUntersuchungImpfungen().getUntersuchungImpfungenMassnahmen()).isNotNull();
        Assertions.assertThat(previousUntersuchung.getUntersuchungImpfungen().getUntersuchungImpfungenMassnahmen().getAchtung()).isEqualTo(achtungString);

        Anlass newAnlass = fixtureService.createAnlass(LocalDate.of(2024, 1, 1));
        Einladung newEinladung = fixtureService.createEinladung(newAnlass, dossier);
        Untersuchung newUntersuchung = fixtureService.createUntersuchungWithImpfungen(newEinladung);
        Assertions.assertThat(newUntersuchung.getUntersuchungImpfungen()).isNotNull();

        // ACT
        UntersuchungImpfungenDataJsonSpec data = api.untersuchungImpfungenResourceGetUntersuchungImpfungenData()
            .anlassIdPath(newEinladung.getAnlass().getId().getId())
            .einladungIdPath(toHasUntersuchungIdWrapper(newEinladung.getId()))
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract()
            .as(UntersuchungImpfungenDataJsonSpec.class);
        UntersuchungImpfungenMassnahmenJsonSpec result = data.getMassnahmen();

        // ASSERT
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getData().getAchtung()).isEqualTo(achtungString);
    }

    @Test
    @TestAsAdminBS
    void getUntersuchungImpfungen_should_get_impfausweis_checkboxen_from_previous_anlass() {
        // ARRANGE
        Dossier dossier = fixtureService.createDossier();

        // config with impfungenUebersicht required so the
        // CheckUntersuchungImpfstatusKrankheitPresentIfReqValidator required-branch is exercised.
        // A non-overlapping (pre-2000) Gueltigkeitszeitraum is used so it does not collide with the
        // seeded default config (valid 2000-01-01..9999-12-31).
        AnlassConfig previousAnlassConfig = AnlassConfigBuilder.create(builder -> builder
            .withGueltigkeitsZeitraum(LocalDate.of(1999, 1, 1), LocalDate.of(1999, 12, 31))
            .withAnlassTyp(AnlassTyp.KLASSE_4)
            .withImpfungenUebersicht(RequirementSetting.REQUIRED));
        anlassConfigRepo.persist(previousAnlassConfig);
        Anlass previousAnlass = fixtureService.createAnlass(previousAnlassConfig);
        Einladung previousEinladung = fixtureService.createEinladung(previousAnlass, dossier);

        Untersuchung previousUntersuchung = fixtureService.createUntersuchungWithImpfungen(previousEinladung);
        Assertions.assertThat(previousUntersuchung.getUntersuchungImpfungen()).isNotNull();
        fixtureService.setImpfausweisCheckboxen(
            previousUntersuchung.getUntersuchungImpfungen().getId(),
            true, false, true
        );

        fixtureService.createOrUpdateUntersuchungImpfstatusKrankheit(
            previousUntersuchung.getUntersuchungImpfungen(),
            Krankheit.MASERN,
            true,
            null
        );

        Anlass newAnlass = fixtureService.createAnlass(LocalDate.of(2024, 1, 1));
        Einladung newEinladung = fixtureService.createEinladung(newAnlass, dossier);

        // ACT
        UntersuchungImpfungenDataJsonSpec data = api.untersuchungImpfungenResourceGetUntersuchungImpfungenData()
            .anlassIdPath(newEinladung.getAnlass().getId().getId())
            .einladungIdPath(toHasUntersuchungIdWrapper(newEinladung.getId()))
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract()
            .as(UntersuchungImpfungenDataJsonSpec.class);

        // ASSERT
        Assertions.assertThat(data.getUntersuchungImpfungen()).isNotNull();
        Assertions.assertThat(data.getUntersuchungImpfungen().getData().getImpfausweis().getImpfausweisNichtVorhanden()).isTrue();
        Assertions.assertThat(data.getUntersuchungImpfungen().getData().getImpfausweis().getImpfausweisUnvollstaendig()).isFalse();
        Assertions.assertThat(data.getUntersuchungImpfungen().getData().getImpfausweis().getImpfausweisZweiPlusEinsSchema()).isTrue();
        Assertions.assertThat(data.getUntersuchungImpfungen().getData().getImpfausweis().getImpfausweisStatusMutiert()).isFalse();

        Assertions.assertThat(data.getImpfstatusList()).isNotNull();
        UntersuchungImpfstatusKrankheitSaveJsonSpec masernImpfstatus = data.getImpfstatusList()
            .getImpfstatusList()
            .stream()
            .filter(impfstatus -> impfstatus.getKrankheit() == KrankheitSpec.MASERN)
            .findFirst()
            .orElseThrow();
        Assertions.assertThat(masernImpfstatus.getImpfschutzGegeben()).isEqualTo(ImpfschutzGegebenSpec.JA);
        Assertions.assertThat(masernImpfstatus.getDatumLetzteImpfung()).isNull();
    }

    @Test
    @TestAsAdminBS
    void saveUntersuchungImpfungenImpfstatus_should_fail_if_datum_required_but_missing_when_mutiert() {
        // ARRANGE
        Dossier dossier = fixtureService.createDossier();

        // config with impfungenUebersicht required so the
        // CheckUntersuchungImpfstatusKrankheitPresentIfReqValidator required-branch is exercised.
        // A non-overlapping (pre-2000) Gueltigkeitszeitraum is used so it does not collide with the
        // seeded default config (valid 2000-01-01..9999-12-31).
        AnlassConfig anlassConfig = AnlassConfigBuilder.create(builder -> builder
            .withGueltigkeitsZeitraum(LocalDate.of(1999, 1, 1), LocalDate.of(1999, 12, 31))
            .withAnlassTyp(AnlassTyp.KLASSE_4)
            .withImpfungenUebersicht(RequirementSetting.REQUIRED));
        anlassConfigRepo.persist(anlassConfig);
        Anlass anlass = fixtureService.createAnlass(anlassConfig);
        Einladung einladung = fixtureService.createEinladung(anlass, dossier);

        Untersuchung untersuchung = fixtureService.createUntersuchungWithImpfungen(einladung);
        UntersuchungImpfungen untersuchungImpfungen = untersuchung.getUntersuchungImpfungen();
        Assertions.assertThat(untersuchungImpfungen).isNotNull();

        // Den Impfausweis mutieren (Modified-Flag setzen). Der Impfausweis ist vollstaendig vorhanden
        // (weder nichtVorhanden noch unvollstaendig), damit der Validator das Datum verlangt.
        UntersuchungImpfungenSaveJsonSpec impfausweisSaveJsonSpec =
            UntersuchungImpfungenBuilder.createJaxSpec(builder -> builder
                .forUntersuchungImpfungen(untersuchungImpfungen)
                .withImpfausweisNichtVorhanden(false)
                .withImpfausweisUnvollstaendig(false)
                .withImpfausweisStatusMutiert(true)).getData();
        api.untersuchungImpfungenResourceSaveUntersuchungImpfungen()
            .anlassIdPath(untersuchung.getAnlass().getId().getId())
            .einladungIdPath(toHasUntersuchungIdWrapper(untersuchung.getEinladungRequired().getId()))
            .body(impfausweisSaveJsonSpec)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode());

        // ACT
        // Impfstatus mit Anzahl Impfungen > 0 aber ohne Datum der letzten Impfung speichern.
        // Da der Impfausweis mutiert und vollstaendig vorhanden ist und das Datum konfigurationsbedingt
        // verlangt wird, muss die Validierung fehlschlagen.
        UntersuchungImpfstatusKrankheitSaveJsonSpec updateSpec =
            UntersuchungImpfstatusKrankheitBuilder.createSaveJaxSpec(builder -> builder
                .forUntersuchungImpfungen(untersuchungImpfungen)
                .forKrankheit(Krankheit.MASERN)
                .withAnzahlImpfungen(2)
                .withDatumLetzteImpfung(null));
        UntersuchungImpfstatusListSaveJsonSpec updateListSpec =
            new UntersuchungImpfstatusListSaveJsonSpec(List.of(updateSpec));

        // ASSERT
        api.untersuchungImpfungenResourceSaveUntersuchungImpfungenImpfstatus()
            .anlassIdPath(untersuchung.getAnlass().getId().getId())
            .einladungIdPath(toHasUntersuchungIdWrapper(untersuchung.getEinladungRequired().getId()))
            .body(updateListSpec)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.BAD_REQUEST.getStatusCode());
    }

	@Test
	@TestAsAdminBS
	void saveUntersuchungMassnahmen_creates_aufgaben() {
		// ARRANGE
		Untersuchung untersuchung = fixtureService.createUntersuchungWithImpfungen();
        UntersuchungImpfungen untersuchungImpfungen = untersuchung.getUntersuchungImpfungen();
        Assertions.assertThat(untersuchungImpfungen).isNotNull();
		UntersuchungImpfungenMassnahmenJsonSpec massnahmen =
				UntersuchungImpfungenMassnahmenBuilder.createJaxSpec(builder -> builder.forUntersuchungImpfungen(
                        untersuchungImpfungen)
						.withInformationEltern(InformationEltern.BRIEF)
						.withInformationHausarzt(true)
						.withRueckfrageHausarzt(true)
						.withBemerkungen("Meine Impfungs-Massnahmen"));
		// ACT
		UntersuchungImpfungenMassnahmenJsonSpec result = api.untersuchungImpfungenResourceSaveUntersuchungMassnahmen()
				.anlassIdPath(untersuchung.getAnlass().getId().getId())
				.einladungIdPath(toHasUntersuchungIdWrapper(untersuchung.getEinladungRequired().getId()))
				.body(massnahmen.getData())
				.execute(ResponseBody::prettyPeek)
				.then()
				.assertThat()
				.statusCode(Status.OK.getStatusCode())
				.extract()
				.as(UntersuchungImpfungenMassnahmenJsonSpec.class);
		// ASSERT
		Assertions.assertThat(result).isNotNull();
		Assertions.assertThat(result.getData().getInformationEltern()).isEqualTo(InformationElternSpec.BRIEF);
		Assertions.assertThat(result.getData().getBemerkungen()).isEqualTo("Meine Impfungs-Massnahmen");

		Untersuchung untersuchungWithLoadedData =
				fixtureService.getUntersuchungWithLoadedData(untersuchung.getEinladungRequired().getId());
		Assertions.assertThat(untersuchungWithLoadedData.getUntersuchungImpfungen()).isNotNull();
		UntersuchungImpfungenMassnahmen massnahmenLoaded =
				untersuchungWithLoadedData.getUntersuchungImpfungen().getUntersuchungImpfungenMassnahmen();
		Assertions.assertThat(massnahmenLoaded).isNotNull();
		Assertions.assertThat(massnahmenLoaded.getAufgabeInformationEltern()).isNotNull();
		Assertions.assertThat(massnahmenLoaded.getAufgabeInformationHausarzt()).isNotNull();
		Assertions.assertThat(massnahmenLoaded.getAufgabeRueckfrageHausarzt()).isNotNull();
	}

	@Test
	@TestAsAdminBS
	void saveUntersuchungMassnahmen_updates_aufgaben() {
		// ARRANGE
		Untersuchung untersuchung = fixtureService.createUntersuchungWithImpfungenMassnahmen();
        UntersuchungImpfungen untersuchungImpfungen = untersuchung.getUntersuchungImpfungen();
        Assertions.assertThat(untersuchungImpfungen).isNotNull();
		Untersuchung untersuchungBefore =
				fixtureService.getUntersuchungWithLoadedData(untersuchung.getEinladungRequired().getId());
		Assertions.assertThat(untersuchungBefore.getUntersuchungImpfungen()).isNotNull();
		UntersuchungImpfungenMassnahmen massnahmenBefore =
				untersuchungBefore.getUntersuchungImpfungen().getUntersuchungImpfungenMassnahmen();
		Assertions.assertThat(massnahmenBefore).isNotNull();
		Assertions.assertThat(massnahmenBefore.getAufgabeInformationEltern())
				.isNotNull()
				.extracting(Aufgabe::getAufgabeStatus)
				.isEqualTo(AufgabeStatus.OFFEN);
		Assertions.assertThat(massnahmenBefore.getAufgabeInformationHausarzt())
				.isNotNull()
				.extracting(Aufgabe::getAufgabeStatus)
				.isEqualTo(AufgabeStatus.OFFEN);
		Assertions.assertThat(massnahmenBefore.getAufgabeRueckfrageHausarzt())
				.isNotNull()
				.extracting(Aufgabe::getAufgabeStatus)
				.isEqualTo(AufgabeStatus.OFFEN);

		// ACT
		UntersuchungImpfungenMassnahmenJsonSpec payload =
				UntersuchungImpfungenMassnahmenBuilder.createJaxSpec(builder -> builder
                    .forUntersuchungImpfungen(untersuchungImpfungen)
                    .withInformationEltern(InformationEltern.KEINE)
                    .withInformationHausarzt(false)
                    .withRueckfrageHausarzt(false));
		UntersuchungImpfungenMassnahmenJsonSpec result = api.untersuchungImpfungenResourceSaveUntersuchungMassnahmen()
				.anlassIdPath(untersuchung.getAnlass().getId().getId())
				.einladungIdPath(toHasUntersuchungIdWrapper(untersuchung.getEinladungRequired().getId()))
				.body(payload.getData())
				.execute(ResponseBody::prettyPeek)
				.then()
				.assertThat()
				.statusCode(Status.OK.getStatusCode())
				.extract()
				.as(UntersuchungImpfungenMassnahmenJsonSpec.class);

		// ASSERT
		Assertions.assertThat(result).isNotNull();
		Assertions.assertThat(result.getData().getInformationEltern()).isEqualTo(InformationElternSpec.KEINE);

		Untersuchung untersuchungAfter =
				fixtureService.getUntersuchungWithLoadedData(untersuchung.getEinladungRequired().getId());
		Assertions.assertThat(untersuchungAfter.getUntersuchungImpfungen()).isNotNull();
		UntersuchungImpfungenMassnahmen massnahmenAfter =
				untersuchungAfter.getUntersuchungImpfungen().getUntersuchungImpfungenMassnahmen();
		Assertions.assertThat(massnahmenAfter).isNotNull();
		Assertions.assertThat(massnahmenAfter.getAufgabeInformationEltern())
				.isNotNull()
				.extracting(Aufgabe::getAufgabeStatus)
				.isEqualTo(AufgabeStatus.ABGESCHLOSSEN);
		Assertions.assertThat(massnahmenAfter.getAufgabeInformationHausarzt())
				.isNotNull()
				.extracting(Aufgabe::getAufgabeStatus)
				.isEqualTo(AufgabeStatus.ABGESCHLOSSEN);
		Assertions.assertThat(massnahmenAfter.getAufgabeRueckfrageHausarzt())
				.isNotNull()
				.extracting(Aufgabe::getAufgabeStatus)
				.isEqualTo(AufgabeStatus.ABGESCHLOSSEN);
	}

	@Test
	@TestAsAdminBS
	void einzelTerminErstellen() {
		// ARRANGE
		Untersuchung untersuchung = fixtureService.createUntersuchungWithImpfungen();
		// ACT
		api.untersuchungImpfungenResourceEinzelTerminErstellen()
				.anlassIdPath(untersuchung.getAnlass().getId().getId())
				.einladungIdPath(untersuchung.getEinladungRequired().getId().getId())
				.execute(ResponseBody::prettyPeek)
				.then()
				.assertThat()
				.statusCode(Status.NO_CONTENT.getStatusCode());

		// ASSERT
		List<Aufgabe> aufgaben =
				aufgabeService.findAllForKind(untersuchung.getEinladungRequired().getDossier().getKind().getId());
		Assertions.assertThat(aufgaben).isNotEmpty().hasSize(1);
		Assertions.assertThat(aufgaben.getFirst().getTitel())
				.isEqualTo("Gerber Emma: Einzeltermin Impfen erstellen - 2022/23 SU 4.PS Test getAnlass");
	}

    @Test
    @TestAsAdminBS
    void getUntersuchungDataMinimal_is_null_if_not_saved() {
        // ARRANGE
        Untersuchung untersuchung = fixtureService.createUntersuchungWithImpfungen();
        Assertions.assertThat(untersuchung.getUntersuchungImpfungen()).isNotNull();
        Assertions.assertThat(untersuchung.getUntersuchungImpfungen().getUntersuchungImpfungenDataMinimal()).isNull();
        // ACT
        UntersuchungImpfungenDataJsonSpec data = api.untersuchungImpfungenResourceGetUntersuchungImpfungenData()
            .anlassIdPath(untersuchung.getAnlass().getId().getId())
            .einladungIdPath(toHasUntersuchungIdWrapper(untersuchung.getEinladungRequired().getId()))
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract()
            .as(UntersuchungImpfungenDataJsonSpec.class);
        // ASSERT
        Assertions.assertThat(data).isNotNull();
        Assertions.assertThat(data.getDataMinimal()).isNull();
    }

    @Test
    @TestAsAdminBS
    void saveUntersuchungDataMinimal() {
        // ARRANGE
        Untersuchung untersuchung = fixtureService.createUntersuchungWithImpfungen();
        UntersuchungImpfungen untersuchungImpfungen = untersuchung.getUntersuchungImpfungen();
        Assertions.assertThat(untersuchungImpfungen).isNotNull();
        UntersuchungImpfungenDataMinimalJsonSpec saveJson =
            UntersuchungImpfungenDataMinimalBuilder.createJaxSpec(builder -> builder.forUntersuchungImpfungen(
                    untersuchungImpfungen)
                .withImpfausweisGeprueft(ImpfausweisGeprueft.JA, null)
                .withMasernImpfstatus(ImpfStatusMinimal.VOLLSTAENDIG, null)
                .withMasernAnzahlImpfungen(4));
        // ACT: Save
        UntersuchungImpfungenDataMinimalJsonSpec saveResult = api.untersuchungImpfungenResourceSaveUntersuchungImpfungenDataMinimal()
            .anlassIdPath(untersuchung.getAnlass().getId().getId())
            .einladungIdPath(toHasUntersuchungIdWrapper(untersuchung.getEinladungRequired().getId()))
            .body(saveJson.getData())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract()
            .as(UntersuchungImpfungenDataMinimalJsonSpec.class);
        // ASSERT: Save
        Assertions.assertThat(saveResult).isNotNull();
        Assertions.assertThat(saveResult.getData().getImpfausweisGeprueft().getValue()).contains(ImpfausweisGeprueft.JA.name());
        Assertions.assertThat(saveResult.getData().getMasernImpfstatus().getValue()).contains(ImpfStatusMinimal.VOLLSTAENDIG.name());
        Assertions.assertThat(saveResult.getData().getMasernAnzahlImpfungen()).isEqualTo(4);
        // ACT: Read
        UntersuchungImpfungenDataJsonSpec resultRead = api.untersuchungImpfungenResourceGetUntersuchungImpfungenData()
            .anlassIdPath(untersuchung.getAnlass().getId().getId())
            .einladungIdPath(toHasUntersuchungIdWrapper(untersuchung.getEinladungRequired().getId()))
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract()
            .as(UntersuchungImpfungenDataJsonSpec.class);
        // ASSERT: Read
        Assertions.assertThat(resultRead).isNotNull();
        Assertions.assertThat(resultRead.getDataMinimal()).isNotNull();
        Assertions.assertThat(resultRead.getDataMinimal().getData().getImpfausweisGeprueft().getValue()).contains(ImpfausweisGeprueft.JA.name());
        Assertions.assertThat(resultRead.getDataMinimal().getData().getMasernImpfstatus().getValue()).contains(ImpfStatusMinimal.VOLLSTAENDIG.name());
        Assertions.assertThat(resultRead.getDataMinimal().getData().getMasernAnzahlImpfungen()).isEqualTo(4);
    }

    @Test
    @TestAsAdminBS
    void getUntersuchungNurUnvollstaendige_is_null_if_not_saved() {
        // ARRANGE
        Untersuchung untersuchung = fixtureService.createUntersuchungWithImpfungen();
        Assertions.assertThat(untersuchung.getUntersuchungImpfungen()).isNotNull();
        Assertions.assertThat(untersuchung.getUntersuchungImpfungen().getUntersuchungImpfungenNurUnvollstaendige()).isNull();
        // ACT
        UntersuchungImpfungenDataJsonSpec data = api.untersuchungImpfungenResourceGetUntersuchungImpfungenData()
            .anlassIdPath(untersuchung.getAnlass().getId().getId())
            .einladungIdPath(toHasUntersuchungIdWrapper(untersuchung.getEinladungRequired().getId()))
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract()
            .as(UntersuchungImpfungenDataJsonSpec.class);
        // ASSERT
        Assertions.assertThat(data).isNotNull();
        Assertions.assertThat(data.getNurUnvollstaendige()).isNull();
    }

    @Test
    @TestAsAdminBS
    void saveUntersuchungNurUnvollstaendige() {
        // ARRANGE
        Untersuchung untersuchung = fixtureService.createUntersuchungWithImpfungen();
        UntersuchungImpfungen untersuchungImpfungen = untersuchung.getUntersuchungImpfungen();
        Assertions.assertThat(untersuchungImpfungen).isNotNull();
        UntersuchungImpfungenNurUnvollstaendigeJsonSpec saveJson =
            UntersuchungImpfungenNurUnvollstaendigeBuilder.createJaxSpec(builder -> builder.forUntersuchungImpfungen(
                    untersuchungImpfungen)
                .withImpfausweisVollstaendig(ImpfausweisVollstaendig.KEINE_ANGABE)
                .withImpfausweisGrundUnvollstaendig(ImpfausweisGrundUnvollstaendig.MEDIZINISCHE_KONTRAINDIKATION));
        // ACT: Save
        UntersuchungImpfungenNurUnvollstaendigeJsonSpec saveResult = api.untersuchungImpfungenResourceSaveUntersuchungImpfungenNurUnvollstaendige()
            .anlassIdPath(untersuchung.getAnlass().getId().getId())
            .einladungIdPath(toHasUntersuchungIdWrapper(untersuchung.getEinladungRequired().getId()))
            .body(saveJson.getData())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract()
            .as(UntersuchungImpfungenNurUnvollstaendigeJsonSpec.class);
        // ASSERT: Save
        Assertions.assertThat(saveResult).isNotNull();
        Assertions.assertThat(saveResult.getData().getImpfausweisVollstaendig()).isNotNull();
        Assertions.assertThat(saveResult.getData().getImpfausweisGrundUnvollstaendig()).isNotNull();
        Assertions.assertThat(saveResult.getData().getImpfausweisVollstaendig().getValue()).contains(ImpfausweisVollstaendig.KEINE_ANGABE.name());
        Assertions.assertThat(saveResult.getData().getImpfausweisGrundUnvollstaendig().getValue()).contains(ImpfausweisGrundUnvollstaendig.MEDIZINISCHE_KONTRAINDIKATION.name());
        // ACT: Read
        UntersuchungImpfungenDataJsonSpec resultRead = api.untersuchungImpfungenResourceGetUntersuchungImpfungenData()
            .anlassIdPath(untersuchung.getAnlass().getId().getId())
            .einladungIdPath(toHasUntersuchungIdWrapper(untersuchung.getEinladungRequired().getId()))
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract()
            .as(UntersuchungImpfungenDataJsonSpec.class);
        // ASSERT: Read
        Assertions.assertThat(resultRead).isNotNull();
        Assertions.assertThat(resultRead.getNurUnvollstaendige()).isNotNull();
        Assertions.assertThat(resultRead.getNurUnvollstaendige().getData().getImpfausweisVollstaendig()).isNotNull();
        Assertions.assertThat(resultRead.getNurUnvollstaendige().getData().getImpfausweisGrundUnvollstaendig()).isNotNull();
        Assertions.assertThat(resultRead.getNurUnvollstaendige().getData().getImpfausweisVollstaendig().getValue()).contains(ImpfausweisVollstaendig.KEINE_ANGABE.name());
        Assertions.assertThat(resultRead.getNurUnvollstaendige().getData().getImpfausweisGrundUnvollstaendig().getValue()).contains(ImpfausweisGrundUnvollstaendig.MEDIZINISCHE_KONTRAINDIKATION.name());
    }

    @Test
    @TestAsSchularztBE
    void saveUntersuchungImpfungen_anonym() {
        // ARRANGE
        tenantProducer.setCurrentTenant(TenantEnum.BE);

        Benutzer schularzt = fixtureService.readBenutzerWithBerechtigungen(TestAsSchularztBE.BENUTZER_ID_ID);
        AnonymerEintrag anonymerEintrag = fixtureService.createAnonymerEintragForArzt(schularzt);
        untersuchungService.getOrCreateUntersuchungAnonym(anonymerEintrag.getId(), false);

        // ACT
        UntersuchungImpfungenSaveJsonSpec saveJsonSpec =
            UntersuchungImpfungenBuilder.createJaxSpec(builder -> builder
                .withBemerkungen("Test saveUntersuchungImpfungen_anonym")
                .withVisum1(schularzt, appClock.offsetDateTime().now().toLocalDate())
            ).getData();
        saveJsonSpec.setVersion(saveJsonSpec.getVersion() - 1);

        UntersuchungImpfungenJsonSpec result = api.untersuchungImpfungenResourceSaveUntersuchungImpfungen()
            .anlassIdPath(anonymerEintrag.getAnlass().getId().getId())
            .einladungIdPath(toHasAnonymerEintragIdWrapper(anonymerEintrag.getId()))
            .body(saveJsonSpec)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract()
            .as(UntersuchungImpfungenJsonSpec.class);

        // ASSERT
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getId()).isNotNull();
        Assertions.assertThat(result.getData().getCheckliste().getBemerkungen())
            .isNotNull()
            .isEqualTo("Test saveUntersuchungImpfungen_anonym");
        Assertions.assertThat(result.getData().getDoppelkontrolle().getVisumPerson1())
            .isNotNull()
            .extracting(BenutzerOptionJsonSpec::getId)
            .isEqualTo(schularzt.getId().getId());
        Assertions.assertThat(untersuchungService.getOrCreateUntersuchungAnonym(anonymerEintrag.getId(), true)
            .getUntersuchungImpfungen()).isNotNull();
    }

    @Test
    @TestAsSchularztBE
    void saveUntersuchungMassnahmen_anonym() {
        // ARRANGE
        tenantProducer.setCurrentTenant(TenantEnum.BE);

        Benutzer schularzt = fixtureService.readBenutzerWithBerechtigungen(TestAsSchularztBE.BENUTZER_ID_ID);
        AnonymerEintrag anonymerEintrag = fixtureService.createAnonymerEintragForArzt(schularzt);
        untersuchungService.getOrCreateUntersuchungAnonym(anonymerEintrag.getId(), false);

        // ACT
        UntersuchungImpfungenMassnahmenJsonSpec massnahmen =
            UntersuchungImpfungenMassnahmenBuilder.createJaxSpec(builder -> builder
                .withInformationEltern(InformationEltern.BRIEF)
                .withInformationHausarzt(true)
                .withRueckfrageHausarzt(true)
                .withBemerkungen("Test saveUntersuchungMassnahmen_anonym"));

        UntersuchungImpfungenMassnahmenJsonSpec result = api.untersuchungImpfungenResourceSaveUntersuchungMassnahmen()
            .anlassIdPath(anonymerEintrag.getAnlass().getId().getId())
            .einladungIdPath(toHasAnonymerEintragIdWrapper(anonymerEintrag.getId()))
            .body(massnahmen.getData())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract()
            .as(UntersuchungImpfungenMassnahmenJsonSpec.class);

        // ASSERT
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getData().getInformationEltern()).isEqualTo(InformationElternSpec.BRIEF);
        Assertions.assertThat(result.getData().getBemerkungen()).isEqualTo("Test saveUntersuchungMassnahmen_anonym");

        Untersuchung untersuchungAfter =
            untersuchungService.getOrCreateUntersuchungAnonym(anonymerEintrag.getId(), true);
        Assertions.assertThat(untersuchungAfter.getUntersuchungImpfungen()).isNotNull();
        UntersuchungImpfungenMassnahmen massnahmenAfter =
            untersuchungAfter.getUntersuchungImpfungen().getUntersuchungImpfungenMassnahmen();
        Assertions.assertThat(massnahmenAfter).isNotNull();
    }

    @Test
    @TestAsSchularztBE
    void saveUntersuchungImpfungenImpfstatus_anonym() {
        // ARRANGE
        tenantProducer.setCurrentTenant(TenantEnum.BE);

        Benutzer schularzt = fixtureService.readBenutzerWithBerechtigungen(TestAsSchularztBE.BENUTZER_ID_ID);
        AnonymerEintrag anonymerEintrag = fixtureService.createAnonymerEintragForArzt(schularzt);
        untersuchungService.getOrCreateUntersuchungAnonym(anonymerEintrag.getId(), false);

        // ACT
        UntersuchungImpfstatusKrankheitSaveJsonSpec createJsonSpec =
            UntersuchungImpfstatusKrankheitBuilder.createSaveJaxSpec(builder -> builder
                .forKrankheit(Krankheit.MASERN)
                .withAnzahlImpfungen(4));
        UntersuchungImpfstatusListSaveJsonSpec listSpec =
            new UntersuchungImpfstatusListSaveJsonSpec(List.of(createJsonSpec));

        UntersuchungImpfstatusListSaveJsonSpec result =
            api.untersuchungImpfungenResourceSaveUntersuchungImpfungenImpfstatus()
                .anlassIdPath(anonymerEintrag.getAnlass().getId().getId())
                .einladungIdPath(toHasAnonymerEintragIdWrapper(anonymerEintrag.getId()))
                .body(listSpec)
                .execute(ResponseBody::prettyPeek)
                .then()
                .assertThat()
                .statusCode(Status.OK.getStatusCode())
                .extract()
                .as(UntersuchungImpfstatusListSaveJsonSpec.class);

        // ASSERT
        Assertions.assertThat(result.getImpfstatusList()).isNotNull().isNotEmpty();
        // Der Impfstatus fuer Masern sollte 4 Impfungen haben
        UntersuchungImpfstatusKrankheitSaveJsonSpec masernJson = result.getImpfstatusList()
            .stream()
            .filter(impfstatus -> impfstatus.getKrankheit() == KrankheitSpec.MASERN)
            .findFirst()
            .orElseThrow();
        Assertions.assertThat(masernJson.getAnzahlImpfungen()).isEqualTo(4);

        // UntersuchungImpfungen wurde auf der anonymen Untersuchung erstellt
        Assertions.assertThat(untersuchungService.getOrCreateUntersuchungAnonym(anonymerEintrag.getId(), true)
            .getUntersuchungImpfungen()).isNotNull();
    }

    @Test
    @TestAsSchularztBE
    void saveUntersuchungImpfungenDataMinimal_anonym() {
        // ARRANGE
        tenantProducer.setCurrentTenant(TenantEnum.BE);

        Benutzer schularzt = fixtureService.readBenutzerWithBerechtigungen(TestAsSchularztBE.BENUTZER_ID_ID);
        AnonymerEintrag anonymerEintrag = fixtureService.createAnonymerEintragForArzt(schularzt);
        untersuchungService.getOrCreateUntersuchungAnonym(anonymerEintrag.getId(), false);

        // ACT
        UntersuchungImpfungenDataMinimalJsonSpec saveJson =
            UntersuchungImpfungenDataMinimalBuilder.createJaxSpec(builder -> builder
                .withImpfausweisGeprueft(ImpfausweisGeprueft.JA, null)
                .withImmunsuppression(Immunsuppression.NEIN, null)
                .withMasernImpfstatus(ImpfStatusMinimal.VOLLSTAENDIG, null)
                .withMasernAnzahlImpfungen(4)
                .withHpvImpfstatus(ImpfStatusMinimal.VOLLSTAENDIG, null)
                .withHpvAnzahlImpfungen(2)
                .withImpfempfehlung(ImpfempfehlungMinimal.JA, null)
                .withImpfungDurchgefuehrt(ImpfungDurchgefuehrtMinimal.JA, null));
        // Neue DataMinimal, daher Version 0
        saveJson.getData().setVersion(0L);

        UntersuchungImpfungenDataMinimalJsonSpec saveResult =
            api.untersuchungImpfungenResourceSaveUntersuchungImpfungenDataMinimal()
                .anlassIdPath(anonymerEintrag.getAnlass().getId().getId())
                .einladungIdPath(toHasAnonymerEintragIdWrapper(anonymerEintrag.getId()))
                .body(saveJson.getData())
                .execute(ResponseBody::prettyPeek)
                .then()
                .assertThat()
                .statusCode(Status.OK.getStatusCode())
                .extract()
                .as(UntersuchungImpfungenDataMinimalJsonSpec.class);

        // ASSERT
        Assertions.assertThat(saveResult).isNotNull();
        Assertions.assertThat(saveResult.getData().getImpfausweisGeprueft().getValue()).contains(ImpfausweisGeprueft.JA.name());
        Assertions.assertThat(saveResult.getData().getMasernImpfstatus().getValue()).contains(ImpfStatusMinimal.VOLLSTAENDIG.name());
        Assertions.assertThat(saveResult.getData().getMasernAnzahlImpfungen()).isEqualTo(4);

        // UntersuchungImpfungenDataMinimal wurde auf der anonymen Untersuchung persistiert
        Untersuchung untersuchungAfter =
            untersuchungService.getOrCreateUntersuchungAnonym(anonymerEintrag.getId(), true);
        Assertions.assertThat(untersuchungAfter.getUntersuchungImpfungen()).isNotNull();
        Assertions.assertThat(untersuchungAfter.getUntersuchungImpfungen().getUntersuchungImpfungenDataMinimal()).isNotNull();
    }

    @Test
    @TestAsSchularztBE
    void saveUntersuchungImpfungenNurUnvollstaendige_anonym() {
        // ARRANGE
        tenantProducer.setCurrentTenant(TenantEnum.BE);

        Benutzer schularzt = fixtureService.readBenutzerWithBerechtigungen(TestAsSchularztBE.BENUTZER_ID_ID);
        AnonymerEintrag anonymerEintrag = fixtureService.createAnonymerEintragForArzt(schularzt);
        untersuchungService.getOrCreateUntersuchungAnonym(anonymerEintrag.getId(), false);

        // ACT
        UntersuchungImpfungenNurUnvollstaendigeJsonSpec saveJson =
            UntersuchungImpfungenNurUnvollstaendigeBuilder.createJaxSpec(builder -> builder
                .withImpfausweisVollstaendig(ImpfausweisVollstaendig.NEIN)
                .withImpfausweisGrundUnvollstaendig(ImpfausweisGrundUnvollstaendig.IMPFENTSCHEIDUNG_ERZIEHUNGSBERECHTIGTE));
        // Neue NurUnvollstaendige, daher Version 0
        saveJson.getData().setVersion(0L);

        UntersuchungImpfungenNurUnvollstaendigeJsonSpec saveResult =
            api.untersuchungImpfungenResourceSaveUntersuchungImpfungenNurUnvollstaendige()
                .anlassIdPath(anonymerEintrag.getAnlass().getId().getId())
                .einladungIdPath(toHasAnonymerEintragIdWrapper(anonymerEintrag.getId()))
                .body(saveJson.getData())
                .execute(ResponseBody::prettyPeek)
                .then()
                .assertThat()
                .statusCode(Status.OK.getStatusCode())
                .extract()
                .as(UntersuchungImpfungenNurUnvollstaendigeJsonSpec.class);

        // ASSERT
        Assertions.assertThat(saveResult).isNotNull();
        Assertions.assertThat(saveResult.getData().getImpfausweisVollstaendig()).isNotNull();
        Assertions.assertThat(saveResult.getData().getImpfausweisGrundUnvollstaendig()).isNotNull();
        Assertions.assertThat(saveResult.getData().getImpfausweisVollstaendig().getValue()).contains(ImpfausweisVollstaendig.NEIN.name());
        Assertions.assertThat(saveResult.getData().getImpfausweisGrundUnvollstaendig().getValue()).contains(ImpfausweisGrundUnvollstaendig.IMPFENTSCHEIDUNG_ERZIEHUNGSBERECHTIGTE.name());

        // UntersuchungImpfungenNurUnvollstaendige wurde auf der anonymen Untersuchung persistiert
        Untersuchung untersuchungAfter =
            untersuchungService.getOrCreateUntersuchungAnonym(anonymerEintrag.getId(), true);
        Assertions.assertThat(untersuchungAfter.getUntersuchungImpfungen()).isNotNull();
        Assertions.assertThat(untersuchungAfter.getUntersuchungImpfungen().getUntersuchungImpfungenNurUnvollstaendige()).isNotNull();
    }

    @Test
    @TestAsSchularztBE
    void getUntersuchungImpfungenData_anonym() {
        // ARRANGE
        tenantProducer.setCurrentTenant(TenantEnum.BE);

        Benutzer schularzt = fixtureService.readBenutzerWithBerechtigungen(TestAsSchularztBE.BENUTZER_ID_ID);
        AnonymerEintrag anonymerEintrag = fixtureService.createAnonymerEintragForArzt(schularzt);
        untersuchungService.getOrCreateUntersuchungAnonym(anonymerEintrag.getId(), false);

        // ACT
        UntersuchungImpfungenDataJsonSpec data = api.untersuchungImpfungenResourceGetUntersuchungImpfungenData()
            .anlassIdPath(anonymerEintrag.getAnlass().getId().getId())
            .einladungIdPath(toHasAnonymerEintragIdWrapper(anonymerEintrag.getId()))
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract()
            .as(UntersuchungImpfungenDataJsonSpec.class);

        // ASSERT
        Assertions.assertThat(data).isNotNull();
        Assertions.assertThat(data.getUntersuchungImpfungen()).isNotNull();
    }
}
