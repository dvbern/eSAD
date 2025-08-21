import {ChangeDetectionStrategy, Component, inject, model} from '@angular/core';
import {FormsModule, NonNullableFormBuilder, ReactiveFormsModule} from '@angular/forms';
import {KontaktPersonSchuleOptionJson} from '@dvb/data-access-backend';
import {ModalDialogComponent} from '@dvb/feat-shared';
import {
  FormControlComponent,
  FormLayoutComponent,
  NgSelectDirective,
  SaveCancelButtonsComponent,
} from '@dvb/ui-components';
import {DetailMode} from '@dvb/util-angular';
import {TranslocoPipe} from '@jsverse/transloco';
import {NgbActiveModal} from '@ng-bootstrap/ng-bootstrap';
import {NgOptionComponent, NgSelectComponent} from '@ng-select/ng-select';
import {SchulenDetailKontaktpersonenAddForm} from './schulen-detail-kontaktpersonen-add.form';

@Component({
  selector: 'feat-schulen-detail-kontaktpersonen-add',
  templateUrl: './schulen-detail-kontaktpersonen-add.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    FormsModule,
    TranslocoPipe,
    ReactiveFormsModule,
    FormLayoutComponent,
    ModalDialogComponent,
    NgOptionComponent,
    NgSelectComponent,
    NgSelectDirective,
    FormControlComponent,
    SaveCancelButtonsComponent,
  ],
})
export class SchulenDetailKontaktpersonenAddComponent {
  readonly activeModal = inject(NgbActiveModal);

  protected readonly DetailMode = DetailMode;

  kontaktpersonen = model.required<KontaktPersonSchuleOptionJson[]>();

  form = new SchulenDetailKontaktpersonenAddForm({
    fb: inject(NonNullableFormBuilder),
  });

  cancel(): void {
    this.activeModal.dismiss('cancel');
  }

  async addContact(): Promise<void> {
    if (await this.form.validate()) {
      this.activeModal.close(this.form.valueOnValid());
    }
  }
}
