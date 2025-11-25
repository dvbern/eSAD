package ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.json;

import ch.dvbern.dvbstarter.infrastructure.json.EntityInfoJson;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.kind.json.UntersuchungKindDatenJson;
import ch.dvbern.oss.hemed.esc.api.features.kinder.json.KindJson;
import ch.dvbern.oss.hemed.esc.api.features.kinder.json.NachkontrolleJson;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Value
@AllArgsConstructor
@JaxDTO
public class EinladungJson {

    @NotNull
    @NonNull
    @Schema(required = true)
    private final ID<Einladung> id;

    @NotNull
    @NonNull
    @Valid
    @Schema(required = true)
    private final EntityInfoJson entityInfo;

    @NotNull
    @NonNull
    @Valid
    @Schema(required = true)
    private final EinladungSaveJson data;

    @NotNull
    @NonNull
    @Valid
    @Schema(required = true)
    private final KindJson kind;

    @Nullable
    @Valid
    @Schema(required = false)
    private final NachkontrolleJson nachkontrolle;

    @Nullable
    @Valid
    @Schema(required = false)
    private final UntersuchungKindDatenJson untersuchungKindDaten;

    @Nullable
    @Valid
    @Schema(required = false)
    private final NaechsteStationJson naechsteStation;
}
