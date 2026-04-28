package ch.dvbern.oss.hemed.esc.api.shared.validator;

import ch.dvbern.oss.hemed.esc.api.features.anlass.anonymereintrag.AnonymerEintrag;
import org.checkerframework.checker.nullness.qual.NonNull;

public class CheckAnlassTypAnonymValidator extends AbstractValidator<CheckAnlassTypAnonym, AnonymerEintrag> {
    @Override
    public boolean isEntityValid(@NonNull AnonymerEintrag anonymerEintrag) {
        return anonymerEintrag.getAnlass().getAnlassConfig().getAnlassTyp().isAnonym();
    }
}
