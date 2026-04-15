package ch.dvbern.oss.hemed.esc.api.shared.util;

import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppValidationException;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppValidationMessage;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import lombok.experimental.UtilityClass;
import org.checkerframework.checker.nullness.qual.NonNull;

import static ch.dvbern.oss.hemed.esc.api.shared.types.EntityAktivStatus.isInaktiv;

@UtilityClass
public class KindValidationUtil {

    public static void throwErrorIfKindIsInactive(@NonNull Kind kind, @NonNull AppValidationMessage message)
        throws AppValidationException {
        if (isInaktiv(kind.getKindAktivStatus())) {
            throw new AppValidationException(message);
        }
    }
}
