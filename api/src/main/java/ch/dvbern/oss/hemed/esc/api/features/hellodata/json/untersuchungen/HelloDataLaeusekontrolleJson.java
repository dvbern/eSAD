package ch.dvbern.oss.hemed.esc.api.features.hellodata.json.untersuchungen;

import java.util.Set;
import java.util.UUID;
import jakarta.validation.constraints.NotNull;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import ch.dvbern.oss.hemed.esc.api.features.anlass.laeusekontrolle.types.LaeusekontrolleBeurteilung;
import ch.dvbern.oss.hemed.esc.api.shared.types.IgnoreForIdClassTest;

import lombok.AllArgsConstructor;
import lombok.Value;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Value
@AllArgsConstructor
@JaxDTO
public class HelloDataLaeusekontrolleJson {

    @IgnoreForIdClassTest
    @NotNull
    @NonNull
    @Schema(required = true)
    UUID id;

    @Nullable
    @Schema(required = false)
    Set<LaeusekontrolleBeurteilung> beurteilung;
}
