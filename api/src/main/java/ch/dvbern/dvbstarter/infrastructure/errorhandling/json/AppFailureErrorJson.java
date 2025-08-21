package ch.dvbern.dvbstarter.infrastructure.errorhandling.json;

import ch.dvbern.dvbstarter.infrastructure.errorhandling.ExceptionId;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.FailureType;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import ch.dvbern.oss.hemed.esc.api.shared.types.IgnoreForIdClassTest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Getter
@JaxDTO
@Schema(hidden = false)  // Ensures it appears in documentation
public class AppFailureErrorJson extends AppErrorJson {

    @NonNull
    @NotNull
    @Valid
    @Schema(required = true)
    @IgnoreForIdClassTest
    private final ExceptionId id;

    @NonNull
    @NotEmpty
    @Schema(required = true)
    private final String summary;

    public AppFailureErrorJson(@NonNull ExceptionId id, @NonNull String summary) {
        super(FailureType.FAILURE);
        this.id = id;
        this.summary = summary;
    }

    public AppFailureErrorJson(
        @NonNull ExceptionId id,
        @NonNull String summary,
        @NonNull String detail
    ) {
        super(FailureType.FAILURE);
        this.id = id;
        this.summary = summary + ", detail: " + detail;
    }
}
