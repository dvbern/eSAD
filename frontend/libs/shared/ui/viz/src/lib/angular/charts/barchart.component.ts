
import {AfterViewInit, ChangeDetectionStrategy, Component, Input, OnInit} from '@angular/core';
import {AbstractChartComponent} from './_common/abstract-chart.component';
import {BarchartD3, BarChartData} from '../../d3/charts';

/**
 * BarchartComponent:
 * - MIK barchart (Angular side)
 */
@Component({
    selector: 'svui-barchart',
    template: '<div [id]="id" #wrapper class="h-100" style="height: 100%"></div>',
    imports: [],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class BarchartComponent extends AbstractChartComponent<BarchartD3, BarChartData> implements
    AfterViewInit {

    @Input()
    set barColor(barColor: string) {
        this.chart.barColor = barColor;
    }

    @Input()
    set xAxisTitle(xAxisTitle: string) {
        this.chart.xAxisTitle = xAxisTitle;
    }

    @Input()
    set yAxisTitle(yAxisTitle: string) {
        this.chart.yAxisTitle = yAxisTitle;
    }

    public override instantiateChart(): BarchartD3 {
        return new BarchartD3();
    }

}
