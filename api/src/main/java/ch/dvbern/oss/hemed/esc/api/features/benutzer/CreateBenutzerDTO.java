package ch.dvbern.oss.hemed.esc.api.features.benutzer;

import ch.dvbern.dvbstarter.shared.types.emailaddress.EmailAddress;
import ch.dvbern.dvbstarter.shared.types.phonenumber.NormalizedPhoneNumber;
import ch.dvbern.oss.hemed.esc.api.shared.types.IgnoreForIdClassTest;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@Value
@AllArgsConstructor
public class CreateBenutzerDTO {

    @NonNull
    private final EmailAddress email;

    @Nullable
    private final String titel;

    @NonNull
    private final String vorname;

    @NonNull
    private final String nachname;

    @Nullable
    private final NormalizedPhoneNumber telefon;

    @Nullable
    @IgnoreForIdClassTest
    private final String keycloakId;
}

