package ch.dvbern.oss.hemed.esc.api.features.anlass.json;

import java.time.LocalDate;

import ch.dvbern.dvbstarter.infrastructure.json.EntityInfoJson;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.UntersuchungKindDaten;
import ch.dvbern.oss.hemed.esc.api.shared.person.LandCode;
import ch.dvbern.oss.hemed.esc.api.shared.types.IgnoreForIdClassTest;
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
public class UntersuchungKindDatenJson {

    // Hier nullable, weil die Daten anfangs vom Kind genommen werden und erst später als
    // UntersuchungKindDaten gespeichert werden.
    @Nullable
    @Schema(required = false)
    private ID<UntersuchungKindDaten> id;

    @NotNull
    @NonNull
    @Valid
    @Schema(required = true)
    private final EntityInfoJson entityInfo;

    @NotNull
    @NonNull
    @Valid
    @Schema(required = true)
    private final UntersuchungKindDatenSaveJson data;

    @Nullable
    @Schema(required = false)
    @IgnoreForIdClassTest
    private String esadId;

    @NotNull
    @NonNull
    @Schema(required = true)
    private String alterDuodezimal;

    @Nullable
    @Schema(required = false)
    private LandCode landCode;

    @Nullable
    @Schema(required = false)
    @IgnoreForIdClassTest
    private String klasseId;

    @NotNull
    @NonNull
    @Schema(required = true)
    private LocalDate anlassDatum;
}
