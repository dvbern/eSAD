import {ChangeDetectionStrategy, Component, inject} from '@angular/core';
import {FormsModule, NonNullableFormBuilder, ReactiveFormsModule} from '@angular/forms';
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
import {KinderEsadidForm} from './kinder-esadid.form';

@Component({
  selector: 'feat-kinder-detail-dossier-esadid-modal',
  standalone: true,
  templateUrl: './kinder-esadid-modal.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    FormsModule,
    ReactiveFormsModule,
    ModalDialogComponent,
    SaveCancelButtonsComponent,
    TranslocoPipe,
    FormControlComponent,
    TextInputDirective,
    FormLayoutComponent,
  ],
})
export class KinderEsadidModalComponent {
  readonly activeModal = inject(NgbActiveModal);

  form = new KinderEsadidForm(inject(NonNullableFormBuilder));
  protected readonly DetailMode = DetailMode;

  abbrechen(): void {
    this.activeModal.dismiss('cancel');
  }

  async speichern(): Promise<void> {
    if (await this.form.validate()) {
      this.activeModal.close(this.form.valueOnValid());
    }
  }

}
