
import {AfterViewInit, ChangeDetectionStrategy, Component, OnInit} from '@angular/core';
import {PercentileChartD3, PercentileChartData} from '../../d3/charts';
import {AbstractChartComponent} from './_common/abstract-chart.component';

/**
 * PercentileChartComponent:
 * - Percentile chart (Angular side)
 */
@Component({
    selector: 'svui-percentilechart',
    template: '<div [id]="id" #wrapper class="h-100" style="height: 100%"></div>',
    imports: [],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class PercentileChartComponent extends AbstractChartComponent<PercentileChartD3, PercentileChartData>
  implements AfterViewInit {

  public override instantiateChart(): PercentileChartD3 {
    return new PercentileChartD3();
  }

}
