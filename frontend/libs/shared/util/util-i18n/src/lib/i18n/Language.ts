import {AppLanguages} from "@dvb/data-access-backend";

/**
 * Languages the frontend ships translations for (see `./translations/translations.<lang>.json`).
 *
 * Must be kept in sync with the available translation files: a language listed here but without
 * translation files would be selected for browsers using that language (e.g. `en`) and then fail
 * to load ("Module not found in bundle: ./translations/translations.en.json").
 * The backend's {@link AppLanguages} may contain more languages, these are mapped via {@link appLanguageToLanguage}.
 */
export type Language = 'de' | 'fr';

export const SUPPORTED_LANGUAGES: Language[] = ['de', 'fr'];

export const DEFAULT_LANGUAGE: Language = 'de';

export function isSupportedLanguage(language: unknown): language is Language {
  return SUPPORTED_LANGUAGES.includes(language as Language);
}

export function appLanguageToLanguage(appLanguage: AppLanguages): Language {
  const language = appLanguage.toLowerCase();
  if (isSupportedLanguage(language)) {
    return language;
  }
  console.warn(`Unsupported language: ${appLanguage}, defaulting to ${DEFAULT_LANGUAGE}`);
  return DEFAULT_LANGUAGE;
}
