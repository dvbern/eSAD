import { Translation } from '@jsverse/transloco';

export function verifyScopeInTranslations(scope: string, translationsPerLang: Translation | undefined): Translation {
  if (!translationsPerLang) {
    console.warn(`No translations found for scope "${scope}". Maybe you used the transloco 'scope' wrong?`);
    return {};
  }

  const invalidKeys = findInvalidTranslationKeys(scope, translationsPerLang);

  // if (invalidKeys.length > 0) {
  //   const keys = invalidKeys.join(', ');
  //   throw new Error(`The following translation-keys do not start with the scope "${scope}": ${keys}`);
  // }

  return translationsPerLang;
}

function findInvalidTranslationKeys(scope: string, translations: Translation): string[] {
  return Object.keys(translations).filter(key => !key.startsWith(`${scope}.`));
}
