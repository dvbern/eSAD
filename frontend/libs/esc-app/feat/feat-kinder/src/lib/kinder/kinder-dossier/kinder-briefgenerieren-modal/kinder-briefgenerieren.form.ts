import {NonNullableFormBuilder, Validators} from '@angular/forms';
import {BriefvorlageJson, BriefvorlageType, KindAnlassSummaryJson} from '@dvb/data-access-backend';
import {BackendEntityId} from '@dvb/data-access-types';
import {AppValidators, BaseForm} from '@dvb/util-angular';
import {ensureProps, requireFilledIn} from '@dvb/util-shared';

export interface KinderBriefgenerierenJson {
  vorlage: BriefvorlageJson,
  einladung: KindAnlassSummaryJson | undefined,
  arztId: BackendEntityId | undefined
}

function makeForm(fb: NonNullableFormBuilder) {
  const group = fb.group({
    vorlage: fb.control<BriefvorlageJson | undefined>(undefined, [Validators.required]),
    einladung: fb.control<KindAnlassSummaryJson | undefined>(undefined),
    arztId: fb.control<BackendEntityId | undefined>(undefined),
  });

  group.controls.einladung.addValidators(AppValidators.requiredIfValidator(
    () => group.controls.vorlage.value?.data?.type === BriefvorlageType.ANLASS));

  return group;
}

export class KinderBriefgenerierenForm extends BaseForm<ReturnType<typeof makeForm>, KinderBriefgenerierenJson> {

  constructor(fb: NonNullableFormBuilder) {
    super(() => makeForm(fb));
  }

  public valueOnValid(): KinderBriefgenerierenJson {
    const value = this.group.getRawValue();
    return ensureProps<KinderBriefgenerierenJson>({
      vorlage: requireFilledIn(value.vorlage),
      einladung: value.einladung,
      arztId: value.arztId,
    });
  }

}
