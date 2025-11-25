import {
  provideTransloco,
  provideTranslocoMissingHandler,
  Translation,
  TranslocoMissingHandler,
  TranslocoMissingHandlerData,
  TranslocoLoader,
} from '@jsverse/transloco';

import { DEFAULT_LANGUAGE, Language, SUPPORTED_LANGUAGES } from './Language';

type TestingTranslations = Record<Language, Translation>;

export function provideTestingTransloco(translations?: Partial<TestingTranslations>) {
  class TestingTranslocoLoader implements TranslocoLoader {
    getTranslation(lang: Language): Promise<Translation> {
      return Promise.resolve(translations?.[lang] ?? {});
    }
  }

  class ReturnsKeyAsTranslationHandler implements TranslocoMissingHandler {
    // Returning the allows for easier A11y-Testing using aria-names/labels.
    //   Using real translations poses a problem since translations might change.
    //   Some projects allow the customer to change translations... and this would break
    //   the tests.
    //   Testing with translation keys ensures translations exist but is independent of actual translations.
    // FIXME: implement: load all real translations,
    //  still only return the keys
    //  but validate if translation-keys are missing
    handle(key: string, _config: TranslocoMissingHandlerData): string {
      return key;
    }
  }

  return [
    ...provideTransloco({
      config: {
        availableLangs: SUPPORTED_LANGUAGES,
        defaultLang: DEFAULT_LANGUAGE,
        fallbackLang: DEFAULT_LANGUAGE,
        missingHandler: {
          // allow missing keys for testing, see also ReturnsKeyAsTranslationHandler
          useFallbackTranslation: false,
          logMissingKey: false,
        },
        // might be useful for testing
        reRenderOnLangChange: true,
      },
      loader: TestingTranslocoLoader,
    }),
    provideTranslocoMissingHandler(ReturnsKeyAsTranslationHandler),

  ];
}
