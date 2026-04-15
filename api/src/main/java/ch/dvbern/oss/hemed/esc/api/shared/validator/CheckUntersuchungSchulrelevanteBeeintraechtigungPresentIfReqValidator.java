package ch.dvbern.oss.hemed.esc.api.shared.validator;

import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.AnlassConfig;
import ch.dvbern.oss.hemed.esc.api.features.anlass.schulrelevantebeeintraechtigung.UntersuchungSchulrelevanteBeeintraechtigung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.schulrelevantebeeintraechtigung.types.Feinmotorik;
import ch.dvbern.oss.hemed.esc.api.features.anlass.schulrelevantebeeintraechtigung.types.FreiesGehen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.schulrelevantebeeintraechtigung.types.GleichgewichtEinbeinstand;
import ch.dvbern.oss.hemed.esc.api.features.anlass.schulrelevantebeeintraechtigung.types.Grafomotorik;
import ch.dvbern.oss.hemed.esc.api.features.anlass.schulrelevantebeeintraechtigung.types.GrobmotorikEinbeinhuepfen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.schulrelevantebeeintraechtigung.types.OhneStuetzradVelofahren;
import ch.dvbern.oss.hemed.esc.api.shared.util.ValueEnhancedUtil;
import jakarta.enterprise.context.ApplicationScoped;
import org.checkerframework.checker.nullness.qual.NonNull;

@ApplicationScoped
public class CheckUntersuchungSchulrelevanteBeeintraechtigungPresentIfReqValidator
    extends AbstractValidator<CheckUntersuchungSchulrelevanteBeeintraechtigungPresentIfReq, UntersuchungSchulrelevanteBeeintraechtigung> {

    @Override
    @SuppressWarnings({
        "java:S1126" /* readability is better with seprate return*/ }
    )
    public boolean isEntityValid(@NonNull UntersuchungSchulrelevanteBeeintraechtigung beeintraechtigung) {
        AnlassConfig anlassConfig = beeintraechtigung.getUntersuchung().getEinladung().getAnlass().getAnlassConfig();

        if (ValueEnhancedUtil.isRequiredAndNull(anlassConfig.getUntersuchungBeeintraechtigungFreiesGehen(), beeintraechtigung.getFreiesGehen(), FreiesGehen.class)) {
            return false;
        }
        if (ValueEnhancedUtil.isRequiredAndNull(anlassConfig.getUntersuchungBeeintraechtigungOhneStuetzradVelofahren(), beeintraechtigung.getOhneStuetzradVelofahren(), OhneStuetzradVelofahren.class)) {
            return false;
        }
        if (ValueEnhancedUtil.isRequiredAndNull(anlassConfig.getUntersuchungBeeintraechtigungGrafomotorik(), beeintraechtigung.getGrafomotorik(), Grafomotorik.class)) {
            return false;
        }
        if (ValueEnhancedUtil.isRequiredAndNull(anlassConfig.getUntersuchungBeeintraechtigungFeinmotorik(), beeintraechtigung.getFeinmotorik(), Feinmotorik.class)) {
            return false;
        }
        if (ValueEnhancedUtil.isRequiredAndNull(anlassConfig.getUntersuchungBeeintraechtigungGrobmotorik(), beeintraechtigung.getGrobmotorikEinbeinhuepfen(), GrobmotorikEinbeinhuepfen.class)) {
            return false;
        }
        if (ValueEnhancedUtil.isRequiredAndNull(anlassConfig.getUntersuchungBeeintraechtigungGleichgewicht(), beeintraechtigung.getGleichgewichtEinbeinstand(), GleichgewichtEinbeinstand.class)) {
            return false;
        }
        return !anlassConfig.getUntersuchungBeeintraechtigungBemerkung().isRequired()
            || beeintraechtigung.getBemerkungen() != null;
    }
}
