package ch.dvbern.dvbstarter.infrastructure.exceptionmapper;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxResource;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import lombok.Value;

@JaxResource
@Path("/test/json-parse-exception-mapper")
@PermitAll
public class JsonParseExceptionResource {

    @Value
    public static class TestObject {
        String name;
    }

    @POST
    public void foo(@Valid TestObject testObject) {
        // the test must send a testObject triggering the error
    }

}
