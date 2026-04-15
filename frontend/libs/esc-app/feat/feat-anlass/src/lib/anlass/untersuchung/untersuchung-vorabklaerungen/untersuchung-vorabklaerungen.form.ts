import {NonNullableFormBuilder} from "@angular/forms";
import {globalConstants, ValueEnhancedForm} from "@dvb/feat-shared";
import {DestroyRef} from "@angular/core";
import {BaseForm} from "@dvb/util-angular";
import {ensurePropsFlat, Nullish, requireValue} from "@dvb/util-shared";
import {AnlassConfigJson, UntersuchungVorabklaerungenSaveJson,} from "@dvb/data-access-backend";

type Config = {
  fb: NonNullableFormBuilder;
  fragebogenStatusForm: ValueEnhancedForm;
  einverstaendnisImpfungenForm: ValueEnhancedForm;
  chronischeKrankheitenForm: ValueEnhancedForm;
  destroyRef: DestroyRef;
};

function makeForm(config: Config) {
  const fb = config.fb;
  return fb.group({
    fragebogenStatus: config.fragebogenStatusForm.group,
    einverstaendnisImpfungen: config.einverstaendnisImpfungenForm.group,
    chronischeKrankheiten: config.chronischeKrankheitenForm.group,
    version: fb.control<number | Nullish>({value: undefined, disabled: true}),
  });
}

export class UntersuchungVorabklaerungenForm extends BaseForm<ReturnType<typeof makeForm>, UntersuchungVorabklaerungenSaveJson> {
  fragebogenStatusForm: ValueEnhancedForm;
  einverstaendnisImpfungenForm: ValueEnhancedForm;
  chronischeKrankheitenForm: ValueEnhancedForm;

  constructor(protected config: Config) {
    super(() => makeForm(config));
    this.fragebogenStatusForm = config.fragebogenStatusForm;
    this.einverstaendnisImpfungenForm = config.einverstaendnisImpfungenForm;
    this.chronischeKrankheitenForm = config.chronischeKrankheitenForm;
  }

  resetForm(value?: UntersuchungVorabklaerungenSaveJson | undefined): void {
    super.reset(value);
    this.fragebogenStatusForm.resetForm(value?.fragebogenStatus);
    this.einverstaendnisImpfungenForm.resetForm(value?.einverstaendnisImpfungen);
    this.chronischeKrankheitenForm.resetForm(value?.chronischeKrankheiten);
  }

  public override setDisabled(disabled: boolean): void {
    super.setDisabled(disabled);
    this.fragebogenStatusForm.setDisabled(disabled);
    this.einverstaendnisImpfungenForm.setDisabled(disabled);
    this.chronischeKrankheitenForm.setDisabled(disabled);
  }

  public override valueOnValid(): UntersuchungVorabklaerungenSaveJson {
    const value = requireValue(this.group.getRawValue());
    return ensurePropsFlat<UntersuchungVorabklaerungenSaveJson>({
      fragebogenStatus: this.fragebogenStatusForm.valueOnValid(),
      einverstaendnisImpfungen: this.einverstaendnisImpfungenForm.valueOnValid(),
      chronischeKrankheiten: this.chronischeKrankheitenForm.valueOnValid(),
      version: value.version ?? globalConstants.entityPrePersistVersion,
    });
  }

  setValidatorsForAnlassConfig(config: AnlassConfigJson, nichtGeprueftGrundEnabled: boolean): void {
    this.fragebogenStatusForm.setValidatorsForAnlassConfig(
      config.data.vorabklaerungenFragebogenStatus,
      nichtGeprueftGrundEnabled,
    );
    this.einverstaendnisImpfungenForm.setValidatorsForAnlassConfig(
      config.data.vorabklaerungenEinverstaendnisImpfungen,
      nichtGeprueftGrundEnabled,
    );
    this.chronischeKrankheitenForm.setValidatorsForAnlassConfig(
      config.data.vorabklaerungenChronischeKrankheiten,
      nichtGeprueftGrundEnabled,
    );
  }
}
