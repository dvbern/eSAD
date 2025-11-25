
import {ChangeDetectionStrategy, Component, inject} from '@angular/core';
import {TenantSettingsStore} from '@dvb/feat-shared';
import {CardComponent} from '@dvb/ui-components';
import {ButtonFiltersComponent, PercentileChartComponent, PercentileChartData, PercentileMainDataPoint} from '@dvb/viz';
import {TranslocoService} from '@jsverse/transloco';
import {
  BaselBmiMaedchenDistribution,
  BaselBmiMaedchenSettings,
} from './config/percentile-distributions-basel-bmi-female';
import {BaselBmiKnabenDistribution, BaselBmiKnabenSettings} from './config/percentile-distributions-basel-bmi-male';
import {
  BaselGewichtMaedchenDistribution,
  BaselGewichtMaedchenSettings,
} from './config/percentile-distributions-basel-gewicht-female';
import {
  BaselGewichtKnabenDistribution,
  BaselGewichtKnabenSettings,
} from './config/percentile-distributions-basel-gewicht-male';
import {
  BaselGroesseMaedchenDistribution,
  BaselGroesseMaedchenSettings,
} from './config/percentile-distributions-basel-groesse-female';
import {
  BaselGroesseKnabenDistribution,
  BaselGroesseKnabenSettings,
} from './config/percentile-distributions-basel-groesse-male';

enum DataSetIdTS {
  ALTER_BMI_BASEL_FEMALE_1 = 'ALTER_BMI_BASEL_FEMALE_1',
  ALTER_BMI_BASEL_FEMALE_2 = 'ALTER_BMI_BASEL_FEMALE_2',
  ALTER_BMI_MALE_1 = 'ALTER_BMI_MALE_1',
  ALTER_GROESSE_MALE_1 = 'ALTER_GROESSE_MALE_1',
  ALTER_GROESSE_FEMALE_1 = 'ALTER_GROESSE_FEMALE_1',
  GROESSE_GEWICHT_FEMALE_1 = 'GROESSE_GEWICHT_FEMALE_1',
  GROESSE_GEWICHT_FEMALE_2 = 'GROESSE_GEWICHT_FEMALE_2',
  GROESSE_GEWICHT_FEMALE_3 = 'GROESSE_GEWICHT_FEMALE_3',
  FURNITURE = 'FURNITURE',
}

interface DatasetJaxTS {
  id: DataSetIdTS;
  label: string;
  data: PercentileChartData;

}

interface DatasetCollectionJaxTS {
  initiallySelectedDatasetId: DataSetIdTS;
  datasets: Array<DatasetJaxTS>;
}

@Component({
  selector: 'feat-playground-percentile-chart',
  imports: [
    ButtonFiltersComponent,
    PercentileChartComponent,
    CardComponent
],
  templateUrl: './playground-percentile-chart.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PlaygroundPercentileChartComponent {

  readonly tenantSettingsStore = inject(TenantSettingsStore);
  readonly transloco = inject(TranslocoService);

  maedchenBmi: PercentileChartData = {
    settings: BaselBmiMaedchenSettings,
    distribution: BaselBmiMaedchenDistribution,
    mainData: new Map<number, PercentileMainDataPoint>(
      [
        [5, {id: 'maedchen-bmi1', label: 'Kindergarten', x: 5, y: 17}],
        [11, {id: 'maedchen-bmi2', label: '4. Klasse', x: 11, y: 21}],
      ]),
  };

  maedchenGroesse: PercentileChartData = {
    settings: BaselGroesseMaedchenSettings,
    distribution: BaselGroesseMaedchenDistribution,
    mainData: new Map<number, PercentileMainDataPoint>([
      [5, {id: 'groessemaedchen1', label: 'Kindergarten', x: 5, y: 125}],
      [12, {id: 'groessemaedchen2', label: '4. Klasse', x: 12, y: 168}],
      [17, {id: 'groessemaedchen3', label: '9. Klasse', x: 17, y: 174}],
    ]),
  };

  maedchenGewicht: PercentileChartData = {
    settings: BaselGewichtMaedchenSettings,
    distribution: BaselGewichtMaedchenDistribution,
    mainData: new Map<number, PercentileMainDataPoint>([
      [5, {id: 'somevalue' + Math.random() * 1000, label: '4. Klasse', x: 110, y: 17}],
      [12, {id: 'somevalue' + Math.random() * 1000, label: '9. Klasse', x: 150, y: 42}],
      [17, {id: 'somevalue' + Math.random() * 1000, label: '10. Schuljahr', x: 167, y: 59}],
    ]),
  };

  knabenBmi: PercentileChartData = {
    settings: BaselBmiKnabenSettings,
    distribution: BaselBmiKnabenDistribution,
    mainData: new Map<number, PercentileMainDataPoint>([
      [ 5, {id: 'knaben-bmi1', label: 'Kindergarten', x: 5, y: 17.3 }],
      [ 12, {id: 'knaben-bmi2', label: '4. Klasse', x: 12, y: 18 }],
      [ 17, {id: 'knaben-bmi2', label: '9. Klasse', x: 17, y: 23.0517 }],
    ]),

  };
  knabenGroesse: PercentileChartData = {
    settings: BaselGroesseKnabenSettings,
    distribution: BaselGroesseKnabenDistribution,
    mainData: new Map<number, PercentileMainDataPoint>([
      [5, {id: 'groesseknaben1', label: 'Kindergarten', x: 5, y: 99}],
      [12, {id: 'groesseknaben2', label: '4. Klasse', x: 12, y: 129}],
      [17, {id: 'groesseknaben3', label: '9. Klasse', x: 17, y: 164}],
    ]),

  };
  knabenGewicht: PercentileChartData = {
    settings: BaselGewichtKnabenSettings,
    distribution: BaselGewichtKnabenDistribution,
    mainData: new Map<number, PercentileMainDataPoint>([
      [ 99, {id: 'somevalue' + Math.random() * 1000, label: 'Kindergarten', x: 99, y: 17}],
      [129, {id: 'somevalue' + Math.random() * 1000, label: '5. Klasse', x: 129, y: 30}],
      [164, {id: 'somevalue' + Math.random() * 1000, label: '9. Klasse', x: 164, y: 62}],
    ]),
  };


  public datasetCollection: DatasetCollectionJaxTS = {
    initiallySelectedDatasetId: DataSetIdTS.ALTER_BMI_BASEL_FEMALE_1,
    datasets: [
      {
        id: DataSetIdTS.ALTER_BMI_BASEL_FEMALE_1,
        label: 'BMI Mädchen 1 BS',
        data:
          {
            settings: BaselBmiMaedchenSettings,
            distribution: BaselBmiMaedchenDistribution,
            mainData: new Map<number, PercentileMainDataPoint>([
              [5, {id: 'maedchen-bmi1', label: 'Kindergarten', x: 5, y: 17}],
              [11, {id: 'maedchen-bmi2', label: '4. Klasse', x: 11, y: 21}],
            ]),
          },

      },
      {
        id: DataSetIdTS.ALTER_BMI_BASEL_FEMALE_2,
        label: 'BMI Mädchen 2 BS',

        data:
          {
            settings: BaselBmiMaedchenSettings,
            distribution: BaselBmiMaedchenDistribution,
            mainData: new Map<number, PercentileMainDataPoint>([
              [ 5, {id: 'maedchen-bmi1', label: 'Kindergarten', x: 5, y: 19}],
              [11, {id: 'maedchen-bmi2', label: '4. Klasse', x: 11, y: 25}],
            ]),
          },

      },
      {
        id: DataSetIdTS.ALTER_BMI_MALE_1,
        label: 'BMI Knaben BS 1',

        data:
          {
            settings: BaselBmiKnabenSettings,
            distribution: BaselBmiKnabenDistribution,
            mainData: new Map<number, PercentileMainDataPoint>([
              [ 6, {id: 'sfsdf1', label: 'Kindergarten', x: 6, y: 20}],
              [12, {id: 'isudhfsid2', label: '4. Klasse', x: 12, y: 27}],
              [16, {id: 'isudhfsid3', label: '9. Klasse', x: 16, y: 29}],
            ]),
          },
      },

      {
        id: DataSetIdTS.GROESSE_GEWICHT_FEMALE_1,
        label: 'Gewicht Mädchen 1 BS',

        data:
          {
            settings: BaselGewichtMaedchenSettings,
            distribution: BaselGewichtMaedchenDistribution,
            mainData: new Map<number, PercentileMainDataPoint>([
              [80, {id: 'somevalue' + Math.random() * 1000, label: 'Kindergarten', x: 80, y: 8}],
              [110, {id: 'somevalue' + Math.random() * 1000, label: '4. Klasse', x: 110, y: 17}],
              [150, {id: 'somevalue' + Math.random() * 1000, label: '9. Klasse', x: 150, y: 42}],
              [167, {id: 'somevalue' + Math.random() * 1000, label: '10. Schuljahr', x: 167, y: 59}],
            ]),
          },
      },

      {
        id: DataSetIdTS.GROESSE_GEWICHT_FEMALE_2,
        label: 'Gewicht Mädchen <130cm',

        data:
          {
            settings: {
              colors: {}, xAxis: {
                title: 'Grösse',
                unit: 'cm',
                unitPrefix: '',
                unitPostfix: ' cm',
                interval: 10,
                min: 50,
                max: 130,
              },
              yAxis: {
                title: 'Gewicht',
                unit: 'kg',
                unitPrefix: '',
                unitPostfix: ' kg',
                interval: 10,
                min: 2,
                max: 40,
              },
            },

            distribution: {
              gender: 'female', lines: [
                {
                  id: 'percentile' + Math.random() * 1000,
                  percentile: 3,
                  type: 'bottom',
                  data: [
                    {x: 50, y: 3},
                    {x: 100, y: 13},
                    {x: 130, y: 22},
                  ],
                }, {
                  id: 'percentile' + Math.random() * 1000,
                  percentile: 50,
                  type: 'main',
                  data: [
                    {x: 50, y: 3.5},
                    {x: 100, y: 15.5},
                    {x: 130, y: 26.8},
                  ],
                },
                {
                  id: 'percentile' + Math.random() * 1000,
                  percentile: 97,
                  type: 'top',
                  data: [
                    {x: 50, y: 4},
                    {x: 90, y: 15},
                    {x: 130, y: 34.5},
                  ],
                },
              ],
            },
            mainData: new Map<number, PercentileMainDataPoint>([
              [ 80, {id: 'somevalue' + Math.random() * 1000, label: 'Kindergarten', x: 80, y: 8}],
              [110, {id: 'somevalue' + Math.random() * 1000, label: '4. Klasse', x: 110, y: 17}],
            ]),
          },
      },

      {
        id: DataSetIdTS.GROESSE_GEWICHT_FEMALE_3,
        label: 'Gewicht Mädchen > 130cm',

        data:
          {
            settings: {
              colors: {}, xAxis: {
                title: 'Grösse',
                unit: 'cm',
                unitPrefix: '',
                unitPostfix: ' cm',
                interval: 5,
                min: 130,
                max: 180,
              },
              yAxis: {
                title: 'Gewicht',
                unit: 'kg',
                unitPrefix: '',
                unitPostfix: ' kg',
                interval: 10,
                min: 20,
                max: 120,
              },
            },
            mainData: new Map<number, PercentileMainDataPoint>([
              [ 150, {id: 'somevalue' + Math.random() * 1000, label: 'A', x: 150, y: 42}],
              [ 167, {id: 'somevalue' + Math.random() * 1000, label: 'B', x: 167, y: 59}],
            ]),
            distribution: {
              gender: 'female', lines: [
                {
                  id: 'percentile' + Math.random() * 1000,
                  percentile: 3,
                  type: 'bottom',
                  data: [
                    {x: 130, y: 22},
                    {x: 150, y: 32},
                    {x: 170, y: 48},
                    {x: 180, y: 55},
                  ],
                },
                {
                  id: 'percentile' + Math.random() * 1000,
                  percentile: 10,
                  type: 'sub',
                  data: [
                    {x: 130, y: 24},
                    {x: 150, y: 35},
                    {x: 170, y: 51},
                    {x: 180, y: 59},
                  ],
                },
                {
                  id: 'percentile' + Math.random() * 1000,
                  percentile: 25,
                  type: 'sub',
                  data: [
                    {x: 130, y: 25},
                    {x: 150, y: 37},
                    {x: 170, y: 55},
                    {x: 180, y: 63},
                  ],
                },
                {
                  id: 'percentile' + Math.random() * 1000,
                  percentile: 50,
                  type: 'main',
                  data: [
                    {x: 130, y: 26.5},
                    {x: 150, y: 41},
                    {x: 170, y: 60.3},
                    {x: 180, y: 69},
                  ],
                },
                {
                  id: 'percentile' + Math.random() * 1000,
                  percentile: 75,
                  type: 'sub',
                  data: [
                    {x: 130, y: 29},
                    {x: 150, y: 46},
                    {x: 170, y: 67},
                    {x: 180, y: 76},
                  ],
                },
                {
                  id: 'percentile' + Math.random() * 1000,
                  percentile: 90,
                  type: 'sub',
                  data: [
                    {x: 130, y: 31.6},
                    {x: 150, y: 51.6},
                    {x: 170, y: 74.3},
                    {x: 180, y: 84},
                  ],
                },
                {
                  id: 'percentile' + Math.random() * 1000,
                  percentile: 97,
                  type: 'top',
                  data: [
                    {x: 130, y: 35},
                    {x: 150, y: 59},
                    {x: 170, y: 84.5},
                    {x: 180, y: 94.6},
                  ],
                },
              ],
            },
          },

      },
      {
        id: DataSetIdTS.ALTER_GROESSE_FEMALE_1,
        label: 'Grösse Mädchen',
        data: {
          settings: BaselGroesseMaedchenSettings,
          distribution: BaselGroesseMaedchenDistribution,
          mainData: new Map<number, PercentileMainDataPoint>(),
        },
      },

      {
        id: DataSetIdTS.ALTER_GROESSE_MALE_1,
        label: 'Grösse Knaben',

        data:
          {

            settings: {
              colors: {},
              yAxis: {
                title: 'Grösse',
                unit: 'cm',
                unitPrefix: '',
                unitPostfix: ' cm',
                interval: 10,
                min: 50,
                max: 220,
              },
              xAxis: {
                title: 'Alter',
                unit: 'Jahre',
                unitPrefix: '',
                unitPostfix: ' J',
                interval: 1,
                min: 2,
                max: 20,
              },
            },
            mainData: new Map<number, PercentileMainDataPoint>([
              [ 5, {id: 'groesseknaben1', label: 'Kindergarten', x: 5, y: 125}],
              [12, {id: 'groesseknaben2', label: '4. Klasse', x: 12, y: 181}],
              [21, {id: 'groesseknaben3', label: 'Lehre', x: 21, y: 193}],
            ]),
            distribution: {
              gender: 'male', lines: [
                {
                  id: 'percentile' + Math.random() * 1000,
                  percentile: 3,
                  type: 'bottom',
                  data: [
                    {x: 1, y: 45},
                    {x: 2, y: 82},
                    {x: 7, y: 115},
                    {x: 13, y: 145},
                    {x: 15, y: 158},
                    {x: 18, y: 165},
                    {x: 20, y: 168},
                  ],
                },
                {
                  id: 'percentile' + Math.random() * 1000,
                  percentile: 50,
                  type: 'main',
                  data: [
                    {x: 2, y: 88},
                    {x: 7, y: 125},
                    {x: 13, y: 155},
                    {x: 15, y: 168},
                    {x: 18, y: 172},
                    {x: 20, y: 175},
                  ],
                },
                {
                  id: 'percentile' + Math.random() * 1000,
                  percentile: 97,
                  type: 'top',
                  data: [
                    {x: 2, y: 95},
                    {x: 7, y: 135},
                    {x: 13, y: 174},
                    {x: 15, y: 186},
                    {x: 18, y: 191},
                    {x: 20, y: 192},
                  ],
                },
              ],
            },
          },

      },

      {
        id: DataSetIdTS.FURNITURE,
        label: 'Custom',

        data:
          {
            settings: {
              colors: {

                mainDotColor: '#3a264d',
                mainDotLabelsColor: '#3a264d',

                tickLabelsColor: '#987eb1',
                genderIconColor: '#987eb1',
                axisLabelYColor: '#3a264d',
                axisLabelXColor: '#987eb1',

                gridColor: '#f1f1f1',
                outsideRangeBgColor: '#f8f7fa',

                percentileLabelColor: '#987eb1',
                percentileLineColor: '#d8cee1',
                percentileLineMinorColor: '#d8cee1',
                percentileLine50Color: '#d8cee1',

              },
              xAxis: {
                title: 'Breite',
                unit: 'Millimeter',
                unitPrefix: '',
                unitPostfix: ' mm',
                interval: 10,
                min: 0,
                max: 200,
                valueDecimalPlaces: 0,
                tickDecimalPlaces: 0,
              },
              yAxis: {
                title: 'Preis',
                unit: 'Schweizer Franken',
                unitPrefix: 'CHF ',
                unitPostfix: '',
                interval: 50,
                min: 1000,
                max: 1500,
                valueDecimalPlaces: 2,
                tickDecimalPlaces: 0,
              },
            },
            mainData: new Map<number, PercentileMainDataPoint>([
              [ 180, {id: 'somevalue' + Math.random() * 1000, label: 'Teppich', x: 180, y: 1120}],
              [136.66666, {id: 'somevalue' + Math.random() * 1000, label: 'Esstisch', x: 136.66666, y: 1426.66666}],
              [18.95, {id: 'somevalue' + Math.random() * 1000, label: 'Teller', x: 18.95, y: 1025}],
            ]),

            distribution: {
              gender: 'female', lines: [
                {
                  id: 'percentile' + Math.random() * 1000,
                  percentile: 3,
                  type: 'bottom',
                  data: [
                    {x: 0, y: 1005},
                    {x: 30, y: 1105},
                    {x: 200, y: 1120},
                  ],
                },
                {
                  id: 'percentile' + Math.random() * 1000,
                  percentile: 10,
                  type: 'sub',
                  data: [
                    {x: 0, y: 1007},
                    {x: 30, y: 1110},
                    {x: 200, y: 1130},
                  ],
                },
                {
                  id: 'percentile' + Math.random() * 1000,
                  percentile: 25,
                  type: 'sub',
                  data: [
                    {x: 0, y: 1010},
                    {x: 30, y: 1120},
                    {x: 200, y: 1160},
                  ],
                },
                {
                  id: 'percentile' + Math.random() * 1000,
                  percentile: 50,
                  type: 'sub',
                  data: [
                    {x: 0, y: 1014},
                    {x: 30, y: 1130},
                    {x: 200, y: 1200},
                  ],
                },
                {
                  id: 'percentile' + Math.random() * 1000,
                  percentile: 75,
                  type: 'sub',
                  data: [
                    {x: 0, y: 1020},
                    {x: 30, y: 1150},
                    {x: 200, y: 1280},
                  ],
                },
                {
                  id: 'percentile' + Math.random() * 1000,
                  percentile: 90,
                  type: 'sub',
                  data: [
                    {x: 0, y: 1029},
                    {x: 30, y: 1167},
                    {x: 200, y: 1370},
                  ],
                },
                {
                  id: 'percentile' + Math.random() * 1000,
                  percentile: 97,
                  type: 'top',
                  data: [
                    {x: 0, y: 1056},
                    {x: 30, y: 1199},
                    {x: 200, y: 1480},
                  ],
                },
              ],
            },
          },
      },
    ],
  };
}
