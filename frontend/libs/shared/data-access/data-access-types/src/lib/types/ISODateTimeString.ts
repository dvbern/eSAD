import {ISODateString} from './ISODateString';

/**
 * The backend does not differentiate between date and time formats.
 *
 * The only format requirement is ISO 8601 date with optional time - depending on usecase.
 */
export type ISODateTimeString =
  `${ISODateString}T${number}:${number}:${number}${string}`;

export function isISODateTimeString(
  value: unknown,
): value is ISODateTimeString {
  if (typeof value !== 'string') {
    return false;
  }

  if (!/^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}/.test(value)) {
    return false;
  }

  try {
    const valid = !isNaN(new Date(value).valueOf());

    return valid;
  } catch (_error: unknown) {
    return false;
  }
}
