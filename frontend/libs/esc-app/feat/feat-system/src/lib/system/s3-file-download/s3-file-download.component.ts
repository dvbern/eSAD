import {CommonModule} from '@angular/common';
import {ChangeDetectionStrategy, Component, inject} from '@angular/core';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {SystemAdminStore} from '@dvb/feat-shared';
import {Path} from '@dvb/feat-shared';
import {
  ButtonComponent,
  CardComponent,
  HeadingDirective,
  StandaloneControlComponent,
  TextInputDirective,
} from '@dvb/ui-components';
import {TranslocoPipe} from '@jsverse/transloco';
import {S3FileDownloadForm} from './s3-file-download-form';

@Component({
  selector: 'feat-system-s3-file-download',
  imports: [
    CommonModule, TranslocoPipe, ButtonComponent, CardComponent, HeadingDirective, FormsModule,
    StandaloneControlComponent, TextInputDirective, ReactiveFormsModule,
  ],
  templateUrl: './s3-file-download.component.html',
  styles: ':host { display:block;}',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class S3FileDownloadComponent {
  protected readonly Path = Path;
  readonly systemAdminStore = inject(SystemAdminStore);

  s3FileDownloadForm = new S3FileDownloadForm();

  async submit(): Promise<void> {
    if (await this.s3FileDownloadForm.validate()) {
      const payload = this.s3FileDownloadForm.valueOnValid();
      this.systemAdminStore.downloadFile({
        fileIdInBucket: payload.fileIdInBucket,
        downloadDirectly: true,
        onSuccess: () => {
          this.s3FileDownloadForm.reset();
        },
      });
    }
  }

}
