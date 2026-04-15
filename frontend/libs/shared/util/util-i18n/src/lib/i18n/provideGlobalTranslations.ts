import {TenantEnum} from '@dvb/data-access-backend';
import {Translation} from '@jsverse/transloco';
import {isSupportedLanguage, Language} from './Language';

export async function provideGlobalTranslations(
  language: Language,
  tenant: TenantEnum,
): Promise<Translation> {
  if (isSupportedLanguage(language)) {
    const baseTranslations = (await import(`./translations/translations.${language}.json`)).default;
    if (tenant === TenantEnum.DEFAULT) {
      return baseTranslations;
    }
    const mandantenTranslations = (await import((`./translations/translations-${tenant.toLowerCase()}.${language}.json`))).default;
    return {...baseTranslations, ...mandantenTranslations};
  }

  return {};
}
