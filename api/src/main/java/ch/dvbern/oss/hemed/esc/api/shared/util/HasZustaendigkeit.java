package ch.dvbern.oss.hemed.esc.api.shared.util;

import java.util.Optional;

import ch.dvbern.oss.hemed.esc.api.features.organisation.Organisation;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import org.checkerframework.checker.nullness.qual.Nullable;

public interface HasZustaendigkeit {

    @Nullable
    Benutzer getZustaendigArzt();

    @Nullable
    Benutzer getZustaendigMPA();

    @Nullable
    default Organisation getSingleOrganisation() {
        return Optional.ofNullable(getZustaendigArzt())
            .map(Benutzer::getSingleOrganisation)
            .orElse(null);
    }
}
