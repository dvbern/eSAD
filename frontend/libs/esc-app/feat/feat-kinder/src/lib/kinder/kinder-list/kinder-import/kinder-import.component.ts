/*
 * Copyright (C) 2025 DV Bern AG, Switzerland
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

import {ChangeDetectionStrategy, Component, computed, DestroyRef, inject} from '@angular/core';
import {FormsModule, NonNullableFormBuilder, ReactiveFormsModule} from '@angular/forms';
import {KlasseOptionJson, SchuleOptionJson} from '@dvb/data-access-backend';
import {
  BenutzerStore,
  FileDropComponent,
  KlassenOptionsStore,
  ModalDialogComponent,
  SchulenOptionsStore,
} from '@dvb/feat-shared';
import {
  FormControlComponent,
  FormErrorMessageComponent,
  FormGroupComponent,
  FormLayoutComponent,
  NgSelectDirective,
  RadioDirective,
  SaveCancelButtonsComponent,
  SingleRadioWrapperComponent,
} from '@dvb/ui-components';
import {AppNotificationStore, DetailMode} from '@dvb/util-angular';
import {compareFn, hasKindEditBerechtigung, isNullish} from '@dvb/util-shared';
import {TranslocoPipe, TranslocoService} from '@jsverse/transloco';
import {NgbActiveModal} from '@ng-bootstrap/ng-bootstrap';
import {NgOptionComponent, NgSelectComponent} from '@ng-select/ng-select';
import {KinderImportForm} from './kinder-import.form';
import {enhanceKinderImportJson, KinderImportJsonEnhanced, KinderImportStore} from './kinder-import.store';

@Component({
  selector: 'feat-kinder-import',
  imports: [
    ModalDialogComponent,
    TranslocoPipe,
    FormControlComponent,
    FormLayoutComponent,
    FormsModule,
    ReactiveFormsModule,
    NgOptionComponent,
    NgSelectComponent,
    NgSelectDirective,
    RadioDirective,
    SingleRadioWrapperComponent,
    FormGroupComponent,
    FileDropComponent,
    FormErrorMessageComponent,
    SaveCancelButtonsComponent,
  ],
  templateUrl: './kinder-import.component.html',
  styleUrl: './kinder-import.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class KinderImportComponent {
  readonly store = inject(KinderImportStore);
  readonly schulenOptionsStore = inject(SchulenOptionsStore);
  readonly klassenOptionsStore = inject(KlassenOptionsStore);
  readonly benutzerStore = inject(BenutzerStore);
  readonly activeModal = inject(NgbActiveModal);
  readonly #transloco = inject(TranslocoService);
  readonly #appNotificationStore = inject(AppNotificationStore);

  protected readonly compareFnSchule = compareFn<SchuleOptionJson>;
  protected readonly compareFnKlasse = compareFn<KlasseOptionJson>;
  protected readonly DetailMode = DetailMode;

  schuleOptions = computed(() => this.schulenOptionsStore.entitiesAktiv());
  klassenOptions = computed(() => this.klassenOptionsStore.entitiesAktiv());
  canEdit = this.benutzerStore.createHasRequiredRoleSig(hasKindEditBerechtigung());

  readonly acceptedFiletypes = '.xlsx';
  fileUpload: File | undefined;

  public form = new KinderImportForm({
      fb: inject(NonNullableFormBuilder),
      destroyRef: inject(DestroyRef),
      klassenOptionsStore: inject(KlassenOptionsStore),
    },
  );

  public onCancelClicked(): void {
    this.activeModal.close();
  }

  public onFilesSelected($event: File[]): void {
    this.fileUpload = $event[0];
  }

  public downloadImportTemplate(standard: boolean): void {
    if (standard) {
      this.store.downloadStandardTemplate();
    } else {
      this.store.downloadKlassenTemplate();
    }
  }

  async submitIfValid(): Promise<void> {
    if (await this.form.validate()) {
      if (isNullish(this.fileUpload)) {
        this.#appNotificationStore.addDangerNotification(this.#transloco.translate('ui.validation-error-file.required'))
      } else {
        const payload: KinderImportJsonEnhanced = enhanceKinderImportJson(this.form.valueOnValid(), this.fileUpload);
        this.activeModal.close(payload);
      }
    }
  }

}
