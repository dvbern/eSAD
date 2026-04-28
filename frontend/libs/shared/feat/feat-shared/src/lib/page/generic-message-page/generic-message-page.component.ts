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

import {ChangeDetectionStrategy, Component, computed, inject} from '@angular/core';
import {toSignal} from '@angular/core/rxjs-interop';
import {ActivatedRoute} from '@angular/router';

import {TranslocoService} from '@jsverse/transloco';
import {PageComponent} from '../page/page.component';

@Component({
  selector: 'feat-generic-message-page',
  imports: [PageComponent],
  templateUrl: './generic-message-page.component.html',
  styles: `
    :host {
      display: block;
    }
  `,
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class GenericMessagePageComponent {
  private readonly route = inject(ActivatedRoute);
  private readonly translocoService = inject(TranslocoService);

  private readonly queryParams = toSignal(this.route.queryParams);

  public readonly title = computed(() => {
    const titleKey = this.queryParams()?.['titleKey'] ?? 'info.title';
    return this.translateOrDefault(titleKey, this.translocoService.translate('info.title'));
  });

  public readonly message = computed(() => {
    const msgKey = this.queryParams()?.['msgKey'];
    return this.translateOrDefault(msgKey, "ERROR");
  });

  private translateOrDefault(msgKey: string, defaultMsg: string): string {
    if (!msgKey) {
      return defaultMsg;
    }
    const translation = this.translocoService.translate(msgKey);
    // If the translation key is returned, it means no translation was found
    if (translation === msgKey || !translation) {
      return defaultMsg;
    }
    return translation;
  }
}
