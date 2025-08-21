package ch.dvbern.oss.hemed.esc.api.features.briefvorlagen.briefgenerierung.docmerger;

import java.util.List;

import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppValidationException;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppValidationMessage;
import ch.dvbern.lib.doctemplate.common.BeanMergeSource;
import ch.dvbern.lib.doctemplate.common.DocTemplateException;
import ch.dvbern.lib.doctemplate.common.MergeContext;
import ch.dvbern.lib.doctemplate.common.MergeSource;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@RequiredArgsConstructor
public class RootMergeSource implements MergeSource {

    @NonNull
    private final AllgemeinDocMergerDTO allgemeinDocMergerDTO;
    @NonNull
    private final KindDocMergerDTO kindDocMergerDTO;
    @Nullable
    private final AnlassMergeSource anlassMergeSource;
    @Nullable
    private final ExternerArztDocMergerDTO externerArztDocMergerDTO;

    @Override
    public Object getData(MergeContext ctx, String key) throws DocTemplateException {

        if (key.startsWith("allgemein.")) {
            return new BeanMergeSource(allgemeinDocMergerDTO, "allgemein.").getData(ctx, key);
        }

        if (key.startsWith("kind.")) {
            return new BeanMergeSource(kindDocMergerDTO, "kind.").getData(ctx, key);
        }

        if (key.startsWith("anlass.")) {
            if (anlassMergeSource == null) {
                throw new AppValidationException(AppValidationMessage.docMergeKeyNotSupportedByBriefvorlageType(key));
            }
            String subKey = key.substring(key.indexOf('.') + 1);
            return anlassMergeSource.getData(ctx, subKey);
        }

        if (key.startsWith("externerArzt.")) {
            return new BeanMergeSource(externerArztDocMergerDTO, "externerArzt.").getData(ctx, key);
        }

        return '[' + key + ']';
    }

    @Override
    public Boolean ifStatement(MergeContext ctx, String key) throws DocTemplateException {
        return false;
    }

    @Override
    public List<MergeSource> whileStatement(MergeContext ctx, String key) throws DocTemplateException {
        return List.of();
    }
}
