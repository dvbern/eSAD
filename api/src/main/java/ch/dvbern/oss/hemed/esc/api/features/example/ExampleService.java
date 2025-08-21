package ch.dvbern.oss.hemed.esc.api.features.example;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.dvbstarter.shared.types.emailaddress.EmailAddress;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ExampleService {

    private final ExampleRepo repo;

    public Benutzer getSomeExample() {
        var newBenutzer = new Benutzer();
        newBenutzer.setKeycloakId("keycloadId");
        newBenutzer.setVorname("vorname");
        newBenutzer.setNachname("nachname");
        newBenutzer.setEmail(EmailAddress.fromString("someone@somewhere.com"));

        repo.persist(newBenutzer);
        return newBenutzer;
    }

}
