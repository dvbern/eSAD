/**
 * A string representing a year and a month, e.g. "2021-07".
 */
export type ISOYearMonthString = `${number}-${number}`;

export function isISOYearMonthString(value: unknown): value is ISOYearMonthString {
  const matchesPattern =
    typeof value === 'string' && /^\d\d\d\d-\d\d$/.test(value);
  if (!matchesPattern) {
    return false;
  }

  try {
    const valid = !isNaN(new Date(value).valueOf());

    return valid;
  } catch (_error: unknown) {
    return false;
  }
}
