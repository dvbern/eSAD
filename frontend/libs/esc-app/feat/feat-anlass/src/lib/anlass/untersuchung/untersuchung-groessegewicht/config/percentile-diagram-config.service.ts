import {inject, Injectable} from '@angular/core';
import {PercentileAxisSettings, PercentileChartData, PercentileGender, PercentileMainDataPoint} from '@dvb/viz';
import {TranslocoService} from '@jsverse/transloco';
import {
  BMI_CURVE_SETS,
  GEWICHT_CURVE_SETS,
  GROESSE_CURVE_SETS,
  PercentileAxisRange,
  PercentileCurveSet,
  PercentileReference,
} from './percentile-curve-sets';

@Injectable({providedIn: 'root'})
export class PercentileDiagramConfigService {
  readonly transloco = inject(TranslocoService);

  // !!IMPORTANT!!
  // The axis ranges live in percentile-curve-sets.ts, next to the distribution they belong to.
  // Changing them has an impact on the percentile charts: if for example the max value of groesse is
  // changed the chart will not display correctly. If values are changed there, make sure all the
  // charts still behave and display correctly.

  private getGroesseDiagramAxisSettings(range: PercentileAxisRange): PercentileAxisSettings {
    return {
      title: this.transloco.translate('perzentile.diagramm.groesse.titel'),
      unit: this.transloco.translate('perzentile.diagramm.groesse.einheit'),
      unitPrefix: '',
      unitPostfix: ' ' + this.transloco.translate('perzentile.diagramm.groesse.einheit'),
      interval: range.interval,
      min: range.min,
      max: range.max,
    };
  }

  private getGewichtDiagramAxisSettings(range: PercentileAxisRange): PercentileAxisSettings {
    return {
      title: this.transloco.translate('perzentile.diagramm.gewicht.titel'),
      unit: this.transloco.translate('perzentile.diagramm.gewicht.einheit'),
      unitPrefix: '',
      unitPostfix: ' ' + this.transloco.translate('perzentile.diagramm.gewicht.einheit'),
      interval: range.interval,
      min: range.min,
      max: range.max,
      valueDecimalPlaces: 1,
    };
  }

  private getAlterDiagramAxisSettings(range: PercentileAxisRange): PercentileAxisSettings {
    return {
      title: this.transloco.translate('perzentile.diagramm.alter.titel'),
      unit: this.transloco.translate('perzentile.diagramm.alter.einheit'),
      unitPrefix: '',
      unitPostfix: ' ' + this.transloco.translate('perzentile.diagramm.alter.einheit.short'),
      interval: range.interval,
      min: range.min,
      max: range.max,
    }
  }

  private getBmiDiagramAxisSettings(range: PercentileAxisRange): PercentileAxisSettings {
    return {
      title: this.transloco.translate('perzentile.diagramm.bmi.titel'),
      unit: this.transloco.translate('perzentile.diagramm.bmi.einheit'),
      unitPrefix: this.transloco.translate('perzentile.diagramm.bmi.titel') + ' ',
      unitPostfix: '',
      interval: range.interval,
      min: range.min,
      max: range.max,
      valueDecimalPlaces: 1,
    }
  }

  private toChartData(
    curveSet: PercentileCurveSet,
    xAxis: PercentileAxisSettings,
    yAxis: PercentileAxisSettings,
  ): PercentileChartData {
    return {
      settings: {colors: {}, xAxis, yAxis},
      distribution: curveSet.distribution,
      mainData: new Map<number, PercentileMainDataPoint>,
    };
  }

  public getChartDataGroesse(gender: PercentileGender, reference: PercentileReference): PercentileChartData {
    const curveSet = GROESSE_CURVE_SETS[reference][gender];
    return this.toChartData(
      curveSet,
      this.getAlterDiagramAxisSettings(curveSet.xAxis),
      this.getGroesseDiagramAxisSettings(curveSet.yAxis),
    );
  }

  public getChartDataGewicht(gender: PercentileGender, reference: PercentileReference): PercentileChartData {
    const curveSet = GEWICHT_CURVE_SETS[reference][gender];
    return this.toChartData(
      curveSet,
      this.getGroesseDiagramAxisSettings(curveSet.xAxis),
      this.getGewichtDiagramAxisSettings(curveSet.yAxis),
    );
  }

  public getChartDataBmi(gender: PercentileGender, reference: PercentileReference): PercentileChartData {
    const curveSet = BMI_CURVE_SETS[reference][gender];
    return this.toChartData(
      curveSet,
      this.getAlterDiagramAxisSettings(curveSet.xAxis),
      this.getBmiDiagramAxisSettings(curveSet.yAxis),
    );
  }

  public getGroesseDataPoint(alter?: number, alterDuodezimal?: string, groesse?: number): PercentileMainDataPoint | undefined {
    return this.getDataPoint(alter, alterDuodezimal, alter, groesse);
  }

  public getGewichtDataPoint(alter?: number, groesse?: number, gewicht?: number): PercentileMainDataPoint | undefined {
    return this.getDataPoint(alter, ''+groesse, groesse, gewicht);
  }

  public getBmiDataPoint(alter?: number, alterDuodezimal?: string, bmi?: number): PercentileMainDataPoint | undefined {
    return this.getDataPoint(alter, alterDuodezimal, alter, bmi);
  }

  private toId(alter?: number): string {
    return 'id_' + alter;
  }

  private getDataPoint(alter?: number, label?: string, valueX?: number, valueY?: number): PercentileMainDataPoint | undefined {
    if (alter && valueX && valueY) {
      const point: PercentileMainDataPoint = {
        id: this.toId(alter),
        alter: alter,
        label: label,
        x: valueX,
        y: valueY,
      };
      return point;
    }
    return undefined;
  }
}
