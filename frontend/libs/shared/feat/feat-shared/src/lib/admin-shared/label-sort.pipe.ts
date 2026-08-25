/*
 * Copyright (C) 2026 DV Bern AG, Switzerland
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
import {LabelJson} from '@dvb/data-access-backend';
import {Nullish} from '@dvb/util-shared';
import {TranslatedStringPipe} from '../translated-strings/view/translated-string.pipe';
import {sortLabelsByName} from './label-sort.util';

/**
 * The backend stores the Labels of an entity in a Set, so their order is arbitrary.
 * Use this pipe wherever Labels are displayed, to show them alphabetically:
 * `@for (label of item.labels | sortLabelsPipe; track label.id)`
 */
@Pipe({
  name: 'sortLabelsPipe',
  standalone: true,
})
@Injectable({providedIn: 'root'})
export class SortLabelsPipe implements PipeTransform {
  readonly #translatedStringPipe = inject(TranslatedStringPipe);

  public transform(labels: LabelJson[] | Nullish): LabelJson[] {
    return sortLabelsByName(labels, this.#translatedStringPipe);
  }
}
