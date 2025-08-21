export interface PercentileColors {
  mainDotColor: string;
  mainDotLabelsColor: string;

  outsideRangeBgColor: string;
  gridColor: string;

  tickLabelsColor: string;
  genderIconColor: string;
  axisLabelYColor: string;
  axisLabelXColor: string;

  percentileLabelColor: string;
  percentileLineColor: string;
  percentileLineMinorColor: string;
  percentileLine50Color: string;
}

export interface PercentileSettings {
  colors: PercentileColors;
  xAxis: PercentileAxisSettings;
  yAxis: PercentileAxisSettings;
}

export interface PercentilePartialSettings {
  colors: Partial<PercentileColors>;
  xAxis: PercentileAxisSettings;
  yAxis: PercentileAxisSettings;
}

export interface PercentileAxisSettings {
  title: string;
  unit: string;
  unitPrefix: string;
  unitPostfix: string;
  min: number;
  max: number;
  interval: number;
  valueDecimalPlaces?: number;
  tickDecimalPlaces?: number;
}
