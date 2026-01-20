package ch.dvbern.dvbstarter.infrastructure.errorhandling;

import java.io.Serial;
import java.util.List;

import ch.dvbern.oss.commons.i18nl10n.I18nMessage;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public final class MassenimportRollbackException extends AppException {

    @Serial
    private static final long serialVersionUID = 2654097053149795616L;

    public MassenimportRollbackException(
        @NotNull int skippedRows, @NotNull List<Exception> exceptionsWhilePersisting
    ) {

        super(
            I18nMessage.of(
                "AppValidation.MASSENIMPORT_ROLLBACK",
                "hasSkipped", skippedRows != 0,
                "hasException", !exceptionsWhilePersisting.isEmpty()), ExceptionId.random(), null);
    }
}
