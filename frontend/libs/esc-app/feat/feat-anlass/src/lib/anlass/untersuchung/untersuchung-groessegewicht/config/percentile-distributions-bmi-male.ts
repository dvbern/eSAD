import {PercentileDistribution} from '@dvb/viz';

export const BaselBmiKnabenDistribution: PercentileDistribution = {
  gender: 'male',
  lines: [
    {
      id: 'bmimaleP3',
      percentile: 3,
      type: 'bottom',
      data: [
        {x: 2, y: 13.9},
        {x: 5, y: 13.15},
        {x: 10, y: 13.9},
        {x: 14, y: 15.6},
        {x: 18, y: 17.5},
      ],
    },
    {
      id: 'bmimaleP10',
      percentile: 10,
      type: 'sub',
      data: [
        {x: 2, y: 14.55},
        {x: 5, y: 13.75},
        {x: 10, y: 14.6},
        {x: 14, y: 16.6},
        {x: 18, y: 18.65},
      ],
    },
    {
      id: 'bmimaleP25',
      percentile: 25,
      type: 'sub',
      data: [
        {x: 2, y: 15.2},
        {x: 5, y: 14.45},
        {x: 10, y: 15.4},
        {x: 14, y: 17.6},
        {x: 18, y: 20},
      ],
    },
    {
      id: 'bmimaleP50',
      percentile: 50,
      type: 'main',
      data: [
        {x: 2, y: 16.05},
        {x: 5, y: 15.25},
        {x: 10, y: 16.5},
        {x: 14, y: 19},
        {x: 18, y: 21.7},
      ],
    },
    {
      id: 'bmimaleP75',
      percentile: 75,
      type: 'sub',
      data: [
        {x: 2, y: 16.9},
        {x: 5, y: 16.15},
        {x: 10, y: 17.8},
        {x: 14, y: 20.8},
        {x: 18, y: 23.8},
      ],
    },
    {
      id: 'bmimaleP90',
      percentile: 90,
      type: 'sub',
      data: [
        {x: 2, y: 17.8},
        {x: 4.5, y: 17.1},
        {x: 10, y: 19.2},
        {x: 14, y: 22.8},
        {x: 18, y: 26},
      ],
    },
    {
      id: 'bmimaleP97',
      percentile: 97,
      type: 'top',
      data: [
        {x: 2, y: 18.7},
        {x: 4, y: 18},
        {x: 10, y: 21},
        {x: 14, y: 25.35},
        {x: 18, y: 28.65},
      ],
    },
    {
      id: 'bmimaleP995',
      percentile: 99.5,
      type: 'sub',
      data: [
        {x: 2, y: 19.9},
        {x: 4, y: 19.5},
        {x: 10, y: 23.8},
        {x: 14, y: 29.5},
        {x: 18, y: 32.5},
      ],
    },
  ],
};
