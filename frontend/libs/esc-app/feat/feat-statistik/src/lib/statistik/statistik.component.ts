/*
 * Copyright (C) 2026 DV Bern AG, Switzerland
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

import { Component, ChangeDetectionStrategy } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import {PageComponent, Path} from "@dvb/feat-shared";
import {TranslocoPipe} from "@jsverse/transloco";
import {CardComponent} from "@dvb/ui-components";

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'feat-statistik',
  standalone: true,
  imports: [RouterOutlet, PageComponent, TranslocoPipe, CardComponent],
  templateUrl: './statistik.component.html',
  styleUrl: './statistik.component.scss',
})
export class StatistikComponent {

  protected readonly Path = Path;
}
