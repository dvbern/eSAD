import {PercentileAxisSettings, PercentileDistribution, PercentilePartialSettings} from '@dvb/viz';

export const BaselGroesseKnabenDistribution: PercentileDistribution = {
  gender: 'male',
  lines: [
    {
      id: 'groessemaleP3',
      percentile: 3,
      type: 'bottom',
      data: [
        {x: 1, y: 71},
        {x: 2, y: 81},
        {x: 4, y: 95},
        {x: 12, y: 136},
        {x: 14, y: 149},
        {x: 16, y: 159},
        {x: 18, y: 162},
      ],
    },
    {
      id: 'groessemaleP10',
      percentile: 10,
      type: 'sub',
      data: [
        {x: 1, y: 73},
        {x: 2, y: 84},
        {x: 4, y: 98},
        {x: 12, y: 140},
        {x: 14, y: 153},
        {x: 16, y: 163},
        {x: 18, y: 166.5},
      ],
    },
    {
      id: 'groessemaleP25',
      percentile: 25,
      type: 'sub',
      data: [
        {x: 1, y: 74},
        {x: 2, y: 86},
        {x: 4, y: 100.5},
        {x: 12, y: 144},
        {x: 14, y: 158},
        {x: 16, y: 168},
        {x: 18, y: 171},
      ],
    },
    {
      id: 'groessemaleP50',
      percentile: 50,
      type: 'main',
      data: [
        {x: 1, y: 75},
        {x: 2, y: 88},
        {x: 4, y: 103},
        {x: 12, y: 149},
        {x: 14, y: 163},
        {x: 16, y: 173},
        {x: 18, y: 176},
      ],
    },
    {
      id: 'groessemaleP75',
      percentile: 75,
      type: 'sub',
      data: [
        {x: 1, y: 78},
        {x: 2, y: 89.5},
        {x: 4, y: 106},
        {x: 12, y: 154},
        {x: 14, y: 168},
        {x: 16, y: 178},
        {x: 18, y: 181},
      ],
    },
    {
      id: 'groessemaleP90',
      percentile: 90,
      type: 'sub',
      data: [
        {x: 1, y: 79},
        {x: 2, y: 91},
        {x: 4, y: 109},
        {x: 12, y: 159},
        {x: 14, y: 173},
        {x: 16, y: 183},
        {x: 18, y: 186},
      ],
    },
    {
      id: 'groessemaleP97',
      percentile: 97,
      type: 'top',
      data: [
        {x: 1, y: 80},
        {x: 2, y: 93},
        {x: 4, y: 111},
        {x: 12, y: 162},
        {x: 14, y: 178},
        {x: 16, y: 188},
        {x: 18, y: 190},
      ],
    },
  ],
};

const AxisGroesse: PercentileAxisSettings = {
  title: 'Grösse',
  unit: 'cm',
  unitPrefix: '',
  unitPostfix: ' cm',
  interval: 10,
  min: 64,
  max: 200,
};

const AxisAlter: PercentileAxisSettings = {
  title: 'Alter',
  unit: 'Jahre',
  unitPrefix: '',
  unitPostfix: ' J.',
  interval: 1,
  min: 2,
  max: 18,
};

export const BaselGroesseKnabenSettings: PercentilePartialSettings = {
  colors: {},
  xAxis: AxisAlter,
  yAxis: AxisGroesse,
};
