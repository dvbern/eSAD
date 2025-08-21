export type Language = 'de' | 'fr';

export const SUPPORTED_LANGUAGES: Language[] = ['de', 'fr'];

export const DEFAULT_LANGUAGE: Language = 'de';

export function isSupportedLanguage(language: unknown): language is Language {
  return SUPPORTED_LANGUAGES.includes(language as Language);
}
