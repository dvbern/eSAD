import * as d3 from 'd3';
import {curveMonotoneX, NumberValue} from 'd3';
import {AbstractChartD3} from '../_common/abstract-chart.d3';
import {PercentileChartData, PercentileLine, PercentileMainDataPoint} from './percentile-chart-data';
import {PercentileAxisSettings, PercentileSettings} from './percentile-chart-settings';

const genderIconPathFemale16 = 'M8 1a4 4 0 1 0 0 8 4 4 0 0 0 0-8M3 5a5 5 0 1 1 5.5 4.975V12h2a.5.5 0 0 1 0 1h-2v2.5a.5.5 0 0 1-1 0V13h-2a.5.5 0 0 1 0-1h2V9.975A5 5 0 0 1 3 5';
const genderIconPathMale16 = 'M9.5 2a.5.5 0 0 1 0-1h5a.5.5 0 0 1 .5.5v5a.5.5 0 0 1-1 0V2.707L9.871 6.836a5 5 0 1 1-.707-.707L13.293 2zM6 6a4 4 0 1 0 0 8 4 4 0 0 0 0-8';

const localeCH_DE = d3.formatLocale({
  decimal: '.',
  thousands: '’',
  grouping: [3],
  currency: ['CHF\u00a0', ''],
  minus: '\u2212',
  percent: '\u202f%',
});

const printValueAndUnitFnY = (axis: PercentileAxisSettings, value: number) => {
  const format = axis.valueDecimalPlaces
    ? `,.${axis.valueDecimalPlaces}f`
    : ',.0f';
  const formattedValue = localeCH_DE.format(format)(value);
  return [axis.unitPrefix, formattedValue, axis.unitPostfix]
    .filter(each => each)
    .join('');
};

const printValueAndUnitFnX = (axis: PercentileAxisSettings, value: string | undefined) => {
  return [axis.unitPrefix, value, axis.unitPostfix]
    .filter(each => each)
    .join('');
};

const printTickFn = (axis: PercentileAxisSettings, value: NumberValue) => {

  const format = axis.tickDecimalPlaces
    ? `,.${axis.tickDecimalPlaces}f`
    : ',.0f';
  const formattedValue = localeCH_DE.format(format)(value);
  return formattedValue;
};

let uniqueId = 0;

/**
 * PercentileChartD3:
 *  an x-y-chart with a few main data points and a few background lines for the percentiles
 */
export class PercentileChartD3 extends AbstractChartD3<PercentileChartData> {

  constructor() {
    super();
  }

  protected id = uniqueId++;

  public defaultSettings: PercentileSettings = {
    colors: {
      mainDotColor: '#26334d',
      mainDotLabelsColor: '#26334d',

      tickLabelsColor: '#8192b5',
      genderIconColor: '#c3cad9',
      axisLabelYColor: '#26334d',
      axisLabelXColor: '#8192b5',

      gridColor: '#f1f1f1',
      outsideRangeBgColor: 'none', //'#f7f8fa',

      percentileLabelColor: '#8192b5',
      percentileLineColor: '#c3cad9',
      percentileLineMinorColor: '#c3cad9',
      percentileLine50Color: '#c3cad9',
    },
    xAxis: {
      title: 'Zeit',
      unit: 'Stunden',
      unitPrefix: '',
      unitPostfix: ' min',
      interval: 1,
      min: 0,
      max: 24,
    },
    yAxis: {
      title: 'Sonnenstand',
      unit: '',
      unitPrefix: '',
      unitPostfix: '°',
      interval: 15,
      min: 0,
      max: 90,
    },
  };

  private settings: PercentileSettings = this.defaultSettings;

  // fontsizes
  fontsize14 = 14;
  fontsize16 = 16;
  fontsize20 = 20;
  fontBelowBaseline = 0.2;
  fontweightNormal = '400';
  fontweightBold = '700';

  // sizes
  tickPadding = 10;
  axisLabelPaddingX = 10;
  axisLabelPaddingY = 10;
  mainDotSize = 4;
  dotLabelOffsetX = 16;
  dotLabelOffsetY = 7;
  genderIconSize = 50;

  // transitions
  dataTransitionDuration = 600;
  gridTransitionDuration = 0;
  percentileLineTransitionDuration = 0;

  private percentilesParent!: d3.Selection<SVGGElement, any, any, any>;
  private bgParent!: d3.Selection<SVGGElement, any, any, any>;
  private genderIconParent!: d3.Selection<SVGGElement, any, any, any>;
  private percentilesLabelParent!: d3.Selection<SVGGElement, any, any, any>;
  private mainDotsParent!: d3.Selection<SVGGElement, any, any, any>;

  private xAxis!: d3.Axis<NumberValue>;
  private xScale!: d3.ScaleLinear<number, number>;
  private xAxisDraw!: d3.Selection<SVGGElement, any, any, any>;

  private yAxis!: d3.Axis<NumberValue>;
  private yScale!: d3.ScaleLinear<number, number>;
  private yAxisDraw!: d3.Selection<SVGGElement, any, any, any>;

  public override onChangedData(): void {

    // update settings first
    this.settings = {
      // colors have defaults that can be overridden
      colors: {...this.defaultSettings.colors, ...this.myDataset!.settings!.colors},

      // axes need to be defined completely
      xAxis: this.myDataset!.settings!.xAxis,
      yAxis: this.myDataset!.settings!.yAxis,
    };

    // scales and axes
    if (this.xAxisDraw && this.yAxisDraw) {
      this.initScalesAndAxes();
    }
    if (this.plotArea) {
      this.drawAxisTitles();
    }

    // data
    this.redrawPercentilesAndPoints();
  }

  override getMargin(): { top: number; right: number; bottom: number; left: number } {
    return {
      top: (.5 * this.fontsize14), // für das oberste y-Achsen-Tick-Label
      right: this.fontsize16 * 3, // für die Perzentil-Labels, z.B. P 99.5
      bottom: (.5 * this.fontsize14) + this.tickPadding + this.fontsize14, // für die x-Achsen-Tick-Labels
      left: this.fontsize16 * 3, // fuer die Beschriftung der y-Achse
    };
  }

  override afterResizedWindow(): void {
    this.updateClipPath();
    this.initScalesAndAxes();
    this.redrawPercentilesAndPoints();
  }

  override setup(): void {
    this.initSvg();
    this.initElements();

    this.redrawPercentilesAndPoints();
  }

  private initElements(): void {


    // clip-path (mask): we need to clip the percentile lines and background areas to the plotArea
    this.svg.select('defs')
      .append('clipPath')
      .attr('id', 'plotAreaMask' + this.id)
      .append('rect')
      .attr('x', 0)
      .attr('y', 0)
      .attr('width', this.plotSize.width)
      .attr('height', this.plotSize.height);

    // background
    this.bgParent = this.plotArea.append('g')
      .attr('class', 'background')
      .attr('clip-path', `url(#plotAreaMask${this.id})`);

    // the axis parent
    this.xAxisDraw = this.plotArea.append('g')
      .attr('class', 'x axis')
      .style('font-family', this.fontFamily)
      .style('font-size', `${this.fontsize14}px`);
    this.yAxisDraw = this.plotArea.append('g')
      .attr('class', 'y axis')
      .style('font-family', this.fontFamily)
      .style('font-size', `${this.fontsize14}px`);

    // gender icon parent
    this.genderIconParent = this.plotArea.append('g')
      .attr('class', 'gendericon')
      .attr('transform', `translate(${this.getGenderIconPosition().x},${this.getGenderIconPosition().y})`);

    // the percentiles parent
    this.percentilesParent = this.plotArea.append('g')
      .attr('class', 'percentiles')
      .attr('clip-path', `url(#plotAreaMask${this.id})`);
    this.percentilesLabelParent = this.plotArea.append('g')
      .attr('class', 'percentileLabels')
      .style('font-size', `${this.fontsize14}px`);

    // the mainDots parent
    this.mainDotsParent = this.plotArea.append('g')
      .attr('class', 'mainDots')
      .attr('clip-path', `url(#plotAreaMask${this.id})`);

    this.initScalesAndAxes();
  }

  private getGenderIconPosition() {
    return {
      x: this.tickPadding,
      y: this.fontsize20 + (this.tickPadding * 2),
    };
  }

  private updateClipPath() {
    this.svg.select(`#plotAreaMask${this.id}`)
      .select('rect')
      .attr('width', this.plotSize.width)
      .attr('height', this.plotSize.height);
  }

  private initScalesAndAxes(): void {

      this.xAxisDraw
        .attr('transform', `translate(0,${this.plotSize.height})`)
        .style('color', this.settings.colors.gridColor);
      this.yAxisDraw
        .style('color', this.settings.colors.gridColor);

      // x axis
      this.xScale = d3.scaleLinear([this.settings.xAxis.min, this.settings.xAxis.max], [0, this.plotSize.width]);
      this.xAxis = d3.axisBottom(this.xScale); // returns a function that takes 1 arg - a parent element it can mount
      // itself on
      this.xAxis.tickSizeInner(-this.plotSize.height); // negative height so the lines go through the chart
      this.xAxis.tickSizeOuter(0);
      this.xAxis.ticks((this.settings.xAxis.max - this.settings.xAxis.min) / this.settings.xAxis.interval); // draw each
                                                                                                            // integer
      //this.xAxis.ticks(10);
      this.xAxis.tickPadding(this.tickPadding);
      this.xAxis.tickFormat((d) => printTickFn(this.settings.xAxis, d));

      this.xAxisDraw
        .transition(d3.transition('xaxis').duration(this.gridTransitionDuration))
        .call(this.xAxis);
      this.xAxisDraw.selectAll('text')
        .style('color', this.settings.colors.tickLabelsColor);

      // y axis
      this.yScale = d3.scaleLinear([this.settings.yAxis.min, this.settings.yAxis.max], [this.plotSize.height, 0]);
      this.yAxis = d3.axisLeft(this.yScale);
      this.yAxis.tickSizeInner(-this.plotSize.width); // negative height so the lines go through the chart
      this.yAxis.tickSizeOuter(0);
      this.yAxis.ticks((this.settings.yAxis.max - this.settings.yAxis.min) / this.settings.yAxis.interval); // draw each
                                                                                                            // integer
      this.yAxis.tickPadding(this.tickPadding);
      this.yAxis.tickFormat((d) => printTickFn(this.settings.yAxis, d));
      this.yAxisDraw
        .transition(d3.transition('yaxis').duration(this.gridTransitionDuration))
        .call(this.yAxis);
      this.yAxisDraw.selectAll('text')
        .style('color', this.settings.colors.tickLabelsColor);

      this.drawAxisTitles();
    }

  private redrawPercentilesAndPoints() {
    if (this.percentilesParent) {
      this.drawPercentiles();
    }
    if (this.mainDotsParent) {
      this.drawMainDots();
      this.drawMainDotLabels();
    }
  }

  private drawPercentiles(): void {

    const data = this.myDataset?.distribution?.lines;
    if (!data) {
      return;
    }

    const line = d3.line<{ x: number, y: number }>()
      .x(d => this.xScale(d.x))
      .y(d => this.yScale(d.y))
      // Curves: monotone x goes through the points and creates no extrema outside of points
      // https://d3js.org/d3-shape/curve
      .curve(curveMonotoneX);

    // Append a path for the line.
    const percentileColorFn = (d: PercentileLine, i: number, a: SVGPathElement[] | d3.ArrayLike<SVGPathElement>) => {

      switch (d.type) {
        case 'main':
          return this.settings.colors.percentileLine50Color;
        case 'top':
        case 'bottom':
          return this.settings.colors.percentileLineColor;
        case 'sub':
          return this.settings.colors.percentileLineMinorColor;
      }
    };
    const percentileWidthFn = (d: PercentileLine, i: number, a: SVGPathElement[] | d3.ArrayLike<SVGPathElement>) => {
      switch (d.type) {
        case 'main':
        case 'top':
        case 'bottom':
          return 1;
        case 'sub':
          return 1;
      }
    };
    const percentileDashedFn = (d: PercentileLine, i: number, a: SVGPathElement[] | d3.ArrayLike<SVGPathElement>) => {
      switch (d.type) {
        case 'main':
        case 'top':
        case 'bottom':
          return '1,0';
        case 'sub':
          return '6,12';
      }
    };
    const percentileFontweightFn = (
      d: PercentileLine,
      i: number,
      a: SVGTextElement[] | d3.ArrayLike<SVGTextElement>,
    ) => {
      switch (d.type) {
        case 'main':
        case 'top':
        case 'bottom':
          return this.fontweightBold;
        case 'sub':
          return this.fontweightNormal;
      }
    };

    this.drawPercentileOutsideAreas(data);

    this.percentilesParent
      .selectAll<SVGPathElement, PercentileLine>('path.percentile-line')
      .data<PercentileLine>(data,
        (d) => d.id)
      .join(
        (enter) => enter
          .append('path')
          .attr('class', 'percentile-line')
          .attr('id', d => this.settings.xAxis.title + this.settings.yAxis.title + 'line' + d.percentile)
          .attr('fill', 'none')
          .attr('stroke', percentileColorFn)
          .attr('stroke-width', percentileWidthFn)
          .attr('stroke-dasharray', percentileDashedFn)
          .attr('d', d => line(d.data))
          .style('stroke-opacity', 0)
          .transition(d3.transition('percentile-line-enter').duration(this.percentileLineTransitionDuration))
          .style('stroke-opacity', 1),
        (update) => update
          .transition(d3.transition('percentile-line-update').duration(this.percentileLineTransitionDuration))
          .attr('d', d => line(d.data))
        ,
        (exit) => exit
          .transition(d3.transition('percentile-line-exit').duration(this.percentileLineTransitionDuration))
          .style('stroke-opacity', 0)
          .remove(),
      );

    // percentile label
    this.percentilesLabelParent
      .selectAll<SVGTextElement, PercentileLine>('text')
      .data<PercentileLine>(data, d => d.percentile)
      .join(
        (enter) => enter
          .append('text')
          .text(d => 'P ' + d.percentile)
          .style('font-weight', percentileFontweightFn)
          .attr('x', this.xScale(this.settings.xAxis.max))
          .attr('y', d => this.yScale(d.data[d.data.length - 1]!.y))
          .style('fill', this.settings.colors.percentileLabelColor)
          .attr('dx', 10)
          .style('fill-opacity', 0)
          .transition(d3.transition('percentile-label-enter').duration(this.percentileLineTransitionDuration))
          .style('fill-opacity', 1)
        ,
        (update) => update
          .style('fill', this.settings.colors.percentileLabelColor)
          .transition(d3.transition('percentile-label-update').duration(this.percentileLineTransitionDuration))
          .attr('x', this.xScale(this.settings.xAxis.max))
          .attr('y', d => this.yScale(d.data[d.data.length - 1]!.y)),
        (exit) => exit
          .transition(d3.transition('percentile-label-exit').duration(this.percentileLineTransitionDuration))
          .style('fill-opacity', 0)
          .remove(),
      );
  }

  drawPercentileOutsideAreas(data: PercentileLine[]) {

    // percentile outside areas
    const areaTop = d3.area<{ x: number, y: number }>()
      .x(d => this.xScale(d.x))
      .y0(0)
      .y1(d => this.yScale(d.y))
      .curve(curveMonotoneX);
    const areaBottom = d3.area<{ x: number, y: number }>()
      .x(d => this.xScale(d.x))
      .y0(this.plotSize.height)
      .y1(d => this.yScale(d.y))
      .curve(curveMonotoneX);

    this.bgParent
      .selectAll<SVGPathElement, PercentileLine>('path.percentile-areaBottom')
      .data<PercentileLine>(data.filter(each => each.type === 'bottom'),
        d => '' + this.settings.yAxis.title + this.settings.yAxis.title + 'areaBottom' + d.percentile)
      .join(
        (enter) => enter
          .append('path')
          .attr('class', 'percentile-areaBottom')
          .attr('fill', this.settings.colors.outsideRangeBgColor)
          .attr('stroke', 'none')
          .attr('d', d => areaBottom(d.data)),
        (update) => update
          .attr('fill', this.settings.colors.outsideRangeBgColor)
          .attr('d', d => areaBottom(d.data)),
        (exit) => exit
          .style('fill-opacity', 0)
          .remove(),
      );
    this.bgParent
      .selectAll<SVGPathElement, PercentileLine>('path.percentile-areaTop')
      .data<PercentileLine>(data.filter(each => each.type === 'top'),
        d => this.settings.yAxis.title + this.settings.yAxis.title + 'areaTop' + d.percentile)
      .join(
        (enter) => enter
          .append('path')
          .attr('class', 'percentile-areaTop')
          .attr('fill', this.settings.colors.outsideRangeBgColor)
          .attr('stroke', 'none')
          .attr('d', d => areaTop(d.data)),
        (update) => update
          .attr('fill', this.settings.colors.outsideRangeBgColor)
          .attr('d', d => areaTop(d.data)),
        (exit) => exit
          .style('fill-opacity', 0)
          .remove(),
      );

  }

  public drawAxisTitles() {

    if (!this.plotArea) {
      return;
    }

    // y axis title
    const yAxisTitlePositionFn = {
      x: () => this.axisLabelPaddingX,
      y: () => this.axisLabelPaddingY + this.fontsize20 - (this.fontBelowBaseline * this.fontsize20),
    };
    const yAxisTitle = this.plotArea
      .selectAll('.y-axis-title')
      .data([0])
      .join(
        (enter) => enter.append('text')
          .attr('class', 'y-axis-title')
          .attr('x', yAxisTitlePositionFn.x())
          .attr('y', yAxisTitlePositionFn.y())
          .style('font-family', this.fontFamily),
        (update) => update
          .transition(d3.transition('yaxis-label-update').duration(this.gridTransitionDuration))
          .attr('x', yAxisTitlePositionFn.x())
          .attr('y', yAxisTitlePositionFn.y()),
      );

    yAxisTitle
      .selectAll('.name')
      .data(d => [d])
      .join(
        (enter) => enter.append('tspan')
          .attr('class', 'name')
          .attr('fill', this.settings.colors.axisLabelYColor)
          .style('font-weight', this.fontweightBold)
          .style('font-size', `${this.fontsize20}px`)
          .attr('dx', 0)
          .text(this.settings.yAxis.title),
        (update) => update
          .attr('fill', this.settings.colors.axisLabelYColor)
          .text(this.settings.yAxis.title),
      );

    yAxisTitle
      .selectAll('.unit')
      .data(d => [d])
      .join(
        (enter) => enter.append('tspan')
          .attr('class', 'unit')
          .attr('fill', this.settings.colors.axisLabelYColor)
          .style('font-weight', this.fontweightBold)
          .style('font-size', `${this.fontsize20}px`)
          .attr('dx', 0)
          .text(this.settings.yAxis.unit ? ' (' + this.settings.yAxis.unit + ')' : ''),
        (update) => update
          .attr('fill', this.settings.colors.axisLabelYColor)
          .text(this.settings.yAxis.unit ? ' (' + this.settings.yAxis.unit + ')' : ''),
      );

    // x axis title
    const xAxisTitlePositionFn = {
      x: () => this.plotSize.width - this.axisLabelPaddingX,
      y: () => this.plotSize.height - this.axisLabelPaddingY - (this.fontBelowBaseline * this.fontsize16),
    };
    const xAxisTitle = this.plotArea
      .selectAll('.x-axis-title')
      .data([0])
      .join(
        (enter) => enter.append('text')
          .attr('class', 'x-axis-title')
          .attr('x', xAxisTitlePositionFn.x())
          .attr('y', xAxisTitlePositionFn.y())
          .attr('text-anchor', 'end'),
        (update) => update
          .transition(d3.transition('xaxis-label-update').duration(this.gridTransitionDuration))
          .attr('x', xAxisTitlePositionFn.x())
          .attr('y', xAxisTitlePositionFn.y()),
      );

    xAxisTitle
      .selectAll('.name')
      .data(d => [d])
      .join(
        (enter) => enter.append('tspan')
          .attr('class', 'name')
          .attr('fill', this.settings.colors.axisLabelXColor)
          .style('font-weight', this.fontweightNormal)
          .style('font-size', `${this.fontsize16}px`)
          .attr('dx', 0)
          .text(this.settings.xAxis.title),
        (update) => update
          .attr('fill', this.settings.colors.axisLabelXColor)
          .text(this.settings.xAxis.title),
      );

    xAxisTitle
      .selectAll('.unit')
      .data(d => [d])
      .join(
        (enter) => enter.append('tspan')
          .attr('class', 'unit')
          .attr('fill', this.settings.colors.axisLabelXColor)
          .style('font-weight', this.fontweightNormal)
          .style('font-size', `${this.fontsize16}px`)
          .attr('dx', 0)
          .text(this.settings.xAxis.unit ? ' (' + this.settings.xAxis.unit + ')' : ''),
        (update) => update
          .attr('fill', this.settings.colors.axisLabelXColor)
          .text(this.settings.xAxis.unit ? ' (' + this.settings.xAxis.unit + ')' : ''),
      );


      this.genderIconParent
        .transition(d3.transition('gendericon-position').duration(this.gridTransitionDuration))
        .attr('transform', `translate(${this.getGenderIconPosition().x},${this.getGenderIconPosition().y})`);

    const genderIconOriginalSize = 16;
    const genderIconResize = this.genderIconSize / genderIconOriginalSize;
    this.genderIconParent
      .selectAll<SVGPathElement, PercentileChartData>('.gender-icon')
      .data([this.myDataset])
      .join(
        (enter) => enter.append('path')
          .attr('class', 'gender-icon')
          .attr('fill', this.settings.colors.genderIconColor)
          .attr('d', d => d?.distribution?.gender === 'female' ? genderIconPathFemale16 : genderIconPathMale16)
          .attr('transform', `scale(${genderIconResize},${genderIconResize})`),
        (update) => update
          .attr('fill', this.settings.colors.genderIconColor)
          .attr('d', d => d?.distribution?.gender === 'female' ? genderIconPathFemale16 : genderIconPathMale16),
        (exit) => exit
          .style('fill-opacity', 0)
          .remove(),
      )

  }

  private drawMainDots(): void {

    const data = Array.from(this.myDataset?.mainData?.values() ?? []);

    // DOTS
    this.mainDotsParent
      .selectAll<SVGCircleElement, PercentileMainDataPoint>('circle')
      .data<PercentileMainDataPoint>(data, d => d.id)
      .join(
        (enter) => enter
          .append('circle')
          .attr('fill', this.settings.colors.mainDotColor)
          .attr('stroke', 'none')
          .attr('r', 0)
          .attr('cx', d => this.xScale(d.x))
          .attr('cy', d => this.yScale(d.y))
          .transition(d3.transition('maindot-enter').duration(this.dataTransitionDuration))
          .attr('r', this.mainDotSize),
        (update) => update
          .transition(d3.transition('maindot-update').duration(this.dataTransitionDuration))
          .attr('cx', d => this.xScale(d.x))
          .attr('cy', d => this.yScale(d.y)),
        (exit) => exit
          .transition(d3.transition('maindot-exit').duration(this.dataTransitionDuration / 2))
          .attr('r', 0)
          .remove(),
      );
  }

  private drawMainDotLabels(): void {

    const data = Array.from(this.myDataset?.mainData?.values() ?? []);

    // LABELS
    const minLabelSpaceHeight = 2 * this.fontsize20;
    const minLabelSpaceWidth = 130;

    const labelPositionFn = (d: PercentileMainDataPoint) => `translate(

      ${(this.xScale(d.x) + minLabelSpaceWidth > this.plotSize.width)
      ? this.xScale(d.x) - this.dotLabelOffsetX
      : this.xScale(d.x) + this.dotLabelOffsetX},

       ${(this.yScale(d.y) + minLabelSpaceHeight > this.plotSize.height)
      ? this.yScale(d.y) - this.dotLabelOffsetY - 6
      : this.yScale(d.y) + this.dotLabelOffsetY})`;

    const labelTextAlignFn = (d: PercentileMainDataPoint) =>
      (this.xScale(d.x) + minLabelSpaceWidth > (this.plotSize.width))
        ? 'end'
        : 'start';

    const labelWrap = this.mainDotsParent
      .selectAll<SVGGElement, PercentileMainDataPoint>('.labelwrap')
      .data<PercentileMainDataPoint>(data, d => d.id)
      .join(
        (enter) => enter
          .append('g')
          .attr('class', 'labelwrap')
          .attr('transform', labelPositionFn)
          .style('fill-opacity', 0)
          .transition(d3.transition('labelwrap-enter').duration(this.dataTransitionDuration))
          .style('fill-opacity', 1),
        (update) => update
          .transition(d3.transition('labelwrap-update').duration(this.dataTransitionDuration))
          .attr('transform', labelPositionFn),
        (exit) => exit
          .transition(d3.transition('labelwrap-exit').duration(this.dataTransitionDuration / 2))
          .style('fill-opacity', 0)
          .remove(),
      );

    const labelTextWrap = labelWrap
      .selectAll<SVGTextElement, PercentileMainDataPoint>('.texts')
      .data(function(d) {
        return [d];
      })
      .join(
        (enter) => enter.append('g')
          .attr('class', 'texts')
          .attr('fill', this.settings.colors.mainDotLabelsColor)
          .attr('text-anchor', labelTextAlignFn),
        (update) => update
          .attr('text-anchor', labelTextAlignFn),
      );

    const lineheight = this.fontsize16 * 1.3;
    const labelYTextFn = (d: PercentileMainDataPoint) =>
      printValueAndUnitFnY(this.settings.yAxis, d.y);
    labelTextWrap
      .selectAll<SVGTextElement, PercentileMainDataPoint>('.labely')
      .data(function(d) {
        return [d];
      })
      .join(
        (enter) => enter
          .append('text')
          .attr('class', 'labely')
          .attr('font-weight', this.fontweightBold)
          .attr('font-size', this.fontsize20)
          .attr('x', 0)
          .attr('y', 0)
          .attr('dy', 0 * lineheight)
          .text(labelYTextFn),
        (update) => update
          .text(labelYTextFn),
      );
    const labelXTextFn = (d: PercentileMainDataPoint) =>
      [ '(', printValueAndUnitFnX(this.settings.xAxis, d.label), ')'].join('');
    labelTextWrap
      .selectAll<SVGTextElement, PercentileMainDataPoint>('.labelx')
      .data(function(d) {
        return [d];
      })
      .join(
        (enter) => enter
          .append('text')
          .attr('class', 'labelx')
          .attr('font-weight', this.fontweightBold)
          .attr('font-size', this.fontsize16)
          .attr('x', 0)
          .attr('y', 0)
          .attr('dy', 1 * lineheight)
          .text(labelXTextFn),
        (update) => update
          .text(labelXTextFn),
      );
  }

}
