import { Translation } from '@jsverse/transloco';
import { Language } from './Language';

export async function provideGlobalTranslations(
  language: Language
): Promise<Translation> {
  switch (language) {
    case 'de':
      return (await import('./translations/translations.de.json')).default;
    case 'fr':
      return (await import('./translations/translations.fr.json')).default;
  }
}
