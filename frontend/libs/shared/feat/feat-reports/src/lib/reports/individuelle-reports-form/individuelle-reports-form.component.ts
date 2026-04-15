import {ChangeDetectionStrategy, Component, DestroyRef, inject} from "@angular/core";
import {
  ButtonComponent,
  CardComponent,
  CheckboxDirective,
  FormControlComponent,
  FormErrorMessageComponent,
  FormLayoutComponent,
  HeadingDirective,
  StandaloneControlComponent,
  TextInputDirective
} from "@dvb/ui-components";
import {FormsModule, NonNullableFormBuilder, ReactiveFormsModule} from "@angular/forms";
import {TranslocoPipe} from "@jsverse/transloco";
import {DocumentQueueStore} from "@dvb/feat-shared";
import {CsvReportType} from "@dvb/data-access-backend";
import {IndividuelleReportsForm} from "./individuelle-reports.form";
import {DetailMode} from "@dvb/util-angular";

@Component({
  selector: 'feat-individuelle-reports-form',
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
    ButtonComponent,
    FormControlComponent,
    TextInputDirective,
    FormErrorMessageComponent,
  ],
  templateUrl: './individuelle-reports-form.component.html',
})
export class IndividuelleReportsFormComponent {
  protected readonly DetailMode = DetailMode;
  readonly individuelleReports = Object.values(CsvReportType);

  readonly documentQueueStore = inject(DocumentQueueStore);

  public form = new IndividuelleReportsForm({
    fb: inject(NonNullableFormBuilder),
    destroyRef: inject(DestroyRef),
  });

  async submitIfValid() {
    if (await this.form.validate()) {
      const documentQueueCsvListSaveJson = this.form.valueOnValid();

      this.documentQueueStore.createMultipleCsvDocumentQueue({
        documentQueueCsvListSaveJson,
        onSuccess: () => {
          this.form.reset();
          this.documentQueueStore.loadDocumentQueueListForCurrentBenutzer();
        },
      });
    }
  }
}
