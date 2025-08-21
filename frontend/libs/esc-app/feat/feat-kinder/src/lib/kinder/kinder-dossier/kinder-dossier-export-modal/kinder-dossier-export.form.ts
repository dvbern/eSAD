import {NonNullableFormBuilder, Validators} from '@angular/forms';
import {KinderdossierPrintVariant} from '@dvb/data-access-backend';
import {BaseForm} from '@dvb/util-angular';
import {ensureProps, requireFilledIn, requireValue} from '@dvb/util-shared';

export type KinderDossierExportFormOutputType = {
  kinderdossierPrintVariant: KinderdossierPrintVariant;
};

function makeForm(fb: NonNullableFormBuilder) {
  const group = fb.group({
    kinderdossierPrintVariant: fb.control<KinderdossierPrintVariant | undefined>(undefined, [Validators.required]),
  });

  return group;
}

export class KinderDossierExportForm extends BaseForm<ReturnType<typeof makeForm>, KinderDossierExportFormOutputType> {
  constructor(fb: NonNullableFormBuilder) {
    super(() => makeForm(fb));
  }

  public override valueOnValid(): KinderDossierExportFormOutputType {
    const value = requireValue(this.group.getRawValue());
    const result = ensureProps<KinderDossierExportFormOutputType>({
      kinderdossierPrintVariant: requireFilledIn(value.kinderdossierPrintVariant),
    });
    return result;
  }
}
