import {AppLanguages} from "@dvb/data-access-backend";

export type Language = 'de' | 'fr' | 'it' | 'en';

export const SUPPORTED_LANGUAGES: Language[] = ['de', 'fr', 'it', 'en'];

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
