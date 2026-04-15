import {DestroyRef} from '@angular/core';
import {NonNullableFormBuilder, Validators} from '@angular/forms';
import {
  CsvReportType,
  DocumentQueueCsvListSaveJson,
  DocumentQueueCsvSaveJson,
  FileTyp,
} from '@dvb/data-access-backend';
import {ISODateString} from '@dvb/data-access-types';
import {AppValidators, BaseForm} from '@dvb/util-angular';
import {requireFilledIn, requireValue} from '@dvb/util-shared';

type Config = {
  fb: NonNullableFormBuilder,
  destroyRef: DestroyRef
}

function makeForm(config: Config) {
  const fb = config.fb;
  return fb.group({
      datumVon: fb.control<ISODateString | undefined>(undefined, [Validators.required]),
      datumBis: fb.control<ISODateString | undefined>(undefined, [Validators.required]),
      SCHULUNTERSUCHUNG: fb.control(false),
      IMPFSTOFFE: fb.control(false),
    },
    {
      validators: [
        AppValidators.datumBisIsAtleastDatumVonOrAfter('datumVon', 'datumBis'),
        AppValidators.dateRangeIsMaximumNumberOfYears('datumVon', 'datumBis', 5),
      ],
    });
}

export class IndividuelleReportsForm extends BaseForm<ReturnType<typeof makeForm>, DocumentQueueCsvListSaveJson> {

  constructor(protected config: Config) {
    super(() => makeForm(config));
  }

  override valueOnValid(): DocumentQueueCsvListSaveJson {
    const value = requireValue(this.group.getRawValue());
    const documentQueueCsvSaveJson: DocumentQueueCsvSaveJson[] = [];
    for (const csvType of Object.values(CsvReportType)) {
      if (value[csvType]) {
        documentQueueCsvSaveJson.push({
          type: FileTyp.CSV_REPORT,
          datumVon: requireFilledIn(value.datumVon),
          datumBis: requireFilledIn(value.datumBis),
          documentQueueCsvParams: {
            csvReportType: csvType,
          },
        })
      }
    }

    return {
      list: documentQueueCsvSaveJson,
    };
  }
}
