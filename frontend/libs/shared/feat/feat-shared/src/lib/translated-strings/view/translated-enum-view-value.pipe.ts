/*
 *
 * Copyright (C) 2025 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

import {inject, Injectable, Pipe, PipeTransform} from '@angular/core';
import {isNullish, Nullish} from '@dvb/util-shared';
import {TranslocoService} from '@jsverse/transloco';

/**
 * Default usage : {{ someValue | translatedEnumViewValuePipe }}
 * With custom prefix: {{ someValue | translatedEnumViewValuePipe:'CustomPrefix' }}
 */
@Pipe({
  name: 'translatedEnumViewValuePipe',
  standalone: true,
})
@Injectable({providedIn: 'root'})
export class TranslatedEnumViewValuePipe implements PipeTransform {
  readonly #transloco = inject(TranslocoService);


  public transform(inputValue: string[]  | string | Nullish, translationPrefix = '', separator = ', '): string {
    if (isNullish(inputValue)) {
      return '';
    }
    if (Array.isArray(inputValue)) {
      return inputValue.map(val => this.translateValue(translationPrefix, val)).join(separator);
    } else {
      return this.translateValue(translationPrefix, inputValue);
    }
  }

  private translateValue(translationPrefix: string, val: string): string {
    // only use translation key separator if there is a prefix passed in, otherwise use pure enumValue for translation
    if (translationPrefix == '') {
      return this.#transloco.translate(`${val}`);
    }
    return this.#transloco.translate(`${translationPrefix}.${val}`);
  }
}
