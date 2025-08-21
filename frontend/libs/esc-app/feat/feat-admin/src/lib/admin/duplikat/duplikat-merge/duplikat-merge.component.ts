import {ChangeDetectionStrategy, Component, effect, inject, model} from '@angular/core';
import {FormsModule, NonNullableFormBuilder, ReactiveFormsModule} from '@angular/forms';
import {DuplikatJson} from '@dvb/data-access-backend';
import {ModalDialogComponent} from '@dvb/feat-shared';
import {
  FormControlComponent,
  FormLayoutComponent,
  LayoutTwoColumnsComponent,
  NgSelectDirective,
  SaveCancelButtonsComponent,
} from '@dvb/ui-components';
import {DetailMode} from '@dvb/util-angular';
import {TranslocoPipe} from '@jsverse/transloco';
import {NgbActiveModal} from '@ng-bootstrap/ng-bootstrap';
import {NgOptionComponent, NgSelectComponent} from '@ng-select/ng-select';
import {DuplikatMergeKindInfoComponent} from '../duplikat-merge-kind-info/duplikat-merge-kind-info.component';
import {DuplikatMergeForm, DuplikatMergeJson} from './duplikat-merge.form';

@Component({
  selector: 'feat-duplikat-merge-modal',
  templateUrl: './duplikat-merge.component.html',
  styleUrl: './duplikat-merge.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    FormsModule,
    TranslocoPipe,
    NgOptionComponent,
    NgSelectComponent,
    NgSelectDirective,
    ReactiveFormsModule,
    FormControlComponent,
    FormLayoutComponent,
    ModalDialogComponent,
    SaveCancelButtonsComponent,
    LayoutTwoColumnsComponent,
    DuplikatMergeKindInfoComponent,
  ],
})
export class DuplikatMergeComponent {

  readonly activeModal = inject(NgbActiveModal);

  protected readonly DetailMode = DetailMode;

  form = new DuplikatMergeForm(inject(NonNullableFormBuilder));
  duplikatData = model<DuplikatJson>();

  constructor() {
    effect(() => {
      const data = this.duplikatData();
      if (data) {
        this.form.resetForm(data);
      }
    });
  }

  abbrechen(): void {
    this.activeModal.dismiss('cancel');
  }

  async speichern(): Promise<void> {
    if (await this.form.validate()) {
      const payload: DuplikatMergeJson = this.form.valueOnValid();
      this.activeModal.close(payload);
    }
  }
}
