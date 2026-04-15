import {inject, Injectable} from '@angular/core';
import {TenantEnum} from '@dvb/data-access-backend';
import {Language, provideGlobalTranslations} from '@dvb/util-i18n';
import {Translation, TranslocoLoader} from '@jsverse/transloco';
import {TenantSettingsStore} from '../tenant/tenant-settings.store';

@Injectable({providedIn: 'root'})
export class TranslocoMultiLoader implements TranslocoLoader {

  private readonly tenant = inject(TenantSettingsStore);

  async getTranslation(lang: Language): Promise<Translation> {
    // FIXME: this is a very stupid way to load translations :(
    //  It should better be lazy-loaded per module!
    const settings = this.tenant.settings();
    if (!settings) {
      console.warn('Tenant settings not loaded! This will result in default translations.');
    }
    const tenant = settings?.tenant ?? TenantEnum.DEFAULT;
    const translations = [
      await provideGlobalTranslations(lang, tenant),
    ];

    return mergeIntoOne(translations);
  }
}

function mergeIntoOne(translations: Translation[]): Translation {
  return translations.reduce((acc, translation) => ({...acc, ...translation}), {} as Translation);
}
