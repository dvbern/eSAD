package ch.dvbern.oss.hemed.esc.api.features.benutzer.ensure;

import java.util.Optional;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.benutzer.BenutzerRepo;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.jwt.JsonWebToken;

@Slf4j
@Service
@RequiredArgsConstructor
public class EnsureBenutzerService {

    private final JsonWebToken jsonWebToken;
    private final BenutzerRepo benutzerRepo;
    private final EnsureBenutzerHelperService ensureBenutzerHelperService;

    public void ensureBenutzer() {
        // Nicht-eingeloggter Benutzer. Wir koennen hier abbrechen
        final String rawToken = jsonWebToken.getRawToken();
        if (rawToken == null) {
            return;
        }
        // Informationen aus dem Token lesen
        JsonWebTokenDataDTO webTokenData = JsonWebTokenDataDTO.fromJsonWebToken(jsonWebToken);
        createOrUpdateBenutzerFromToken(webTokenData);
    }

    private void createOrUpdateBenutzerFromToken(@NonNull JsonWebTokenDataDTO webTokenData) {
        // Wir ueberpruefen, ob es schon einen Benutzer mit diesem Benutzernamen gibt
        Optional<ID<Benutzer>> existingUserId = findExistingBenutzer(webTokenData);
        if (existingUserId.isPresent()) {
            ensureBenutzerHelperService.updateExistingUser(existingUserId.get(), webTokenData);
        } else {
            ensureBenutzerHelperService.createNewUserFromToken(webTokenData);
        }
    }

    @NonNull
    private Optional<ID<Benutzer>> findExistingBenutzer(@NonNull JsonWebTokenDataDTO webTokenData) {
        // Wir ueberpruefen, ob es schon einen Benutzer mit diesem Benutzernamen gibt
        Optional<ID<Benutzer>> existingBenutzerOptional =
            benutzerRepo.findBenutzerIdFromBenutzername(webTokenData.getEmail());
        return existingBenutzerOptional;
    }
}
