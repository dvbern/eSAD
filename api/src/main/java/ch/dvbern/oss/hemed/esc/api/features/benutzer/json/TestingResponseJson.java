package ch.dvbern.oss.hemed.esc.api.features.benutzer.json;

import java.net.URL;

import ch.dvbern.dvbstarter.infrastructure.deployment.DeploymentEnvironment;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import ch.dvbern.dvbstarter.shared.types.emailaddress.EmailAddress;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.dvbstarter.shared.types.url.URLSchema;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@JaxDTO
@Value
@AllArgsConstructor
public class TestingResponseJson {

    @Schema(required = true)
    private final ID<Benutzer> id;

    @Schema(required = true)
    private final EmailAddress email;

    @Schema(required = true, implementation = URLSchema.class)
    private final URL publicURL;

    @Schema(required = true)
    private final DeploymentEnvironment environment;
}
