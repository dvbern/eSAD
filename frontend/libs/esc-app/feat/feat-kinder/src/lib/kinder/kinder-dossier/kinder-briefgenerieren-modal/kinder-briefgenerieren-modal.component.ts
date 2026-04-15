import {ChangeDetectionStrategy, Component, inject, model} from '@angular/core';
import {FormsModule, NonNullableFormBuilder, ReactiveFormsModule} from '@angular/forms';
import {BriefvorlageType, KindAnlassSummaryJson, KindJson} from '@dvb/data-access-backend';
import {AerzteStore, BriefvorlagenStore, TenantSettingsStore} from '@dvb/feat-shared';
import {ModalDialogComponent} from '@dvb/feat-shared';
import {
  FormControlComponent,
  FormLayoutComponent,
  NgSelectDirective,
  SaveCancelButtonsComponent,
} from '@dvb/ui-components';
import {TranslocoPipe} from '@jsverse/transloco';
import {NgbActiveModal} from '@ng-bootstrap/ng-bootstrap';
import {NgOptionComponent, NgSelectComponent} from '@ng-select/ng-select';
import {KinderBriefgenerierenForm} from './kinder-briefgenerieren.form';

@Component({
  selector: 'feat-kinder-detail-dossier-briefgenerieren-modal',
  templateUrl: './kinder-briefgenerieren-modal.html',
  styles: ':host { display:block;}',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    FormsModule,
    ReactiveFormsModule,
    ModalDialogComponent,
    TranslocoPipe,
    NgSelectComponent,
    NgOptionComponent,
    NgSelectDirective,
    FormControlComponent,
    FormLayoutComponent,
    SaveCancelButtonsComponent,
  ],
})
export class KinderBriefgenerierenModalComponent {
  protected readonly BriefvorlageType = BriefvorlageType;

  readonly activeModal = inject(NgbActiveModal);
  readonly briefvorlagenStore = inject(BriefvorlagenStore);
  readonly aerzteStore = inject(AerzteStore);
  readonly tenantSettingsStore = inject(TenantSettingsStore);

  kindData = model<KindJson>();
  anlassList = model<KindAnlassSummaryJson[]>([]);

  form = new KinderBriefgenerierenForm(inject(NonNullableFormBuilder));

  async submit(): Promise<void> {
    if (await this.form.validate()) {
      this.activeModal.close(this.form.valueOnValid());
    }
  }

  abbrechen(): void {
    this.activeModal.dismiss('cancel');
  }
}
