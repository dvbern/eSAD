package ch.dvbern.oss.hemed.esc.api.features.anlass;

import java.time.LocalDate;

import ch.dvbern.oss.hemed.esc.api.features.anlass.durchfuehrung.UntersuchungDurchfuehrung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.EinladungService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.kind.UntersuchungKindDaten;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.types.EinladungStatus;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.UntersuchungService;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Dossier;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Nachkontrolle;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import ch.dvbern.oss.hemed.esc.api.testutil.AbstractITest;
import ch.dvbern.oss.hemed.esc.api.testutil.FixtureService;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungDurchfuehrungBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungKindDatenBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.restassured.RequestSpecUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsAdminBS;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsMpaBS;
import ch.dvbern.oss.hemed.esc.generated.restassured.api.AnlassApiSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.api.UntersuchungDurchfuehrungApiSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.AnlassJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.AnlassStatusSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.UntersuchungDurchfuehrungDataJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.UntersuchungDurchfuehrungDataSaveJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.UntersuchungDurchfuehrungJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.UntersuchungDurchfuehrungSaveJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.UntersuchungKindDatenSaveJsonSpec;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.ResponseBody;
import jakarta.ws.rs.core.Response.Status;
import lombok.RequiredArgsConstructor;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
@RequiredArgsConstructor
class UntersuchungDurchfuehrungResourceITest extends AbstractITest {

    private final UntersuchungDurchfuehrungApiSpec api =
        UntersuchungDurchfuehrungApiSpec.untersuchungDurchfuehrung(RequestSpecUtil.quarkusSpec());

    private final AnlassApiSpec apiAnlass = AnlassApiSpec.anlass(RequestSpecUtil.quarkusSpec());

    private final EinladungService einladungService;
    private final UntersuchungService untersuchungService;
    private final FixtureService fixtureService;

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
    }

    @Test
    @TestAsAdminBS
    void getUntersuchungDurchfuehrung_non_existing_should_create_data() {
        // ARRANGE
        Einladung einladung = fixtureService.createEinladung();
        untersuchungService.getOrCreateUntersuchung(einladung.getId());
        // ACT / ASSERT
        UntersuchungDurchfuehrungDataJsonSpec data = api
            .untersuchungDurchfuehrungResourceGetUntersuchungDurchfuehrung()
            .anlassIdPath(einladung.getAnlass().getId().getId())
            .einladungIdPath(einladung.getId().getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(UntersuchungDurchfuehrungDataJsonSpec.class);
        // ASSERT
        Assertions.assertThat(data).isNotNull();
        Assertions.assertThat(data.getInitialValues()).isNotNull();
        Assertions.assertThat(data.getKindDaten()).isNotNull();
    }

    @Test
    @TestAsAdminBS
    void getUntersuchungDurchfuehrung_existing_should_return_existing() {
        // ARRANGE
        Untersuchung untersuchung = fixtureService.createUntersuchungWithDurchfuehrung();
        UntersuchungDurchfuehrung existingDurchfuehrung = untersuchung.getUntersuchungDurchfuehrung();
        Assertions.assertThat(existingDurchfuehrung).isNotNull();
        Assertions.assertThat(existingDurchfuehrung.getArzt()).isNotNull();
        Assertions.assertThat(existingDurchfuehrung.getMpa()).isNotNull();
        // ACT
        UntersuchungDurchfuehrungDataJsonSpec result = api
            .untersuchungDurchfuehrungResourceGetUntersuchungDurchfuehrung()
            .anlassIdPath(untersuchung.getEinladung().getAnlass().getId().getId())
            .einladungIdPath(untersuchung.getEinladung().getId().getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(UntersuchungDurchfuehrungDataJsonSpec.class);
        // ASSERT
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getUntersuchungDurchfuehrung()).isNotNull();
        Assertions.assertThat(result.getUntersuchungDurchfuehrung().getId())
            .isEqualTo(existingDurchfuehrung.getId().getId());
        Assertions.assertThat(result.getUntersuchungDurchfuehrung().getData().getArzt()).isNotNull();
        Assertions.assertThat(result.getUntersuchungDurchfuehrung().getData().getArzt().getId())
            .isEqualTo(existingDurchfuehrung.getArzt().getId().getId());
        Assertions.assertThat(result.getUntersuchungDurchfuehrung().getData().getMpa()).isNotNull();
        Assertions.assertThat(result.getUntersuchungDurchfuehrung().getData().getMpa().getId())
            .isEqualTo(existingDurchfuehrung.getMpa().getId().getId());
    }

    @Test
    @TestAsAdminBS
    void getUntersuchungDurchfuehrung_if_existing_einladung_with_status_nachzuegler_exists_nachzuegler_should_be_true() {
        Dossier dossier = fixtureService.createDossier();
        Anlass anlass = fixtureService.createAnlass(LocalDate.of(2023, 2, 1));
        fixtureService.createEinladung(anlass, dossier, EinladungStatus.NACHZUEGLER);

        Anlass anlassNeu = fixtureService.createAnlass(LocalDate.of(2023, 6, 30));
        Einladung einladungNeu = fixtureService.createEinladung(anlassNeu, dossier);

        // ACT / ASSERT
        UntersuchungDurchfuehrungDataJsonSpec data = api
            .untersuchungDurchfuehrungResourceGetUntersuchungDurchfuehrung()
            .anlassIdPath(einladungNeu.getAnlass().getId().getId())
            .einladungIdPath(einladungNeu.getId().getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(UntersuchungDurchfuehrungDataJsonSpec.class);
        // ASSERT
        Assertions.assertThat(data).isNotNull();
        Assertions.assertThat(data.getInitialValues()).isNotNull();
        Assertions.assertThat(data.getInitialValues().getNachzuegler()).isTrue();
    }

    @Test
    @TestAsAdminBS
    void getUntersuchungDurchfuehrung_if_dossier_has_nachkontrolle_flag_nachkontrolle_should_be_true() {
        Dossier dossier = fixtureService.createDossier();
        Nachkontrolle nachkontrolle = fixtureService.createNachkontrolle(dossier, true);

        Anlass anlassNeu = fixtureService.createAnlass(LocalDate.of(2023, 6, 30));
        Einladung einladungNeu = fixtureService.createEinladung(anlassNeu, dossier);

        Assertions.assertThat(nachkontrolle).isNotNull();
        Assertions.assertThat(nachkontrolle.getAnlassFlags().hasAnyNachkontrolle()).isTrue();

        // ACT / ASSERT
        UntersuchungDurchfuehrungDataJsonSpec data = api
            .untersuchungDurchfuehrungResourceGetUntersuchungDurchfuehrung()
            .anlassIdPath(einladungNeu.getAnlass().getId().getId())
            .einladungIdPath(einladungNeu.getId().getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(UntersuchungDurchfuehrungDataJsonSpec.class);
        // ASSERT
        Assertions.assertThat(data).isNotNull();
        Assertions.assertThat(data.getInitialValues()).isNotNull();
        Assertions.assertThat(data.getInitialValues().getNachkontrolle()).isTrue();
    }

    @Test
    @TestAsAdminBS
    void getUntersuchungDurchfuehrung_if_dossier_doesnt_have_nachkontrolle_flag_nachkontrolle_should_be_false() {
        Dossier dossier = fixtureService.createDossier();
        Nachkontrolle nachkontrolle = fixtureService.createNachkontrolle(dossier, false);

        Anlass anlassNeu = fixtureService.createAnlass(LocalDate.of(2023, 6, 30));
        Einladung einladungNeu = fixtureService.createEinladung(anlassNeu, dossier);

        Assertions.assertThat(nachkontrolle).isNotNull();
        Assertions.assertThat(nachkontrolle.getAnlassFlags().hasAnyNachkontrolle()).isFalse();

        // ACT / ASSERT
        UntersuchungDurchfuehrungDataJsonSpec data = api
            .untersuchungDurchfuehrungResourceGetUntersuchungDurchfuehrung()
            .anlassIdPath(einladungNeu.getAnlass().getId().getId())
            .einladungIdPath(einladungNeu.getId().getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(UntersuchungDurchfuehrungDataJsonSpec.class);
        // ASSERT
        Assertions.assertThat(data).isNotNull();
        Assertions.assertThat(data.getInitialValues()).isNotNull();
        Assertions.assertThat(data.getInitialValues().getNachkontrolle()).isFalse();
    }

    @Test
    @TestAsAdminBS
    void saveUntersuchungDurchfuehrungAbgeschlossenDurchfuehrung() {
        // ARRANGE
        Einladung einladung = fixtureService.createEinladung();
        UntersuchungDurchfuehrungSaveJsonSpec createJson =
            UntersuchungDurchfuehrungBuilder.createJaxSpec(builder -> builder
                .withArzt(null)
                .withMpa(null)).getData();
        api
            .untersuchungDurchfuehrungResourceSaveUntersuchungDurchfuehrung()
            .anlassIdPath(einladung.getAnlass().getId().getId())
            .einladungIdPath(einladung.getId().getId())
            .body(createJson)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(UntersuchungDurchfuehrungJsonSpec.class);
        // Reload
        einladung = einladungService.getById(einladung.getId());
        Assertions.assertThat(einladung.getUntersuchung()).isNotNull();
        UntersuchungDurchfuehrung existingDurchfuehrung =
            fixtureService.getUntersuchungWithLoadedData(einladung.getId()).getUntersuchungDurchfuehrung();
        Assertions.assertThat(existingDurchfuehrung).isNotNull();

        // ACT
        Benutzer arztNachher = fixtureService.createBenutzer();
        UntersuchungDurchfuehrungSaveJsonSpec saveJsonSpec =
            UntersuchungDurchfuehrungBuilder.createJaxSpec(builder -> builder
                .forUntersuchungDurchfuehrung(existingDurchfuehrung)
                .withArzt(arztNachher)
                .withMpa(arztNachher)).getData();
        UntersuchungDurchfuehrungJsonSpec result = api
            .untersuchungDurchfuehrungResourceSaveUntersuchungDurchfuehrung()
            .anlassIdPath(einladung.getAnlass().getId().getId())
            .einladungIdPath(einladung.getId().getId())
            .body(saveJsonSpec)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(UntersuchungDurchfuehrungJsonSpec.class);

        // ASSERT
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getId()).isNotNull();
        Assertions.assertThat(result.getData().getArzt()).isNotNull();
        Assertions.assertThat(result.getData().getArzt().getId()).isEqualTo(arztNachher.getId().getId());
        Assertions.assertThat(result.getData().getMpa()).isNotNull();
        Assertions.assertThat(result.getData().getMpa().getId()).isEqualTo(arztNachher.getId().getId());
    }

    @Test
    @TestAsAdminBS
    void save_untersuchung_durchfuehrund_kind_daten_bei_abgeschlossenen_anlass_changes_data() {
        // ARRANGE
        Untersuchung untersuchung = fixtureService.createUntersuchungWithEveryTab();

        apiAnlass
            .anlassResourceChangeAnlassStatus()
            .anlassIdPath(untersuchung.getEinladung().getAnlass().getId().getId())
            .statusPath(AnlassStatusSpec.ABGESCHLOSSEN)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(AnlassJsonSpec.class);

        Assertions.assertThat(untersuchung).isNotNull();
        Assertions.assertThat(untersuchung.getUntersuchungDurchfuehrung()).isNotNull();
        Assertions.assertThat(untersuchung.getEinladung().getDossier().getKind().getVorname()).isEqualTo("Emma");

        UntersuchungDurchfuehrungSaveJsonSpec saveJsonSpec =
            UntersuchungDurchfuehrungBuilder.createJaxSpec(builder -> builder
                .forUntersuchungDurchfuehrung(untersuchung.getUntersuchungDurchfuehrung())).getData();

        UntersuchungKindDaten kind = untersuchung.getUntersuchungKindDaten();
        Assertions.assertThat(kind).isNotNull();

        UntersuchungKindDatenSaveJsonSpec kindApi =
            UntersuchungKindDatenBuilder.createSaveJaxSpec(builder -> builder.forKind(kind).withVorname("Barbara"));

        UntersuchungDurchfuehrungDataSaveJsonSpec apiSpec = new UntersuchungDurchfuehrungDataSaveJsonSpec();
        apiSpec.setUntersuchungDurchfuehrung(saveJsonSpec);
        apiSpec.setKindDaten(kindApi);

        // ACT
        UntersuchungDurchfuehrungDataJsonSpec result =
            api.untersuchungDurchfuehrungResourceSaveUntersuchungDurchfuehrungAbgeschlossen()
                .anlassIdPath(untersuchung.getEinladung().getAnlass().getId().getId())
                .einladungIdPath(untersuchung.getEinladung().getId().getId())
                .body(apiSpec)
                .execute(ResponseBody::prettyPeek)
                .then()
                .assertThat()
                .statusCode(Status.OK.getStatusCode())
                .extract().as(UntersuchungDurchfuehrungDataJsonSpec.class);

        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getKindDaten().getData().getVorname()).isEqualTo("Barbara");
    }

    @Test
    @TestAsMpaBS
    void save_untersuchung_durchfuehrund_kind_daten_bei_abgeschlossenen_anlass_throw_wenn_nicht_admin() {
        // ARRANGE
        Untersuchung untersuchung = fixtureService.createUntersuchungWithEveryTab();

        apiAnlass
            .anlassResourceChangeAnlassStatus()
            .anlassIdPath(untersuchung.getEinladung().getAnlass().getId().getId())
            .statusPath(AnlassStatusSpec.ABGESCHLOSSEN)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode());

        Assertions.assertThat(untersuchung).isNotNull();
        Assertions.assertThat(untersuchung.getUntersuchungDurchfuehrung()).isNotNull();

        UntersuchungKindDaten kind = untersuchung.getUntersuchungKindDaten();
        Assertions.assertThat(kind).isNotNull();

        UntersuchungDurchfuehrungSaveJsonSpec saveJsonSpec =
            UntersuchungDurchfuehrungBuilder.createJaxSpec(builder -> builder
                .forUntersuchungDurchfuehrung(untersuchung.getUntersuchungDurchfuehrung())).getData();

        UntersuchungKindDatenSaveJsonSpec kindApi =
            UntersuchungKindDatenBuilder.createSaveJaxSpec(builder -> builder.forKind(kind));

        UntersuchungDurchfuehrungDataSaveJsonSpec apiSpec = new UntersuchungDurchfuehrungDataSaveJsonSpec();
        apiSpec.setUntersuchungDurchfuehrung(saveJsonSpec);
        apiSpec.setKindDaten(kindApi);

        // ACT
        api.untersuchungDurchfuehrungResourceSaveUntersuchungDurchfuehrungAbgeschlossen()
            .anlassIdPath(untersuchung.getEinladung().getAnlass().getId().getId())
            .einladungIdPath(untersuchung.getEinladung().getId().getId())
            .body(apiSpec)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode());
    }

    @Test
    @TestAsAdminBS
    void save_untersuchung_durchfuehrund_kind_daten_bei_abgeschlossenen_anlass_throw_wenn_anlass_nicht_abgeschlossen() {
        // ARRANGE
        Untersuchung untersuchung = fixtureService.createUntersuchungWithEveryTab();

        Assertions.assertThat(untersuchung).isNotNull();
        Assertions.assertThat(untersuchung.getUntersuchungDurchfuehrung()).isNotNull();

        UntersuchungKindDaten kind = untersuchung.getUntersuchungKindDaten();
        Assertions.assertThat(kind).isNotNull();

        UntersuchungDurchfuehrungSaveJsonSpec saveJsonSpec =
            UntersuchungDurchfuehrungBuilder.createJaxSpec(builder -> builder
                .forUntersuchungDurchfuehrung(untersuchung.getUntersuchungDurchfuehrung())).getData();

        UntersuchungKindDatenSaveJsonSpec kindApi =
            UntersuchungKindDatenBuilder.createSaveJaxSpec(builder -> builder.forKind(kind).withVorname("Barbara"));

        UntersuchungDurchfuehrungDataSaveJsonSpec apiSpec = new UntersuchungDurchfuehrungDataSaveJsonSpec();
        apiSpec.setUntersuchungDurchfuehrung(saveJsonSpec);
        apiSpec.setKindDaten(kindApi);

        // ACT
        api.untersuchungDurchfuehrungResourceSaveUntersuchungDurchfuehrungAbgeschlossen()
            .anlassIdPath(untersuchung.getEinladung().getAnlass().getId().getId())
            .einladungIdPath(untersuchung.getEinladung().getId().getId())
            .body(apiSpec)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode());
    }
}
