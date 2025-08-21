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

import {CommonModule} from '@angular/common';
import {ChangeDetectionStrategy, Component, inject} from '@angular/core';
import {SystemAdminStore} from '@dvb/feat-shared';
import {ButtonComponent, CardComponent, HeadingDirective} from '@dvb/ui-components';
import {TranslocoPipe} from '@jsverse/transloco';

@Component({
  selector: 'feat-kind-namen-normalisierung',
  imports: [CommonModule, TranslocoPipe, ButtonComponent, CardComponent, HeadingDirective],
  templateUrl: './kind-namen-normalisierung.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
  styleUrl: './kind-namen-normalisierung.component.scss',
})
export class KindNamenNormalisierungComponent {

  readonly systemAdminStore = inject(SystemAdminStore);

  normalizeKindNamen() {
    this.systemAdminStore.normalizeKindNamen();
  }
}
