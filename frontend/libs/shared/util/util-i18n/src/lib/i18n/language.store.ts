import { Inject, inject, Injectable, Optional } from '@angular/core';
import { AbstractSignalStore } from '@dvb/util-angular';
import { TranslocoService } from '@jsverse/transloco';
import { DEFAULT_LANGUAGE, isSupportedLanguage, Language } from './Language';

type LanguageState = {
  selectedLanguage: Language;
};

const languageStateInitialState: LanguageState = {
  selectedLanguage: DEFAULT_LANGUAGE,
};

@Injectable({ providedIn: 'root' })
export class LanguageStore extends AbstractSignalStore<LanguageState> {
  private static readonly languageLocalStorageKey = 'language';
  readonly #translocoService = inject(TranslocoService);

  /**
   * @param testInit - for testing purposes only
   */
  constructor(@Inject('nothing') @Optional() testInit?: LanguageState) {
    super(testInit ?? languageStateInitialState);
  }

  async init(): Promise<void> {
    let language = this.getStoredLanguage();

    if (!language) {
      const browserLanguage = this.getBrowserLanguage();
      if (isSupportedLanguage(browserLanguage)) {
        language = browserLanguage;
      }
    }

    const finalLanguage = language ?? languageStateInitialState.selectedLanguage;
    this.syncLanguage(finalLanguage);

    this.patchState(draft => {
      draft.selectedLanguage = finalLanguage;
    });
  }

  setActiveLanguage(language: Language): void {
    this.syncLanguage(language);
    // without window reload, this would do the trick:
    this.patchState(draft => {
      draft.selectedLanguage = language;
    });
    // window reload to avoid issues with cached translated values etc.:
    window.location.reload();
  }

  private getStoredLanguage(): Language | null {
    const storedLang = localStorage.getItem(LanguageStore.languageLocalStorageKey);

    if (!isSupportedLanguage(storedLang)) {
      return null;
    }

    return storedLang;
  }

  private getBrowserLanguage(): string {
    // FIXME: implement properly
    const browserLanguage = navigator.language;
    return browserLanguage.includes('-') ? browserLanguage.split('-')[0] : browserLanguage;
  }

  private syncLanguage(language: string): void {
    localStorage.setItem(LanguageStore.languageLocalStorageKey, language);
    document.documentElement.lang = language;
    this.#translocoService.setActiveLang(language);
  }

  activeLocale(): Language {
    return this.state().selectedLanguage;
  }
}
