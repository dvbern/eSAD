import {inject, Injectable} from '@angular/core';
import {AbstractSignalStore} from '@dvb/util-angular';
import {TranslocoService} from '@jsverse/transloco';
import {DEFAULT_LANGUAGE, isSupportedLanguage, Language} from './Language';

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
  constructor() {
    const testInit = inject<LanguageState>('nothing' as never, { optional: true });

    super(testInit ?? languageStateInitialState);
  }

  async init(): Promise<void> {
    let language = this.getStoredLanguage();

    if (!language) {
      const browserLanguage = this.getBrowserLanguage();
      // TODO ESC-1089: Team: check against languages from TenantSettingsStore?
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
    const browserLanguage = navigator.language;
    const normalizedLang = browserLanguage.split(';')[0].trim();
    return normalizedLang.includes('-') ? normalizedLang.split('-')[0] : normalizedLang;
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
