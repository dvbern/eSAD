package ch.dvbern.oss.hemed.esc.api.shared.preview;

import java.io.Serial;

import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppException;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureMessage;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class PreviewGenerationException extends AppException {

    @Serial
    private static final long serialVersionUID = -7432198765432198765L;

    public PreviewGenerationException(@NonNull String message) {
        this(message, null);
    }

    public PreviewGenerationException(@NonNull String message, @Nullable Throwable cause) {
        this(AppFailureMessage.previewGenerationFailed(message), cause);
    }

    private PreviewGenerationException(@NonNull AppFailureMessage failureMessage, @Nullable Throwable cause) {
        super(failureMessage.getI18nMessage(), failureMessage.getId(), cause);
    }
}
