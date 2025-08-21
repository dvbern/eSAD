package ch.dvbern.dvbstarter.infrastructure.exceptionmapper;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxResource;
import jakarta.annotation.security.PermitAll;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

@JaxResource
@Path("/test/web-application-exception-mapper")
@PermitAll
public class WebApplicationExceptionResource {

    @POST
    public void ignored() {
        throw new IllegalStateException("should-not-see-me");
    }

}
