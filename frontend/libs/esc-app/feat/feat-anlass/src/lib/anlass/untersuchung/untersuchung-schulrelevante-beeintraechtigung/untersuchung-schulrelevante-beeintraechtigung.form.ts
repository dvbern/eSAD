import {AbstractControl, NonNullableFormBuilder, ValidationErrors, ValidatorFn, Validators} from "@angular/forms";
import {DestroyRef} from "@angular/core";
import {
  AnlassConfigJson,
  RequirementSetting,
  UntersuchungSchulrelevanteBeeintraechtigungSaveJson,
} from "@dvb/data-access-backend";
import {AppValidatorKeys, AppValidators, BaseForm} from "@dvb/util-angular";
import {ensurePropsFlat, isFilledIn, isNotNullish, Nullish, requireValue, trimToUndefined} from "@dvb/util-shared";
import {globalConstants, ValueEnhancedForm} from "@dvb/feat-shared";
import {distinctUntilChanged} from "rxjs";
import {takeUntilDestroyed} from "@angular/core/rxjs-interop";
import {
  combineMonthsAndYearsIntoAnzahlMonths,
  splitAnzahlMonateIntoMonthsAndYears
} from "./untersuchung-schulrelevante-beeintraechtigung.utils";

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
  const group = fb.group({
    freiesGehen: config.freiesGehenValueEnhancedForm.group,
    anzahlJahreFreiesGehen: fb.control<number | Nullish>(undefined, AppValidators.numberWithinRange(0, 9)),
    anzahlMonateFreiesGehen: fb.control<number | Nullish>(undefined, AppValidators.numberWithinRange(0, 11)),
    ohneStuetzradVelofahren: config.ohneStuetzradVelofahrenValueEnhancedForm.group,
    grafomotorik: config.grafomotorikValueEnhancedForm.group,
    feinmotorik: config.feinmotorikValueEnhancedForm.group,
    grobmotorikEinbeinhuepfen: config.grobmotorikEinbeinhuepfenValueEnhancedForm.group,
    gleichgewichtEinbeinstand: config.gleichgewichtEinbeinstandValueEnhancedForm.group,
    bemerkungen: fb.control<string>('', AppValidators.textareaMaxLength2000()),
    version: fb.control<number | Nullish>({value: undefined, disabled: true}),
  });

  group.controls.anzahlMonateFreiesGehen.valueChanges.pipe(distinctUntilChanged(),
    takeUntilDestroyed(config.destroyRef)).subscribe({
    next: (anzahl) => {
      if (isNotNullish(anzahl)) {
        group.controls.freiesGehen.reset();
      }
    },
    error: console.error,
  });

  group.controls.anzahlJahreFreiesGehen.valueChanges.pipe(distinctUntilChanged(), takeUntilDestroyed(config.destroyRef))
    .subscribe({
      next: (anzahl) => {
        if (isNotNullish(anzahl)) {
          group.controls.freiesGehen.reset();
        }
      },
      error: console.error,
    });

  group.controls.freiesGehen.controls.value.valueChanges.pipe(distinctUntilChanged(),
    takeUntilDestroyed(config.destroyRef)).subscribe({
    next: value => {
      if (isNotNullish(value)) {
        group.controls.anzahlJahreFreiesGehen.reset();
        group.controls.anzahlMonateFreiesGehen.reset();
      } else {
        group.controls.anzahlJahreFreiesGehen.updateValueAndValidity();
        group.controls.anzahlMonateFreiesGehen.updateValueAndValidity();
      }
    },
    error: console.error,
  });

  return group;
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

    const values = splitAnzahlMonateIntoMonthsAndYears(value?.anzahlMonateFreiesGehen);
    this.group.controls.anzahlJahreFreiesGehen.reset(values?.jahre);
    this.group.controls.anzahlMonateFreiesGehen.reset(values?.monate);

  }

  public override valueOnValid(): UntersuchungSchulrelevanteBeeintraechtigungSaveJson {
    const value = requireValue(this.group.getRawValue());
    return ensurePropsFlat<UntersuchungSchulrelevanteBeeintraechtigungSaveJson>({
      freiesGehen: this.freiesGehenSingleValueForm.valueOnValid(),
      anzahlMonateFreiesGehen: combineMonthsAndYearsIntoAnzahlMonths(value.anzahlJahreFreiesGehen,
        value.anzahlMonateFreiesGehen),
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
    if (config.data.untersuchungBeeintraechtigungFreiesGehen === RequirementSetting.REQUIRED) {
      this.group.addValidators(this.freiesGehenAtLeastOneRequiredValidator());
      if (nichtGeprueftGrundEnabled) {
        this.freiesGehenSingleValueForm.addNichtGeprueftGrundValidator();
      }
    }
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

  /**
   * Validator welcher kontrolliert, dass entweder `anzahlJahreFreiesGehen`, `anzahlMonateFreiesGehen`
   * oder `freiesGehenSingleValueForm.value` gesetzt ist. Wird nur angewendet, wenn in der
   * AnlassConfig `untersuchungBeeintraechtigungFreiesGehen` auf REQUIRED gesetzt ist.
   *
   * Der `atLeastOneRequired`-Fehler wird auf den 3 einzelnen FormControls gesetzt (statt auf der
   * FormGroup), damit er direkt an den jeweiligen Feldern angezeigt werden kann.
   */
  private freiesGehenAtLeastOneRequiredValidator(): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      const group = control as ReturnType<typeof makeForm>;
      const anzahlJahreControl = group.controls.anzahlJahreFreiesGehen;
      const anzahlMonateControl = group.controls.anzahlMonateFreiesGehen;
      const freiesGehenValueControl = this.freiesGehenSingleValueForm.group.controls.value;

      const atLeastOneFilled = isFilledIn(anzahlJahreControl.value)
        || isFilledIn(anzahlMonateControl.value)
        || isNotNullish(freiesGehenValueControl.value);

      // Den `touched`-Zustand zwischen den 3 Controls synchronisieren, damit der Fehler entweder
      // auf allen oder auf keinem angezeigt wird. Dies geschieht aber nur, wenn der User bereits
      // mit mindestens einem der 3 Controls interagiert hat (touched oder dirty), damit beim
      // initialen Anzeigen des Formulars noch keine Fehler angezeigt werden.
      const anyInteracted = anzahlJahreControl.touched || anzahlJahreControl.dirty
        || anzahlMonateControl.touched || anzahlMonateControl.dirty
        || freiesGehenValueControl.touched || freiesGehenValueControl.dirty;

      this.setAtLeastOneRequiredError(anzahlJahreControl, atLeastOneFilled, anyInteracted);
      this.setAtLeastOneRequiredError(anzahlMonateControl, atLeastOneFilled, anyInteracted);
      this.setAtLeastOneRequiredError(freiesGehenValueControl, atLeastOneFilled, anyInteracted);

      return null;
    };
  }

  /**
   * Setzt oder entfernt den `atLeastOneRequired`-Fehler auf einem Control, ohne andere Fehler
   * zu überschreiben. Das Control wird nur dann als `touched` markiert, wenn der User bereits mit
   * einem der zusammenhängenden Controls interagiert hat — damit beim erstmaligen Anzeigen des
   * Formulars noch keine Fehler erscheinen.
   */
  private setAtLeastOneRequiredError(
    control: AbstractControl,
    atLeastOneFilled: boolean,
    markAsTouched: boolean,
  ): void {
    const currentErrors = control.errors ?? {};
    const {[AppValidatorKeys.atLeastOneRequired]: _removed, ...remainingErrors} = currentErrors;

    if (atLeastOneFilled) {
      control.setErrors(Object.keys(remainingErrors).length > 0 ? remainingErrors : null);
    } else {
      control.setErrors({...remainingErrors, [AppValidatorKeys.atLeastOneRequired]: true});
    }
    if (markAsTouched) {
      control.markAsTouched();
    }
  }
}
