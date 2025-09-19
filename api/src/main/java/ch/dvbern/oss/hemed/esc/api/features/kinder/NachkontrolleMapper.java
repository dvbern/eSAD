package ch.dvbern.oss.hemed.esc.api.features.kinder;

import ch.dvbern.dvbstarter.infrastructure.json.EntityInfoJson;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxMapper;
import ch.dvbern.oss.hemed.esc.api.features.kinder.json.NachkontrolleJson;
import ch.dvbern.oss.hemed.esc.api.features.kinder.json.NachkontrolleSaveJson;
import ch.dvbern.oss.hemed.esc.api.shared.anlass.anlassflags.AnlassFlagsMapper;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@JaxMapper
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class NachkontrolleMapper {

    @Nullable
    public static NachkontrolleJson toJson(@Nullable Nachkontrolle nachkontrolle) {
        if (nachkontrolle == null) {
            return null;
        }
        return new NachkontrolleJson(
            nachkontrolle.getId(),
            EntityInfoJson.fromEntity(nachkontrolle),
            new NachkontrolleSaveJson(
                AnlassFlagsMapper.toJson(nachkontrolle.getAnlassFlags()),
                nachkontrolle.getVersion()
            )
        );
    }

    @NonNull
    public static Nachkontrolle toEntity(
        @NonNull Nachkontrolle existingNachkontrolle,
        @Nullable NachkontrolleJson saveJson
    ) {
        if (saveJson != null) {
            existingNachkontrolle.setAnlassFlags(AnlassFlagsMapper.toEntity(saveJson.getData().getAnlassFlags()));
        }
        return existingNachkontrolle;
    }
}
