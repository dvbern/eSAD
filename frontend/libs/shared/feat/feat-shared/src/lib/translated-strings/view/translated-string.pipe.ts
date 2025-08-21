import {inject, Injectable, Pipe, PipeTransform} from '@angular/core';
import {TranslatedStringJson} from '@dvb/data-access-backend';
import { TenantSettingsStore } from '../../tenant/tenant-settings.store';
import {isNullish, Nullish} from '@dvb/util-shared';
import {TranslocoService} from '@jsverse/transloco';

@Pipe({
  name: 'translatedStringPipe',
  standalone: true,
})
@Injectable({providedIn: 'root'})
export class TranslatedStringPipe implements PipeTransform {
  readonly #transloco = inject(TranslocoService);
  readonly #tenantSettingsStore = inject(TenantSettingsStore);

  public transform(value: TranslatedStringJson | Nullish): string {
    if (isNullish(value)) {
      return '';
    }
    if (this.#tenantSettingsStore.supportsLanguageFr() && 'fr' === this.#transloco.getActiveLang()) {
      return value.fr ?? '';
    }
    return value.de ?? '';
  }
}
