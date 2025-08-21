package ch.dvbern.dvbstarter.infrastructure.errorhandling.json;

import ch.dvbern.dvbstarter.infrastructure.errorhandling.FailureType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.ToString;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Getter
@ToString
public abstract class AppErrorJson {

    @NonNull
    @NotNull
    @Schema(required = true)
    private final FailureType type;

    protected AppErrorJson(@NonNull FailureType type) {
        this.type = type;
    }

}
