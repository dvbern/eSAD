package ch.dvbern.oss.hemed.esc.api.testutil.builder;

import java.util.EnumSet;
import java.util.List;
import java.util.function.Consumer;

import ch.dvbern.dvbstarter.shared.i18n.translations.AppLanguages;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.types.EinladungStatus;
import ch.dvbern.oss.hemed.esc.api.shared.settings.TenantSetting;
import ch.dvbern.oss.hemed.esc.api.shared.settings.features.FeatureEnabledSetting;
import ch.dvbern.oss.hemed.esc.api.shared.settings.features.TenantSettingFeatures;
import ch.dvbern.oss.hemed.esc.api.shared.types.AgbResponsability;
import ch.dvbern.oss.hemed.esc.api.shared.types.LandingPageOption;
import ch.dvbern.oss.hemed.esc.api.shared.util.EnumSetUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.TestUtil;
import org.assertj.core.annotation.CanIgnoreReturnValue;
import org.checkerframework.checker.nullness.qual.NonNull;

public class TenantSettingBuilder {

    private TenantSetting tenantSetting;

    private TenantSettingBuilder() {
        this.tenantSetting = defaultTenantSetting();
        TestUtil.setEntityInfo(tenantSetting);
    }

    @NonNull
    public static TenantSetting defaultTenantSetting(
    ) {
        return new TenantSetting(
            AppLanguages.DE,
            "de,fr",
            5,
            List.of("test@mailbucket.dvbern.ch"),
            "test@mailbucket.dvbern.ch",
            AgbResponsability.AGB_MANAGED_BY_USER,
            LandingPageOption.DASHBOARD,
            new TenantSettingFeatures(
                FeatureEnabledSetting.ENABLED,
                FeatureEnabledSetting.ENABLED,
                FeatureEnabledSetting.ENABLED,
                FeatureEnabledSetting.ENABLED,
                FeatureEnabledSetting.ENABLED,
                FeatureEnabledSetting.ENABLED,
                FeatureEnabledSetting.ENABLED,
                FeatureEnabledSetting.ENABLED,
                FeatureEnabledSetting.ENABLED,
                FeatureEnabledSetting.ENABLED,
                FeatureEnabledSetting.ENABLED,
                FeatureEnabledSetting.ENABLED,
                FeatureEnabledSetting.ENABLED,
                FeatureEnabledSetting.ENABLED
            ),
            EnumSetUtil.toString(EnumSet.allOf(EinladungStatus.class))
        );
    }

    @NonNull
    @CanIgnoreReturnValue
    public TenantSettingBuilder forTenantSetting(@NonNull TenantSetting setting) {
        this.tenantSetting = setting;
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public TenantSettingBuilder withArztListeFeature(@NonNull FeatureEnabledSetting setting) {
        this.tenantSetting.getTenantSettingFeatures().setArztListeFeature(setting);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public TenantSettingBuilder withAdHocImpfstoffChargenFeature(@NonNull FeatureEnabledSetting setting) {
        this.tenantSetting.getTenantSettingFeatures().setAdHocImpfstoffChargenFeature(setting);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public TenantSettingBuilder withBenutzerverwaltungInternFeature(@NonNull FeatureEnabledSetting setting) {
        this.tenantSetting.getTenantSettingFeatures().setBenutzerverwaltungInternFeature(setting);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public TenantSettingBuilder withBenutzerverwaltungIgnoreFachRolleFromKeycloakFeature(@NonNull FeatureEnabledSetting setting) {
        this.tenantSetting.getTenantSettingFeatures().setBenutzerverwaltungIgnoreFachRolleFromKeycloakFeature(setting);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public TenantSettingBuilder withZentraleOrganisationFeature(@NonNull FeatureEnabledSetting setting) {
        this.tenantSetting.getTenantSettingFeatures().setZentraleOrganisationFeature(setting);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public TenantSettingBuilder withArztListeFeature(int folgeTermineMaxSlots) {
        this.tenantSetting.setFolgeTermineMaxSlots(folgeTermineMaxSlots);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public TenantSettingBuilder withDefaultLangague(@NonNull AppLanguages defaultLangague) {
        this.tenantSetting.setDefaultLanguage(defaultLangague);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public TenantSettingBuilder withSupportedLangagues(@NonNull List<AppLanguages> supportedLanguages) {
        this.tenantSetting.setSupportedLanguages(supportedLanguages);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public TenantSettingBuilder withEinladungStatusValues(@NonNull List<EinladungStatus> einladungStatusValues) {
        this.tenantSetting.setEinladungStatusValues(einladungStatusValues);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public TenantSettingBuilder withCustomerAdminEmails(@NonNull List<String> customerAdminEmails) {
        this.tenantSetting.setCustomerAdminEmails(customerAdminEmails);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public TenantSettingBuilder withAgbResponsability(@NonNull AgbResponsability agbResponsability) {
        this.tenantSetting.setAgbResponsability(agbResponsability);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public TenantSettingBuilder withDefaultLandingPage(@NonNull LandingPageOption defaultLandingPage) {
        this.tenantSetting.setDefaultLandingPage(defaultLandingPage);
        return this;
    }

    @NonNull
    public static TenantSetting create(@NonNull Consumer<TenantSettingBuilder> block) {
        TenantSettingBuilder builder = new TenantSettingBuilder();
        block.accept(builder);
        return builder.tenantSetting;
    }
}
