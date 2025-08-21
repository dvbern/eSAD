import {LogFactory} from '@dvb/util-shared';
import * as d3 from 'd3';
import {ChartD3, ChartData} from '../../_interface';
import {createTooltipElement} from './tooltip-util.d3';

const LOG = LogFactory.createLog('AbstractChartComponent');

/**
 * AbstractChartD3:
 * - the parent of all d3 charts
 * - no angular code, only d3
 * - handles the setup, size, styles, fonts etc. that are the same for all charts
 */

export abstract class AbstractChartD3<D extends ChartData> implements ChartD3<D> {

    // wrapper: the outside element
    protected wrapperSelector!: string;
    public wrapperSize!: { width: number, height: number };

    set data(dataset: D) {
        this.myDataset = dataset;
        this.onChangedData();
    }

    protected myDataset?: D;

    protected svg!: d3.Selection<SVGSVGElement, unknown, HTMLElement, any>;
    protected tooltip!: d3.Selection<HTMLDivElement, unknown, HTMLElement, any>;

    get mySvg(): d3.Selection<SVGSVGElement, any, any, any> {
        return this.svg;
    }

    protected plotArea!: d3.Selection<SVGGElement, any, any, any>;
    protected plotSize!: { width: number, height: number };

    // Fonts must be included via viz-styles.scss
    protected fontFamily = '"Roboto Condensed", "Roboto", "Helvetica Neue", sans-serif';

    public initWithin(selector: string, wrapperSize: { width: number, height: number }): void {
        this.initWrapper(selector, wrapperSize);
        this.setup();
    }

    public initWrapper(selector: string, wrapperSize: { width: number, height: number }): void {
        this.wrapperSelector = selector;
        this.wrapperSize = wrapperSize;
    }

    abstract setup(): void

    abstract onChangedData(): void

    abstract getMargin(): { top: number; right: number; bottom: number; left: number }

    public onResizedWindow(
        size: { width: number, height: number },
        wrapperSize: { width: number, height: number },
    ): void {
        this.wrapperSize = wrapperSize;
        this.initSizes();
        this.afterResizedWindow();
    }

    abstract afterResizedWindow(): void

    initSvg(): void {
        this.addSvg();
        this.initSizes();
    }

    private addSvg(): void {
        // add relative positioned div (needed for tooltips)
        const container = d3.select(this.wrapperSelector)
            .append('div')
            .attr('class', 'svg-and-tooltip')
            .style('width', '100%')
            .style('height', '100%')
            .style('position', 'relative');

        // add svg to my wrapper div
        this.svg = container
            .append('svg')
            .attr('width', '100%')
            .attr('height', '100%')
            .attr('font-family', this.fontFamily);

        // add styles (for fonts)
        this.svg
            .append('defs')
            .append('style')
            .attr('type', 'text/css')
            .text('@import url("viz-styles.css");');

        // add plot area
        this.plotArea = this.svg
            .append('g')
            .attr('class', 'plotarea');

        // add tooltip
        this.tooltip = createTooltipElement(container);
    }

    private initSizes(): void {
        const svgSize = this.initSvgSize();
        this.initPlotAreaSize(svgSize);
    }

    private initSvgSize(): { width: number; height: number } {
        // svg size (viewbox)
        const svgSize = {
            width: this.wrapperSize.width, //this.wrapper.nativeElement.clientWidth,
            height: this.wrapperSize.height, // this.wrapper.nativeElement.clientHeight,
        };
        this.svg
            .attr('viewbox', [0, 0, svgSize.width, svgSize.height])
            .attr('preserveAspectRatio', 'xMidYMid meet')
            .style('overview', 'visible');
        return svgSize;
    }

    private initPlotAreaSize(wrapperSize: { width: number; height: number }): void {
        // plot area (chart area inside the axes)
        const plotMargin = this.getMargin(); // you could have this be dependent on the wrapperSize
        this.plotSize = {
            width: Math.max(0, wrapperSize.width - plotMargin.left - plotMargin.right),
            height: Math.max(0, wrapperSize.height - plotMargin.top - plotMargin.bottom)
        };
        this.plotArea
            .attr('width', this.plotSize.width)
            .attr('height', this.plotSize.height)
            .attr('transform', `translate(${plotMargin.left}, ${plotMargin.top})`);
    }

}
