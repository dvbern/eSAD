import {ChangeDetectionStrategy, Component, effect, inject, model} from '@angular/core';
import {FormsModule, NonNullableFormBuilder, ReactiveFormsModule} from '@angular/forms';
import {EinladungJson} from '@dvb/data-access-backend';
import {ModalDialogComponent} from '@dvb/feat-shared';
import {
  FormControlComponent,
  FormLayoutComponent,
  SaveCancelButtonsComponent,
  TextInputDirective,
} from '@dvb/ui-components';
import {DetailMode} from '@dvb/util-angular';
import {TranslocoPipe} from '@jsverse/transloco';
import {NgbActiveModal} from '@ng-bootstrap/ng-bootstrap';
import {ConfirmEinladungRemoveForm} from './confirm-einladung-remove.form';

@Component({
  selector: 'feat-anlass-planung-einladungen-abmelden-modal',
  templateUrl: './confirm-einladung-remove.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    FormsModule,
    TranslocoPipe,
    TextInputDirective,
    ReactiveFormsModule,
    FormControlComponent,
    FormLayoutComponent,
    ModalDialogComponent,
    SaveCancelButtonsComponent,
  ],
})
export class ConfirmEinladungRemoveComponent {
  readonly activeModal = inject(NgbActiveModal);

  protected readonly DetailMode = DetailMode;

  einladungData = model.required<EinladungJson | undefined>();

  form = new ConfirmEinladungRemoveForm({
    fb: inject(NonNullableFormBuilder),
  });

  constructor() {
    effect(() => {
      const data = this.einladungData();
        this.form.resetForm(data);
    });
  }

  cancel(): void {
    this.activeModal.dismiss('cancel');
  }

  async remove(): Promise<void> {
    if (await this.form.validate()) {
      this.activeModal.close(this.form.valueOnValid());
    }
  }
}
