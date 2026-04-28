package ch.dvbern.oss.hemed.esc.api.shared.validator;

import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.AnlassConfig;
import ch.dvbern.oss.hemed.esc.api.features.anlass.vorabklaerungen.UntersuchungVorabklaerungen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.vorabklaerungen.types.ChronischeKrankheiten;
import ch.dvbern.oss.hemed.esc.api.features.anlass.vorabklaerungen.types.EinverstaendnisImpfungen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.vorabklaerungen.types.FragebogenStatus;
import ch.dvbern.oss.hemed.esc.api.shared.util.ValueEnhancedUtil;
import org.checkerframework.checker.nullness.qual.NonNull;

public class CheckUntersuchungVorabklaerungenPresentIfReqValidator
    extends AbstractValidator<CheckUntersuchungVorabklaerungenPresentIfReq, UntersuchungVorabklaerungen> {

    @Override
    /* readability is better with seprate return*/
    @SuppressWarnings("java:S1126")
    public boolean isEntityValid(@NonNull UntersuchungVorabklaerungen entity) {
        AnlassConfig anlassConfig = entity.getUntersuchung().getAnlass().getAnlassConfig();

        if (ValueEnhancedUtil.isRequiredAndNull(anlassConfig.getVorabklaerungenFragebogenStatus(), entity.getFragebogenStatus(), FragebogenStatus.class)) {
            return false;
        }
        if (ValueEnhancedUtil.isRequiredAndNull(anlassConfig.getVorabklaerungenEinverstaendnisImpfungen(), entity.getEinverstaendnisImpfungen(), EinverstaendnisImpfungen.class)) {
            return false;
        }
        if (ValueEnhancedUtil.isRequiredAndNull(anlassConfig.getVorabklaerungenChronischeKrankheiten(), entity.getChronischeKrankheiten(), ChronischeKrankheiten.class)) {
            return false;
        }
        return true;
    }
}
