
import {ChangeDetectionStrategy, Component, DestroyRef, inject} from '@angular/core';
import {FormsModule, NonNullableFormBuilder, ReactiveFormsModule} from '@angular/forms';
import {SpssReportType} from '@dvb/data-access-backend';
import {DocumentQueueStore} from '@dvb/feat-shared';
import {
  ButtonComponent,
  CardComponent,
  CheckboxDirective,
  FormLayoutComponent,
  HeadingDirective,
  StandaloneControlComponent,
} from '@dvb/ui-components';
import {DetailMode} from '@dvb/util-angular';
import {TranslocoPipe} from '@jsverse/transloco';
import {SpssForm} from './spss.form';

@Component({
  selector: 'feat-spss-form',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    CardComponent,
    CheckboxDirective,
    FormLayoutComponent,
    FormsModule,
    HeadingDirective,
    StandaloneControlComponent,
    TranslocoPipe,
    ReactiveFormsModule,
    ButtonComponent
],
  templateUrl: './spss-form.component.html',
})
export class SpssFormComponent {

  readonly documentQueueStore = inject(DocumentQueueStore);
  readonly spssReports = Object.values(SpssReportType);

  public form = new SpssForm({
    fb: inject(NonNullableFormBuilder),
    destroyRef: inject(DestroyRef),
  });

  async submitIfValid() {
    if (await this.form.validate()) {
      const documentQueueListSaveJson = this.form.valueOnValid();

      this.documentQueueStore.createMultipleSpssDocumentQueue({
        documentQueueListSaveJson,
        onSuccess: () => {
          this.form.reset();
          this.documentQueueStore.loadDocumentQueueList();
        },
      });
    }
  }

  protected readonly DetailMode = DetailMode;
}
