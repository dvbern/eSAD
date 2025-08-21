import {inject, Injectable} from '@angular/core';
import {PercentileAxisSettings, PercentileChartData, PercentileGender, PercentileMainDataPoint} from '@dvb/viz';
import {TranslocoService} from '@jsverse/transloco';
import {BaselBmiMaedchenDistribution} from './percentile-distributions-bmi-female';
import {BaselBmiKnabenDistribution} from './percentile-distributions-bmi-male';
import {BaselGewichtMaedchenDistribution} from './percentile-distributions-gewicht-female';
import {BaselGewichtKnabenDistribution} from './percentile-distributions-gewicht-male';
import {BaselGroesseMaedchenDistribution} from './percentile-distributions-groesse-female';
import {BaselGroesseKnabenDistribution} from './percentile-distributions-groesse-male';

@Injectable({providedIn: 'root'})
export class PercentileDiagramConfigService {
  readonly transloco = inject(TranslocoService);


  // !!IMPORTANT!!
  // Changes to any of those value have an impact on the percentile charts
  // if for example the max value of groesse is changed the chart will not display correctly
  // if values are changed here, make sure all the charts still behave and display correctly
  GROESSE_AXIS_SETTING_MIN_GROESSE_CHART = 55;
  GROESSE_AXIS_SETTING_MAX_GROESSE_CHART = 200;
  GROESSE_AXIS_SETTING_MIN_GEWICHT_CHART = 105;
  GROESSE_AXIS_SETTING_MAX_GEWICHT_CHART = 190;
  GEWICHT_AXIS_SETTING_MIN = 10;
  GEWICHT_AXIS_SETTING_MAX = 100;
  ALTER_AXIS_SETTING_MIN = 2;
  ALTER_AXIS_SETTING_MAX = 18;
  BMI_AXIS_SETTING_MIN = 12;
  BMI_AXIS_SETTING_MAX = 34;


  private getGroesseDiagramAxisSettings(min: number, max: number): PercentileAxisSettings {
    return {
      title: this.transloco.translate('perzentile.diagramm.groesse.titel'),
      unit: this.transloco.translate('perzentile.diagramm.groesse.einheit'),
      unitPrefix: '',
      unitPostfix: ' ' + this.transloco.translate('perzentile.diagramm.groesse.einheit'),
      interval: 10,
      min: min,
      max: max,
    };
  }

  private getGewichtDiagramAxisSettings(): PercentileAxisSettings {
    return {
      title: this.transloco.translate('perzentile.diagramm.gewicht.titel'),
      unit: this.transloco.translate('perzentile.diagramm.gewicht.einheit'),
      unitPrefix: '',
      unitPostfix: ' ' + this.transloco.translate('perzentile.diagramm.gewicht.einheit'),
      interval: 5,
      min: this.GEWICHT_AXIS_SETTING_MIN,
      max: this.GEWICHT_AXIS_SETTING_MAX,
      valueDecimalPlaces: 1,
    };
  }

  private getAlterDiagramAxisSettings(): PercentileAxisSettings {
    return {
      title: this.transloco.translate('perzentile.diagramm.alter.titel'),
      unit: this.transloco.translate('perzentile.diagramm.alter.einheit'),
      unitPrefix: '',
      unitPostfix: ' ' + this.transloco.translate('perzentile.diagramm.alter.einheit.short'),
      interval: 1,
      min: this.ALTER_AXIS_SETTING_MIN,
      max: this.ALTER_AXIS_SETTING_MAX,
    }
  }

  private getBmiDiagramAxisSettings(): PercentileAxisSettings {
    return {
      title: this.transloco.translate('perzentile.diagramm.bmi.titel'),
      unit: this.transloco.translate('perzentile.diagramm.bmi.einheit'),
      unitPrefix: this.transloco.translate('perzentile.diagramm.bmi.titel') + ' ',
      unitPostfix: '',
      interval: 1,
      min: this.BMI_AXIS_SETTING_MIN,
      max: this.BMI_AXIS_SETTING_MAX,
      valueDecimalPlaces: 1,
    }
  }

  public getChartDataGroesse(gender: PercentileGender): PercentileChartData {
    return  {
      settings: {
        colors: {},
        xAxis: this.getAlterDiagramAxisSettings(),
        yAxis: this.getGroesseDiagramAxisSettings(this.GROESSE_AXIS_SETTING_MIN_GROESSE_CHART, this.GROESSE_AXIS_SETTING_MAX_GROESSE_CHART),
      },
      distribution: gender === 'female' ? BaselGroesseMaedchenDistribution : BaselGroesseKnabenDistribution,
      mainData: new Map<number, PercentileMainDataPoint>,
    };
  }

  public getChartDataGewicht(gender: PercentileGender): PercentileChartData {
    return {
      settings: {
        colors: {},
        xAxis: this.getGroesseDiagramAxisSettings(this.GROESSE_AXIS_SETTING_MIN_GEWICHT_CHART, this.GROESSE_AXIS_SETTING_MAX_GEWICHT_CHART),
        yAxis: this.getGewichtDiagramAxisSettings(),
      },
      distribution: gender === 'female' ? BaselGewichtMaedchenDistribution : BaselGewichtKnabenDistribution,
      mainData: new Map<number, PercentileMainDataPoint>,
    };
  }

  public getChartDataBmi(gender: PercentileGender): PercentileChartData {
    return {
      settings: {
        colors: {},
        xAxis: this.getAlterDiagramAxisSettings(),
        yAxis: this.getBmiDiagramAxisSettings(),
      },
      distribution: gender === 'female' ? BaselBmiMaedchenDistribution : BaselBmiKnabenDistribution,
      mainData: new Map<number, PercentileMainDataPoint>,
    };
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
