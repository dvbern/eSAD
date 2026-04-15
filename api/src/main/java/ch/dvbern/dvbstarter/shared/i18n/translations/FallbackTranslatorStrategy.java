package ch.dvbern.dvbstarter.shared.i18n.translations;

import java.io.Serializable;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.function.Function;

import ch.dvbern.dvbstarter.DebugUtil;
import ch.dvbern.oss.commons.i18nl10n.AppLanguage;
import ch.dvbern.oss.commons.i18nl10n.I18nMessage;
import ch.dvbern.oss.commons.i18nl10n.Translator;
import com.ibm.icu.text.MessageFormat;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.Generated;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FallbackTranslatorStrategy implements Translator {
    private static final Logger LOG = LoggerFactory.getLogger(FallbackTranslatorStrategy.class);
    private final ResourceBundle resourceBundle;
    private final ResourceBundle fallbackBundle;
    private final AppLanguage appLanguage;
    private final FallbackTranslatorStrategy.FormatterFactory formatterFactory;

    public static FallbackTranslatorStrategy.FormatterFactory defaultMessageFormatFactory() {
        return (message, language, bundle, fallbackBundle) -> {
            String messageKey = readStringFromBundleOrReturnKey(bundle, fallbackBundle, message.key().toString());
            MessageFormat var10000 = new MessageFormat(messageKey, language.locale());
            return var10000::format;
        };
    }

    private static String readStringFromBundleOrReturnKey(
        ResourceBundle bundle,
        ResourceBundle fallbackBundle,
        String key) {
        try {
            return bundle.getString(key);
        } catch (MissingResourceException ignore) {
            return fallbackBundle.getString(key);
        }
    }

    public static FallbackTranslatorStrategy create(
        AppLanguage appLanguage,
        Function<AppLanguage, ResourceBundle> bundleLoader,
        Function<AppLanguage, ResourceBundle> fallbackBundleLoader,
        FallbackTranslatorStrategy.FormatterFactory formatterFactory) {
        ResourceBundle bundle = bundleLoader.apply(appLanguage);
        ResourceBundle fallbackBundle = fallbackBundleLoader.apply(appLanguage);
        return new FallbackTranslatorStrategy(bundle, fallbackBundle, appLanguage, formatterFactory);
    }

    public static FallbackTranslatorStrategy createDefault(
        AppLanguage appLanguage,
        Function<AppLanguage, ResourceBundle> bundleLoader,
        Function<AppLanguage, ResourceBundle> fallbackBundleLoader) {
        return create(appLanguage, bundleLoader, fallbackBundleLoader, defaultMessageFormatFactory());
    }

    @Override
    public String translate(I18nMessage message) {
        try {
            FallbackTranslatorStrategy.Formatter messageFormat =
                this.formatterFactory.create(message, this.appLanguage, this.resourceBundle, this.fallbackBundle);
            return messageFormat.format(message.args());
        } catch (MissingResourceException var4) {
            LOG.warn("Translation not found for key/locale: {}/{}", message.key(), this.appLanguage);
            return "!%s[%s]!".formatted(message.key().toString(), DebugUtil.prettyPrintMap(message.args()));
        }
    }

    @SuppressFBWarnings(
        justification = "generated code"
    )
    @Generated
    private FallbackTranslatorStrategy(
        ResourceBundle resourceBundle,
        ResourceBundle fallbackBundle,
        AppLanguage appLanguage,
        FallbackTranslatorStrategy.FormatterFactory formatterFactory) {
        this.resourceBundle = resourceBundle;
        this.fallbackBundle = fallbackBundle;
        this.appLanguage = appLanguage;
        this.formatterFactory = formatterFactory;
    }

    @SuppressFBWarnings(
        justification = "generated code"
    )
    @Generated
    public @NonNull FallbackTranslatorStrategy withAppLanguage(AppLanguage appLanguage) {
        return this.appLanguage == appLanguage ?
            this :
            new FallbackTranslatorStrategy(
                this.resourceBundle,
                this.fallbackBundle,
                appLanguage,
                this.formatterFactory);
    }

    @SuppressFBWarnings(
        justification = "generated code"
    )
    @Generated
    public @NonNull FallbackTranslatorStrategy withFormatterFactory(FallbackTranslatorStrategy.FormatterFactory formatterFactory) {
        return this.formatterFactory == formatterFactory ?
            this :
            new FallbackTranslatorStrategy(
                this.resourceBundle,
                this.fallbackBundle,
                this.appLanguage,
                formatterFactory);
    }

    @FunctionalInterface
    public interface Formatter {
        String format(Map<String, Serializable> var1);
    }

    @FunctionalInterface
    public interface FormatterFactory {
        FallbackTranslatorStrategy.Formatter create(
            I18nMessage var1,
            AppLanguage var2,
            ResourceBundle var3,
            ResourceBundle var4);
    }

}
