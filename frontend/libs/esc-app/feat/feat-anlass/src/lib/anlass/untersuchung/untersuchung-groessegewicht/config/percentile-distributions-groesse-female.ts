import {PercentileDistribution} from '@dvb/viz';

export const BaselGroesseMaedchenDistribution: PercentileDistribution = {
  gender: 'female',
  lines: [
    {
      id: 'groessefemaleP3',
      percentile: 3,
      type: 'bottom',
      data: [
        {x: 1, y: 69},
        {x: 2, y: 80},
        {x: 4, y: 94},
        {x: 12, y: 138},
        {x: 14, y: 147},
        {x: 18, y: 151},
      ],
    },
    {
      id: 'groessefemaleP10',
      percentile: 10,
      type: 'sub',
      data: [
        {x: 1, y: 71},
        {x: 2, y: 82},
        {x: 4, y: 97},
        {x: 12, y: 143},
        {x: 14, y: 151},
        {x: 18, y: 155},
      ],
    },
    {
      id: 'groessefemaleP25',
      percentile: 25,
      type: 'sub',
      data: [
        {x: 1, y: 72},
        {x: 2, y: 84},
        {x: 4, y: 100},
        {x: 12, y: 147},
        {x: 14, y: 155},
        {x: 18, y: 159},
      ],
    },
    {
      id: 'groessefemaleP50',
      percentile: 50,
      type: 'main',
      data: [
        {x: 1, y: 74},
        {x: 2, y: 86},
        {x: 4, y: 103},
        {x: 12, y: 151.5},
        {x: 14, y: 160},
        {x: 18, y: 163},
      ],
    },
    {
      id: 'groessefemaleP75',
      percentile: 75,
      type: 'sub',
      data: [
        {x: 1, y: 76},
        {x: 2, y: 88},
        {x: 4, y: 106},
        {x: 12, y: 156},
        {x: 14, y: 165},
        {x: 18, y: 168},
      ],
    },
    {
      id: 'groessefemaleP90',
      percentile: 90,
      type: 'sub',
      data: [
        {x: 1, y: 77.5},
        {x: 2, y: 90},
        {x: 4, y: 109},
        {x: 12, y: 160},
        {x: 14, y: 169},
        {x: 18, y: 171.7},
      ],
    },
    {
      id: 'groessefemaleP97',
      percentile: 97,
      type: 'top',
      data: [
        {x: 1, y: 79},
        {x: 2, y: 92},
        {x: 4, y: 111},
        {x: 12, y: 164},
        {x: 14, y: 173},
        {x: 18, y: 175.5},
      ],
    },
  ],
};
