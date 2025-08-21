import {inject} from '@angular/core';
import {NonNullableFormBuilder, Validators} from '@angular/forms';
import {BaseForm} from '@dvb/util-angular';
import {ensureProps, requireValue} from '@dvb/util-shared';

function makeForm() {
  const fb = inject(NonNullableFormBuilder);

  const group = fb.group({
    fileIdInBucket: fb.control<string | undefined>(undefined, [Validators.required]),
  });

  return group;
}

export type FileDownloadOutputType = {
  fileIdInBucket: string;
}

export class S3FileDownloadForm extends BaseForm<ReturnType<typeof makeForm>, FileDownloadOutputType> {
  constructor() {
    super(() => makeForm());
  }

  public override valueOnValid(): FileDownloadOutputType {
    const value = requireValue(this.group.getRawValue());
    const result = ensureProps<FileDownloadOutputType>({
      fileIdInBucket: requireValue(value.fileIdInBucket),
    });
    return result;
  }
}
