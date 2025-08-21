package ch.dvbern.dvbstarter.infrastructure.errorhandling;

import lombok.Getter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@Getter
public final class AppValidationException extends AppException {
    private static final long serialVersionUID = -6978804033368571677L;

    private final String clientKey;

    public AppValidationException(@NonNull AppValidationMessage message) {
        this(message, null);
    }

    public AppValidationException(@NonNull AppValidationMessage message, @Nullable Throwable cause) {
        super(message.getI18nMessage(), ExceptionId.random(), cause);
        this.clientKey = message.getClientKey();
    }
}
