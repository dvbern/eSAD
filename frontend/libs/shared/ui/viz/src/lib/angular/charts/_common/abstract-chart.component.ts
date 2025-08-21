import {
  AfterViewInit,
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  DestroyRef,
  ElementRef,
  inject,
  Input,
  OnDestroy,
  ViewChild,
} from '@angular/core';
import {LogFactory, requireValue} from '@dvb/util-shared';
import {ChartD3, ChartData} from '../../../d3/_interface';

const LOG = LogFactory.createLog('AbstractChartComponent');

let uniqueId = 0;

/**
 * AbstractChartComponent
 * - the parent of all ChartComponents (Angular side)
 * - resize listener
 * - data input
 * - (all the Angular stuff that we did not want to have in the actual d3 files)
 */
@Component({
  selector: 'svui-abstract-chart',
  template: '',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export abstract class AbstractChartComponent<T extends ChartD3<C>, C extends ChartData>
  implements AfterViewInit, OnDestroy
{

  protected id = AbstractChartComponent.nextControlId(this.constructor.name);

  @ViewChild('wrapper')
  protected wrapper!: ElementRef<HTMLDivElement>;


  protected chart = this.instantiateChart();

  abstract instantiateChart(): T;

  destroyRef = inject(DestroyRef);
  cdRef = inject(ChangeDetectorRef);
  private resizeObserver?: ResizeObserver;

  private getWrapperSize(): { width: number; height: number } {
    return {
      width: this.wrapper.nativeElement.clientWidth,
      height: this.wrapper.nativeElement.clientHeight,
    };
  }


  public ngAfterViewInit(): void {
    this.chart.initWithin('#' + requireValue(this.id), this.getWrapperSize());
    // listen to window resize
    this.resizeObserver = new ResizeObserver(_ => {
      const size = { width: window.innerWidth, height: window.innerHeight };
      this.chart.onResizedWindow(size, this.getWrapperSize());
    });
    this.resizeObserver.observe(this.wrapper.nativeElement);

    const firstSize = { width: window.innerWidth, height: window.innerHeight };
    this.cdRef.detectChanges(); // Force layout recalculation
    this.chart.onResizedWindow(firstSize, this.getWrapperSize());
  }

  @Input()
    set dataset(dataset: C) {
      this.chart.data = dataset;
    }

  setDataset(dataset: C): void {
    this.chart.data = dataset;
  }
  ngOnDestroy(): void {
    this.resizeObserver?.disconnect();
  }

  static nextControlId(prefix: string): string {
    const nextId = uniqueId++;
    const result = prefix + nextId.toString();

    return result;
  }
}
