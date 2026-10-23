import {ChangeDetectionStrategy, Component, inject} from '@angular/core';
import {NonNullableFormBuilder, ReactiveFormsModule} from '@angular/forms';
import {KinderdossierPrintVariant} from '@dvb/data-access-backend';

import {ModalDialogComponent} from '@dvb/feat-shared';
import {ButtonComponent, RadioDirective, SingleRadioWrapperComponent} from '@dvb/ui-components';
import {TranslocoPipe} from '@jsverse/transloco';
import {NgbActiveModal} from '@ng-bootstrap/ng-bootstrap';
import {KinderDossierExportForm} from './kinder-dossier-export.form';

@Component({
  selector: 'feat-kinder-detail-dossier-zuusatzinformationen-modal',
  templateUrl: './kinder-dossier-export-modal.component.html',
  styleUrl: './kinder-dossier-export-modal.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    ModalDialogComponent,
    TranslocoPipe,
    ReactiveFormsModule,
    RadioDirective,
    SingleRadioWrapperComponent,
    ButtonComponent,
  ],
})
export class KinderDossierExportModalComponent {
  readonly activeModal = inject(NgbActiveModal);

  form = new KinderDossierExportForm(inject(NonNullableFormBuilder));

  protected readonly exportVariants = Object.values(KinderdossierPrintVariant);

  abbrechen(): void {
    this.activeModal.dismiss('cancel');
  }

  async speichern(): Promise<void> {
    if (await this.form.validate()) {
      this.activeModal.close(this.form.valueOnValid());
    }
  }
}
