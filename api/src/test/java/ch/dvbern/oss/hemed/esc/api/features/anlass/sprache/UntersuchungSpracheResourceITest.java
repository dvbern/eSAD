package ch.dvbern.oss.hemed.esc.api.features.anlass.sprache;

import ch.dvbern.dvbstarter.shared.types.id.HasUntersuchungIDWrapper;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anonymereintrag.AnonymerEintrag;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sprache.types.Nachsprechen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sprache.types.Sprachverstaendnis;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sprache.types.Wortschatz;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.UntersuchungService;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantEnum;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantContext;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import ch.dvbern.oss.hemed.esc.api.testutil.AbstractITest;
import ch.dvbern.oss.hemed.esc.api.testutil.FixtureService;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungSpracheBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.restassured.RequestSpecUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsAdminBS;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsSchularztBE;
import ch.dvbern.oss.hemed.esc.generated.restassured.api.UntersuchungSpracheApiSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.UntersuchungSpracheJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.UntersuchungSpracheSaveJsonSpec;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.ResponseBody;
import jakarta.ws.rs.core.Response.Status;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static ch.dvbern.oss.hemed.esc.api.testutil.restassured.RequestSpecUtil.toHasAnonymerEintragIdWrapper;
import static ch.dvbern.oss.hemed.esc.api.testutil.restassured.RequestSpecUtil.toHasUntersuchungIdWrapper;
import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@RequiredArgsConstructor
class UntersuchungSpracheResourceITest extends AbstractITest {

    private final UntersuchungSpracheApiSpec api =
        UntersuchungSpracheApiSpec.untersuchungSprache(RequestSpecUtil.quarkusSpec());

    private final UntersuchungService untersuchungService;
    private final UntersuchungSpracheService untersuchungSpracheService;
    private final FixtureService fixtureService;
    private final TenantContext tenantContext;

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
    }

    @Test
    @TestAsAdminBS
    void getUntersuchungSprache_non_existing_should_not_create_data() {
        // ARRANGE
        Einladung einladung = fixtureService.createEinladung();
        untersuchungService.getOrCreateUntersuchung(HasUntersuchungIDWrapper.fromEinladungId(einladung.getId()));
        // ACT / ASSERT
        api.untersuchungSpracheResourceGetUntersuchungSprache()
            .anlassIdPath(einladung.getAnlass().getId().getId())
            .einladungIdPath(toHasUntersuchungIdWrapper(einladung.getId()))
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.NO_CONTENT.getStatusCode());
    }

    @Test
    @TestAsAdminBS
    void getUntersuchungSprache_existing_should_return_existing() {
        // ARRANGE
        Untersuchung untersuchung = fixtureService.createUntersuchungWithSprache();
        assertThat(untersuchung.getUntersuchungSprache()).isNotNull();

        UntersuchungSprache
            existingSprache = untersuchungSpracheService.getById(untersuchung.getUntersuchungSprache().getId());
        assertThat(existingSprache).isNotNull();
        assertThat(existingSprache.getNachsprechen()).isNotNull();
        assertThat(existingSprache.getNachsprechen()
            .getEnumValue(Nachsprechen.class)).isEqualTo(Nachsprechen.BESTANDEN);
        assertThat(existingSprache.getBemerkungen()).isNotNull();
        // ACT
        UntersuchungSpracheJsonSpec result = api.untersuchungSpracheResourceGetUntersuchungSprache()
            .anlassIdPath(untersuchung.getAnlass().getId().getId())
            .einladungIdPath(toHasUntersuchungIdWrapper(untersuchung.getEinladungRequired().getId()))
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract()
            .as(UntersuchungSpracheJsonSpec.class);
        // ASSERT
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(existingSprache.getId().getId());
        assertThat(existingSprache.getNachsprechen()).isNotNull();
        String existingNachsprechen = existingSprache.getNachsprechen().toEnumValueName(Nachsprechen.class);
        assertThat(existingNachsprechen).isNotNull();
        assertThat(result.getData().getNachsprechen().getValue()).contains(existingNachsprechen);
        assertThat(result.getData().getBemerkungen()).isNotNull().isEqualTo(existingSprache.getBemerkungen());
    }

    @Test
    @TestAsAdminBS
    void saveUntersuchungSprache() {
        // ARRANGE
        Einladung einladung = fixtureService.createEinladung();
        Untersuchung untersuchung = fixtureService.createUntersuchungWithSprache();
        assertThat(untersuchung.getUntersuchungSprache()).isNotNull();
        UntersuchungSprache
            existingSprache = untersuchungSpracheService.getById(untersuchung.getUntersuchungSprache().getId());
        assertThat(existingSprache).isNotNull();
        assertThat(existingSprache.getNachsprechen()).isNotNull();
        assertThat(existingSprache.getNachsprechen()
            .getEnumValue(Nachsprechen.class)).isEqualTo(Nachsprechen.BESTANDEN);
        assertThat(existingSprache.getBemerkungen()).isNotNull().isEqualTo("Untersuchung Sprache Bemerkung");

        // ACT
        UntersuchungSpracheSaveJsonSpec saveJsonSpec =
            UntersuchungSpracheBuilder.createJaxSpec(builder -> builder
                .forUntersuchungSprache(existingSprache)
                .withNachsprechen(Nachsprechen.NICHT_BESTANDEN, null)
                .withBemerkung("Test updateUntersuchungSprache")).getData();

        UntersuchungSpracheJsonSpec result = api.untersuchungSpracheResourceSaveUntersuchungSprache()
            .anlassIdPath(einladung.getAnlass().getId().getId())
            .einladungIdPath(toHasUntersuchungIdWrapper(einladung.getId()))
            .body(saveJsonSpec)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract()
            .as(UntersuchungSpracheJsonSpec.class);

        // ASSERT
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getData().getBemerkungen()).isNotNull().isEqualTo("Test updateUntersuchungSprache");
        assertThat(result.getData().getNachsprechen().getValue()).contains(Nachsprechen.NICHT_BESTANDEN.name());
    }

    @Test
    @TestAsAdminBS
    void getDefaultUntersuchungSprache() {
        // ARRANGE
        Einladung einladung = fixtureService.createEinladung();

        // ACT
        UntersuchungSpracheSaveJsonSpec result = api.untersuchungSpracheResourceGetDefaultUntersuchungSprache()
            .anlassIdPath(einladung.getAnlass().getId().getId())
            .einladungIdPath(toHasUntersuchungIdWrapper(einladung.getId()))
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract()
            .as(UntersuchungSpracheSaveJsonSpec.class);

        // ASSERT
        assertThat(result).isNotNull();
        assertThat(result.getNachsprechen()).isNotNull();
        assertThat(result.getNachsprechen().getValue()).contains(Nachsprechen.BESTANDEN.name());
        assertThat(result.getWortschatz()).isNotNull();
        assertThat(result.getWortschatz().getValue()).contains(Wortschatz.BESTANDEN.name());
        assertThat(result.getSprachverstaendnis()).isNotNull();
        assertThat(result.getSprachverstaendnis().getValue()).contains(Sprachverstaendnis.BESTANDEN.name());
        assertThat(result.getBemerkungen()).isNull();
    }

    @Test
    @TestAsSchularztBE
    void saveUntersuchungSprache_anonym() {
        // ARRANGE
        tenantContext.setCurrentTenant(TenantEnum.BE);
        Benutzer schularzt = fixtureService.readBenutzerWithBerechtigungen(TestAsSchularztBE.BENUTZER_ID_ID);
        AnonymerEintrag anonymerEintrag = fixtureService.createAnonymerEintragForArzt(schularzt);
        Untersuchung untersuchung = untersuchungService.getOrCreateUntersuchungAnonym(anonymerEintrag.getId(), false);
        assertThat(untersuchung.getUntersuchungSprache()).isNull();

        // ACT
        UntersuchungSpracheSaveJsonSpec saveJsonSpec =
            UntersuchungSpracheBuilder.createSaveJaxSpec(builder -> builder
                .withNachsprechen(Nachsprechen.NICHT_BESTANDEN, null)
                .withBemerkung("Test newUntersuchungSprache anonym"));

        UntersuchungSpracheJsonSpec result = api.untersuchungSpracheResourceSaveUntersuchungSprache()
            .anlassIdPath(anonymerEintrag.getAnlass().getId().getId())
            .einladungIdPath(toHasAnonymerEintragIdWrapper(anonymerEintrag.getId()))
            .body(saveJsonSpec)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract()
            .as(UntersuchungSpracheJsonSpec.class);

        // ASSERT
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getData().getBemerkungen()).isNotNull().isEqualTo("Test newUntersuchungSprache anonym");
        assertThat(result.getData().getNachsprechen().getValue()).contains(Nachsprechen.NICHT_BESTANDEN.name());
        assertThat(untersuchungService.getOrCreateUntersuchungAnonym(anonymerEintrag.getId(), true)
            .getUntersuchungSprache()).isNotNull();
    }
}
