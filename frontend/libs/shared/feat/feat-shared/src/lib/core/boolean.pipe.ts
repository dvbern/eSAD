import {inject, Injectable, Pipe, PipeTransform} from '@angular/core';
import {isNullish, Nullish} from '@dvb/util-shared';
import {TranslocoService} from '@jsverse/transloco';

@Pipe({
  name: 'booleanPipe',
  standalone: true,
})
@Injectable({providedIn: 'root'})
export class BooleanPipe implements PipeTransform {

  readonly #transloco = inject(TranslocoService);

  public transform(value: boolean | Nullish): string {
    if (isNullish(value)) {
      return '';
    }
    if (value) {
      return this.#transloco.translate('global.ja');
    }
    return this.#transloco.translate('global.nein');
  }
}
