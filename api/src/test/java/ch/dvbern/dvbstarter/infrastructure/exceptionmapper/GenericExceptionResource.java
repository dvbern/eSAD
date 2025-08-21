package ch.dvbern.dvbstarter.infrastructure.exceptionmapper;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxResource;
import jakarta.annotation.security.PermitAll;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

@JaxResource
@Path("/test/generic-exception-mapper")
@PermitAll
public class GenericExceptionResource {

    @POST
    public void foo() {
        throw new IllegalStateException("hello exception");
    }

}
