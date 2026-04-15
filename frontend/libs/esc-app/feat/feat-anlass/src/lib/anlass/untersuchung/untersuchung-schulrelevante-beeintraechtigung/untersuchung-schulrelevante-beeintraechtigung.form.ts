import {NonNullableFormBuilder, Validators} from "@angular/forms";
import {DestroyRef} from "@angular/core";
import {
  AnlassConfigJson,
  RequirementSetting,
  UntersuchungSchulrelevanteBeeintraechtigungSaveJson,
} from "@dvb/data-access-backend";
import {AppValidators, BaseForm} from "@dvb/util-angular";
import {ensurePropsFlat, Nullish, requireValue, trimToUndefined} from "@dvb/util-shared";
import {globalConstants, ValueEnhancedForm} from "@dvb/feat-shared";

type Config = {
  fb: NonNullableFormBuilder;
  freiesGehenValueEnhancedForm: ValueEnhancedForm;
  ohneStuetzradVelofahrenValueEnhancedForm: ValueEnhancedForm;
  grafomotorikValueEnhancedForm: ValueEnhancedForm;
  feinmotorikValueEnhancedForm: ValueEnhancedForm;
  grobmotorikEinbeinhuepfenValueEnhancedForm: ValueEnhancedForm;
  gleichgewichtEinbeinstandValueEnhancedForm: ValueEnhancedForm;
  destroyRef: DestroyRef;
};

function makeForm(config: Config) {
  const fb = config.fb;
  return fb.group({
    freiesGehen: config.freiesGehenValueEnhancedForm.group,
    ohneStuetzradVelofahren: config.ohneStuetzradVelofahrenValueEnhancedForm.group,
    grafomotorik: config.grafomotorikValueEnhancedForm.group,
    feinmotorik: config.feinmotorikValueEnhancedForm.group,
    grobmotorikEinbeinhuepfen: config.grobmotorikEinbeinhuepfenValueEnhancedForm.group,
    gleichgewichtEinbeinstand: config.gleichgewichtEinbeinstandValueEnhancedForm.group,
    bemerkungen: fb.control<string>('', AppValidators.textareaMaxLength2000()),
    version: fb.control<number | Nullish>({value: undefined, disabled: true}),
  });
}

export class UntersuchungSchulrelevanteBeeintraechtigungForm
  extends BaseForm<ReturnType<typeof makeForm>, UntersuchungSchulrelevanteBeeintraechtigungSaveJson> {

  readonly freiesGehenSingleValueForm: ValueEnhancedForm;
  readonly ohneStuetzradVelofahrenSingleValueForm: ValueEnhancedForm;
  readonly grafomotorikSingleValueForm: ValueEnhancedForm;
  readonly feinmotorikSingleValueForm: ValueEnhancedForm;
  readonly grobmotorikEinbeinhuepfenSingleValueForm: ValueEnhancedForm;
  readonly gleichgewichtEinbeinstandSingleValueForm: ValueEnhancedForm;

  constructor(protected config: Config) {
    super(() => makeForm(config));
    this.freiesGehenSingleValueForm = config.freiesGehenValueEnhancedForm;
    this.ohneStuetzradVelofahrenSingleValueForm = config.ohneStuetzradVelofahrenValueEnhancedForm;
    this.grafomotorikSingleValueForm = config.grafomotorikValueEnhancedForm;
    this.feinmotorikSingleValueForm = config.feinmotorikValueEnhancedForm;
    this.grobmotorikEinbeinhuepfenSingleValueForm = config.grobmotorikEinbeinhuepfenValueEnhancedForm;
    this.gleichgewichtEinbeinstandSingleValueForm = config.gleichgewichtEinbeinstandValueEnhancedForm;
  }

  resetForm(value: UntersuchungSchulrelevanteBeeintraechtigungSaveJson | undefined): void {
    super.reset(value);
    this.freiesGehenSingleValueForm.resetForm(value?.freiesGehen);
    this.ohneStuetzradVelofahrenSingleValueForm.resetForm(value?.ohneStuetzradVelofahren);
    this.grafomotorikSingleValueForm.resetForm(value?.grafomotorik);
    this.feinmotorikSingleValueForm.resetForm(value?.feinmotorik);
    this.grobmotorikEinbeinhuepfenSingleValueForm.resetForm(value?.grobmotorikEinbeinhuepfen);
    this.gleichgewichtEinbeinstandSingleValueForm.resetForm(value?.gleichgewichtEinbeinstand);
  }

  public override valueOnValid(): UntersuchungSchulrelevanteBeeintraechtigungSaveJson {
    const value = requireValue(this.group.getRawValue());
    return ensurePropsFlat<UntersuchungSchulrelevanteBeeintraechtigungSaveJson>({
      freiesGehen: this.freiesGehenSingleValueForm.valueOnValid(),
      ohneStuetzradVelofahren: this.ohneStuetzradVelofahrenSingleValueForm.valueOnValid(),
      grafomotorik: this.grafomotorikSingleValueForm.valueOnValid(),
      feinmotorik: this.feinmotorikSingleValueForm.valueOnValid(),
      grobmotorikEinbeinhuepfen: this.grobmotorikEinbeinhuepfenSingleValueForm.valueOnValid(),
      gleichgewichtEinbeinstand: this.gleichgewichtEinbeinstandSingleValueForm.valueOnValid(),
      bemerkungen: trimToUndefined(value.bemerkungen),
      version: value.version ?? globalConstants.entityPrePersistVersion,
    });
  }

  setValidatorsForAnlassConfig(config: AnlassConfigJson, nichtGeprueftGrundEnabled: boolean): void {
    this.freiesGehenSingleValueForm.setValidatorsForAnlassConfig(
      config.data.untersuchungBeeintraechtigungFreiesGehen,
      nichtGeprueftGrundEnabled,
    );
    this.ohneStuetzradVelofahrenSingleValueForm.setValidatorsForAnlassConfig(
      config.data.untersuchungBeeintraechtigungOhneStuetzradVelofahren,
      nichtGeprueftGrundEnabled,
    );
    this.grafomotorikSingleValueForm.setValidatorsForAnlassConfig(
      config.data.untersuchungBeeintraechtigungGrafomotorik,
      nichtGeprueftGrundEnabled,
    );
    this.feinmotorikSingleValueForm.setValidatorsForAnlassConfig(
      config.data.untersuchungBeeintraechtigungFeinmotorik,
      nichtGeprueftGrundEnabled,
    );
    this.grobmotorikEinbeinhuepfenSingleValueForm.setValidatorsForAnlassConfig(
      config.data.untersuchungBeeintraechtigungGrobmotorik,
      nichtGeprueftGrundEnabled,
    );
    this.gleichgewichtEinbeinstandSingleValueForm.setValidatorsForAnlassConfig(
      config.data.untersuchungBeeintraechtigungGleichgewicht,
      nichtGeprueftGrundEnabled,
    );
    if (config.data.untersuchungBeeintraechtigungBemerkung === RequirementSetting.REQUIRED) {
      this.group.controls.bemerkungen.addValidators(Validators.required);
    }
  }
}
