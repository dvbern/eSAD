import {DestroyRef} from '@angular/core';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {NonNullableFormBuilder, Validators} from '@angular/forms';
import {
  AnlassConfigJson,
  PerzentileBmi,
  PerzentileGewicht,
  PerzentileGroesse,
  RequirementSetting,
  UntersuchungGroesseGewichtDataJson,
  UntersuchungGroesseGewichtSaveJson,
} from '@dvb/data-access-backend';
import {globalConstants} from '@dvb/feat-shared';
import {AppValidators, BaseForm} from '@dvb/util-angular';
import {ensureProps, isNullish, Nullish, requireValue, trimToUndefined} from '@dvb/util-shared';
import {PercentileChartData, PercentileMainDataPoint} from '@dvb/viz';
import {distinctUntilChanged} from 'rxjs';
import {PercentileDiagramConfigService} from './config/percentile-diagram-config.service';

type Config = {
  fb: NonNullableFormBuilder,
  destroyRef: DestroyRef,
  diagramConfigService: PercentileDiagramConfigService,
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
  const group = fb.group({
    groesse: fb.control<number | undefined>(undefined, AppValidators.zeroDecimalPlaces()),
    groessePerzentile: fb.control<PerzentileGroesse | undefined>(undefined),
    gewicht: fb.control<number | undefined>(undefined, AppValidators.oneDecimalPlace()),
    gewichtPerzentile: fb.control<PerzentileGewicht | undefined>(undefined),
    bmi: fb.control<number | undefined>(undefined),
    bmiPerzentile: fb.control<PerzentileBmi | undefined>(undefined),
    bemerkungen: fb.control<string>('', AppValidators.textareaMaxLength2000()),
    version: fb.control<number | Nullish>({value: undefined, disabled: true}),
  });
  return group;
}

export class UntersuchungGroesseGewichtForm
  extends BaseForm<ReturnType<typeof makeForm>, UntersuchungGroesseGewichtSaveJson> {

  private readonly dataPointsGroesse: Map<number, PercentileMainDataPoint> = new Map<number, PercentileMainDataPoint>();
  private readonly dataPointsGewicht: Map<number, PercentileMainDataPoint> = new Map<number, PercentileMainDataPoint>();
  private readonly dataPointsBmi: Map<number, PercentileMainDataPoint> = new Map<number, PercentileMainDataPoint>();

  public chartGroesseMale: PercentileChartData;
  public chartGewichtMale: PercentileChartData;
  public chartBmiMale: PercentileChartData;

  public chartGroesseFemale: PercentileChartData;
  public chartGewichtFemale: PercentileChartData;
  public chartBmiFemale: PercentileChartData;

  public alter: number | undefined = undefined;
  public alterDuodezimal: string | undefined = undefined;

  constructor(protected config: Config) {
    super(() => makeForm(config));
    this.chartGroesseMale = this.config.diagramConfigService.getChartDataGroesse('male');
    this.chartGewichtMale = this.config.diagramConfigService.getChartDataGewicht('male');
    this.chartBmiMale = this.config.diagramConfigService.getChartDataBmi('male');

    this.chartGroesseFemale = this.config.diagramConfigService.getChartDataGroesse('female');
    this.chartGewichtFemale = this.config.diagramConfigService.getChartDataGewicht('female');
    this.chartBmiFemale = this.config.diagramConfigService.getChartDataBmi('female');

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
    this.group.controls.groessePerzentile.valueChanges.pipe(distinctUntilChanged(),
      takeUntilDestroyed(config.destroyRef))
      .subscribe({
        next: value => {
          if (!isNullish(value) && value === PerzentileGroesse.NICHT_GEPRUEFT) {
            this.group.controls.groesse.removeValidators([Validators.required]);
          } else {
            this.group.controls.groesse.addValidators([Validators.required]);
          }
          this.group.controls.groesse.updateValueAndValidity();
        },
      });
    this.group.controls.gewichtPerzentile.valueChanges.pipe(distinctUntilChanged(),
      takeUntilDestroyed(config.destroyRef))
      .subscribe({
        next: value => {
          if (!isNullish(value) && value === PerzentileGewicht.NICHT_GEPRUEFT) {
            this.group.controls.gewicht.removeValidators([Validators.required]);
          } else {
            this.group.controls.gewicht.addValidators([Validators.required]);
          }
          this.group.controls.gewicht.updateValueAndValidity();
        },
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
    this.group.reset({...currentData});
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
    return ensureProps<UntersuchungGroesseGewichtSaveJson>({
      groesse: value.groesse,
      groessePerzentile: value.groessePerzentile,
      gewicht: value.gewicht,
      gewichtPerzentile: value.gewichtPerzentile,
      bmiPerzentile: value.bmiPerzentile,
      bemerkungen: trimToUndefined(value.bemerkungen),
      version: value.version ?? globalConstants.entityPrePersistVersion,
    });
  }

  setValidatorsForAnlassConfig(config: AnlassConfigJson): void {
    if (config.data.groesseGewichtGroesse === RequirementSetting.REQUIRED) {
      this.group.controls.groesse.addValidators(Validators.required);
    }
    if (config.data.groesseGewichtGroessePerzentile === RequirementSetting.REQUIRED) {
      this.group.controls.groessePerzentile.addValidators(Validators.required);
    }
    if (config.data.groesseGewichtGewicht === RequirementSetting.REQUIRED) {
      this.group.controls.gewicht.addValidators(Validators.required);
    }
    if (config.data.groesseGewichtGewichtPerzentile === RequirementSetting.REQUIRED) {
      this.group.controls.gewichtPerzentile.addValidators(Validators.required);
    }
    if (config.data.groesseGewichtBmiPerzentile === RequirementSetting.REQUIRED) {
      this.group.controls.bmiPerzentile.addValidators(Validators.required);
    }
    if (config.data.groesseGewichtBemerkungen === RequirementSetting.REQUIRED) {
      this.group.controls.bemerkungen.addValidators(Validators.required);
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
