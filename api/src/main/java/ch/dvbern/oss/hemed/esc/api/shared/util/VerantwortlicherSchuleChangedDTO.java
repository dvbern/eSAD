package ch.dvbern.oss.hemed.esc.api.shared.util;

import java.util.Objects;

import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.schule.Schule;
import ch.dvbern.oss.hemed.esc.api.features.schule.json.SchuleSaveJson;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class VerantwortlicherSchuleChangedDTO {

    @Nullable
    ID<Benutzer> arztVorher;

    @Nullable
    ID<Benutzer> arztNachher;

    @Nullable
    ID<Benutzer> mpaVorher;

    @Nullable
    ID<Benutzer> mpaNachher;

    @NonNull
    public static VerantwortlicherSchuleChangedDTO of(
        @Nullable Schule schuleOld,
        @Nullable Schule schuleNew
    ) {
        return new VerantwortlicherSchuleChangedDTO(
            schuleOld != null && schuleOld.getZustaendigArzt() != null ? schuleOld.getZustaendigArzt().getId() : null,
            schuleNew != null && schuleNew.getZustaendigArzt() != null ? schuleNew.getZustaendigArzt().getId() : null,
            schuleOld != null && schuleOld.getZustaendigMPA() != null ? schuleOld.getZustaendigMPA().getId() : null,
            schuleNew != null && schuleNew.getZustaendigMPA() != null ? schuleNew.getZustaendigMPA().getId() : null
        );
    }

    @NonNull
    public static VerantwortlicherSchuleChangedDTO of(
        @NonNull Schule schuleOld,
        @NonNull SchuleSaveJson saveJson
    ) {
        return new VerantwortlicherSchuleChangedDTO(
            schuleOld.getZustaendigArzt() != null ? schuleOld.getZustaendigArzt().getId() : null,
            saveJson.getZustaendigArzt() != null ? saveJson.getZustaendigArzt().getId() : null,
            schuleOld.getZustaendigMPA() != null ? schuleOld.getZustaendigMPA().getId() : null,
            saveJson.getZustaendigMPA() != null ? saveJson.getZustaendigMPA().getId() : null
        );
    }

    public boolean hasArztChanged() {
        return !Objects.equals(this.arztVorher, this.arztNachher);
    }

    public boolean hasMpaChanged() {
        return !Objects.equals(this.mpaVorher, this.mpaNachher);
    }
}
