package ch.dvbern.dvbstarter.shared.i18n.translations;

import java.util.Locale;
import java.util.ResourceBundle;
import java.util.ResourceBundle.Control;
import java.util.concurrent.ConcurrentHashMap;

import ch.dvbern.oss.commons.i18nl10n.AppLanguage;
import ch.dvbern.oss.commons.i18nl10n.Translator;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantEnum;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantContext;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import org.jspecify.annotations.NonNull;

import static java.lang.Thread.currentThread;
import static java.util.Objects.requireNonNull;

@ApplicationScoped
public class TLProducer {
    private record LanguageTenantKey(AppLanguage appLanguage, TenantEnum tenant) {
    }

    private final ConcurrentHashMap<LanguageTenantKey, ResourceBundle> bundleMap = new ConcurrentHashMap<>();
    private final AppLanguageProvider appLanguageProvider;
    private final TenantContext tenantContext;
    private final String baseName;

    @Inject
    public TLProducer(
        AppLanguageProvider appLanguageProvider,
        TenantContext tenantContext
    ) {
        this(
            requireNonNull(appLanguageProvider),
            requireNonNull(tenantContext),
            "translations.translations"
        );
    }

    TLProducer(
        AppLanguageProvider appLanguageProvider,
        TenantContext tenantContext,
        String baseName
    ) {
        this.appLanguageProvider = requireNonNull(appLanguageProvider);
        this.tenantContext = requireNonNull(tenantContext);
        this.baseName = baseName;
    }

    public static TLProducer forTesting(
        AppLanguageProvider provider,
        TenantContext tenantContext,
        String baseName
    ) {
        return new TLProducer(provider, tenantContext, baseName);
    }

    // should not need the @RequestScoped annotation!
    // but since the FromRequestAppLanguageProvider is @RequestScoped
    @Produces
    @RequestScoped
    public TL produceTL() {
        var appLanguage = appLanguageProvider.current();

        return forAppLanguage(appLanguage);
    }

    public TL forAppLanguage(AppLanguage appLanguage) {
        Translator translator = FallbackTranslatorStrategy.createDefault(
            appLanguage,
            this::loadBundle,
            this::loadFallbackBundle
        );

        return translator::translate;
    }

    ResourceBundle loadBundle(AppLanguage appLanguage) {
        LanguageTenantKey key = new LanguageTenantKey(appLanguage, getTenant());
        return bundleMap.computeIfAbsent(key, this::initBundle);
    }

    ResourceBundle loadFallbackBundle(AppLanguage appLanguage) {
        LanguageTenantKey languageTenantKey = new LanguageTenantKey(appLanguage, TenantEnum.DEFAULT);
        return bundleMap.computeIfAbsent(languageTenantKey, this::initBundle);
    }

    ResourceBundle initBundle(LanguageTenantKey languageTenantKey) {
        String bundleName = getBundleName(languageTenantKey.tenant);

        return ResourceBundle.getBundle(
            bundleName,
            languageTenantKey.appLanguage.javaLocale(),
            requireNonNull(currentThread().getContextClassLoader()),
            new FallbackToBaseBundleControl()
        );
    }

    @NonNull
    private String getBundleName(@NonNull TenantEnum tenant) {
        return baseName + '-' + (tenant == TenantEnum.DEFAULT ? "esc" : tenant.toString().toLowerCase());
    }

    @NonNull
    TenantEnum getTenant() {
        // translations must also work without tenant (e.g. error responses for URLs without tenant)
        TenantEnum tenant = tenantContext.getOrNull();
        if (tenant == null) {
            return TenantEnum.DEFAULT;
        }
        return tenant;
    }

    private static class FallbackToBaseBundleControl extends Control {
        @Override
        public Locale getFallbackLocale(String baseName, Locale locale) {
            if (locale.equals(Locale.ROOT)) {
                throw new IllegalStateException(
                    ("No fallback resource bundle found for baseName: %s. The default bundle-file must be named: "
                        + "%s.properties without any locale suffix")
                        .formatted(baseName, baseName)
                );
            }

            return Locale.ROOT;
        }
    }
}
