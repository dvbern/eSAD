package ch.dvbern.oss.hemed.esc.api.features.organisation;

import java.util.List;

import ch.dvbern.oss.hemed.esc.api.features.benutzer.BenutzerService;
import ch.dvbern.oss.hemed.esc.api.shared.auth.FachRolle;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantEnum;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantProducer;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import ch.dvbern.oss.hemed.esc.api.testutil.AbstractITest;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.BenutzerBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.OrganisationBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.restassured.RequestSpecUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsAdminBE;
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

    private final OrganisationService organisationService;
    private final BenutzerService benutzerService;
    private final TenantProducer tenantProducer;

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
    }

    @Test
    @TestAsAdminBE
    void findAllOrganisationen() {
        // ARRANGE
        tenantProducer.setCurrentTenant(TenantEnum.BE);
        Benutzer verantwortlicherArzt1 = BenutzerBuilder.defaultBenutzer();
        Benutzer verantwortlicherArzt2 = BenutzerBuilder.defaultBenutzer();
        benutzerService.persist(verantwortlicherArzt1);
        benutzerService.persist(verantwortlicherArzt2);
        Organisation organisation1 =
            OrganisationBuilder.create(builder -> builder.withVerantwortlicherArzt(verantwortlicherArzt1));
        Organisation organisation2 =
            OrganisationBuilder.create(builder -> builder.withVerantwortlicherArzt(verantwortlicherArzt2));
        organisationService.persist(organisation1);
        organisationService.persist(organisation2);

        Benutzer benutzer1 = BenutzerBuilder.create(builder -> builder.withOrganisationRollen(organisation1, List.of(
            FachRolle.MPA)));
        Benutzer benutzer2 = BenutzerBuilder.create(builder -> builder.withOrganisationRollen(organisation2, List.of(FachRolle.MPA)));
        benutzerService.persist(benutzer1);
        benutzerService.persist(benutzer2);

        // ACT
        OrganisationListJsonSpec result = api
            .organisationResourceFindAllOrganisationen()
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(OrganisationListJsonSpec.class);

        // ASSERT
        Assertions.assertThat(result.getOrganisationList())
            .isNotEmpty()
            .map(OrganisationJsonSpec::getId)
            .containsAll(List.of(organisation1.getId().getId(), organisation2.getId().getId()));
    }

    @Test
    @TestAsAdminBE
    void findOrganisation() {
        // ARRANGE
        tenantProducer.setCurrentTenant(TenantEnum.BE);
        Benutzer verantwortlicherArzt1 = BenutzerBuilder.defaultBenutzer();
        Benutzer verantwortlicherArzt2 = BenutzerBuilder.defaultBenutzer();
        benutzerService.persist(verantwortlicherArzt1);
        benutzerService.persist(verantwortlicherArzt2);
        Organisation organisation1 =
            OrganisationBuilder.create(builder -> builder.withVerantwortlicherArzt(verantwortlicherArzt1));
        Organisation organisation2 =
            OrganisationBuilder.create(builder -> builder.withVerantwortlicherArzt(verantwortlicherArzt2));
        organisationService.persist(organisation1);
        organisationService.persist(organisation2);

        // ACT
        OrganisationJsonSpec result = api
            .organisationResourceFindOrganisation()
            .organisationIdPath(organisation1.getId().getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(OrganisationJsonSpec.class);

        // ASSERT
        Assertions.assertThat(result)
            .isNotNull()
            .extracting(OrganisationJsonSpec::getVerantwortlicherArztFullname).isEqualTo(verantwortlicherArzt1.getVorname() + ' ' + verantwortlicherArzt1.getNachname());
    }
}
