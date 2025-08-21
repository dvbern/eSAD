package ch.dvbern.oss.hemed.esc.api.features.benutzer;

import java.util.Set;

import ch.dvbern.dvbstarter.shared.types.emailaddress.EmailAddress;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import lombok.With;

@With
public record BenutzerFixtureBuilder(
    ID<Benutzer> id,
    EmailAddress email,
    String keycloakId
) {
    public static final ID<Benutzer> BENUTZER_ID = ID.parse("99999999-9999-9999-9999-999999999999", Benutzer.class);
    public static final EmailAddress BENUTZER_EMAIL = EmailAddress.fromString("thomas.tester@somewhere.org");

    public static BenutzerFixtureBuilder validBenutzer(ID<Benutzer> id, EmailAddress email) {
        return new BenutzerFixtureBuilder(id, email, id.getUUID().toString());
    }

    public static BenutzerFixtureBuilder validBenutzer() {
        return validBenutzer(BENUTZER_ID, BENUTZER_EMAIL);
    }

    public static BenutzerFixtureBuilder admin() {
        return validBenutzer()
            .withId(Benutzer.SYSTEM_ADMIN_ID)
            .withEmailString("admin@somewhere.org");
    }

    public BenutzerFixtureBuilder withEmailString(String email) {
        return withEmail(EmailAddress.fromString(email));
    }

    public Benutzer build() {
        return new Benutzer(
            keycloakId,
            email,
            "Herr",
            "Tester",
            "Thomas",
            null,
            null,
            null,
            Set.of(),
            true,
            null
        );
    }
}
