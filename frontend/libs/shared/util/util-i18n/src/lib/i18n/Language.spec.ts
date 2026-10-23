import {AppLanguages} from '@dvb/data-access-backend';
import {appLanguageToLanguage, DEFAULT_LANGUAGE, isSupportedLanguage, SUPPORTED_LANGUAGES} from './Language';

describe('Language', () => {
  describe('SUPPORTED_LANGUAGES', () => {
    it.each(SUPPORTED_LANGUAGES)('should ship base translations for "%s"', async language => {
      // guards against listing a language without translation files (which would fail at runtime)
      const module = await import(`./translations/translations.${language}.json`);
      const translations = module.default ?? module;

      expect(Object.keys(translations).length).toBeGreaterThan(0);
    });
  });

  describe('isSupportedLanguage', () => {
    it.each(['de', 'fr'])('should accept "%s"', language => {
      expect(isSupportedLanguage(language)).toBe(true);
    });

    it.each(['en', 'it', 'de-CH', 'DE', '', null, undefined])(
      'should reject "%s" (no translations available)',
      language => {
        expect(isSupportedLanguage(language)).toBe(false);
      },
    );
  });

  describe('appLanguageToLanguage', () => {
    it('should map DE and FR to their frontend language', () => {
      expect(appLanguageToLanguage(AppLanguages.DE)).toBe('de');
      expect(appLanguageToLanguage(AppLanguages.FR)).toBe('fr');
    });

    it.each([AppLanguages.IT, AppLanguages.EN])(
      'should fall back to the default language for %s',
      appLanguage => {
        const warnSpy = vi.spyOn(console, 'warn').mockImplementation(() => undefined);

        expect(appLanguageToLanguage(appLanguage)).toBe(DEFAULT_LANGUAGE);
        expect(warnSpy).toHaveBeenCalledTimes(1);

        warnSpy.mockRestore();
      },
    );
  });
});
