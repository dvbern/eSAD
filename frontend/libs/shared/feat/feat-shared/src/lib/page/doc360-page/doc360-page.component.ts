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

import {ChangeDetectionStrategy, Component, inject, OnInit} from '@angular/core';
import {DOCUMENT} from '@angular/common';
import {TranslocoPipe} from '@jsverse/transloco';
import {PageComponent} from '../page/page.component';

@Component({
  selector: 'feat-doc360-page',
  imports: [PageComponent, TranslocoPipe],
  templateUrl: './doc360-page.component.html',
  styles: `
    :host {
      display: block;
    }
  `,
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class Doc360PageComponent implements OnInit {
  private readonly document = inject(DOCUMENT);

  /**
   * Full-page navigation (not an XHR) so the browser follows the API's 303 to Document360.
   * Cookies / the session stay on the ESC origin; Document360 then consumes the one-time `code`.
   */
  public ngOnInit(): void {
    this.document.location.href = '/api/document360/authenticate';
  }
}
