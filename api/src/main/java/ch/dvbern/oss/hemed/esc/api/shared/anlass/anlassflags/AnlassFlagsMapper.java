package ch.dvbern.oss.hemed.esc.api.shared.anlass.anlassflags;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxMapper;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@JaxMapper
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AnlassFlagsMapper {

    @Nullable
    public static AnlassFlagsJson toJson(@Nullable AnlassFlags anlassFlags) {
        if (anlassFlags == null) {
            return null;
        }
        return new AnlassFlagsJson(
            anlassFlags.isNachkontrolleSehen(),
            anlassFlags.isNachkontrolleHoeren(),
            anlassFlags.isNachkontrolleGroesseGewicht(),
            anlassFlags.isNachkontrolleSprache(),
            anlassFlags.isNachkontrolleMotorik(),
            anlassFlags.isNachkontrolleAnderes()
        );
    }

    @NonNull
    public static AnlassFlags toEntity(@NonNull AnlassFlagsJson anlassFlags) {
        return new AnlassFlags(
            anlassFlags.isNachkontrolleSehen(),
            anlassFlags.isNachkontrolleHoeren(),
            anlassFlags.isNachkontrolleGroesseGewicht(),
            anlassFlags.isNachkontrolleSprache(),
            anlassFlags.isNachkontrolleMotorik(),
            anlassFlags.isNachkontrolleAnderes()
        );
    }
}
