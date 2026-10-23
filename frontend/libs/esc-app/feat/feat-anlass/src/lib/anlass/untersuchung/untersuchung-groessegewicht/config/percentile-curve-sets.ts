import {ISODateString} from '@dvb/data-access-types';
import {PercentileDistribution, PercentileGender} from '@dvb/viz';
import {BaselBmiMaedchenDistribution} from './percentile-distributions-bmi-female';
import {Pch2026BmiMaedchenDistribution} from './percentile-distributions-bmi-female-2026';
import {BaselBmiKnabenDistribution} from './percentile-distributions-bmi-male';
import {Pch2026BmiKnabenDistribution} from './percentile-distributions-bmi-male-2026';
import {BaselGewichtMaedchenDistribution} from './percentile-distributions-gewicht-female';
import {Pch2026GewichtMaedchenDistribution} from './percentile-distributions-gewicht-female-2026';
import {BaselGewichtKnabenDistribution} from './percentile-distributions-gewicht-male';
import {Pch2026GewichtKnabenDistribution} from './percentile-distributions-gewicht-male-2026';
import {BaselGroesseMaedchenDistribution} from './percentile-distributions-groesse-female';
import {Pch2026GroesseMaedchenDistribution} from './percentile-distributions-groesse-female-2026';
import {BaselGroesseKnabenDistribution} from './percentile-distributions-groesse-male';
import {Pch2026GroesseKnabenDistribution} from './percentile-distributions-groesse-male-2026';

/**
 * Which set of reference curves a Perzentil diagram is drawn against.
 *
 * The reference changes over time, so both sets stay in the code: an Untersuchung has to be shown
 * against the curves that were in force when the Anlass took place, not against today's.
 */
export const PercentileReference = ['BASEL_2019', 'PCH_2026'] as const;

export type PercentileReference = typeof PercentileReference[number];

/**
 * Picks the reference for an Anlass. Without a date we stay on the old curves, so a not-yet-loaded
 * Anlass never briefly renders the wrong reference.
 */
export function percentileReferenceForAnlass(gueltigAbWachstumskurvePch: ISODateString, anlassDatum: ISODateString | undefined): PercentileReference {
  // ISODateString is `YYYY-MM-DD`, so comparing lexicographically is comparing chronologically.
  return anlassDatum && anlassDatum >= gueltigAbWachstumskurvePch ? 'PCH_2026' : 'BASEL_2019';
}

export interface PercentileAxisRange {
  min: number;
  max: number;
  /** Spacing between grid lines and tick labels. Belongs with the range: widen one, revisit the other. */
  interval: number;
}

/**
 * A distribution together with the axis ranges it is meant to be drawn in. The two belong together:
 * the 2026 reference covers different ranges than the old one, and for Gewicht the range even
 * differs per gender, so the axes cannot stay global constants.
 */
export interface PercentileCurveSet {
  distribution: PercentileDistribution;
  xAxis: PercentileAxisRange;
  yAxis: PercentileAxisRange;
}

type CurveSetsByGender = Record<PercentileGender, PercentileCurveSet>;
type CurveSetsByReference = Record<PercentileReference, CurveSetsByGender>;

/** For the charts where only the curves differ per gender, not the axes they are drawn in. */
function sharedAxes(
  maleDistribution: PercentileDistribution,
  femaleDistribution: PercentileDistribution,
  xAxis: PercentileAxisRange,
  yAxis: PercentileAxisRange,
): CurveSetsByGender {
  return {
    male: {distribution: maleDistribution, xAxis, yAxis},
    female: {distribution: femaleDistribution, xAxis, yAxis},
  };
}

// !!IMPORTANT!!
// Changes to any of these ranges have an impact on the percentile charts. If for example the max
// value of groesse is changed the chart will not display correctly. If values are changed here, make
// sure all the charts still behave and display correctly.

const ALTER_2_TO_18: PercentileAxisRange = {min: 2, max: 18, interval: 1};

/** The official Groesse chart is titled "Länge 1-18 Jahre alt" and the curves start at age 1. */
const ALTER_1_TO_18: PercentileAxisRange = {min: 1, max: 18, interval: 1};

export const GROESSE_CURVE_SETS: CurveSetsByReference = {
  BASEL_2019: sharedAxes(
    BaselGroesseKnabenDistribution,
    BaselGroesseMaedchenDistribution,
    ALTER_2_TO_18,
    {min: 55, max: 200, interval: 10},
  ),
  // Same y range as the old chart: at age 1 the P3 curve starts at 69 cm (girls) / 71 cm (boys), so
  // 55 costs a little space at the bottom but keeps both references visually comparable.
  PCH_2026: sharedAxes(
    Pch2026GroesseKnabenDistribution,
    Pch2026GroesseMaedchenDistribution,
    ALTER_1_TO_18,
    {min: 55, max: 200, interval: 10},
  ),
};

export const GEWICHT_CURVE_SETS: CurveSetsByReference = {
  BASEL_2019: sharedAxes(
    BaselGewichtKnabenDistribution,
    BaselGewichtMaedchenDistribution,
    {min: 105, max: 190, interval: 10},
    {min: 10, max: 100, interval: 5},
  ),
  // Gewicht für Länge is the one chart whose axes differ per gender: the published table runs to
  // 200 cm / 128 kg for boys but only to 180 cm / 95 kg for girls (table S7). The girls' CSV is
  // extended to 190 cm with the rows delivered by paediatrie schweiz (see
  // scripts/data/pch-2026/README.md), so the girls' curves now run to 190 cm as well. The girls'
  // y axis goes to 110 kg so P97 (~107.7 kg at 190 cm) stays fully visible; the boys' axis goes to
  // 130 kg because of their data.
  PCH_2026: {
    male: {
      distribution: Pch2026GewichtKnabenDistribution,
      xAxis: {min: 100, max: 200, interval: 10},
      yAxis: {min: 10, max: 130, interval: 5},
    },
    female: {
      distribution: Pch2026GewichtMaedchenDistribution,
      xAxis: {min: 100, max: 190, interval: 10},
      yAxis: {min: 10, max: 110, interval: 5},
    },
  },
};

export const BMI_CURVE_SETS: CurveSetsByReference = {
  BASEL_2019: sharedAxes(
    BaselBmiKnabenDistribution,
    BaselBmiMaedchenDistribution,
    ALTER_2_TO_18,
    {min: 12, max: 34, interval: 1},
  ),
  // Same range and grid as the old chart. The official chart runs to 50 to leave room for the
  // Adipositas zones, but those are out of scope here and the new curves fit entirely inside
  // 12-34 (P3 girls at age 2 is 12.87, P97 boys at 18 is 31.34).
  PCH_2026: sharedAxes(
    Pch2026BmiKnabenDistribution,
    Pch2026BmiMaedchenDistribution,
    ALTER_2_TO_18,
    {min: 12, max: 34, interval: 1},
  ),
};
