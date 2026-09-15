import {
  BMI_CURVE_SETS,
  GEWICHT_CURVE_SETS,
  GROESSE_CURVE_SETS,
  PercentileCurveSet,
  percentileReferenceForAnlass,
} from './percentile-curve-sets';

/**
 * Guards the generated PCH 2026 curves. The values are transcribed from the supplementary tables of
 * Eiholzer et al. 2025 (J Clin Med 14:5912), so the spot checks below are read straight out of the
 * publication -- if the generator or the source CSVs are broken, these stop matching.
 */
describe('percentile-curve-sets', () => {

  describe('percentileReferenceForAnlass', () => {
    it.each([
      {gueltigAbWachstumskurvePch: '2026-10-06', anlassDatum: undefined, expected: 'BASEL_2019', description: 'no Anlass loaded yet'},
      {gueltigAbWachstumskurvePch: '2026-10-06', anlassDatum: '2020-01-01', expected: 'BASEL_2019', description: 'long before the cut-off'},
      {gueltigAbWachstumskurvePch: '2026-10-06', anlassDatum: '2026-06-09', expected: 'BASEL_2019', description: 'the day before the cut-off'},
      {gueltigAbWachstumskurvePch: '2026-10-06', anlassDatum: '2026-10-06', expected: 'PCH_2026', description: 'the cut-off day itself'},
      {gueltigAbWachstumskurvePch: '2026-10-06', anlassDatum: '2027-03-15', expected: 'PCH_2026', description: 'after the cut-off'},
    ])('returns $expected for $description', ({gueltigAbWachstumskurvePch, anlassDatum, expected}) => {
      expect(percentileReferenceForAnlass(gueltigAbWachstumskurvePch as never, anlassDatum as never)).toBe(expected);
    });
  });

  const allSets: Array<[string, PercentileCurveSet]> = [
    ['groesse male 2026', GROESSE_CURVE_SETS.PCH_2026.male],
    ['groesse female 2026', GROESSE_CURVE_SETS.PCH_2026.female],
    ['gewicht male 2026', GEWICHT_CURVE_SETS.PCH_2026.male],
    ['gewicht female 2026', GEWICHT_CURVE_SETS.PCH_2026.female],
    ['bmi male 2026', BMI_CURVE_SETS.PCH_2026.male],
    ['bmi female 2026', BMI_CURVE_SETS.PCH_2026.female],
    ['groesse male 2019', GROESSE_CURVE_SETS.BASEL_2019.male],
    ['groesse female 2019', GROESSE_CURVE_SETS.BASEL_2019.female],
    ['gewicht male 2019', GEWICHT_CURVE_SETS.BASEL_2019.male],
    ['gewicht female 2019', GEWICHT_CURVE_SETS.BASEL_2019.female],
    ['bmi male 2019', BMI_CURVE_SETS.BASEL_2019.male],
    ['bmi female 2019', BMI_CURVE_SETS.BASEL_2019.female],
  ];

  const generatedSets = allSets.filter(([name]) => name.endsWith('2026'));

  // Only the generated 2026 curves share one x grid across all seven lines. The old Basel curves were
  // traced off a printed chart by hand, so each line has its own handful of x values.
  describe.each(generatedSets)('%s', (_name, curveSet) => {
    it('has percentile lines on a common x grid that never cross', () => {
      const lines = [...curveSet.distribution.lines].sort((a, b) => a.percentile - b.percentile);

      // At every x a higher percentile must sit above a lower one. Crossing lines would mean the
      // columns got mixed up somewhere between the paper and the generated file.
      for (let i = 1; i < lines.length; i++) {
        const lower = lines[i - 1]!;
        const upper = lines[i]!;
        expect(upper.data.length).toBe(lower.data.length);

        upper.data.forEach((point, index) => {
          const below = lower.data[index]!;
          expect(point.x).toBe(below.x);
          expect(point.y).toBeGreaterThan(below.y);
        });
      }
    });

    // The old Basel curves do not satisfy this: they carry stray points outside the plotted range
    // (Groesse down to age 1, Gewicht down to 104 cm) which the chart's clip-path hides.
    it('stays inside its own axis ranges', () => {
      for (const line of curveSet.distribution.lines) {
        for (const point of line.data) {
          expect(point.x).toBeGreaterThanOrEqual(curveSet.xAxis.min);
          expect(point.x).toBeLessThanOrEqual(curveSet.xAxis.max);
          expect(point.y).toBeGreaterThanOrEqual(curveSet.yAxis.min);
          expect(point.y).toBeLessThanOrEqual(curveSet.yAxis.max);
        }
      }
    });
  });

  describe.each(allSets)('%s', (_name, curveSet) => {
    it('has strictly ascending x values', () => {
      for (const line of curveSet.distribution.lines) {
        const xs = line.data.map(point => point.x);
        expect(xs).toEqual([...xs].sort((a, b) => a - b));
        expect(new Set(xs).size).toBe(xs.length);
      }
    });

    it('has unique line ids', () => {
      const ids = curveSet.distribution.lines.map(line => line.id);
      expect(new Set(ids).size).toBe(ids.length);
    });
  });

  // The old curves must keep rendering exactly as they did before the 2026 reference was added, so
  // that Untersuchungen from before the cut-off are unaffected. The distribution files themselves are
  // untouched; these are the axis ranges that used to be the *_AXIS_SETTING_* constants of
  // PercentileDiagramConfigService, pinned here so a later edit cannot quietly move them.
  describe('BASEL_2019 keeps its original axis ranges', () => {
    it.each([
      ['groesse male x (Alter)', GROESSE_CURVE_SETS.BASEL_2019.male.xAxis, {min: 2, max: 18, interval: 1}],
      ['groesse male y (Grösse)', GROESSE_CURVE_SETS.BASEL_2019.male.yAxis, {min: 55, max: 200, interval: 10}],
      ['groesse female y (Grösse)', GROESSE_CURVE_SETS.BASEL_2019.female.yAxis, {min: 55, max: 200, interval: 10}],
      ['gewicht male x (Grösse)', GEWICHT_CURVE_SETS.BASEL_2019.male.xAxis, {min: 105, max: 190, interval: 10}],
      ['gewicht male y (Gewicht)', GEWICHT_CURVE_SETS.BASEL_2019.male.yAxis, {min: 10, max: 100, interval: 5}],
      ['gewicht female x (Grösse)', GEWICHT_CURVE_SETS.BASEL_2019.female.xAxis, {min: 105, max: 190, interval: 10}],
      ['gewicht female y (Gewicht)', GEWICHT_CURVE_SETS.BASEL_2019.female.yAxis, {min: 10, max: 100, interval: 5}],
      ['bmi male x (Alter)', BMI_CURVE_SETS.BASEL_2019.male.xAxis, {min: 2, max: 18, interval: 1}],
      ['bmi male y (BMI)', BMI_CURVE_SETS.BASEL_2019.male.yAxis, {min: 12, max: 34, interval: 1}],
      ['bmi female y (BMI)', BMI_CURVE_SETS.BASEL_2019.female.yAxis, {min: 12, max: 34, interval: 1}],
    ])('%s', (_description, actual, expected) => {
      expect(actual).toEqual(expected);
    });

    it('still has the P99.5 line on the BMI charts', () => {
      for (const gender of ['male', 'female'] as const) {
        const percentiles = BMI_CURVE_SETS.BASEL_2019[gender].distribution.lines.map(l => l.percentile);
        expect(percentiles).toContain(99.5);
        expect(percentiles).toHaveLength(8);
      }
    });
  });

  describe('PCH 2026 values match the published tables', () => {
    const valueAt = (curveSet: PercentileCurveSet, percentile: number, x: number): number | undefined =>
      curveSet.distribution.lines
        .find(line => line.percentile === percentile)
        ?.data.find(point => point.x === x)
        ?.y;

    it.each([
      // [description, curve set, percentile, x, published value]
      ['BMI boys age 10 P50', BMI_CURVE_SETS.PCH_2026.male, 50, 10, 16.84],
      ['BMI boys age 18 P3', BMI_CURVE_SETS.PCH_2026.male, 3, 18, 17.53],
      ['BMI boys age 18 P97', BMI_CURVE_SETS.PCH_2026.male, 97, 18, 31.34],
      ['BMI girls age 10 P50', BMI_CURVE_SETS.PCH_2026.female, 50, 10, 16.87],
      ['BMI girls age 18 P97', BMI_CURVE_SETS.PCH_2026.female, 97, 18, 29.88],
      ['Groesse boys age 18 P50', GROESSE_CURVE_SETS.PCH_2026.male, 50, 18, 178.3],
      ['Groesse girls age 18 P50', GROESSE_CURVE_SETS.PCH_2026.female, 50, 18, 165.9],
      ['Gewicht boys at 150 cm P50', GEWICHT_CURVE_SETS.PCH_2026.male, 50, 150, 40.1],
      ['Gewicht girls at 150 cm P50', GEWICHT_CURVE_SETS.PCH_2026.female, 50, 150, 41.0],
    ])('%s', (_description, curveSet, percentile, x, expected) => {
      expect(valueAt(curveSet as PercentileCurveSet, percentile as number, x as number))
        .toBeCloseTo(expected as number, 2);
    });

    it('covers the full charted age range for BMI', () => {
      const line = BMI_CURVE_SETS.PCH_2026.male.distribution.lines[0]!;
      expect(line.data[0]!.x).toBe(2);
      expect(line.data[line.data.length - 1]!.x).toBe(18);
    });

    it('covers the charted age range 1-18 for Groesse', () => {
      for (const gender of ['male', 'female'] as const) {
        for (const line of GROESSE_CURVE_SETS.PCH_2026[gender].distribution.lines) {
          expect(line.data[0]!.x).toBe(1);
          expect(line.data[line.data.length - 1]!.x).toBe(18);
        }
      }
    });

    it('starts the Gewicht curves at 100 cm and runs to the end of the published table', () => {
      for (const [gender, lastHeight] of [['male', 200], ['female', 180]] as const) {
        for (const line of GEWICHT_CURVE_SETS.PCH_2026[gender].distribution.lines) {
          expect(line.data[0]!.x).toBe(100);
          expect(line.data[line.data.length - 1]!.x).toBe(lastHeight);
        }
      }
    });

    it('draws the curves out to the end of the Gewicht x axis', () => {
      for (const gender of ['male', 'female'] as const) {
        const curveSet = GEWICHT_CURVE_SETS.PCH_2026[gender];
        const line = curveSet.distribution.lines[0]!;
        expect(line.data[0]!.x).toBe(curveSet.xAxis.min);
        expect(line.data[line.data.length - 1]!.x).toBe(curveSet.xAxis.max);
      }
    });
  });
});
