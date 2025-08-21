import {ChangeDetectionStrategy, Component, effect, inject, model} from '@angular/core';
import {FormsModule, NonNullableFormBuilder, ReactiveFormsModule} from '@angular/forms';
import {NgbActiveModal} from '@ng-bootstrap/ng-bootstrap';
import {DossierSaveJson} from '@dvb/data-access-backend';
import {KinderNachkontrolleForm} from './kinder-nachkontrolle.form';
import {DetailMode} from '@dvb/util-angular';
import {CheckboxDirective, SaveCancelButtonsComponent, StandaloneControlComponent} from '@dvb/ui-components';
import {ModalDialogComponent} from '@dvb/feat-shared';
import {TranslocoPipe} from '@jsverse/transloco';

@Component({
    selector: 'feat-kinder-detail-dossier-nachkontrolle-modal',
    templateUrl: './kinder-nachkontrolle-modal.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [
        FormsModule,
        ReactiveFormsModule,
        CheckboxDirective,
        ModalDialogComponent,
        SaveCancelButtonsComponent,
        StandaloneControlComponent,
        TranslocoPipe,
    ]
})
export class KinderNachkontrolleModalComponent {

  readonly activeModal = inject(NgbActiveModal);

  dossierData = model<DossierSaveJson>();

  form = new KinderNachkontrolleForm(inject(NonNullableFormBuilder));

  protected readonly DetailMode = DetailMode;

  constructor() {
    effect(() => {
      const data = this.dossierData();
      if (data?.nachkontrolle) {
        this.form.reset(data.nachkontrolle.data.anlassFlags, DetailMode.EDIT);
      }
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
