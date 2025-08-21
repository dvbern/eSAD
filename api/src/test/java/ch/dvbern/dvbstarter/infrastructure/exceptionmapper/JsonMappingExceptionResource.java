package ch.dvbern.dvbstarter.infrastructure.exceptionmapper;

import java.util.List;

import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppValidationException;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxResource;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import lombok.Value;

import static ch.dvbern.dvbstarter.infrastructure.errorhandling.AppValidationMessage.invalidEmail;

@JaxResource
@Path("/test/json-mapping-exception-mapper")
@PermitAll
public class JsonMappingExceptionResource {

    @Value
    public static class TestJson {
        @NotNull
        String value;
    }

    @Value
    public static class MappingJson {
        @NotNull
        List<Integer> values;
    }

    interface CannotInstantiate {
        String getName();

        void setName();
    }

    @POST
    @Path("constraint-violation-exception")
    public void constraintViolationException(@Valid TestJson testJson) {
        // testcase must pass an invalid TestJson value
    }

    @POST
    @Path("app-validation-exception")
    public void appValidationException(@Valid TestJson testJson) {
        throw new AppValidationException(invalidEmail(testJson.getValue()));
    }

    @POST
    @Path("json-mapping-exception")
    public void jsonMappingException(@Valid CannotInstantiate cannotInstantiate) {
        // testcase must pass an invalid TestJson value
    }

}
