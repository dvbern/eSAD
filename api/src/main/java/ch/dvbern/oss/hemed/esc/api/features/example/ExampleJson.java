package ch.dvbern.oss.hemed.esc.api.features.example;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import ch.dvbern.dvbstarter.shared.types.emailaddress.EmailAddress;
import ch.dvbern.dvbstarter.shared.types.phonenumber.NormalizedPhoneNumber;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@JaxDTO
@Value
@AllArgsConstructor
public class ExampleJson {

    @Schema(required = false)
    private final String example;

    @Schema(required = false)
    private final EmailAddress email;

    @Schema(required = true)
    private final NormalizedPhoneNumber phoneNumber;
}
