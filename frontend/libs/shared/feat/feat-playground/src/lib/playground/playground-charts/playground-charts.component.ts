import {CommonModule} from '@angular/common';
import {ChangeDetectionStrategy, Component} from '@angular/core';
import {PlaygroundPercentileChartComponent} from './percentile/playground-percentile-chart.component';

@Component({
    selector: 'feat-playground-charts',
    imports: [
        CommonModule, PlaygroundPercentileChartComponent,
    ],
    templateUrl: './playground-charts.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class PlaygroundChartsComponent {

}

