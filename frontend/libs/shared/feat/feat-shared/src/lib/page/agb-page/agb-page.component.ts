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

import {ChangeDetectionStrategy, Component, inject, signal} from '@angular/core';

import {TranslocoPipe} from '@jsverse/transloco';
import {PageComponent} from '../page/page.component';
import {ButtonComponent, CheckboxDirective} from "@dvb/ui-components";
import {FormsModule} from "@angular/forms";
import {BenutzerStore} from "../../benutzer/benutzer/benutzer.store";
import {TenantSettingsStore} from "../../tenant/tenant-settings.store";
import {ActivatedRoute, Router} from "@angular/router";
import {Path} from "../../core/path";
import {AgbResponsability} from "@dvb/data-access-backend";

@Component({
  selector: 'feat-agb-page',
  imports: [
    PageComponent, TranslocoPipe, FormsModule, ButtonComponent, CheckboxDirective
  ],
  templateUrl: './agb-page.component.html',
  styles: `
    :host {
      display: block;
    }
  `,
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class AgbPageComponent {
  readonly benutzerStore = inject(BenutzerStore);
  readonly tenantSettingStore = inject(TenantSettingsStore);
  readonly #router = inject(Router);
  readonly #activatedRoute = inject(ActivatedRoute);

  protected accepted = signal(false);

  toggleAccepted() {
    this.accepted.update(v => !v);
  }

  acceptAGB() {
    const responsability: AgbResponsability | undefined = this.tenantSettingStore.agbResponsibility();
    if (this.accepted() && responsability === AgbResponsability.AGB_MANAGED_BY_USER) {
      this.benutzerStore.acceptAGB({
        onSuccess: () => {
          this.#router.navigate(
            [Path.RELATIVE_ONE_UP, Path.DASHBOARD],
            {relativeTo: this.#activatedRoute});
        }
      });
    }
  }
}
