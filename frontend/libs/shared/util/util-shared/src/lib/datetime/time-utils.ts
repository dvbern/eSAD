import {ISODateString, ISODateTimeString, ISOYearMonthString} from '@dvb/data-access-types';
import {format, isValid, parse, setHours, setMinutes, toDate} from 'date-fns';
import {isNullish} from '../nullish';

export type ISOTime = `${string}:${string}`;

export function isValidTime(time: string): boolean {
  const date: Date = parse(time, 'HH:mm', new Date());
  return isValid(date);
}

export function getDateTimeFromDateAndTime(
  isoDateString: ISODateString | undefined,
  isoTimeString: ISOTime | undefined | '',
): ISODateTimeString | undefined {

  if (isNullish(isoDateString) || isNullish(isoTimeString) || !isValidTime(isoTimeString)) {
    return undefined;
  }
  const [hours, minutes]: string[] = isoTimeString.split(':');
  let date = toDate(isoDateString);
  date = setHours(date, +hours);
  date = setMinutes(date, +minutes);
  if (isNullish(date) || !isValid(date)) {
    return undefined;
  }
  return format(date, 'yyyy-MM-dd\'T\'HH:mm:ss') as ISODateTimeString;
}

export function getTimeFromISODateTimeString(
  isoTimeString: ISODateTimeString | undefined,
): ISOTime | undefined {

  if (isNullish(isoTimeString)) {
    return undefined;
  }
  const date = toDate(isoTimeString);
  if (isNullish(date) || !isValid(date)) {
    return undefined;
  }
  return format(date, 'HH:mm') as ISOTime;
}

export function getDateFromISOYearMonthString(
  isoTimeString: ISOYearMonthString | undefined,
): ISODateString | undefined {

  if (isNullish(isoTimeString)) {
    return undefined;
  }
  const date = toDate(isoTimeString);
  if (isNullish(date) || !isValid(date)) {
    return undefined;
  }
  return format(date, 'yyyy-MM-dd') as ISODateString;
}

export function getYearMonthFromISODateString(
  isoTimeString: ISODateString | undefined,
): ISOYearMonthString | undefined {

  if (isNullish(isoTimeString)) {
    return undefined;
  }
  const date = toDate(isoTimeString);
  if (isNullish(date) || !isValid(date)) {
    return undefined;
  }
  return format(date, 'yyyy-MM') as ISOYearMonthString;
}

export function getSchoolYearFromISODateString(
  isoDateString: ISODateString | undefined,
): number | undefined {
  if (isNullish(isoDateString)) {
    return undefined;
  }
  const date = toDate(isoDateString);
  if (isNullish(date) || !isValid(date)) {
    return undefined;
  }
  // date.getMonth() < 7 because 7 is the value for august
  if (date.getMonth() < 7) {
    return date.getFullYear() - 1;
  }
  return date.getFullYear();
}



