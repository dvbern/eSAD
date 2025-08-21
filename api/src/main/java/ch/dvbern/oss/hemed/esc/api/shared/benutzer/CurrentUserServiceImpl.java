package ch.dvbern.oss.hemed.esc.api.shared.benutzer;

import java.util.Optional;

import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppAuthException;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppValidationException;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.dvbstarter.shared.types.emailaddress.EmailAddress;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.benutzer.BenutzerRepo;
import ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName;
import io.quarkus.security.identity.CurrentIdentityAssociation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;

@Service
@RequiredArgsConstructor
@Slf4j
public class CurrentUserServiceImpl implements CurrentUserService {

    private final CurrentIdentityAssociation association;
    private final BenutzerRepo repo;

    @Override
    public ID<Benutzer> currentUserID() {
        requireNotAnonymous();

        if (isSystemAdmin()) {
            return Benutzer.SYSTEM_ADMIN_ID;
        }

        // otherwise check who we are
        String email = association.getIdentity().getPrincipal().getName(); // we use email as username
        EmailAddress parsedEmail = parsePrincipalNameAsEmail(email);

        return repo.findBenutzerIdFromBenutzername(parsedEmail)
            .orElseThrow(() -> {
                LOG.info("Could not find user with email: {}", email);
                return new AppAuthException();
            });
    }

    @NonNull
    private EmailAddress parsePrincipalNameAsEmail(@NonNull String username) {
        try {
            return EmailAddress.fromString(username);
        } catch (AppValidationException e) {
            LOG.error("The current SecurityIdentity has username: {} which does not seem to be an email", username);
            throw e;
        }
    }

    @Override
    public EmailAddress currentUserName() {
        requireNotAnonymous();

        if (isSystemAdmin()) {
            return Benutzer.SYSTEM_ADMIN_EMAIL;
        }

        // otherwise check who we are
        String email = association.getIdentity().getPrincipal().getName(); // we use email as username
        return parsePrincipalNameAsEmail(email);

    }

    private boolean isSystemAdmin() {
        boolean isSystemAdmin = association.getIdentity().getRoles().contains(BerechtigungName.SYSTEM_ADMIN);
        if (isSystemAdmin) {
            LOG.debug("Found SYSTEM_ADMIN role, switching to that user");
        }
        return isSystemAdmin;
    }

    private void requireNotAnonymous() {
        if (association.getIdentity() == null) {
            LOG.error("No identity found in context");
            throw new AppAuthException();
        }

        if (association.getIdentity().isAnonymous()) {
            LOG.error("User is Anonymous");
            throw new AppAuthException();
        }
    }

    @Override
    public Optional<Benutzer> findCurrentUser() {
        if (association.getIdentity().isAnonymous()) {
            return Optional.empty();
        }
        return repo.getByBenutzernameWithBerechtigungen(currentUserName());
    }

    @Override
    public Benutzer currentUser() {
        return repo.getById(currentUserID());
    }

    @Override
    public Benutzer currentUserRef() {
        return repo.referenceById(currentUserID());
    }
}
