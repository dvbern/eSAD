import { verifyScopeInTranslations } from './verifyScopeInTranslations';

describe('ensureScopeInTranslations', () => {
  it('accepts translations where every key starts with the scope', () => {
    const translations = {
      'scope.valid': 'valid',
    };

    expect(() => verifyScopeInTranslations('scope', translations)).not.toThrow();
  });

  //   FIXME: starter: we decided against scoping of translations
  /*  it('throws with nice error message if entries do not start with scope', () => {
    const translations = {
      'scope.valid': 'valid',
      'foo.invalid': 'translation',
      another: 'failure',
    };

    expect(() => verifyScopeInTranslations('scope', translations)).toThrowError(
      'The following translation-keys do not start with the scope "scope": foo.invalid, another',
    );
  });*/
});
