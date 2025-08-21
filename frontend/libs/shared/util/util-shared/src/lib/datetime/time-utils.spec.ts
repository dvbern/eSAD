import {truncateToDate} from './date-utils';
import {getDateTimeFromDateAndTime, getTimeFromISODateTimeString, ISOTime} from './time-utils';

describe('time-utils', () => {
  describe('getDateTimeFromDateAndTime', () => {
    it(`returns undefined for undefined input`, () => {
      const actual = getDateTimeFromDateAndTime(undefined, undefined);
      expect(actual).toBeUndefined();
    });

    it(`returns undefined for undefined input`, () => {
      const dateTime = truncateToDate(new Date('2020-01-01'));
      const actual = getDateTimeFromDateAndTime(dateTime, undefined);
      expect(actual).toBeUndefined();
    });

    it(`returns undefined for undefined input`, () => {
      const time = '12:00' as `${string}:${string}`;
      const actual = getDateTimeFromDateAndTime(undefined, time);
      expect(actual).toBeUndefined();
    });

    it.skip('converts correct dates', () => {
      const dateTime = truncateToDate(new Date('2020-01-01'));
      const time = '12:00' as `${string}:${string}`;
      const result = '2020-01-01T12:00:00';
      const actual = getDateTimeFromDateAndTime(dateTime, time);
      expect(actual).toEqual(result);
    });

    it.each([
      '-01:00' as ISOTime,
      '24:12' as ISOTime,
      '12:-01' as ISOTime,
      '12:60' as ISOTime,
      '' as ISOTime,
    ])('rejects incorrect Time: %s', (time) => {
      const dateTime = truncateToDate(new Date('2020-01-01'));

      const actual = getDateTimeFromDateAndTime(dateTime, time);
      expect(actual).toBeUndefined();
    });
  });

  describe('getTimeFromISODateTimeString', () => {
    it(`returns undefined for undefined input`, () => {
      const actual = getTimeFromISODateTimeString(undefined);
      expect(actual).toBeUndefined();
    });

    it('rejects for invalid Date', () => {
      const actual = getTimeFromISODateTimeString('2024-12-32T12:00:00.000Z');
      expect(actual).toBeUndefined();
    });

    it('converts for valid Date', () => {
      const actual = getTimeFromISODateTimeString('2024-01-01T12:30:00.000');
      expect(actual).toEqual('12:30');
    });

    it('converts for valid Date with 0 for single digits', () => {
      const actual = getTimeFromISODateTimeString('2024-01-01T09:00:00.000');
      expect(actual).toEqual('09:00');
    });
  });
});
