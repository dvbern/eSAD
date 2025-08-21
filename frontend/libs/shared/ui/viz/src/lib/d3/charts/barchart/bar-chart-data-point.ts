import {ChartData} from '../../_interface';

export interface BarChartDataPoint {
    id: string,
    label: string,
    value: number
}

export interface BarChartData extends ChartData {
    data: BarChartDataPoint[];
}
