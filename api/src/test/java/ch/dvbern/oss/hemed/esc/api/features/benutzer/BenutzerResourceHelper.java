package ch.dvbern.oss.hemed.esc.api.features.benutzer;

import java.util.List;

import ch.dvbern.dvbstarter.shared.types.emailaddress.EmailAddress;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantEnum;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantProducer;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import ch.dvbern.oss.hemed.esc.api.testutil.FixtureService;
import ch.dvbern.oss.hemed.esc.api.testutil.TestUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.restassured.RequestSpecUtil;
import ch.dvbern.oss.hemed.esc.generated.restassured.api.BenutzerApiSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.BenutzerJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.BenutzerSaveJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.EntityAktivStatusSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.FachRolleSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.OrganisationRollenJsonSpec;
import io.restassured.response.ResponseBody;
import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.core.Response.Status;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;

@RequestScoped
@RequiredArgsConstructor
public class BenutzerResourceHelper {

    private final BenutzerApiSpec api = BenutzerApiSpec.benutzer(RequestSpecUtil.quarkusSpec());

    private final TenantProducer tenantProducer;
    private final FixtureService fixtureService;

    @NonNull
    public Benutzer createSchularztAsOrganisationsChef(@NonNull TenantEnum tenant) {
        tenantProducer.setCurrentTenant(tenant);
        String randomName = TestUtil.getRandomString("OrganisationsChef");
        EmailAddress benutzerEmail2 = EmailAddress.fromString(randomName + "@mailbucket.dvbern.ch");
        BenutzerSaveJsonSpec saveJson = new BenutzerSaveJsonSpec(
            null,
            randomName,
            randomName,
            randomName + ' ' + randomName,
            benutzerEmail2.getAddress(),
            benutzerEmail2.getAddress(),
            null,
            EntityAktivStatusSpec.AKTIV,
            List.of(new OrganisationRollenJsonSpec(
                null,
                List.of(FachRolleSpec.SCHULAERZTIN),
                List.of()
            )),
            null,
            0L
        );
        BenutzerJsonSpec benutzerJsonSpec = api.
            benutzerResourceCreateBenutzer()
            .body(saveJson)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(BenutzerJsonSpec.class);
        return fixtureService.readBenutzerWithBerechtigungen(ID.parse(benutzerJsonSpec.getId(), Benutzer.class));
    }
}
