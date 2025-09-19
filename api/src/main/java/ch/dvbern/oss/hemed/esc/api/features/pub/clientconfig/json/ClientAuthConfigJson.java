package ch.dvbern.oss.hemed.esc.api.features.pub.clientconfig.json;

import java.net.URL;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import ch.dvbern.dvbstarter.shared.types.url.URLSchema;
import ch.dvbern.oss.hemed.esc.api.shared.types.IgnoreForIdClassTest;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * @param authUrl @parameter authUrl URL to the Keycloak server, for example: http://keycloak-server/auth
 */
@JaxDTO
public record ClientAuthConfigJson(

    @NotEmpty
    @Schema(required = true)
    @IgnoreForIdClassTest
    String clientId,

    @NotEmpty
    @Schema(required = true)
    String realm,

    @NotNull
    @Schema(required = true, implementation = URLSchema.class)
    URL authUrl,

    @Nullable
    @Schema(required = false, implementation = URLSchema.class)
    URL logoutRedirectUrl
) {
}
