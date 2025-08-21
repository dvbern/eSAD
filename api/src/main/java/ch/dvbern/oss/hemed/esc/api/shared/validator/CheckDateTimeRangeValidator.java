package ch.dvbern.oss.hemed.esc.api.shared.validator;

import ch.dvbern.oss.hemed.esc.api.shared.types.DateTimeRange;
import org.checkerframework.checker.nullness.qual.NonNull;

public class CheckDateTimeRangeValidator extends AbstractValidator<CheckDateTimeRange, DateTimeRange> {

    @Override
    public boolean isEntityValid(@NonNull DateTimeRange instance) {
        return instance.hasValidRange();
    }
}
