import {ChartData} from '../../_interface';
import {PercentilePartialSettings} from './percentile-chart-settings';

export interface PercentileChartData extends ChartData {
  mainData: Map<number, PercentileMainDataPoint>
  distribution: PercentileDistribution;
  settings: PercentilePartialSettings;
}

const PercentileGender = [
  'male',
  'female'
] as const;

export type PercentileGender = typeof PercentileGender[number];


const PercentileType = [
  'top',
  'bottom',
  'main',
  'sub'
] as const;

export type PercentileType = typeof PercentileType[number];

export interface PercentileLine {
  id: string; // needed to avoid weird transitions
  percentile: number;
  type: PercentileType;
  data: PercentileSimpleDataPoint[];
}

export interface PercentileMainDataPoint extends PercentileSimpleDataPoint {
  id: string;
  alter?: number;
  label?: string;
}

export interface PercentileSimpleDataPoint {
  x: number;
  y: number;
}

export interface PercentileDistribution {
  gender: PercentileGender;
  lines: PercentileLine[];
}
