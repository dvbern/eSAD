package ch.dvbern.oss.hemed.esc.api.features.organisation;

import java.util.List;

import ch.dvbern.oss.hemed.esc.api.features.benutzer.BenutzerResourceHelper;
import ch.dvbern.oss.hemed.esc.api.features.benutzer.BenutzerService;
import ch.dvbern.oss.hemed.esc.api.shared.auth.FachRolle;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantEnum;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantProducer;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import ch.dvbern.oss.hemed.esc.api.testutil.AbstractITest;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.BenutzerBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.restassured.RequestSpecUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsSchularztBE;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsSupportBE;
import ch.dvbern.oss.hemed.esc.generated.restassured.api.OrganisationApiSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.OrganisationJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.OrganisationListJsonSpec;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.ResponseBody;
import jakarta.ws.rs.core.Response.Status;
import lombok.RequiredArgsConstructor;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
@RequiredArgsConstructor
class OrganisationResourceITest extends AbstractITest {

    private final OrganisationApiSpec api = OrganisationApiSpec.organisation(RequestSpecUtil.quarkusSpec());

    private final BenutzerService benutzerService;
    private final TenantProducer tenantProducer;
    private final BenutzerResourceHelper benutzerResourceHelper;

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
    }

    @Test
    @TestAsSupportBE
    void findOrganisationenBerechtigt_as_SUPPORT_returns_all() {
        // ARRANGE
        Benutzer verantwortlicherArzt1 = benutzerResourceHelper.createSchularztAsOrganisationsChef(TenantEnum.BE);
        Benutzer verantwortlicherArzt2 = benutzerResourceHelper.createSchularztAsOrganisationsChef(TenantEnum.BE);
        Organisation organisation1 = verantwortlicherArzt1.getOrganisationRollen().stream().findFirst().get().getOrganisation();
        Organisation organisation2 = verantwortlicherArzt2.getOrganisationRollen().stream().findFirst().get().getOrganisation();

        Benutzer benutzer1 = BenutzerBuilder.create(builder -> builder.withOrganisationRollen(organisation1, List.of(
            FachRolle.MPA)));
        Benutzer benutzer2 = BenutzerBuilder.create(builder -> builder.withOrganisationRollen(organisation2, List.of(FachRolle.MPA)));
        benutzerService.persist(benutzer1);
        benutzerService.persist(benutzer2);

        // ACT
        OrganisationListJsonSpec result = api
            .organisationResourceFindOrganisationenBerechtigt()
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(OrganisationListJsonSpec.class);

        // ASSERT
        Assertions.assertThat(result.getOrganisationList())
            .isNotEmpty()
            .map(OrganisationJsonSpec::getId)
            .containsAll(List.of(TestAsSchularztBE.ORGANISATION_ID, organisation1.getId().getId(), organisation2.getId().getId()));
    }

    @Test
    @TestAsSchularztBE
    void findOrganisationenBerechtigt_as_SCHULARZT_returns_only_own() {
        // ARRANGE
        tenantProducer.setCurrentTenant(TenantEnum.BE);
        Benutzer verantwortlicherArzt1 = benutzerResourceHelper.createSchularztAsOrganisationsChef(TenantEnum.BE);
        Benutzer verantwortlicherArzt2 = benutzerResourceHelper.createSchularztAsOrganisationsChef(TenantEnum.BE);
        Organisation organisation1 = verantwortlicherArzt1.getOrganisationRollen().stream().findFirst().get().getOrganisation();
        Organisation organisation2 = verantwortlicherArzt2.getOrganisationRollen().stream().findFirst().get().getOrganisation();

        // ACT
        OrganisationListJsonSpec result = api
            .organisationResourceFindOrganisationenBerechtigt()
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(OrganisationListJsonSpec.class);

        // ASSERT
        Assertions.assertThat(result.getOrganisationList())
            .isNotEmpty()
            .map(OrganisationJsonSpec::getId)
            .doesNotContain(organisation1.getId().getId(), organisation2.getId().getId())
            .contains(TestAsSchularztBE.ORGANISATION_ID);
    }
}
