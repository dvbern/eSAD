import {hasValue, Nullish} from './nullish';

/**
 * Returns true for string values !== '' and !== null and !== undefined
 */
export function isFilledIn<T extends unknown | '' | Nullish>(
  value: T,
): value is Exclude<T, '' | Nullish> {
  return hasValue(value) && value !== '';
}

export function requireFilledIn<T>(
  value: T | '' | Nullish,
  description?: string,
): T {
  if (!isFilledIn(value)) {
    throw Error('values is not filled in: ' + (description ?? value));
  }

  return value as T;
}

/**
 * Parses a floating number from the given string.
 * Throws error if given value is empty, Nullish or not parseable as a number.
 */
export function requireNumber(value: string | '' | Nullish): number {
  const s = requireFilledIn(value);
  const n = Number.parseFloat(s);
  if (Number.isNaN(n)) {
    throw new Error(`value is not a number: ${value}`);
  }
  return n;
}

/**
 * trim empty | Nullish strings to undefined
 */
export function trimToUndefined<T extends string = string>(
  value: T | Nullish,
): Exclude<T, ''> | undefined {
  const trimmed = (value ?? '').trim();

  if (trimmed !== '') {
    // possible error: if T = enum and enum value is defined to start/end with whitespace.
    return trimmed as Exclude<T, ''>;
  }

  return undefined;
}

export function isEqualCaseInsensitive(s1: string | Nullish, s2: string | Nullish): boolean {
  return s1?.toLowerCase() === s2?.toLowerCase();
}
