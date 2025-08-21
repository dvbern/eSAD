import {ChangeDetectionStrategy, Component, inject, model} from '@angular/core';
import {NonNullableFormBuilder, ReactiveFormsModule} from '@angular/forms';
import {LehrpersonOptionJson} from '@dvb/data-access-backend';
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
import {LehrpersonenAddForm} from './lehrpersonen-add.form';

@Component({
  selector: 'feat-lehrpersonen-add',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    ModalDialogComponent,
    FormLayoutComponent,
    TranslocoPipe,
    FormControlComponent,
    NgSelectDirective,
    NgSelectComponent,
    NgOptionComponent,
    SaveCancelButtonsComponent,
  ],
  templateUrl: './lehrpersonen-add.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class LehrpersonenAddComponent {
  readonly activeModal = inject(NgbActiveModal);
  protected readonly DetailMode = DetailMode;

  lehrpersonen = model.required<LehrpersonOptionJson[]>();

  form = new LehrpersonenAddForm({
    fb: inject(NonNullableFormBuilder),
  });

  cancel(): void {
    this.activeModal.dismiss('cancel');
  }

  async addLehrperson(): Promise<void> {
    if (await this.form.validate()) {
      this.activeModal.close(this.form.valueOnValid());
    }
  }
}
