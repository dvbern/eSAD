import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';
import { TranslocoPipe } from '@jsverse/transloco';
import { ButtonComponent } from '../../buttons/button';

@Component({
    selector: 'svui-file-upload-button',
    templateUrl: './file-upload-button.component.html',
    styleUrl: './file-upload-button.component.scss',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [TranslocoPipe, ButtonComponent]
})
export class FileUploadButtonComponent {
  readonly maxSize = input<number>();
  readonly fileUploaded = output<File>();
  readonly accepted = input<string>();
  readonly labelOverride = input<string>('');

  onFileSelected(event: Event): void {
    const target = event?.target as HTMLInputElement;
    const files = target.files;

    if (files && files.length > 0) {
      this.fileUploaded.emit(files[0]);
      // Reset the value of the form, allowing a repeated upload of the same file.
      target.value = '';
    }
  }
}
