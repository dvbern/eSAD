package ch.dvbern.oss.hemed.esc.api.testutil.i18n;

import java.util.function.Function;

import ch.dvbern.dvbstarter.shared.i18n.translations.TLProducer;
import ch.dvbern.oss.commons.i18nl10n.AppLanguage;
import ch.dvbern.oss.commons.i18nl10n.Translator;
import lombok.experimental.UtilityClass;

@UtilityClass
public class TestTranslations {
    public static final Function<AppLanguage, Translator> TL_FACTORY =
        new TLProducer(() -> null, () -> null)::forAppLanguage;
}
