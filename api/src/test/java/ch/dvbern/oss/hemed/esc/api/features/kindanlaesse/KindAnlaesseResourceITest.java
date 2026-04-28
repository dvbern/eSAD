package ch.dvbern.oss.hemed.esc.api.features.kindanlaesse;

import ch.dvbern.dvbstarter.shared.i18n.datetime.DateConst;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Anlass;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfstatusKrankheit;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfstatusKrankheitService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfungInternService;
import ch.dvbern.oss.hemed.esc.api.features.impfstoff.Impfstoff;
import ch.dvbern.oss.hemed.esc.api.features.impfstoff.types.Krankheit;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import ch.dvbern.oss.hemed.esc.api.testutil.AbstractITest;
import ch.dvbern.oss.hemed.esc.api.testutil.FixtureService;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungImpfstatusKrankheitBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungImpfungInternBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.restassured.RequestSpecUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsAdminBS;
import ch.dvbern.oss.hemed.esc.generated.restassured.api.KindanlaesseApiSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.KindAnlassListJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.KindAnlassSummaryJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.LastImpfungJsonSpec;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.ResponseBody;
import jakarta.ws.rs.core.Response.Status;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@RequiredArgsConstructor
class KindAnlaesseResourceITest extends AbstractITest {

    private final KindanlaesseApiSpec api = KindanlaesseApiSpec.kindanlaesse(RequestSpecUtil.quarkusSpec());
    private final FixtureService fixtureService;
    private final UntersuchungImpfungInternService untersuchungImpfungInternService;
    private final UntersuchungImpfstatusKrankheitService untersuchungImpfstatusKrankheitService;


    @Test
    @TestAsAdminBS
    void loadAnlaesseForKind() {

        // Prepare
        Einladung einladung = fixtureService.createEinladung();
        var kind = einladung.getDossier().getKind();
        var anlass = einladung.getAnlass();

        // Execute
        KindAnlassListJsonSpec result = api
            .kindAnlaesseResourceFindAnlaesseForKind()
            .kindIdPath(kind.getId().getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(KindAnlassListJsonSpec.class);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getAnlassList()).isNotEmpty();
        KindAnlassSummaryJsonSpec result1 = result.getAnlassList().getFirst();
        assertThat(result1.getAnlassId()).isEqualTo(anlass.getId().getId());
        assertThat(result1.getEinladungId()).isEqualTo(einladung.getId().getId());

    }

    @Test
    @TestAsAdminBS
    void getLatestImpfung_returnsNullFields_whenNoManualImpfungExists() {
        // ARRANGE
        Untersuchung untersuchung = fixtureService.createUntersuchungWithCompleteData();
        assertThat(untersuchung.getUntersuchungImpfungen()).isNotNull();
        ID<Kind> kindId = untersuchung.getEinladungRequired().getDossier().getKind().getId();

        // ACT
        LastImpfungJsonSpec latestImpfung = api
            .kindAnlaesseResourceGetLatestImpfung()
            .kindIdPath(kindId.getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(LastImpfungJsonSpec.class);

        // ASSERT
        assertThat(latestImpfung).isNotNull();
        assertThat(latestImpfung.getAnlassId()).isNull();
        assertThat(latestImpfung.getTimestampLastManualChange()).isNull();
    }

    @Test
    @TestAsAdminBS
    void getLatestImpfung_returnsEntryWithLatestManualChangeTimestamp() {
        // ARRANGE
        Untersuchung untersuchung = fixtureService.createUntersuchungWithCompleteData();
        assertThat(untersuchung.getUntersuchungImpfungen()).isNotNull();
        Impfstoff impfstoff = fixtureService.createImpfstoff();

        // untersuchungImpfungIntern 1
        untersuchungImpfungInternService
            .persist(UntersuchungImpfungInternBuilder.create(builder -> builder
                .forUntersuchungImpfungen(untersuchung.getUntersuchungImpfungen())
                .withImmunitaetForKrankheit(Krankheit.HEPATITIS_B, true)
                .withUserErfasst(fixtureService.createBenutzerFuerZentraleOrganisation())
                .withImpfstoff(impfstoff)
                .withTimestampLastManualChange(getAppClock().offsetDateTime().now().minusYears(2)))
            );

        // untersuchungImpfungIntern 2
        untersuchungImpfungInternService
            .persist(UntersuchungImpfungInternBuilder.create(builder -> builder
                .forUntersuchungImpfungen(untersuchung.getUntersuchungImpfungen())
                .withImmunitaetForKrankheit(Krankheit.HEPATITIS_B, true)
                .withUserErfasst(fixtureService.createBenutzerFuerZentraleOrganisation())
                .withImpfstoff(impfstoff)
                .withTimestampLastManualChange(getAppClock().offsetDateTime().now().minusYears(4)))
            );

        // untersuchungImpfstatusKrankheit
        UntersuchungImpfstatusKrankheit untersuchungImpfstatusKrankheit =
            untersuchungImpfstatusKrankheitService.persist(
                UntersuchungImpfstatusKrankheitBuilder.create(builder -> builder
                    .forUntersuchungImpfungen(untersuchung.getUntersuchungImpfungen())
                    .withTimestampLastManualChange(getAppClock().offsetDateTime().now().minusYears(1))));

        ID<Kind> kindId = untersuchung.getEinladungRequired().getDossier().getKind().getId();
        // ACT
        LastImpfungJsonSpec latestImpfung = api
            .kindAnlaesseResourceGetLatestImpfung()
            .kindIdPath(kindId.getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(LastImpfungJsonSpec.class);

        // ASSERT
        ID<Anlass> anlassIdExpected = untersuchung.getAnlass().getId();
        OffsetDateTime offsetDateTimeExpected = untersuchungImpfstatusKrankheit.getTimestampLastManualChange();
        assertThat(latestImpfung).isNotNull();
        assertThat(latestImpfung.getAnlassId()).isEqualTo(anlassIdExpected.getId());
        assertThat(latestImpfung.getEinladungId()).isEqualTo(untersuchung.getEinladungRequired().getId().getId());
        assertThat(latestImpfung.getTimestampLastManualChange()).isNotNull();
        assertThat(latestImpfung
            // Gives us "time at wall", so UTC
            .getTimestampLastManualChange()
            // Explicitly tell it that zone
            .atZone(ZoneId.of("UTC"))
            // Move the zone but keep the same time
            .withZoneSameInstant(DateConst.ZONE_ID)
            // Back to "time at wall"
            .toLocalDateTime())
            // Equal to normal "time at wall" + offset
            .isEqualTo(offsetDateTimeExpected.toLocalDateTime());
    }
}
