import type * as BASE_LANG from './translations/translations.de.json';

export type TranslationKey = keyof typeof BASE_LANG;

/**
 * This ensures proper translation-keys.
 *
 * Usage: TranslationMap<typeof Foo>
 */
export type TranslationMap<T extends Record<string, unknown>> = {
  [K in Required<keyof T>]: TranslationKey;
};
