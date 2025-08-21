package ch.dvbern.oss.hemed.esc.api.shared.mandant;

import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppValidationException;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppValidationMessage;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxMapper;
import ch.dvbern.dvbstarter.shared.i18n.translations.AppLanguages;
import ch.dvbern.oss.hemed.esc.api.features.pub.settings.TenantSetting;
import ch.dvbern.oss.hemed.esc.api.features.pub.settings.TenantSettingsService;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@JaxMapper
@RequiredArgsConstructor
public final class TranslatedStringMapper {

    private final TenantSettingsService tenantSettingsService;


    @NonNull
    public static TranslatedStringJson toJson(@NonNull TranslatedString translatedString) {
        return new TranslatedStringJson(
            translatedString.getNameDe(),
            translatedString.getNameFr()
        );
    }

    @NonNull
    public TranslatedString toEntity(@Nullable TranslatedString existingEntity, @NonNull TranslatedStringJson json) {
        if (existingEntity == null) {
            existingEntity = new TranslatedString();
        }
        TenantSetting tenantSetting = tenantSettingsService.getTenantSetting();
        String nameInDefaultLanguage = extractNameInDefaultLanguage(tenantSetting, json);
        // Nicht unterstuetzte Sprachen werden auf die Default-Sprache gesetzt
        existingEntity.setNameDe(
            extractNameInLanguageOrDefault(tenantSetting, AppLanguages.DE, json.getDe(), nameInDefaultLanguage)
        );
        existingEntity.setNameFr(
            extractNameInLanguageOrDefault(tenantSetting, AppLanguages.FR, json.getFr(), nameInDefaultLanguage)
        );
        return existingEntity;
    }

    @NonNull
    private String extractNameInDefaultLanguage(
        @NonNull TenantSetting tenantSetting,
        @NonNull TranslatedStringJson json
    ) {
        String nameInDefaultLanguage =
            tenantSetting.getDefaultLanguage() == AppLanguages.FR ? json.getFr() : json.getDe();
        if (nameInDefaultLanguage == null) {
            throw new AppValidationException(AppValidationMessage.missingTranslatedString(tenantSetting.getDefaultLanguage().name()));
        }
        return nameInDefaultLanguage;
    }

    @NonNull
    private String extractNameInLanguageOrDefault(
        @NonNull TenantSetting tenantSetting,
        @NonNull AppLanguages language,
        @Nullable String nameInLanguage,
        @NonNull String nameInDefaultLanguage
    ) {
        if (tenantSetting.getSupportedLanguages().contains(language)) {
            if (nameInLanguage == null) {
                throw new AppValidationException(AppValidationMessage.missingTranslatedString(language.name()));
            }
            return nameInLanguage;
        }
        return nameInDefaultLanguage;
    }
}
