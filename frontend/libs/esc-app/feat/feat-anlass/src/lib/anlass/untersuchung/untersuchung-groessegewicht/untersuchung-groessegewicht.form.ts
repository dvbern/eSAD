import {DestroyRef} from '@angular/core';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {NonNullableFormBuilder, Validators} from '@angular/forms';
import {
  AnlassConfigJson,
  RequirementSetting,
  UntersuchungGroesseGewichtDataJson,
  UntersuchungGroesseGewichtSaveJson,
} from '@dvb/data-access-backend';
import {globalConstants, ValueEnhancedForm} from '@dvb/feat-shared';
import {AppValidators, BaseForm} from '@dvb/util-angular';
import {ensurePropsFlat, Nullish, requireValue, trimToUndefined} from '@dvb/util-shared';
import {PercentileChartData, PercentileMainDataPoint} from '@dvb/viz';
import {distinctUntilChanged} from 'rxjs';
import {PercentileReference} from './config/percentile-curve-sets';
import {PercentileDiagramConfigService} from './config/percentile-diagram-config.service';

type Config = {
  fb: NonNullableFormBuilder,
  destroyRef: DestroyRef,
  diagramConfigService: PercentileDiagramConfigService,
  groesseSingleValueForm: ValueEnhancedForm;
  groesseMultiValueForm: ValueEnhancedForm;
  gewichtSingleValueForm: ValueEnhancedForm;
  gewichtMultiValueForm: ValueEnhancedForm;
  bmiSingleValueForm: ValueEnhancedForm;
  bmiMultiValueForm: ValueEnhancedForm;
}

export function calculateBmi(groesse: number | undefined, gewicht: number | undefined): number | undefined {
  if (!groesse || !gewicht) {
    return;
  }
  const groesseInMeter = groesse / 100;
  const bmi = gewicht / (groesseInMeter * groesseInMeter);
  return bmi.toFixed(1) as unknown as number;
}

function makeForm(config: Config) {
  const fb = config.fb;
  return fb.group({
    groesse: fb.control<number | undefined>(undefined, AppValidators.zeroDecimalPlaces()),
    groessePerzentileSingleValue: config.groesseSingleValueForm.group,
    groessePerzentileMultiValue: config.groesseMultiValueForm.group,
    gewicht: fb.control<number | undefined>(undefined, AppValidators.oneDecimalPlace()),
    gewichtPerzentileSingleValue: config.gewichtSingleValueForm.group,
    gewichtPerzentileMultiValue: config.gewichtMultiValueForm.group,
    bmi: fb.control<number | undefined>(undefined),
    bmiPerzentileSingleValue: config.bmiSingleValueForm.group,
    bmiPerzentileMultiValue: config.bmiMultiValueForm.group,
    bemerkungen: fb.control<string>('', AppValidators.textareaMaxLength2000()),
    version: fb.control<number | Nullish>({value: undefined, disabled: true}),
  });
}

export class UntersuchungGroesseGewichtForm
  extends BaseForm<ReturnType<typeof makeForm>, UntersuchungGroesseGewichtSaveJson> {

  readonly groesseSingleValueForm: ValueEnhancedForm;
  readonly groesseMultiValueForm: ValueEnhancedForm;
  readonly gewichtSingleValueForm: ValueEnhancedForm;
  readonly gewichtMultiValueForm: ValueEnhancedForm;
  readonly bmiSingleValueForm: ValueEnhancedForm;
  readonly bmiMultiValueForm: ValueEnhancedForm;

  private readonly dataPointsGroesse: Map<number, PercentileMainDataPoint> = new Map<number, PercentileMainDataPoint>();
  private readonly dataPointsGewicht: Map<number, PercentileMainDataPoint> = new Map<number, PercentileMainDataPoint>();
  private readonly dataPointsBmi: Map<number, PercentileMainDataPoint> = new Map<number, PercentileMainDataPoint>();

  // Assigned by buildCharts() as the first thing the constructor does.
  public chartGroesseMale!: PercentileChartData;
  public chartGewichtMale!: PercentileChartData;
  public chartBmiMale!: PercentileChartData;

  public chartGroesseFemale!: PercentileChartData;
  public chartGewichtFemale!: PercentileChartData;
  public chartBmiFemale!: PercentileChartData;

  public alter: number | undefined = undefined;
  public alterDuodezimal: string | undefined = undefined;

  /**
   * Which reference curves the charts are drawn against. Defaults to the old set, because the Anlass
   * -- and with it its date -- may still be loading when the form is constructed.
   */
  private reference: PercentileReference = 'BASEL_2019';

  constructor(protected config: Config) {
    super(() => makeForm(config));
    this.buildCharts(this.reference);

    this.groesseSingleValueForm = config.groesseSingleValueForm;
    this.groesseMultiValueForm = config.groesseMultiValueForm;
    this.gewichtSingleValueForm = config.gewichtSingleValueForm;
    this.gewichtMultiValueForm = config.gewichtMultiValueForm;
    this.bmiSingleValueForm = config.bmiSingleValueForm;
    this.bmiMultiValueForm = config.bmiMultiValueForm;

    this.group.controls.groesse.valueChanges.pipe(distinctUntilChanged(), takeUntilDestroyed(config.destroyRef))
      .subscribe({
        next: _ => {
          const bmi = calculateBmi(
            this.group.controls.groesse.value,
            this.group.controls.gewicht.value,
          );
          this.group.controls.bmi.setValue(bmi);
          this.addPointsToDataset(this.alter,
            this.alterDuodezimal,
            this.group.value.groesse,
            this.group.value.gewicht,
            bmi);
        },
        error: console.error,
      });
    this.group.controls.gewicht.valueChanges.pipe(distinctUntilChanged(), takeUntilDestroyed(config.destroyRef))
      .subscribe({
        next: _ => {
          const bmi = calculateBmi(
            this.group.controls.groesse.value,
            this.group.controls.gewicht.value,
          );
          this.group.controls.bmi.setValue(bmi);
          this.addPointsToDataset(this.alter,
            this.alterDuodezimal,
            this.group.value.groesse,
            this.group.value.gewicht,
            bmi);
        },
        error: console.error,
      });
    this.groesseSingleValueForm.group.valueChanges.pipe(distinctUntilChanged(), takeUntilDestroyed(config.destroyRef))
      .subscribe({
        next: () => this.group.controls.groesse.updateValueAndValidity(),
        error: console.error,
      });
    this.groesseMultiValueForm.group.valueChanges.pipe(distinctUntilChanged(), takeUntilDestroyed(config.destroyRef))
      .subscribe({
        next: () => this.group.controls.groesse.updateValueAndValidity(),
        error: console.error,
      });
    this.gewichtSingleValueForm.group.valueChanges.pipe(distinctUntilChanged(), takeUntilDestroyed(config.destroyRef))
      .subscribe({
        next: () => this.group.controls.gewicht.updateValueAndValidity(),
        error: console.error,
      });
    this.gewichtMultiValueForm.group.valueChanges.pipe(distinctUntilChanged(), takeUntilDestroyed(config.destroyRef))
      .subscribe({
        next: () => this.group.controls.gewicht.updateValueAndValidity(),
        error: console.error,
      });
  }

  resetForm(
    value: UntersuchungGroesseGewichtDataJson | Nullish,
    alter: number | undefined,
    alterDuodezimal?: string,
  ): void {
    this.alter = alter;
    this.alterDuodezimal = alterDuodezimal;
    const currentData: UntersuchungGroesseGewichtSaveJson | undefined = value?.untersuchungGroesseGewichtJson?.data;
    super.reset(currentData);

    this.groesseSingleValueForm.resetForm(currentData?.groessePerzentileSingleValue);
    this.groesseMultiValueForm.resetForm(currentData?.groessePerzentileMultiValue);
    this.gewichtSingleValueForm.resetForm(currentData?.gewichtPerzentileSingleValue);
    this.gewichtMultiValueForm.resetForm(currentData?.gewichtPerzentileMultiValue);
    this.bmiSingleValueForm.resetForm(currentData?.bmiPerzentileSingleValue);
    this.bmiMultiValueForm.resetForm(currentData?.bmiPerzentileMultiValue);

    this.group.controls.bmi.disable();
    const bmi = calculateBmi(
      currentData?.groesse,
      currentData?.gewicht,
    );
    this.group.controls.bmi.setValue(bmi);
    if (value?.previousDataPoints) {
      for (const point of value.previousDataPoints) {
        this.addPointsToDataset(point.alter, point.alterDuodezimal, point.groesse, point.gewicht, point.bmi);
      }
    }
    this.addPointsToDataset(this.alter, this.alterDuodezimal, currentData?.groesse, currentData?.gewicht, bmi);
  }

  public override valueOnValid(): UntersuchungGroesseGewichtSaveJson {
    const value = requireValue(this.group.getRawValue());
    return ensurePropsFlat<UntersuchungGroesseGewichtSaveJson>({
      groesse: value.groesse,
      groessePerzentileSingleValue: this.groesseSingleValueForm.valueOnValid(),
      groessePerzentileMultiValue: this.groesseMultiValueForm.valueOnValid(),
      gewicht: value.gewicht,
      gewichtPerzentileSingleValue: this.gewichtSingleValueForm.valueOnValid(),
      gewichtPerzentileMultiValue: this.gewichtMultiValueForm.valueOnValid(),
      bmiPerzentileSingleValue: this.bmiSingleValueForm.valueOnValid(),
      bmiPerzentileMultiValue: this.bmiMultiValueForm.valueOnValid(),
      bemerkungen: trimToUndefined(value.bemerkungen),
      version: value.version ?? globalConstants.entityPrePersistVersion,
    });
  }

  /**
   * Aktualisiert die Perzentilen-Optionslisten der Single-/Multi-Value-Forms aus der aktuellen
   * Anlasskonfiguration. Notwendig, da diese Optionen nur ein Snapshot bei Formular-Konstruktion
   * waeren, wenn die Config (z.B. bei einem Hard-Reload) erst nachtraeglich eintrifft.
   */
  updatePerzentileOptions(config: AnlassConfigJson): void {
    this.groesseSingleValueForm.updateOptions(config.data.groesseGewichtGroessePerzentileValues ?? []);
    this.groesseMultiValueForm.updateOptions(config.data.groesseGewichtGroessePerzentileValues ?? []);
    this.gewichtSingleValueForm.updateOptions(config.data.groesseGewichtGewichtPerzentileValues ?? []);
    this.gewichtMultiValueForm.updateOptions(config.data.groesseGewichtGewichtPerzentileValues ?? []);
    this.bmiSingleValueForm.updateOptions(config.data.groesseGewichtBmiPerzentileValues ?? []);
    this.bmiMultiValueForm.updateOptions(config.data.groesseGewichtBmiPerzentileValues ?? []);
  }

  setValidatorsForAnlassConfig(config: AnlassConfigJson, nichtGeprueftGrundEnabled: boolean): void {
    if (config.data.groesseGewichtGroesse === RequirementSetting.REQUIRED) {
      this.group.controls.groesse.addValidators(
        AppValidators.requiredIfValidator(
          () => !this.groesseSingleValueForm.isNichtGeprueftChecked() && !this.groesseMultiValueForm.isNichtGeprueftChecked(),
        ),
      );
      this.group.controls.groesse.updateValueAndValidity();
    }
    this.groesseSingleValueForm.setValidatorsForAnlassConfig(
      config.data.groesseGewichtGroessePerzentileSingleValue,
      nichtGeprueftGrundEnabled,
    );
    this.groesseMultiValueForm.setValidatorsForAnlassConfig(
      config.data.groesseGewichtGroessePerzentileMultiValue,
      nichtGeprueftGrundEnabled,
    );
    if (config.data.groesseGewichtGewicht === RequirementSetting.REQUIRED) {
      this.group.controls.gewicht.addValidators(
        AppValidators.requiredIfValidator(
          () => !this.gewichtSingleValueForm.isNichtGeprueftChecked() && !this.gewichtMultiValueForm.isNichtGeprueftChecked(),
        ),
      );
      this.group.controls.gewicht.updateValueAndValidity();
    }
    this.gewichtSingleValueForm.setValidatorsForAnlassConfig(
      config.data.groesseGewichtGewichtPerzentileSingleValue,
      nichtGeprueftGrundEnabled,
    );
    this.gewichtMultiValueForm.setValidatorsForAnlassConfig(
      config.data.groesseGewichtGewichtPerzentileMultiValue,
      nichtGeprueftGrundEnabled,
    );
    this.bmiSingleValueForm.setValidatorsForAnlassConfig(
      config.data.groesseGewichtBmiPerzentileSingleValue,
      nichtGeprueftGrundEnabled,
    );
    this.bmiMultiValueForm.setValidatorsForAnlassConfig(
      config.data.groesseGewichtBmiPerzentileMultiValue,
      nichtGeprueftGrundEnabled,
    );
    if (config.data.groesseGewichtBemerkungen === RequirementSetting.REQUIRED) {
      this.group.controls.bemerkungen.addValidators(Validators.required);
      this.group.controls.bemerkungen.updateValueAndValidity();
    }
  }

  private addPointsToDataset(
    alter?: number,
    alterDuodezimal?: string,
    groesse?: number,
    gewicht?: number,
    bmi?: number,
  ): void {
    if (alter && alterDuodezimal && groesse) {
      this.addGroessePoint(alter, alterDuodezimal, groesse);
      if (gewicht) {
        this.addGewichtPoint(alter, groesse, gewicht);
        if (bmi) {
          this.addBmiPoint(alter, alterDuodezimal, bmi);
        }
      }
    }
    this.refreshCharts();
  }

  /**
   * Switches the charts to the reference curves that were in force for the given Anlass.
   *
   * The charts are built in the constructor, but the Anlass may only arrive afterwards, so this has
   * to be able to rebuild them. Rebuilding is skipped while the reference is unchanged, which is the
   * normal case on every later change detection pass.
   */
  public setReference(reference: PercentileReference): void {
    if (reference === this.reference) {
      return;
    }
    this.reference = reference;
    this.buildCharts(reference);
    // The freshly built charts have an empty mainData, so the measurements have to be put back.
    this.refreshCharts();
  }

  /** Creates the six charts (three measures x two genders) against the given reference. */
  private buildCharts(reference: PercentileReference): void {
    const service = this.config.diagramConfigService;

    this.chartGroesseMale = service.getChartDataGroesse('male', reference);
    this.chartGewichtMale = service.getChartDataGewicht('male', reference);
    this.chartBmiMale = service.getChartDataBmi('male', reference);

    this.chartGroesseFemale = service.getChartDataGroesse('female', reference);
    this.chartGewichtFemale = service.getChartDataGewicht('female', reference);
    this.chartBmiFemale = service.getChartDataBmi('female', reference);
  }

  /** Re-attaches the data points and hands the charts new object identities so they redraw. */
  private refreshCharts(): void {
    this.chartGroesseMale.mainData = this.dataPointsGroesse;
    this.chartGewichtMale.mainData = this.dataPointsGewicht;
    this.chartBmiMale.mainData = this.dataPointsBmi;
    this.chartGroesseFemale.mainData = this.dataPointsGroesse;
    this.chartGewichtFemale.mainData = this.dataPointsGewicht;
    this.chartBmiFemale.mainData = this.dataPointsBmi;

    this.chartGroesseMale = {...this.chartGroesseMale};
    this.chartGewichtMale = {...this.chartGewichtMale};
    this.chartBmiMale = {...this.chartBmiMale};
    this.chartGroesseFemale = {...this.chartGroesseFemale};
    this.chartGewichtFemale = {...this.chartGewichtFemale};
    this.chartBmiFemale = {...this.chartBmiFemale};
  }

  private addGroessePoint(alter: number, alterDuodezimal: string, groesse: number): void {
    const groessePoint = this.config.diagramConfigService.getGroesseDataPoint(alter, alterDuodezimal, groesse);
    if (groessePoint) {
      this.dataPointsGroesse.set(requireValue(groessePoint.alter), groessePoint);
    }
  }

  private addGewichtPoint(alter: number, groesse: number, gewicht: number): void {
    const currentGewicht = this.config.diagramConfigService.getGewichtDataPoint(alter, groesse, gewicht);
    if (currentGewicht) {
      this.dataPointsGewicht.set(requireValue(currentGewicht.alter), currentGewicht);
    }
  }

  private addBmiPoint(alter: number, alterDuodezimal: string, bmi: number): void {
    const currentBmi = this.config.diagramConfigService.getBmiDataPoint(alter, alterDuodezimal, bmi);
    if (currentBmi) {
      this.dataPointsBmi.set(requireValue(currentBmi.alter), currentBmi);
    }
  }
}
