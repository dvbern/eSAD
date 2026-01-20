import { DestroyRef } from '@angular/core';
import { NonNullableFormBuilder } from '@angular/forms';
import {
  DocumentQueueListSaveJson,
  DocumentQueueSpssSaveJson,
  FileTyp,
  SpssReportType,
} from '@dvb/data-access-backend';
import { BaseForm } from '@dvb/util-angular';
import { requireValue } from '@dvb/util-shared';

type Config = {
  fb: NonNullableFormBuilder;
  destroyRef: DestroyRef;
};

function makeForm(config: Config) {
  const fb = config.fb;
  return fb.group({
    KINDERGARTEN: fb.control(false),
    KLASSE_4: fb.control(false),
    KLASSE_8: fb.control(false),
    KLASSE_9: fb.control(false),
    FREMDSPRACHEN: fb.control(false),
    IMPF_ANLAESSE: fb.control(false),
  });
}

export class SpssForm extends BaseForm<ReturnType<typeof makeForm>, DocumentQueueListSaveJson> {
  constructor(protected config: Config) {
    super(() => makeForm(config));
  }

  override valueOnValid(): DocumentQueueListSaveJson {
    const value = requireValue(this.group.getRawValue());

    const documentQueueSaveJson: DocumentQueueSpssSaveJson[] = [];
    for (const spssType of Object.values(SpssReportType)) {
      if (value[spssType]) {
        documentQueueSaveJson.push({
          type: FileTyp.SPSS_REPORT,
          documentQueueParams: {
            spssReportType: spssType,
          },
        });
      }
    }

    return {
      list: documentQueueSaveJson,
    };
  }
}
