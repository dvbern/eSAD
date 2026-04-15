import {DestroyRef} from '@angular/core';
import {NonNullableFormBuilder, Validators} from '@angular/forms';
import {AnlassConfigJson, UntersuchungImpfungenDataMinimalSaveJson} from '@dvb/data-access-backend';
import {globalConstants, ValueEnhancedForm} from '@dvb/feat-shared';
import {AppValidators, BaseForm} from '@dvb/util-angular';
import {ensurePropsFlat, Nullish, requireValue} from '@dvb/util-shared';

type Config = {
  fb: NonNullableFormBuilder;
  impfausweisGeprueftForm: ValueEnhancedForm;
  immunsuppressionForm: ValueEnhancedForm;
  masernImpfstatusForm: ValueEnhancedForm;
  hpvImpfstatusForm: ValueEnhancedForm;
  impfempfehlungForm: ValueEnhancedForm;
  impfungDurchgefuehrtForm: ValueEnhancedForm;
  destroyRef: DestroyRef;
};

function makeForm(config: Config) {
  const fb = config.fb;
  return fb.group({
    impfausweisGeprueft: config.impfausweisGeprueftForm.group,
    immunsuppression: config.immunsuppressionForm.group,
    masernImpfstatus: config.masernImpfstatusForm.group,
    masernAnzahlImpfungen: fb.control<number | undefined>(undefined, [AppValidators.nonNegativNumber()]),
    hpvImpfstatus: config.hpvImpfstatusForm.group,
    hpvAnzahlImpfungen: fb.control<number | undefined>(undefined, [AppValidators.nonNegativNumber()]),
    impfempfehlung: config.impfempfehlungForm.group,
    impfungDurchgefuehrt: config.impfungDurchgefuehrtForm.group,
    version: fb.control<number | Nullish>({ value: undefined, disabled: true }),
  });
}

export class UntersuchungImpfungenDataMinimalForm extends BaseForm<
  ReturnType<typeof makeForm>,
  UntersuchungImpfungenDataMinimalSaveJson
> {
  readonly impfausweisGeprueftForm: ValueEnhancedForm;
  readonly immunsuppressionForm: ValueEnhancedForm;
  readonly masernImpfstatusForm: ValueEnhancedForm;
  readonly hpvImpfstatusForm: ValueEnhancedForm;
  readonly impfempfehlungForm: ValueEnhancedForm;
  readonly impfungDurchgefuehrtForm: ValueEnhancedForm;

  constructor(protected config: Config) {
    super(() => makeForm(config));
    this.impfausweisGeprueftForm = config.impfausweisGeprueftForm;
    this.immunsuppressionForm = config.immunsuppressionForm;
    this.masernImpfstatusForm = config.masernImpfstatusForm;
    this.hpvImpfstatusForm = config.hpvImpfstatusForm;
    this.impfempfehlungForm = config.impfempfehlungForm;
    this.impfungDurchgefuehrtForm = config.impfungDurchgefuehrtForm;
  }

  resetForm(value: UntersuchungImpfungenDataMinimalSaveJson | undefined): void {
    super.reset(value);
    this.impfausweisGeprueftForm.resetForm(value?.impfausweisGeprueft);
    this.immunsuppressionForm.resetForm(value?.immunsuppression);
    this.masernImpfstatusForm.resetForm(value?.masernImpfstatus);
    this.hpvImpfstatusForm.resetForm(value?.hpvImpfstatus);
    this.impfempfehlungForm.resetForm(value?.impfempfehlung);
    this.impfungDurchgefuehrtForm.resetForm(value?.impfungDurchgefuehrt);
  }

  setValidatorsForAnlassConfig(config: AnlassConfigJson, nichtGeprueftGrundEnabled: boolean): void {
    this.impfausweisGeprueftForm.setValidatorsForAnlassConfig(
      config.data.impfungenDataMinimalImpfausweisGeprueft,
      nichtGeprueftGrundEnabled,
    );
    this.immunsuppressionForm.setValidatorsForAnlassConfig(
      config.data.impfungenDataMinimalImmunsuppression,
      nichtGeprueftGrundEnabled,
    );
    this.masernImpfstatusForm.setValidatorsForAnlassConfig(
      config.data.impfungenDataMinimalMasernImpfstatus,
      nichtGeprueftGrundEnabled,
    );
    if (config.data.impfungenDataMinimalMasernAnzahlImpfungen === 'REQUIRED') {
      this.group.controls.masernAnzahlImpfungen.addValidators(Validators.required);
    }
    this.hpvImpfstatusForm.setValidatorsForAnlassConfig(
      config.data.impfungenDataMinimalHpvImpfstatus,
      nichtGeprueftGrundEnabled,
    );
    if (config.data.impfungenDataMinimalHpvAnzahlImpfungen === 'REQUIRED') {
      this.group.controls.hpvAnzahlImpfungen.addValidators(Validators.required);
    }
    this.impfempfehlungForm.setValidatorsForAnlassConfig(
      config.data.impfungenDataMinimalImpfempfehlung,
      nichtGeprueftGrundEnabled,
    );
    this.impfungDurchgefuehrtForm.setValidatorsForAnlassConfig(
      config.data.impfungenDataMinimalImpfungDurchgefuehrt,
      nichtGeprueftGrundEnabled,
    );
  }

  public override valueOnValid(): UntersuchungImpfungenDataMinimalSaveJson {
    const value = requireValue(this.group.getRawValue());
    return ensurePropsFlat<UntersuchungImpfungenDataMinimalSaveJson>({
      impfausweisGeprueft: this.impfausweisGeprueftForm.valueOnValid(),
      immunsuppression: this.immunsuppressionForm.valueOnValid(),
      masernImpfstatus: this.masernImpfstatusForm.valueOnValid(),
      masernAnzahlImpfungen: value.masernAnzahlImpfungen,
      hpvImpfstatus: this.hpvImpfstatusForm.valueOnValid(),
      hpvAnzahlImpfungen: value.hpvAnzahlImpfungen,
      impfempfehlung: this.impfempfehlungForm.valueOnValid(),
      impfungDurchgefuehrt: this.impfungDurchgefuehrtForm.valueOnValid(),
      version: value.version ?? globalConstants.entityPrePersistVersion,
    });
  }
}
