import {Type} from '@angular/core';
import {provideTransloco, TranslocoLoader} from '@jsverse/transloco';
import {DEFAULT_LANGUAGE, SUPPORTED_LANGUAGES} from './Language';

export function provideAppTransloco(loader: Type<TranslocoLoader>) {
  return [
    ...provideTransloco({
      config: {
        availableLangs: SUPPORTED_LANGUAGES,
        defaultLang: DEFAULT_LANGUAGE,
        fallbackLang: DEFAULT_LANGUAGE,
        missingHandler: {
          useFallbackTranslation: true,
        },
        // we're doing a complete reload of the application => no need to re-render
        reRenderOnLangChange: true,
      },
      loader,
    }),
  ];
}
