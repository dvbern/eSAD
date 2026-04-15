import {Translation} from '@jsverse/transloco';

export function verifyScopeInTranslations(scope: string, translationsPerLang: Translation | undefined): Translation {
  if (!translationsPerLang) {
    console.warn(`No translations found for scope "${scope}". Maybe you used the transloco 'scope' wrong?`);
    return {};
  }

  findInvalidTranslationKeys(scope, translationsPerLang);

  return translationsPerLang;
}

function findInvalidTranslationKeys(scope: string, translations: Translation): string[] {
  return Object.keys(translations).filter(key => !key.startsWith(`${scope}.`));
}
