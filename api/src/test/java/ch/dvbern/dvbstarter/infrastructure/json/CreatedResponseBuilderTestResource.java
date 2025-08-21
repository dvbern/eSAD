package ch.dvbern.dvbstarter.infrastructure.json;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxResource;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

@JaxResource
@Path("/testing/CreatedResponseBuilderTestResource")
public class CreatedResponseBuilderTestResource {

    @POST
    public Response create() {
        return CreatedResponseBuilder.of(
                CreatedResponseBuilderTest.FIXTURE_ID,
                CreatedResponseBuilderTestResource.class
            )
            .build();
    }

    @GET
    @Path("{id}")
    public String get(ID<Benutzer> id) {
        // nop
        return "Created: %s".formatted(id);
    }

}
