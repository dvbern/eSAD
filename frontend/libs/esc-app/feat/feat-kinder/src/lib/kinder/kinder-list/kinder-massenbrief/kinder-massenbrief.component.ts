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

import {ChangeDetectionStrategy, Component, DestroyRef, inject} from '@angular/core';
import {FormsModule, NonNullableFormBuilder, ReactiveFormsModule} from '@angular/forms';
import {BriefvorlageType} from '@dvb/data-access-backend';
import {AerzteStore, BenutzerStore, BriefvorlagenStore, ModalDialogComponent,} from '@dvb/feat-shared';
import {DetailMode} from '@dvb/util-angular';
import {hasBriefvorlagenViewBerechtigung} from '@dvb/util-shared';
import {TranslocoPipe} from '@jsverse/transloco';
import {NgbActiveModal} from '@ng-bootstrap/ng-bootstrap';
import {KinderMassenbriefStore} from "./kinder-massenbrief.store";
import {KinderMassenbriefForm} from "./kinder-massenbrief.form";
import {
  FormControlComponent,
  FormLayoutComponent,
  NgSelectDirective,
  SaveCancelButtonsComponent
} from "@dvb/ui-components";
import {NgOptionComponent, NgSelectComponent} from "@ng-select/ng-select";

@Component({
  selector: 'feat-kinder-massenbrief',
  imports: [
    FormsModule,
    ReactiveFormsModule,
    FormControlComponent,
    FormLayoutComponent,
    ModalDialogComponent,
    NgOptionComponent,
    NgSelectComponent,
    NgSelectDirective,
    SaveCancelButtonsComponent,
    TranslocoPipe,

  ],
  templateUrl: './kinder-massenbrief.component.html',
  styleUrl: './kinder-massenbrief.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class KinderMassenbriefComponent {
  readonly store = inject(KinderMassenbriefStore);
  readonly benutzerStore = inject(BenutzerStore);
  readonly briefvorlagenStore = inject(BriefvorlagenStore);
  readonly aerzteStore = inject(AerzteStore);
  readonly activeModal = inject(NgbActiveModal);

  protected readonly DetailMode = DetailMode;
  protected readonly BriefvorlageType = BriefvorlageType;

  briefVorlageOptions = this.briefvorlagenStore.entities()
    .filter(vorlage => vorlage.data.type == this.BriefvorlageType.KIND);

  canGenerateMassenbrief = this.benutzerStore.createHasRequiredRoleSig(hasBriefvorlagenViewBerechtigung());

  public form = new KinderMassenbriefForm({
      fb: inject(NonNullableFormBuilder),
      destroyRef: inject(DestroyRef),
      store: this.store
    },
  );

  public onCancelClicked(): void {
    this.activeModal.close();
  }

  async submitIfValid(): Promise<void> {
    if (await this.form.validate()) {
      this.activeModal.close(this.form.valueOnValid());
    }
  }
}
