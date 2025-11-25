import {isISODateString, isISODateTimeString, ISODateString, ISODateTimeString} from '@dvb/data-access-types';
import {addDays, format, formatISO, Locale, parseISO} from 'date-fns';
import {isNotNullish, isNullish, Nullish} from '../nullish';

export function today(): Date {
  const now = new Date(Date.now());
  return new Date(now.setUTCHours(0, 0, 0, 0));
}

export function tomorrow(): Date {
  const tomorrow = addDays(Date.now(), 1);
  return new Date(tomorrow.setUTCHours(0, 0, 0, 0));
}

export function fromISODateTimeString<T extends string | Nullish>(
  value: T,
): T extends Nullish ? Date | (T & Nullish) : Date {
  if (isNullish(value)) {
    return value as Date & (T & Nullish);
  }

  if (!isISODateTimeString(value)) {
    throw Error('Invalid ISODateTimeString: ' + value);
  }

  // Is there a better way?
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  return parseISO(value) as unknown as any;
}

export function fromISODateString<T extends string | Nullish>(
  value: T,
): T extends Nullish ? Date | (T & Nullish) : Date {
  if (isNullish(value)) {
    return value as Date & (T & Nullish);
  }

  if (!isISODateString(value)) {
    throw Error('Invalid ISODateString: ' + value);
  }

  // Is there a better way?
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  return parseISO(value) as unknown as any;
}

export function toISODateTimeString<T extends Date | Nullish>(
  value: T,
): T extends Nullish ? ISODateTimeString | (T & Nullish) : ISODateTimeString {
  if (isNullish(value)) {
    return value as ISODateTimeString & (T & Nullish);
  }

  const result = value.toISOString();
  if (!isISODateTimeString(result)) {
    throw Error(`Could not convert ${value} to ISODateTimeString`);
  }

  // Is there a better way?
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  return result as unknown as any;
}

export function toISODateString<T extends Date | Nullish>(
  value: T,
): T extends Nullish ? ISODateString | (T & Nullish) : ISODateString {
  if (isNullish(value)) {
    return value as ISODateString & (T & Nullish);
  }
  const result = formatISO(value, { representation: 'date' });
  // Is there a better way?
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  return result as unknown as any;
}

export function truncateToDate<T extends ISODateTimeString | Date | Nullish>(
  utcDate: T,
): T extends Nullish ? ISODateString | (T & Nullish) : ISODateString {
  if (isNullish(utcDate)) {
    return utcDate as ISODateString & (T & Nullish);
  }

  const isoString: string = utcDate instanceof Date
    ? utcDate.toISOString()
    : utcDate;

  const result = isoString.replace(/T.*/, '') as ISODateString;

  if (!isISODateString(result)) {
    throw Error(`Could not convert ${utcDate} to ISODateString`);
  }

  // Is there a better way?
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  return result as unknown as any;
}

/**
 * Returns the date at the start of the day (at 00:00:00) in UTC.
 */
export function atStartOfDay<T extends ISODateString | Nullish>(
  utcDate: T,
): T extends Nullish ? ISODateTimeString | (T & Nullish) : ISODateTimeString {
  if (isNullish(utcDate)) {
    return utcDate as ISODateTimeString & (T & Nullish);
  }

  const resultDate = new Date(utcDate);
  resultDate.setUTCHours(0, 0, 0, 0);

  const result = toISODateTimeString(resultDate);

  // Is there a better way?
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  return result as unknown as any;
}

/**
 * Macht aus einem ISODateTimeString einen string im Format dd.MM.yyyy.
 * @param date to be transformed
 * @param locale current locale
 */
export function formatDateStr(
  date: ISODateTimeString | undefined,
  locale?: Locale,
): string {
  if (isNotNullish(date)) {
    return toDateStr(truncateToDate(date), locale);
  }
  return '';
}

/**
 * Gibt einen string im Format dd.MM.yyyy zurück.
 * @param utcDate Datum in UTC
 * @param locale current locale
 */
export function toDateStr(
  utcDate: Date | ISODateString,
  locale?: Locale,
): string {

  const date = isISODateString(utcDate) ? new Date(utcDate) : utcDate;
  return format(date, 'dd.MM.yyyy', { locale });
}

/**
 * Gibt einen string im Format `dd.MM.yyyy, hh:mm` zurück (ohne Sekunden, im 24h format).
 * @param utcDate
 * @param locale
 */
export function toDateTimeStr(
  utcDate: Date | ISODateTimeString,
  locale?: Locale,
): string {
  const date = isISODateTimeString(utcDate) ? new Date(utcDate) : utcDate;
  return format(date, 'dd.MM.yyyy, kk:mm', {locale});
}

export function formatTimeWithDateIfNotToday(
  dateTimeString: ISODateTimeString | undefined,
  timeOnlyForToday = false,
): string | undefined {
  if (!isISODateTimeString(dateTimeString)) {
    return undefined;
  }

  const date = new Date(dateTimeString);
  const now = new Date();

  const isToday =
    date.getDate() === now.getDate() &&
    date.getMonth() === now.getMonth() &&
    date.getFullYear() === now.getFullYear();

  const time = date.toLocaleTimeString('de-CH', {
    hour: '2-digit',
    minute: '2-digit',
  });

  const fullDate = date.toLocaleDateString('de-CH', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
  });

  if (isToday) {
    return time;
  }

  // New behavior: return only date if timeOnlyForToday is true
  if (timeOnlyForToday) {
    return fullDate;
  }

  return `${fullDate}, ${time}`;
}
