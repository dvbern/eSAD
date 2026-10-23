package ch.dvbern.dvbstarter.shared.i18n.translations;

import java.util.List;

import ch.dvbern.oss.commons.i18nl10n.AppLanguage;
import ch.dvbern.oss.commons.i18nl10n.I18nMessage;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantEnum;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantContext;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TLProducerTest {
    private static final String TESTING_BUNDLE_NAME = "%s/%s".formatted(
        TLProducerTest.class.getSimpleName(),
        TLProducerTest.class.getSimpleName()
    );

    private final AppLanguageProvider appLanguageProvider = () -> AppLanguages.DE;
    private final TenantContext tenantContext = TenantContext.forTenant(TenantEnum.BS);
    private final TLProducer sut = TLProducer.forTesting(appLanguageProvider, tenantContext, TESTING_BUNDLE_NAME);

    @ParameterizedTest
    @EnumSource(AppLanguages.class)
    @SuppressWarnings("ResultOfMethodCallIgnored")
    void resource_bundles_exist_for_all_languages(AppLanguage appLanguage) {
        assertThatCode(() -> new TLProducer(() -> appLanguage, tenantContext).produceTL())
            .doesNotThrowAnyException();
    }

    @Test
    void produces_from_LocaleProvider() {
        TL actual = sut.produceTL();

        assertThat(actual.translate(I18nMessage.of("hello.world")))
            .isEqualTo("Hallo Welt");
    }

    @Test
    void forAppLanguage_produces_for_the_given_AppLanguage_and_not_from_LocaleProvider() {
        TL actual = sut.forAppLanguage(AppLanguages.FR);

        assertThat(actual.translate(I18nMessage.of("hello.world")))
            .isEqualTo("Bonjour le monde");
    }

    @Test
    void forAppLanguage_falls_back_to_default_locale_for_missing_resource_bundle() {
        TL actual = sut.forAppLanguage(AppLanguages.IT);

        assertThat(actual.translate(I18nMessage.of("hello.world")))
            .isEqualTo("Hallo Welt");
    }

    @Test
    void forTenant_produces_for_the_given_Tenant_and_not_from_LocaleProvider() {
        TL actual = TLProducer.forTesting(
            appLanguageProvider,
            TenantContext.forTenant(TenantEnum.WIN),
            TESTING_BUNDLE_NAME
        ).produceTL();

        assertThat(actual.translate(I18nMessage.of("hello.world")))
            .isEqualTo("Hallo Welt WIN");
    }

    @Test
    void testTranslationForAllTenantsAndCurrentlyRelevantLanguages() {
        List<TenantEnum> tenants = List.of(TenantEnum.BS, TenantEnum.WIN, TenantEnum.BE);
        List<AppLanguages> languages = List.of(AppLanguages.DE, AppLanguages.FR);

        tenants.forEach(tenantEnum -> languages.forEach(language -> {

            TL actual = getTLProducerForTest(language, tenantEnum).produceTL();

            assertThat(actual.translate(I18nMessage.of("tenant.test")))
                .isEqualTo("Test for tenant %s in language %s".formatted(tenantEnum.name(), language));
        }));
    }

    @Test
    void throws_if_base_bundle_is_missing() {
        var brokenProducer =
            TLProducer.forTesting(appLanguageProvider, tenantContext, "resource-bundle-does-not-exist");

        var ex = assertThrows(
            IllegalStateException.class,
            () -> brokenProducer.forAppLanguage(AppLanguages.IT)
        );

        assertThat(ex)
            .hasMessageContaining("resource-bundle-does-not-exist");
    }

    TLProducer getTLProducerForTest(@NonNull AppLanguages language, @NonNull TenantEnum tenantEnum) {
        return TLProducer.forTesting(() -> language, TenantContext.forTenant(tenantEnum), TESTING_BUNDLE_NAME);
    }
}
