import {isNotNullish, isNullish, Nullish} from "@dvb/util-shared";

const MONTHS_PER_YEAR = 12;

export function splitAnzahlMonateIntoMonthsAndYears(anzahlMonate: number | undefined): {jahre: number, monate: number} | Nullish {
  if (isNullish(anzahlMonate)) {
    return null;
  }

  return {
    jahre: Math.floor(anzahlMonate / MONTHS_PER_YEAR),
    monate: anzahlMonate % MONTHS_PER_YEAR,
  }
}

export function combineMonthsAndYearsIntoAnzahlMonths(jahre: number | Nullish, monate: number | Nullish): number | undefined {
  if (isNullish(jahre) && isNullish(monate)) {
    return undefined;
  }

  let result = 0;
  if (isNotNullish(jahre)) {
    result += +jahre * MONTHS_PER_YEAR;
  }
  if (isNotNullish(monate)) {
    result += +monate;
  }
  return result;
}
