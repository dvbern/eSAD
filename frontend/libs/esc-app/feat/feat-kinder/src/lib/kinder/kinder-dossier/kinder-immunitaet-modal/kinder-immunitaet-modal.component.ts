import {ChangeDetectionStrategy, Component, effect, inject, model} from '@angular/core';
import {FormsModule, NonNullableFormBuilder, ReactiveFormsModule} from '@angular/forms';
import {DossierMedizinischSaveJson} from '@dvb/data-access-backend';
import {ModalDialogComponent} from '@dvb/feat-shared';
import {CheckboxDirective, SaveCancelButtonsComponent, StandaloneControlComponent} from '@dvb/ui-components';
import {DetailMode} from '@dvb/util-angular';
import {isNotNullish} from '@dvb/util-shared';
import {TranslocoPipe} from '@jsverse/transloco';
import {NgbActiveModal} from '@ng-bootstrap/ng-bootstrap';
import {KinderImmunitaetForm} from './kinder-immunitaet.form';

@Component({
    selector: 'feat-kinder-detail-dossier-immunitaet-modal',
    templateUrl: './kinder-immunitaet-modal.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [
        FormsModule,
        ReactiveFormsModule,
        ModalDialogComponent,
        TranslocoPipe,
        SaveCancelButtonsComponent,
        CheckboxDirective,
        StandaloneControlComponent,
    ]
})
export class KinderImmunitaetModalComponent {
  readonly activeModal = inject(NgbActiveModal);

  dossierData = model<DossierMedizinischSaveJson>();

  form = new KinderImmunitaetForm(inject(NonNullableFormBuilder));

  protected readonly DetailMode = DetailMode;

  constructor() {
    effect(() => {
      const data = this.dossierData();
      this.form.reset(
        {
          immunitaetMasern: isNotNullish(data) ? data.immunitaetMasern : false,
          immunitaetVarizellen: isNotNullish(data) ? data.immunitaetVarizellen : false,
        },
        DetailMode.EDIT);
    });
  }

  abbrechen(): void {
    this.activeModal.dismiss('cancel');
  }

  async speichern(): Promise<void> {
    if (await this.form.validate()) {
      this.activeModal.close(this.form.valueOnValid());
    }
  }
}
