import {TestBed} from '@angular/core/testing';
import {TranslocoService} from '@jsverse/transloco';
import type {Mock} from 'vitest';
import {DEFAULT_LANGUAGE} from './Language';
import {LanguageStore} from './language.store';

describe('LanguageStore', () => {
  let store: LanguageStore;
  let translocoService: {setActiveLang: Mock};

  function mockBrowserLanguage(language: string): void {
    Object.defineProperty(navigator, 'language', {
      value: language,
      configurable: true,
    });
  }

  beforeEach(() => {
    localStorage.clear();
    translocoService = {setActiveLang: vi.fn()};

    TestBed.configureTestingModule({
      providers: [
        LanguageStore,
        {provide: TranslocoService, useValue: translocoService},
      ],
    });

    store = TestBed.inject(LanguageStore);
  });

  afterEach(() => {
    localStorage.clear();
  });

  describe('init', () => {
    it('should use the stored language when it is supported', async () => {
      localStorage.setItem('language', 'fr');
      mockBrowserLanguage('de-CH');

      await store.init();

      expect(store.activeLocale()).toBe('fr');
      expect(translocoService.setActiveLang).toHaveBeenCalledWith('fr');
    });

    it('should use the browser language when it is supported and nothing is stored', async () => {
      mockBrowserLanguage('fr-CH');

      await store.init();

      expect(store.activeLocale()).toBe('fr');
      expect(localStorage.getItem('language')).toBe('fr');
      expect(document.documentElement.lang).toBe('fr');
      expect(translocoService.setActiveLang).toHaveBeenCalledWith('fr');
    });

    it.each(['en', 'en-US', 'it', 'it-CH'])(
      'should fall back to the default language for the unsupported browser language "%s"',
      async browserLanguage => {
        mockBrowserLanguage(browserLanguage);

        await store.init();

        expect(store.activeLocale()).toBe(DEFAULT_LANGUAGE);
        expect(localStorage.getItem('language')).toBe(DEFAULT_LANGUAGE);
        expect(translocoService.setActiveLang).toHaveBeenCalledWith(DEFAULT_LANGUAGE);
      },
    );

    it.each(['en', 'it', 'xx'])(
      'should ignore the unsupported stored language "%s" and heal the stored value',
      async storedLanguage => {
        localStorage.setItem('language', storedLanguage);
        mockBrowserLanguage('en-US');

        await store.init();

        expect(store.activeLocale()).toBe(DEFAULT_LANGUAGE);
        expect(localStorage.getItem('language')).toBe(DEFAULT_LANGUAGE);
      },
    );
  });
});
