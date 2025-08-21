/**
 * A string representing a date in ISO 8601 format, e.g. "2021-07-01".
 */
export type ISODateString = `${number}-${number}-${number}`;

export function isISODateString(value: unknown): value is ISODateString {
  const matchesPattern =
    typeof value === 'string' && /^\d\d\d\d-\d\d-\d\d$/.test(value);
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
