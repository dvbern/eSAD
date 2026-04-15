import {NonNullableFormBuilder, Validators} from "@angular/forms";
import {globalConstants, ValueEnhancedForm} from "@dvb/feat-shared";
import {DestroyRef} from "@angular/core";
import {AppValidators, BaseForm} from "@dvb/util-angular";
import {ensurePropsFlat, Nullish, requireValue} from "@dvb/util-shared";
import {
  AnlassConfigJson,
  RequirementSetting,
  UntersuchungAbschlussSaveJson,
} from "@dvb/data-access-backend";

type Config = {
  fb: NonNullableFormBuilder;
  ueberweisungValueEnhancedForm: ValueEnhancedForm;
  aerztlicheValidierungValueEnhancedForm: ValueEnhancedForm;
  beratungValueEnhancedForm: ValueEnhancedForm;
  destroyRef: DestroyRef;
};

function makeForm(config: Config) {
  const fb = config.fb;
  return fb.group({
    ueberweisungErfolgt: config.ueberweisungValueEnhancedForm.group,
    aerztlicheValidierungErforderlich: config.aerztlicheValidierungValueEnhancedForm.group,
    beratungDurchgefuehrt: config.beratungValueEnhancedForm.group,
    beurteilungEmpfohleneMassnahmen: fb.control<string>('', AppValidators.textareaMaxLength2000()),
    version: fb.control<number | Nullish>({value: undefined, disabled: true}),
  });
}

export class UntersuchungAbschlussForm extends BaseForm<ReturnType<typeof makeForm>, UntersuchungAbschlussSaveJson> {
  ueberweisungValueEnhancedForm: ValueEnhancedForm;
  aerztlicheValidierungValueEnhancedForm: ValueEnhancedForm;
  beratungValueEnhancedForm: ValueEnhancedForm;

  constructor(protected config: Config) {
    super(() => makeForm(config));
    this.ueberweisungValueEnhancedForm = config.ueberweisungValueEnhancedForm;
    this.aerztlicheValidierungValueEnhancedForm = config.aerztlicheValidierungValueEnhancedForm;
    this.beratungValueEnhancedForm = config.beratungValueEnhancedForm;
  }

  resetForm(value?: UntersuchungAbschlussSaveJson | undefined): void {
    super.reset(value);
    this.ueberweisungValueEnhancedForm.resetForm(value?.ueberweisungErfolgt);
    this.aerztlicheValidierungValueEnhancedForm.resetForm(value?.aerztlicheValidierungErforderlich);
    this.beratungValueEnhancedForm.resetForm(value?.beratungDurchgefuehrt);
  }

  public override setDisabled(disabled: boolean): void {
    super.setDisabled(disabled);
    this.ueberweisungValueEnhancedForm.setDisabled(disabled);
    this.aerztlicheValidierungValueEnhancedForm.setDisabled(disabled);
    this.beratungValueEnhancedForm.setDisabled(disabled);
  }

  public override valueOnValid(): UntersuchungAbschlussSaveJson {
    const value = requireValue(this.group.getRawValue());
    return ensurePropsFlat<UntersuchungAbschlussSaveJson>({
      ueberweisungErfolgt: this.ueberweisungValueEnhancedForm.valueOnValid(),
      aerztlicheValidierungErforderlich: this.aerztlicheValidierungValueEnhancedForm.valueOnValid(),
      beratungDurchgefuehrt: this.beratungValueEnhancedForm.valueOnValid(),
      beurteilungEmpfohleneMassnahmen: value.beurteilungEmpfohleneMassnahmen,
      version: value.version ?? globalConstants.entityPrePersistVersion,
    });
  }

  setValidatorsForAnlassConfig(config: AnlassConfigJson, nichtGeprueftGrundEnabled: boolean): void {
    this.ueberweisungValueEnhancedForm.setValidatorsForAnlassConfig(
      config.data.abschlussUeberweisungErfolgt,
      nichtGeprueftGrundEnabled,
    );

    this.aerztlicheValidierungValueEnhancedForm.setValidatorsForAnlassConfig(
      config.data.abschlussAerztlicheValidierungErforderlich,
      nichtGeprueftGrundEnabled,
    );

    this.beratungValueEnhancedForm.setValidatorsForAnlassConfig(
      config.data.abschlussBeratungDurchgefuehrt,
      nichtGeprueftGrundEnabled,
    );

    if (config.data.abschlussBeurteilungEmpfohleneMassnahmen === RequirementSetting.REQUIRED) {
      this.group.controls.beurteilungEmpfohleneMassnahmen.addValidators(Validators.required);
    }
  }
}
