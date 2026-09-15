import {DestroyRef} from '@angular/core';
import {NonNullableFormBuilder, Validators} from '@angular/forms';
import {
  AnlassConfigJson,
  ImpfausweisGrundUnvollstaendig,
  ImpfausweisVollstaendig,
  RequirementSetting,
  UntersuchungImpfungenNurUnvollstaendigeSaveJson
} from '@dvb/data-access-backend';
import {globalConstants} from '@dvb/feat-shared';
import {AppValidators, BaseForm} from '@dvb/util-angular';
import {ensurePropsFlat, Nullish, requireValue} from '@dvb/util-shared';

type Config = {
  fb: NonNullableFormBuilder;
  destroyRef: DestroyRef;
};

function makeForm(config: Config) {
  const fb = config.fb;
  return fb.group({
    impfausweisVollstaendig: fb.control<ImpfausweisVollstaendig | undefined>(undefined),
    impfausweisGrundUnvollstaendig: fb.control<ImpfausweisGrundUnvollstaendig | undefined>(undefined),
    version: fb.control<number | Nullish>({ value: undefined, disabled: true }),
  });
}

export class UntersuchungImpfungenNurUnvollstaendigeForm extends BaseForm<
  ReturnType<typeof makeForm>,
  UntersuchungImpfungenNurUnvollstaendigeSaveJson
> {

  constructor(protected config: Config) {
    super(() => makeForm(config));
  }

  setValidatorsForAnlassConfig(config: AnlassConfigJson): void {
    // Validationen anhand der AnlassConfig setzen
    if (config.data.impfungenNurUnvollstaendige === RequirementSetting.REQUIRED) {
      this.group.controls.impfausweisVollstaendig.addValidators(Validators.required);
      this.group.controls.impfausweisGrundUnvollstaendig.addValidators(AppValidators.requiredIfValidator(
        () => this.group.controls.impfausweisVollstaendig.value === ImpfausweisVollstaendig.NEIN));
    }
  }

  public override valueOnValid(): UntersuchungImpfungenNurUnvollstaendigeSaveJson {
    const value = requireValue(this.group.getRawValue());
    return ensurePropsFlat<UntersuchungImpfungenNurUnvollstaendigeSaveJson>({
      impfausweisVollstaendig: requireValue(value.impfausweisVollstaendig),
      impfausweisGrundUnvollstaendig: value.impfausweisGrundUnvollstaendig,
      version: value.version ?? globalConstants.entityPrePersistVersion,
    });
  }
}
