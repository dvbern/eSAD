package ch.dvbern.oss.hemed.esc.api.features.briefvorlagen.briefgenerierung.docmerger;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import ch.dvbern.lib.doctemplate.common.BeanMergeSource;
import ch.dvbern.lib.doctemplate.common.DocTemplateException;
import ch.dvbern.lib.doctemplate.common.MergeContext;
import ch.dvbern.lib.doctemplate.common.MergeSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class AnlassMergeSource implements MergeSource {

    private static final String X_MARKER = "X";
    private static final String EMPTY = "";
    private final AnlassMetadatenDocMergerDTO anlassMetadatenDocMergerDTO;
    private final List<ImpfstatusDocMergerDTO> impfstatusDocMergerDTO;
    private final ImpfstatusListSummaryDocMergerDTO impfstatusListSummaryDocMergerDTO;
    private final ImpfungDocMergerDTO impfungDocMergerDTO;
    private final SehenDocMergerDTO sehenDocMergerDTO;
    private final HoerenAudiogrammDocMergerDTO hoerenAudiogrammDocMergerDTO;
    private final HoerenOtoskopieDocMergerDTO hoerenOtoskopieDocMergerDTO;
    private final GroesseGewichtDocMergerDTO groesseGewichtDocMergerDTO;
    private final UntersuchungArztDocMergerDTO untersuchungArztDocMergerDTO;

    @Override
    public Object getData(MergeContext ctx, String key) throws DocTemplateException {
        if (key.startsWith("meta.")) {
            return new BeanMergeSource(anlassMetadatenDocMergerDTO, "meta.").getData(ctx, key);
        }

        if (key.startsWith("krankheit.")) {
            String subKey = key.substring(key.indexOf('.') + 1);

            for (ImpfstatusDocMergerDTO impfstatus : impfstatusDocMergerDTO) {
                if (subKey.startsWith(impfstatus.getKrankheitKey())) {
                    return new BeanMergeSource(impfstatus, impfstatus.getKrankheitKey() + '.').getData(ctx, subKey);
                }
            }
        }

        if (key.startsWith("impfung.")) {
            return new BeanMergeSource(impfungDocMergerDTO, "impfung.").getData(ctx, key);
        }

        if (key.startsWith("impfstatusListe.")) {
            return new BeanMergeSource(impfstatusListSummaryDocMergerDTO, "impfstatusListe.").getData(ctx, key);
        }

        if (key.startsWith("sehen.")) {
            return new BeanMergeSource(sehenDocMergerDTO, "sehen.").getData(ctx, key);
        }

        if (key.startsWith("hoeren.")) {
            String subKey = key.substring(key.indexOf('.') + 1);
            if (subKey.startsWith("audiogramm")) {
                return evaluateHoerenAudiogrammKey(ctx, subKey);
            }
            if (subKey.startsWith("otoskopie.")) {
                return new BeanMergeSource(hoerenOtoskopieDocMergerDTO, "hoeren.otoskopie.").getData(ctx, key);
            }
        }

        if (key.startsWith("groesseGewicht.")) {
            return new BeanMergeSource(groesseGewichtDocMergerDTO, "groesseGewicht.").getData(ctx, key);
        }

        if (key.startsWith("untersuchungArzt.")) {
            return new BeanMergeSource(untersuchungArztDocMergerDTO, "untersuchungArzt.").getData(ctx, key);
        }
        return '[' + key + ']';
    }

    @Override
    public Boolean ifStatement(MergeContext ctx, String key) throws DocTemplateException {
        return false;
    }

    private Object evaluateHoerenAudiogrammKey(MergeContext ctx, String key) throws DocTemplateException {
        String subKey = key.substring(key.indexOf('.') + 1);
        if (subKey.startsWith("rechts") || subKey.startsWith("links")) {
            Map<String, String> audiograms = subKey.startsWith("rechts.") ?
                hoerenAudiogrammDocMergerDTO.getAudiogrammRechts() :
                hoerenAudiogrammDocMergerDTO.getAudiogrammLinks();
            String[] measures = subKey.substring(subKey.indexOf('.') + 1).split("\\.");
            String herzt = measures[0];
            String dezibels = measures[1];
            return Objects.equals(audiograms.get(herzt), dezibels) ? X_MARKER : EMPTY;
        }
        return new BeanMergeSource(hoerenAudiogrammDocMergerDTO, "audiogramm.").getData(ctx, key);
    }

    @Override
    public List<MergeSource> whileStatement(MergeContext ctx, String key) throws DocTemplateException {
        return List.of();
    }
}
