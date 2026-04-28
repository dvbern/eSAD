package ch.dvbern.oss.hemed.esc.api.shared.validator;

import ch.dvbern.oss.hemed.esc.api.features.anlass.abschluss.UntersuchungAbschluss;
import ch.dvbern.oss.hemed.esc.api.features.anlass.abschluss.types.AbschlussResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.AnlassConfig;
import ch.dvbern.oss.hemed.esc.api.shared.util.ValueEnhancedUtil;
import org.checkerframework.checker.nullness.qual.NonNull;

public class CheckUntersuchungAbschlussPresentIfReqValidator
    extends AbstractValidator<CheckUntersuchungAbschlussPresentIfReq, UntersuchungAbschluss> {

    @Override
    public boolean isEntityValid(@NonNull UntersuchungAbschluss entity) {
        AnlassConfig anlassConfig = entity.getUntersuchung().getAnlass().getAnlassConfig();

        if (ValueEnhancedUtil.isRequiredAndNull(anlassConfig.getAbschlussUeberweisungErfolgt(), entity.getUeberweisungErfolgt(), AbschlussResultat.class)) {
            return false;
        }
        if (ValueEnhancedUtil.isRequiredAndNull(anlassConfig.getAbschlussAerztlicheValidierungErforderlich(), entity.getAerztlicheValidierungErforderlich(), AbschlussResultat.class)) {
            return false;
        }
        if (ValueEnhancedUtil.isRequiredAndNull(anlassConfig.getAbschlussBeratungDurchgefuehrt(), entity.getBeratungDurchgefuehrt(), AbschlussResultat.class)) {
            return false;
        }
        return !anlassConfig.getAbschlussBeurteilungEmpfohleneMassnahmen().isRequired()
            || entity.getBeurteilungEmpfohleneMassnahmen() != null;
    }
}
