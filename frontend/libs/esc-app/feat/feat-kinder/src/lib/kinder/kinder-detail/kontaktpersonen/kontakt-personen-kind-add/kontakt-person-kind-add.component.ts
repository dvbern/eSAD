import {ChangeDetectionStrategy, Component, inject, model} from '@angular/core';
import {NonNullableFormBuilder, ReactiveFormsModule} from '@angular/forms';
import {KontaktPersonKindOptionJson} from '@dvb/data-access-backend';
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
import {KontaktPersonKindAddForm} from './kontakt-person-kind-add.form';

@Component({
  selector: 'feat-kontakt-person-kind-add',
  templateUrl: './kontakt-person-kind-add.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
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
export class KontaktPersonKindAddComponent {
  readonly activeModal = inject(NgbActiveModal);
  protected readonly DetailMode = DetailMode;

  kontaktPersonen = model.required<KontaktPersonKindOptionJson[]>();

  form = new KontaktPersonKindAddForm({
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
