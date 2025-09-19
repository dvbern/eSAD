package ch.dvbern.oss.hemed.esc.api.shared.util;

import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppValidationException;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppValidationMessage;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import ch.dvbern.oss.hemed.esc.api.shared.types.EntityAktivStatus;
import lombok.experimental.UtilityClass;
import org.checkerframework.checker.nullness.qual.NonNull;

@UtilityClass
public class KindValidationUtil {

    public static void throwErrorIfKindIsInactive(@NonNull Kind kind, @NonNull AppValidationMessage message) throws AppValidationException {
        if (kind.getKindAktivStatus() == EntityAktivStatus.INAKTIV) {
            throw new AppValidationException(message);
        }
    }
}
