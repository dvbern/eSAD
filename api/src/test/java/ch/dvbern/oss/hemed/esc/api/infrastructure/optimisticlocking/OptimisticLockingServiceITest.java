package ch.dvbern.oss.hemed.esc.api.infrastructure.optimisticlocking;

import java.util.UUID;

import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.UntersuchungService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.types.Dezibel;
import ch.dvbern.oss.hemed.esc.api.features.labels.Label;
import ch.dvbern.oss.hemed.esc.api.features.labels.LabelService;
import ch.dvbern.oss.hemed.esc.api.testutil.AbstractITest;
import ch.dvbern.oss.hemed.esc.api.testutil.FixtureService;
import ch.dvbern.oss.hemed.esc.api.testutil.TestUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.LabelBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungHoerenAudiogrammBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.restassured.RequestSpecUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsAdminBS;
import ch.dvbern.oss.hemed.esc.generated.restassured.api.LabelsApiSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.api.UntersuchungHoerenAudiogrammApiSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.AppFailureErrorJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.LabelJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.LabelSaveJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.UntersuchungHoerenAudiogrammJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.UntersuchungHoerenAudiogrammSaveJsonSpec;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.ResponseBody;
import jakarta.ws.rs.core.Response.Status;
import lombok.RequiredArgsConstructor;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import static ch.dvbern.oss.hemed.esc.api.testutil.restassured.RequestSpecUtil.toHasUntersuchungIdWrapper;

@QuarkusTest
@RequiredArgsConstructor
class OptimisticLockingServiceITest extends AbstractITest {

    private final LabelsApiSpec api = LabelsApiSpec.labels(RequestSpecUtil.quarkusSpec());
	private final UntersuchungHoerenAudiogrammApiSpec audiogrammApi = UntersuchungHoerenAudiogrammApiSpec.untersuchungHoerenAudiogramm(RequestSpecUtil.quarkusSpec());
	private final LabelService labelService;
	private final FixtureService fixtureService;
	private final UntersuchungService untersuchungService;

    @Test
    @TestAsAdminBS
    void updateLabel() {
        // ARRANGE
        Label label = labelService.persist(LabelBuilder.defaultLabel());
        LabelSaveJsonSpec saveJson = LabelBuilder.createSaveJaxSpec(
            builder -> builder.forLabel(label));

        // ACT
        // Update 1 - this should work
        saveJson.setVersion(0L);
        saveJson.getName().setDe(TestUtil.getRandomString("OptimisticLocking: Update 1"));
        LabelJsonSpec result = api
            .labelResourceUpdateLabel()
            .labelIdPath(label.getId().getUUID())
            .body(saveJson)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(LabelJsonSpec.class);
        // Update 2 - should fail due to optimistic locking
        saveJson.setVersion(0L); // Still the same version
        saveJson.getName().setDe(TestUtil.getRandomString("OptimisticLocking: Update 2"));
        var error = api
            .labelResourceUpdateLabel()
            .labelIdPath(label.getId().getUUID())
            .body(saveJson)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode()).extract().as(AppFailureErrorJsonSpec.class);

        // ASSERT
        Label labelRead = labelService.getById(ID.of(UUID.fromString(result.getId()), Label.class));
        Assertions.assertThat(labelRead.getName().getNameDe()).contains("OptimisticLocking: Update 1");
        Assertions.assertThat(error).isNotNull().extracting(AppFailureErrorJsonSpec::getSummary)
            .isEqualTo("Die Daten wurden seit dem letzten Laden geändert. Bitte die Ansicht aktualisieren (F5). Entity: Label, Version Backend: 1, Version Frontend: 0.");
    }

	@Test
	@TestAsAdminBS
	void initialize_audiogram() {
		// ARRANGE
		Einladung einladung = fixtureService.createEinladung();
		Untersuchung untersuchung = untersuchungService.getOrCreateUntersuchung(einladung.getId());

		UntersuchungHoerenAudiogrammSaveJsonSpec saveJsonSpec =
			UntersuchungHoerenAudiogrammBuilder.createJaxSpec(builder -> builder.forUntersuchung(untersuchung)
				.withAudiogrammLinks(Dezibel.DB30)
				.withAudiogrammRechts(Dezibel.DB30)).getAudiogrammData();

		// set dummy versions
		saveJsonSpec.setVersion(-1L);
		org.junit.jupiter.api.Assertions.assertNotNull(saveJsonSpec.getAudiogrammLinks());
		saveJsonSpec.getAudiogrammLinks().setVersion(-1L);
		org.junit.jupiter.api.Assertions.assertNotNull(saveJsonSpec.getAudiogrammRechts());
		saveJsonSpec.getAudiogrammRechts().setVersion(-1L);

		// save or create audiogram
		UntersuchungHoerenAudiogrammJsonSpec result =
			audiogrammApi.untersuchungHoerenAudiogrammResourceSaveUntersuchungHoerenAudiogramm()
				.anlassIdPath(einladung.getAnlass().getId().getId())
				.einladungIdPath(toHasUntersuchungIdWrapper(einladung.getId()))
				.body(saveJsonSpec)
				.execute(ResponseBody::prettyPeek)
				.then()
				.assertThat()
				.statusCode(Status.OK.getStatusCode())
				.extract()
				.as(UntersuchungHoerenAudiogrammJsonSpec.class);

		Assertions.assertThat(result.getAudiogrammData().getVersion()).isZero();
		Assertions.assertThat(result.getAudiogrammData().getAudiogrammLinks()).isNotNull();
		Assertions.assertThat(result.getAudiogrammData().getAudiogrammLinks().getVersion()).isZero();
		Assertions.assertThat(result.getAudiogrammData().getAudiogrammRechts()).isNotNull();
		Assertions.assertThat(result.getAudiogrammData().getAudiogrammRechts().getVersion()).isZero();

		// save it again
		// trigger version update by actually changing something
		saveJsonSpec.setBemerkungen(TestUtil.getRandomString("OptimisticLocking: Bemerkung"));

		AppFailureErrorJsonSpec error =
			audiogrammApi.untersuchungHoerenAudiogrammResourceSaveUntersuchungHoerenAudiogramm()
				.anlassIdPath(einladung.getAnlass().getId().getId())
				.einladungIdPath(toHasUntersuchungIdWrapper(einladung.getId()))
				.body(saveJsonSpec)
				.execute(ResponseBody::prettyPeek)
				.then()
				.assertThat()
				.statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode())
				.extract()
				.as(AppFailureErrorJsonSpec.class);

		Assertions.assertThat(error)
			.isNotNull()
			.extracting(AppFailureErrorJsonSpec::getSummary)
			.isEqualTo(
				"Die Daten wurden seit dem letzten Laden geändert. Bitte die Ansicht aktualisieren (F5). Entity: UntersuchungHoerenAudiogramm, Version Backend: 0, Version Frontend: -1.");
	}
}
