import * as d3 from 'd3';
import {ChartData} from './chart-data';

export interface ChartD3<C extends ChartData> {

    /**
     * After the surrounding view is ready, this method is called and we setup everything
     * @param wrapperSelector selector to get the containing element (we will add the svg and tooltip inside of that element)
     * @param wrapperSize initial size of the containing element
     */
    initWithin(wrapperSelector: string, wrapperSize: {width: number, height: number}): void

    /**
     * On a window resize, we need to adapt the chart
     * @param windowSize
     * @param wrapperSize
     */
    onResizedWindow(windowSize: { width: number, height: number }, wrapperSize: { width: number, height: number }): void

    /**
     * On data change, we need to draw or redraw some parts of the chart
     * @param dataset
     */
    set data(dataset: C)

    /**
     * For testing purposes
     */
    get mySvg(): d3.Selection<SVGSVGElement, any, any, any>
}
