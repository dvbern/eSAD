package ch.dvbern.oss.hemed.esc.api.shared.validator;

import java.util.EnumSet;

import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.AnlassConfig;
import ch.dvbern.oss.hemed.esc.api.features.impfstoff.types.Krankheit;
import org.checkerframework.checker.nullness.qual.NonNull;

public class CheckAnlassConfigImpfungKrankheitenUniquenessValidator
    extends AbstractValidator<CheckAnlassConfigImpfungKrankheitenUniqueness, AnlassConfig> {

    @Override
    public boolean isEntityValid(@NonNull AnlassConfig instance) {
        EnumSet<Krankheit> krankheitenLinks = instance.getUntersuchungImpfungKrankheitenLinksValues();
        EnumSet<Krankheit> krankheitenRechts = instance.getUntersuchungImpfungKrankheitenRechtsValues();

        return krankheitenLinks.stream().noneMatch(krankheitenRechts::contains);
    }
}
