import * as d3 from 'd3';
import {AbstractChartD3} from '../_common/abstract-chart.d3';
import {addHoverEffect, addTooltipToHoverables, positionTooltipNextToMouse} from '../_common/tooltip-util.d3';
import {BarChartData, BarChartDataPoint} from './bar-chart-data-point';
import {truncateWithEllipses} from './truncate-with-ellipses';

/**
 * BarchartD3:
 *  a bar chart
 */
export class BarchartD3 extends AbstractChartD3<BarChartData> {

  public barColor = 'gray(102)';
  public xAxisTitle = '';
  public yAxisTitle = '';

  private barsParent!: d3.Selection<SVGGElement, any, any, any>;
  private valueLabelsParent!: d3.Selection<SVGGElement, any, any, any>;
  private hoverablesParent!: d3.Selection<SVGGElement, any, any, any>;

  private xAxis!: d3.Axis<string>;
  private xScale!: d3.ScaleBand<string>;
  private xAxisDraw!: d3.Selection<SVGGElement, any, any, any>;

  private yAxis!: d3.Axis<number | { valueOf(): number }>;
  private yScale!: d3.ScaleLinear<number, number>;
  private yAxisDraw!: d3.Selection<SVGGElement, any, any, any>;

  public override onChangedData(): void {
    this.redrawBars();
  }

  override getMargin(): { top: number; right: number; bottom: number; left: number } {
    return {
      top: 36, // fuer das schwebende Label ueber dem hoechsten Balken
      right: 0,
      bottom: 30, // fuer die Beschriftung der x-Achse
      left: 50, // fuer die Beschriftung der y-Achse
    };
  }

  override afterResizedWindow(): void {
    this.initScalesAndAxes();
    this.redrawBars();
  }

  override setup(): void {
    this.initSvg();
    this.initElements();

    this.redrawBars();

    // remove the 0 inner tick line
    this.yAxisDraw.select('.tick line').remove();
  }

  private initElements(): void {
    // the axis parent
    this.xAxisDraw = this.plotArea.append('g')
      .attr('class', 'x axis')
      .style('font-family', this.fontFamily)
      .style('font-size', '14px');
    this.yAxisDraw = this.plotArea.append('g')
      .attr('class', 'y axis')
      .style('font-family', this.fontFamily)
      .style('font-size', '14px');

    // the bars parent
    this.barsParent = this.plotArea.append('g').attr('class', 'bars');

    // labels
    this.valueLabelsParent = this.plotArea.append('g').attr('class', 'valuelabels');

    // hoverable areas (not just the bars)
    this.hoverablesParent = this.plotArea.append('g').attr('class', 'hoverables');

    this.initScalesAndAxes();
  }

  public redrawBars(): void {
    if (this.barsParent) {
      this.drawBarsAndAxes(this.myDataset?.data ?? [], 'value');
    }
  }

  private initScalesAndAxes(): void {
    this.xAxisDraw
      .attr('transform', `translate(0,${this.plotSize.height})`);

    // x axis
    this.xScale = d3.scaleBand()
      .range([0, this.plotSize.width])
      .rangeRound([0, this.plotSize.width]) // gives rounded values
      .paddingInner(0.15) // spacing between bars
      .paddingOuter(0.2); // padding around outest bars

    this.xAxis = d3.axisBottom(this.xScale);
    this.xAxis.tickSize(0); // no tick lines
    this.xAxis.tickPadding(10 * this.plotSize.height / 600);

    // y axis
    this.yScale = d3.scaleLinear([0, 0], [this.plotSize.height, 0]);

    this.yAxis = d3.axisLeft(this.yScale); // returns a function that takes 1 arg - a parent element it can mount
    // itself on
    this.yAxis.tickSizeInner(-this.plotSize.width); // negative height so the lines go through the chart
    this.yAxis.tickSizeOuter(0);

    this.yAxis.ticks(5);
    this.yAxis.tickPadding(10 * this.plotSize.width / 800);
  }

  drawBarsAndAxes(chartData: BarChartDataPoint[], metric: 'value'): void {
    this.xScale.domain(chartData.map((d) => d.label));
    this.yScale.domain([0, d3.max(chartData, (d) => d[metric])] as [number, number]);

    const dur = 1000;
    const t = d3.transition().duration(dur);

    this.barsParent

      .selectAll<SVGRectElement, BarChartDataPoint>('.bar')
      // creates placeholders (you can't see) with the nonexistent "bar" class, waiting to be told what to be, and
      // binds the data in the array to the placeholders - selectAll gets multiple elements
      .data<BarChartDataPoint>(
        chartData,
        (datum) => datum.label,
      )

      // tell the placeholders what they are
      .join(
        (enter) => enter
          .append<SVGRectElement>('rect')
          .attr('class', 'bar')

          .attr('y', (_d) => this.yScale(0))
          .attr('x', (d) => +this.xScale(d.label)!)
          .attr('width', (_d) => this.xScale.bandwidth())
          .style('fill', this.barColor)

          .transition(t)
          .attr('y', (d) => this.yScale(d[metric]))
          .attr('height', (d) => this.yScale(0) - this.yScale(d[metric]))
          .style('fill', this.barColor)
          .style('fill-opacity', 1)
        ,
        (update) => update
          .transition(t)
          .delay((d, i) => i * 20)
          .attr('y',
            (d) => this.yScale(d[metric]))
          .attr('x',
            (d) => +this.xScale(d.label)!)
          .attr('width',
            (_d) => this.xScale.bandwidth())
          .attr('height',
            (d) => this.yScale(0) - this.yScale(d[metric]))
          .style('fill', this.barColor)
        ,
        (exit) => exit
          .transition()
          .duration(dur / 2)
          .style('fill-opacity', 0)
          .remove()
        ,
      );

    // Value labels
    this.valueLabelsParent
      .attr('pointer-events', 'none')
      .attr('text-anchor', 'middle')
      .style('user-select', 'none')
      .style('font-size', '20px')
      .selectAll<SVGTextElement, BarChartDataPoint>('text')
      .data<BarChartDataPoint>(
        chartData,
        (datum: BarChartDataPoint) => datum.label)
      .join(
        (enter) => enter
          .append('text')
          .text((d) => d.value)

          .style('fill-opacity', 0)

          .attr('y', (_d) => this.yScale(0) - 12)
          .attr('x', (d) => +this.xScale(d.label)! + (this.xScale.bandwidth() / 2))

          .transition(t)
          .attr('y', (d) => this.yScale(d[metric]) - 12)
          .attr('x', (d) => +this.xScale(d.label)! + (this.xScale.bandwidth() / 2))
          .style('fill-opacity', 1)
        ,
        (update) => update
          .style('fill-opacity', 0)
          .text((d) => d.value)
          .transition(t)
          .delay((d, i) => i * 20)
          .attr('y', (d) => this.yScale(d[metric]) - 12)
          .attr('x', (d) => +this.xScale(d.label)! + (this.xScale.bandwidth() / 2))
          .style('fill-opacity', 1)
        ,
        (exit) => exit
          .transition()
          .duration(dur / 2)
          .style('fill-opacity', 0)
          .remove(),
      );

    // place hoverable areas (not just the bars but the whole columns because the bars can be tiny!)
    const hoverables = this.hoverablesParent
      .selectAll<SVGRectElement, BarChartDataPoint>('rect')
      .data<BarChartDataPoint>(
        chartData,
        (datum: BarChartDataPoint) => datum.label)
      .join('rect')
      .attr('y', 0 - this.getMargin().top)
      .attr('height', this.plotSize.height + this.getMargin().top + this.getMargin().bottom)
      .attr('x', (d) => +this.xScale(d.label)!)
      .attr('width', (_d) => this.xScale.bandwidth())
      .style('fill', 'transparent');

    // add mouseover effect to hoverables
    addHoverEffect(
      hoverables,
      (transition) => transition.style('fill', 'rgba(0,0,0,0.01)'),
      (transition) => transition.style('fill', 'transparent'),
    );

    // add mouseover to hoverables to trigger the tooltip
    const tooltipPositionFn = (event: any, _d: BarChartDataPoint): void => positionTooltipNextToMouse(event,
      this.tooltip,
      () => this.wrapperSize,
      this.getMargin(),
      this.getTooltipOffset(),
      0.8, 0.8);
    const tooltipContentFn = (d: BarChartDataPoint): string => this.getTooltipContent(d);
    addTooltipToHoverables(
      hoverables,
      this.tooltip,
      tooltipContentFn,
      tooltipPositionFn,
    );

    // shorten the x axis labels (now that we know the bandwidth)
    const maxCharsPerBar = this.xScale.bandwidth() * .13;
    this.xAxis.tickFormat((domainValue, _index) => truncateWithEllipses(domainValue,
      maxCharsPerBar));

    // add axes (now that we have the labels)
    this.xAxisDraw.transition(t).call(this.xAxis);
    this.yAxisDraw.transition(t).call(this.yAxis);

    // styling
    this.svg.selectAll('.tick line')
      .style('stroke', '#999')
      .style('stroke-width', '0.3px');

    this.svg.selectAll('.x.axis .tick')
      .style('max-width', '40px')
      .style('overflow', 'hidden')
      .style('display', 'block')
      .style('white-space', 'nowrap')
      .style('overflow', 'hidden')
      .style('text-overflow', 'ellipsis')
      .style('-o-text-overflow', 'ellipsis');
  }

  private getTooltipContent(d: BarChartDataPoint): string {
    const xAxisTitle = this.xAxisTitle;
    const yAxisTitle = this.yAxisTitle;

    return `${xAxisTitle}: ${d.label}<br>${yAxisTitle}: ${d.value}`;
  }

  private getTooltipOffset(): { top: number, left: number, bottom: number, right: number } {
    return {left: -20, top: 50, right: -20, bottom: 30} // offset around cursor, leaving space to read
  }

}

