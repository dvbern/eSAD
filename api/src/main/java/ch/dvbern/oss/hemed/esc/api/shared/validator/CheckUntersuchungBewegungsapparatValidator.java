package ch.dvbern.oss.hemed.esc.api.shared.validator;

import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.AnlassConfig;
import ch.dvbern.oss.hemed.esc.api.features.anlass.bewegungsapparat.UntersuchungBewegungsapparat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.bewegungsapparat.types.Haltungsschwaeche;
import ch.dvbern.oss.hemed.esc.api.features.anlass.bewegungsapparat.types.Skoliose;
import ch.dvbern.oss.hemed.esc.api.features.anlass.bewegungsapparat.types.VerkuerzungHamstringmuskulatur;
import ch.dvbern.oss.hemed.esc.api.shared.util.ValueEnhancedUtil;
import org.checkerframework.checker.nullness.qual.NonNull;

public class CheckUntersuchungBewegungsapparatValidator
    extends AbstractValidator<CheckUntersuchungBewegungsapparatPresentIfReq, UntersuchungBewegungsapparat> {

    @Override
    public boolean isEntityValid(@NonNull UntersuchungBewegungsapparat entity) {
        AnlassConfig anlassConfig = entity.getUntersuchung().getAnlass().getAnlassConfig();
        if (ValueEnhancedUtil.isRequiredAndNull(anlassConfig.getBewegungsapparatSkoliose(), entity.getSkoliose(), Skoliose.class)) {
            return false;
        }
        if (ValueEnhancedUtil.isRequiredAndNull(anlassConfig.getBewegungsapparatHaltungsschwaeche(), entity.getHaltungsschwaeche(), Haltungsschwaeche.class)) {
            return false;
        }
        if (ValueEnhancedUtil.isRequiredAndNull(anlassConfig.getBewegungsapparatVerkuerzungHamstringmuskulatur(), entity.getVerkuerzungHamstringmuskulatur(), VerkuerzungHamstringmuskulatur.class)) {
            return false;
        }
        return !anlassConfig.getBewegungsapparatBemerkung().isRequired() || entity.getBemerkungen() != null;
    }
}
