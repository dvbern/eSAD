import {PercentileDistribution} from '@dvb/viz';

export const BaselGewichtMaedchenDistribution: PercentileDistribution = {
  gender: 'female',
  lines: [
    {
      id: 'gewichtfemaleP5',
      percentile: 5,
      type: 'sub',
      data: [
        {x: 104, y: 14},
        {x: 130, y: 22},
        {x: 146, y: 29},
        {x: 154, y: 36},
        {x: 170, y: 47},
        {x: 190, y: 58},
      ],
    },
    {
      id: 'gewichtfemaleP10',
      percentile: 10,
      type: 'bottom',
      data: [
        {x: 130, y: 23},
        {x: 146, y: 30.5},
        {x: 154, y: 38},
        {x: 170, y: 49},
        {x: 190, y: 61},
      ],
    },
    {
      id: 'gewichtfemaleP25',
      percentile: 25,
      type: 'sub',
      data: [
        {x: 130, y: 24},
        {x: 146, y: 33},
        {x: 154, y: 41},
        {x: 170, y: 52},
        {x: 190, y: 65},
      ],
    },
    {
      id: 'gewichtfemaleP50',
      percentile: 50,
      type: 'main',
      data: [
        {x: 104, y: 16},
        {x: 130, y: 25},
        {x: 146, y: 36},
        {x: 154, y: 45.5},
        {x: 170, y: 56.5},
        {x: 190, y: 71},
      ],
    },
    {
      id: 'gewichtfemaleP75',
      percentile: 75,
      type: 'sub',
      data: [
        {x: 130, y: 28},
        {x: 146, y: 39},
        {x: 154, y: 49.5},
        {x: 170, y: 61.5},
        {x: 190, y: 79.5},
      ],
    },
    {
      id: 'gewichtfemaleP90',
      percentile: 90,
      type: 'top',
      data: [
        {x: 130, y: 30},
        {x: 146, y: 43},
        {x: 154, y: 54},
        {x: 170, y: 68},
        {x: 190, y: 89},
      ],
    },
    {
      id: 'gewichtfemaleP95',
      percentile: 95,
      type: 'sub',
      data: [
        {x: 104, y: 18},
        {x: 130, y: 32},
        {x: 146, y: 46},
        {x: 154, y: 58},
        {x: 170, y: 72.5},
        {x: 190, y: 97},
      ],
    },
  ],
};
