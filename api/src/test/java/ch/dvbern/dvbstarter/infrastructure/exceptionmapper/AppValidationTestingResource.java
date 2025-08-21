package ch.dvbern.dvbstarter.infrastructure.exceptionmapper;

import java.util.List;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxResource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import lombok.With;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.hibernate.validator.constraints.Length;

@JaxResource
@Path("testing/app-validation")
public class AppValidationTestingResource {

    record NestedString(
        @Length(max = 2)
        String value
    ) {
    }

    record NestedList(
        @Valid
        @Size(min = 2)
        List<NestedString> list
    ) {
    }

    @With
    public record ParamObject(
        @Nullable @Length(max = 2) String rootValue,
        @Nullable @Valid NestedString nestedString,
        @Nullable @Valid NestedList nestedList
    ) {
        public static ParamObject empty() {
            return new ParamObject(null, null, null);
        }
    }

    @POST
    @Path("/validate")
    public void validate(@Valid ParamObject paramObject) {
        // nop
    }
}
