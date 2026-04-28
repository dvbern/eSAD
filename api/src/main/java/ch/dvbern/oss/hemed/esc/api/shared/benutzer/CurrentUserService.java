package ch.dvbern.oss.hemed.esc.api.shared.benutzer;

import java.util.Optional;

import ch.dvbern.dvbstarter.shared.types.emailaddress.EmailAddress;
import ch.dvbern.dvbstarter.shared.types.id.ID;

public interface CurrentUserService {
    ID<Benutzer> currentUserID();

    EmailAddress currentUserName();

    Optional<Benutzer> findCurrentUserWithBerechtigungen();

    Benutzer getCurrentUserWithBerechtigungen();

    Benutzer currentUser();

    Benutzer currentUserRef();
}
