import {PercentileAxisSettings, PercentileDistribution, PercentilePartialSettings} from '@dvb/viz';

export const BaselGewichtKnabenDistribution: PercentileDistribution = {
  gender: 'male',
  lines: [
    {
      id: 'gewichtmaleP5',
      percentile: 5,
      type: 'bottom',
      data: [
        {x: 105, y: 14.5},
        {x: 120, y: 18.5},
        {x: 129, y: 22},
        {x: 140, y: 27},
        {x: 150, y: 32},
        {x: 170, y: 46},
        {x: 190, y: 61},
      ],
    },
    {
      id: 'gewichtmaleP10',
      percentile: 10,
      type: 'sub',
      data: [
        {x: 129, y: 23},
        {x: 140, y: 28},
        {x: 150, y: 34},
        {x: 170, y: 48},
        {x: 190, y: 64},
      ],
    },
    {
      id: 'gewichtmaleP25',
      percentile: 25,
      type: 'sub',
      data: [
        {x: 129, y: 24},
        {x: 140, y: 29},
        {x: 150, y: 35.5},
        {x: 170, y: 51},
        {x: 190, y: 68},
      ],
    },
    {
      id: 'gewichtmaleP50',
      percentile: 50,
      type: 'main',
      data: [
        {x: 105, y: 16.5},
        {x: 120, y: 21.5},
        {x: 129, y: 25.5},
        {x: 140, y: 31},
        {x: 150, y: 38.5},
        {x: 170, y: 55},
        {x: 190, y: 72},
      ],
    },
    {
      id: 'gewichtmaleP75',
      percentile: 75,
      type: 'sub',
      data: [
        {x: 129, y: 27.5},
        {x: 140, y: 34},
        {x: 150, y: 41.5},
        {x: 170, y: 59.5},
        {x: 190, y: 78},
      ],
    },
    {
      id: 'gewichtmaleP90',
      percentile: 90,
      type: 'sub',
      data: [
        {x: 129, y: 29},
        {x: 140, y: 37},
        {x: 150, y: 46},
        {x: 170, y: 65},
        {x: 190, y: 82.5},
      ],
    },
    {
      id: 'gewichtmaleP95',
      percentile: 95,
      type: 'top',
      data: [
        {x: 105, y: 18.5},
        {x: 120, y: 25.5},
        {x: 129, y: 31},
        {x: 140, y: 39},
        {x: 150, y: 48},
        {x: 170, y: 70},
        {x: 190, y: 92},
      ],
    },
  ],
};

const AxisGewicht: PercentileAxisSettings = {
  title: 'Gewicht',
  unit: 'kg',
  unitPrefix: '',
  unitPostfix: ' kg',
  interval: 5,
  min: 10,
  max: 100,
  valueDecimalPlaces: 1
};

const AxisGroesse: PercentileAxisSettings = {
  title: 'Grösse',
  unit: 'cm',
  unitPrefix: '',
  unitPostfix: ' cm',
  interval: 10,
  min: 105,
  max: 190,
};

export const BaselGewichtKnabenSettings: PercentilePartialSettings = {
  colors: {},
  xAxis: AxisGroesse,
  yAxis: AxisGewicht,
};
