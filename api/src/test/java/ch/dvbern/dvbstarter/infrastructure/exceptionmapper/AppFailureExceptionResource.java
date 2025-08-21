package ch.dvbern.dvbstarter.infrastructure.exceptionmapper;

import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureException;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxResource;
import jakarta.annotation.security.PermitAll;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

import static ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureMessage.internalError;

@JaxResource
@Path("/test/app-failure-exception-mapper")
@PermitAll
public class AppFailureExceptionResource {

    @POST
    public void throwAppFailure() {
        throw new AppFailureException(internalError("hello exception"));
    }

}
