package ch.dvbern.oss.hemed.esc.api.shared.validator;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import org.checkerframework.checker.nullness.qual.NonNull;

public class CheckAnlassTypNichtAnonymValidator extends AbstractValidator<CheckAnlassTypNichtAnonym, Einladung> {
    @Override
    public boolean isEntityValid(@NonNull Einladung einladung) {
        return !einladung.getAnlass().getAnlassConfig().getAnlassTyp().isAnonym();
    }
}
