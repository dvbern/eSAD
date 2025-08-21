import {ChangeDetectionStrategy, Component, DestroyRef, effect, inject, model} from '@angular/core';
import {NonNullableFormBuilder, ReactiveFormsModule} from '@angular/forms';
import {DossierSaveJson} from '@dvb/data-access-backend';
import {ModalDialogComponent} from '@dvb/feat-shared';
import {
  CheckboxDirective,
  SaveCancelButtonsComponent,
  StandaloneControlComponent,
  TextareaDirective,
} from '@dvb/ui-components';
import {DetailMode} from '@dvb/util-angular';
import {TranslocoPipe} from '@jsverse/transloco';
import {NgbActiveModal} from '@ng-bootstrap/ng-bootstrap';
import {KinderZusatzinformationenForm, KinderZusatzinformationenJson} from './kinder-zusatzinformationen.form';

@Component({
    selector: 'feat-kinder-detail-dossier-zuusatzinformationen-modal',
    templateUrl: './kinder-zusatzinformationen-modal.component.html',
    styleUrl: './kinder-zusatzinformationen-modal.component.scss',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [
        ModalDialogComponent,
        TranslocoPipe,
        ReactiveFormsModule,
        StandaloneControlComponent,
        SaveCancelButtonsComponent,
        CheckboxDirective,
        TextareaDirective,
    ]
})
export class KinderZusatzinformationenModalComponent {
  readonly activeModal = inject(NgbActiveModal);

  dossierData = model<DossierSaveJson>();

  form = new KinderZusatzinformationenForm({fb: inject(NonNullableFormBuilder), destroyRef: inject(DestroyRef)});

  protected readonly DetailMode = DetailMode;

  constructor() {
    effect(() => {
      const data = this.dossierData();
      if (data) {
        const zusatzinformationen: KinderZusatzinformationenJson = {
          kinderschutzfall: data.kinderschutzfall,
          keineAuskunftAnDrittpersonen: data.keineAuskunftAnDrittpersonen,
          sansPapier: data.sansPapier,
          andereZusatzinformation: data.andereZusatzinformation,
          andereZusatzInformationText: data.andereZusatzInformationText,
        };
        this.form.reset(
          zusatzinformationen,
          DetailMode.EDIT);
      } else {
        this.form.reset(undefined, DetailMode.EDIT,);
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
