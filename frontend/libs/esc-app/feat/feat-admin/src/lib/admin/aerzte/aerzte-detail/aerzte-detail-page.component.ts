/*
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

import {ChangeDetectionStrategy, Component, DestroyRef, effect, inject, untracked} from '@angular/core';
import {NonNullableFormBuilder, ReactiveFormsModule} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {Fachrichtung, HpvTeilnahme, Organisation} from '@dvb/data-access-backend';
import {AerzteStore} from '@dvb/feat-shared';
import {AdresseComponent, AdresseForm} from '@dvb/feat-shared';
import {BenutzerStore} from '@dvb/feat-shared';
import {Path} from '@dvb/feat-shared';
import {PageComponent} from '@dvb/feat-shared';
import {
  CardComponent,
  FormButtonsComponent,
  FormControlComponent,
  FormFullWidthDirective,
  FormLayoutComponent,
  HeadingDirective,
  NgSelectDirective,
  TextareaDirective,
  TextInputDirective,
} from '@dvb/ui-components';
import {DirtyMarker} from '@dvb/util-angular';
import {hasAerztePageEditBerechtigung, isNotNullish, isNullish} from '@dvb/util-shared';
import {TranslocoPipe} from '@jsverse/transloco';
import {NgOptionComponent, NgSelectComponent} from '@ng-select/ng-select';
import {ArztDetailForm} from './aerzte-detail.form';

@Component({
  selector: 'feat-aerzte-detail.page',
  templateUrl: 'aerzte-detail-page.component.html',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    PageComponent,
    TranslocoPipe,
    CardComponent,
    FormLayoutComponent,
    ReactiveFormsModule,
    AdresseComponent,
    FormFullWidthDirective,
    HeadingDirective,
    FormControlComponent,
    NgOptionComponent,
    NgSelectComponent,
    NgSelectDirective,
    TextInputDirective,
    FormButtonsComponent,
    TextareaDirective,
  ],
})
export class AerzteDetailPageComponent implements DirtyMarker {
  protected readonly Path = Path;

  readonly aerzteStore = inject(AerzteStore);
  readonly #benutzerStore = inject(BenutzerStore);
  readonly #router = inject(Router);
  readonly #activatedRoute = inject(ActivatedRoute);

  fachrichtungen = Object.values(Fachrichtung);
  organsisationen = Object.values(Organisation);
  hpvTeilnahmen = Object.values(HpvTeilnahme);

  form = new ArztDetailForm({
    fb: inject(NonNullableFormBuilder),
    adresseForm: new AdresseForm({
      fb: inject(NonNullableFormBuilder),
      destroyRef: inject(DestroyRef),
      withLand: true,
      parentIdentifier: 'kind-personendaten',
    }),
  });

  canEditList = this.#benutzerStore.createHasRequiredRoleSig(hasAerztePageEditBerechtigung());

  constructor() {
    effect(() => {
      const selectedEntity = this.aerzteStore.selectedEntity();
      const detailMode = this.aerzteStore.detailMode();
      if (isNullish(selectedEntity)) {
        return;
      }
      untracked(() => {
        this.form.reset(selectedEntity.data, detailMode);
      });
    });
  }

  async submitIfValid(): Promise<void> {
    if (!(await this.form.validate())) {
      return;
    }

    const params = {
      arzt: this.form.valueOnValid(),
      onSuccess: () => {
        this.form.reset();
        this.#router.navigate([Path.RELATIVE_ONE_UP], {relativeTo: this.#activatedRoute});
      },
    };

    if (this.aerzteStore.selectedIsNew() && isNotNullish(params.arzt)) {
      this.aerzteStore.createArzt(params);
      return;
    }

    this.aerzteStore.updateArzt(params);
  }

  isDirty() {
    return this.form.isDirty() || this.aerzteStore.saveLoader.isLoading();
  }
}
