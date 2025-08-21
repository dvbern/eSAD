import {ChartData} from '../../d3/_interface/chart-data';

export interface NamedChartDataCollection<D extends ChartData> {
    id: string,
    label: string,
    data: D
}
