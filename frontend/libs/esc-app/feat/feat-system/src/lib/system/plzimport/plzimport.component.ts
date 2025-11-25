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


import {ChangeDetectionStrategy, Component, inject} from '@angular/core';
import {SystemAdminStore} from '@dvb/feat-shared';
import {Path} from '@dvb/feat-shared';
import {ButtonComponent, CardComponent, HeadingDirective} from '@dvb/ui-components';
import {TranslocoPipe} from '@jsverse/transloco';

@Component({
  selector: 'feat-system-plzimport',
  imports: [TranslocoPipe, ButtonComponent, CardComponent, HeadingDirective],
  templateUrl: './plzimport.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
  styleUrl: './plzimport.component.scss',
})
export class PlzImportComponent {
  protected readonly Path = Path;
  readonly systemAdminStore = inject(SystemAdminStore);

  importPlzData() {
    this.systemAdminStore.importPlzFromCsv();
  }
}
