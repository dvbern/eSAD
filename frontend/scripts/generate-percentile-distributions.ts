import * as fs from 'node:fs';
import * as path from 'node:path';

/**
 * Generates the PCH 2026 Perzentil curve data from the published reference tables.
 *
 * Input:  scripts/data/pch-2026/*.csv  (see the README there for provenance)
 * Output: libs/esc-app/feat/feat-anlass/.../untersuchung-groessegewicht/config/*-2026.ts
 *
 * The CSVs already contain the smoothed percentile values as published, so nothing is recomputed
 * here -- the generator only narrows each table to the range eSAD actually charts, rounds, and
 * writes it out as TypeScript. Run with:
 *
 *   npx tsx scripts/generate-percentile-distributions.ts
 */

const DATA_DIR = path.join(__dirname, 'data', 'pch-2026');
const OUT_DIR = path.join(
  __dirname,
  '..',
  'libs/esc-app/feat/feat-anlass/src/lib/anlass/untersuchung/untersuchung-groessegewicht/config',
);

/** The seven percentile lines of the published tables, in drawing order. */
const PERCENTILES = [3, 10, 25, 50, 75, 90, 97] as const;

/**
 * P3 and P97 bound the reference range and are drawn emphasised, P50 is the median.
 * Matches how the existing Groesse curves are typed.
 */
function lineType(percentile: number): string {
  switch (percentile) {
    case 3:
      return 'bottom';
    case 97:
      return 'top';
    case 50:
      return 'main';
    default:
      return 'sub';
  }
}

type Row = Record<string, number>;

function readTable(file: string): Row[] {
  const lines = fs
    .readFileSync(path.join(DATA_DIR, file), 'utf-8')
    .split('\n')
    .filter(line => line.trim() !== '' && !line.startsWith('#'));

  const header = lines[0]!.split(',');
  return lines.slice(1).map(line => {
    const cells = line.split(',');
    const row: Row = {};
    header.forEach((name, i) => (row[name] = Number(cells[i])));
    return row;
  });
}

interface ChartSpec {
  /** Basename of the generated file, without extension. */
  outFile: string;
  /** Name of the exported constant. */
  constName: string;
  gender: 'male' | 'female';
  /** Prefix for the line ids, which must be unique per chart. */
  idPrefix: string;
  sourceFile: string;
  /** Column holding the x value. */
  xColumn: 'age' | 'height';
  xMin: number;
  xMax: number;
  /**
   * Only keep rows whose x is a multiple of this. The age tables are published in monthly steps up
   * to age 2 and in half-year steps above it; without this the first year would be drawn at twelve
   * times the resolution of the rest for no gain.
   */
  xStep?: number;
  /** Decimal places for the y values. */
  yDecimals: number;
}

const CHARTS: ChartSpec[] = [
  // Groesse: age 1-18, as the official chart ("Laenge 1-18 Jahre alt"). The published table starts
  // at 0; the first year of life is not charted in eSAD.
  {
    outFile: 'percentile-distributions-groesse-male-2026',
    constName: 'Pch2026GroesseKnabenDistribution',
    gender: 'male',
    idPrefix: 'groesseMale2026',
    sourceFile: 'height-boys.csv',
    xColumn: 'age',
    xMin: 1,
    xMax: 18,
    xStep: 0.5,
    yDecimals: 1,
  },
  {
    outFile: 'percentile-distributions-groesse-female-2026',
    constName: 'Pch2026GroesseMaedchenDistribution',
    gender: 'female',
    idPrefix: 'groesseFemale2026',
    sourceFile: 'height-girls.csv',
    xColumn: 'age',
    xMin: 1,
    xMax: 18,
    xStep: 0.5,
    yDecimals: 1,
  },

  // BMI: age 2-18. The official chart runs 0-18, but the Swiss reference itself starts at 2 and the
  // 0-2 range would come from WHO -- out of scope for a school application.
  {
    outFile: 'percentile-distributions-bmi-male-2026',
    constName: 'Pch2026BmiKnabenDistribution',
    gender: 'male',
    idPrefix: 'bmiMale2026',
    sourceFile: 'bmi-boys.csv',
    xColumn: 'age',
    xMin: 2,
    xMax: 18,
    xStep: 0.5,
    yDecimals: 2,
  },
  {
    outFile: 'percentile-distributions-bmi-female-2026',
    constName: 'Pch2026BmiMaedchenDistribution',
    gender: 'female',
    idPrefix: 'bmiFemale2026',
    sourceFile: 'bmi-girls.csv',
    xColumn: 'age',
    xMin: 2,
    xMax: 18,
    xStep: 0.5,
    yDecimals: 2,
  },

  // Gewicht fuer Laenge, from 100 cm as requested. The paper publishes one continuous table; the two
  // printed charts (50-120 and 120-200 cm) are a paper-layout split. The girls' table ends at
  // 180 cm, the boys' at 200 cm.
  {
    outFile: 'percentile-distributions-gewicht-male-2026',
    constName: 'Pch2026GewichtKnabenDistribution',
    gender: 'male',
    idPrefix: 'gewichtMale2026',
    sourceFile: 'weight-for-height-boys.csv',
    xColumn: 'height',
    xMin: 100,
    xMax: 200,
    yDecimals: 1,
  },
  {
    outFile: 'percentile-distributions-gewicht-female-2026',
    constName: 'Pch2026GewichtMaedchenDistribution',
    gender: 'female',
    idPrefix: 'gewichtFemale2026',
    sourceFile: 'weight-for-height-girls.csv',
    xColumn: 'height',
    xMin: 100,
    xMax: 180,
    yDecimals: 1,
  },
];

const HEADER = `// GENERATED FILE -- do not edit by hand.
// Run \`npx tsx scripts/generate-percentile-distributions.ts\` to regenerate.
//
// Wachstumskurven 2026, recommended by pädiatrie schweiz.
// Source: Eiholzer U, Stephan A, Dubinski I, Fritz C, Noordam C (2025) Updated Swiss Growth
// References 2025: No Height Differences, but BMI Variations Associated with Migration.
// J. Clin. Med. 14, 5912. https://doi.org/10.3390/jcm14165912
`;

function round(value: number, decimals: number): number {
  return Number(value.toFixed(decimals));
}

/** The published rows narrowed to the range this chart actually plots. */
function rowsInRange(spec: ChartSpec): Row[] {
  const rows = readTable(spec.sourceFile).filter(row => {
    const x = row[spec.xColumn]!;
    if (x < spec.xMin || x > spec.xMax) {
      return false;
    }

    // The published ages carry three decimals (1/12 steps), so compare on the rounded remainder.
    return spec.xStep === undefined || Math.abs(x / spec.xStep - Math.round(x / spec.xStep)) < 0.01;
  });

  if (rows.length === 0) {
    throw new Error(`No rows in range ${spec.xMin}..${spec.xMax} for ${spec.sourceFile}`);
  }

  return rows;
}

function generate(spec: ChartSpec, rows: Row[]): string {
  const lines = PERCENTILES.map(percentile => {
    const points = rows
      .map(row => `        {x: ${row[spec.xColumn]}, y: ${round(row['p' + percentile]!, spec.yDecimals)}},`)
      .join('\n');

    return `    {
      id: '${spec.idPrefix}P${percentile}',
      percentile: ${percentile},
      type: '${lineType(percentile)}',
      data: [
${points}
      ],
    },`;
  }).join('\n');

  return `${HEADER}
import {PercentileDistribution} from '@dvb/viz';

export const ${spec.constName}: PercentileDistribution = {
  gender: '${spec.gender}',
  lines: [
${lines}
  ],
};
`;
}

for (const spec of CHARTS) {
  const rows = rowsInRange(spec);
  fs.writeFileSync(path.join(OUT_DIR, `${spec.outFile}.ts`), generate(spec, rows), 'utf-8');
  console.log(`${spec.outFile}.ts  ${PERCENTILES.length} lines x ${rows.length} points`);
}
