// FIXME: fix module boundaries
// this is required to trick the compiler into not doing automatic type inferrence for test fixtures
import {ISODateString, ISODateTimeString} from '@dvb/data-access-types';
// which would making these tests useless.
/* eslint-disable @typescript-eslint/no-explicit-any */
import {atStartOfDay, fromISODateTimeString, toISODateTimeString, truncateToDate} from './date-utils';
import '@lukemorales/jest-type-matchers';

describe('date-utils', () => {
  // <editor-fold desc="fromISODateTimeString" defaultstate="collapsed">
  describe('fromISODateTimeString', () => {
    it.each([
      ['2021-07-01T12:34:56Z', new Date('2021-07-01T12:34:56.000Z')],
      ['2021-07-01T12:34:56.123Z', new Date('2021-07-01T12:34:56.123Z')],
    ])('converts an ISODateTimeString to a Date: %s', (input, expected) => {
      const actual = fromISODateTimeString(input);

      expect(actual).toEqual(expected);
    });

    it.each([null, undefined])(
      `returns null/undefined for null/undefined input: %s`,
      (input) => {
        const actual = fromISODateTimeString(input);

        expect(actual).toEqual(input);
      },
    );

    it.each([
      'asdfT123' as ISODateTimeString,
      '2021-07-01' as ISODateTimeString,
    ])('throws an error for invalid input: %s', (input) => {
      expect(() => fromISODateTimeString(input)).toThrowError();
    });

    describe('type-magic', () => {
      it('implements returning Non-Nullish ISODateString', () => {
        const input: ISODateTimeString = '2021-07-01T12:34:56Z' as any;

        const actual = fromISODateTimeString(input);

        expect(actual).toHaveStrictType<Date>();
      });

      it('returns a nullable ISODateString for nullable input', () => {
        const input: ISODateTimeString | null = '2021-07-01T12:34:56Z' as any;

        const actual = fromISODateTimeString(input);

        expect(actual).toHaveStrictType<Date | null>();
      });

      it('returns an undefinedable ISODateString for undefined input', () => {
        const input: ISODateTimeString | undefined =
          '2021-07-01T12:34:56Z' as any;

        const actual = fromISODateTimeString(input);

        expect(actual).toHaveStrictType<Date | undefined>();
      });

      it('returns null|undefined for null|undefined input', () => {
        const input: ISODateTimeString | null | undefined =
          '2021-07-01T12:34:56Z' as any;

        const actual = fromISODateTimeString(input);

        expect(actual).toHaveStrictType<Date | null | undefined>();
      });
    });
  });
  // </editor-fold>

  // <editor-fold desc="toISODateTimeString" defaultstate="collapsed">
  describe('toISODateTimeString', () => {
    it.each([
      ['2021-07-01', '2021-07-01T00:00:00.000Z'],
      ['2021-07-01T12:34:56Z', '2021-07-01T12:34:56.000Z'],
      ['2021-07-01T12:34:56.123Z', '2021-07-01T12:34:56.123Z'],
    ])('converts a Date to an ISODateTimeString: %s', (input, expected) => {
      const inputDate = new Date(input);

      const actual = toISODateTimeString(inputDate);

      expect(actual).toEqual(expected);
    });

    it.each([null, undefined])(
      `returns null/undefined for null/undefined input: %s`,
      (input) => {
        const actual = toISODateTimeString(input);

        expect(actual).toEqual(input);
      },
    );

    describe('type-magic', () => {
      it('implements returning Non-Nullish ISODateTimeString', () => {
        const input: Date = new Date('2021-07-01T12:34:56Z') as any;

        const actual = toISODateTimeString(input);

        expect(actual).toHaveStrictType<ISODateTimeString>();
      });

      it('returns a nullable ISODateTimeString for nullable input', () => {
        const input: Date | null = new Date('2021-07-01T12:34:56Z') as any;

        const actual = toISODateTimeString(input);

        expect(actual).toHaveStrictType<ISODateTimeString | null>();
      });

      it('returns an undefinedable ISODateTimeString for undefined input', () => {
        const input: Date | undefined = new Date('2021-07-01T12:34:56Z') as any;

        const actual = toISODateTimeString(input);

        expect(actual).toHaveStrictType<ISODateTimeString | undefined>();
      });

      it('returns null|undefined for null|undefined input', () => {
        const input: Date | null | undefined = new Date(
          '2021-07-01T12:34:56Z',
        ) as any;

        const actual = toISODateTimeString(input);

        expect(actual).toHaveStrictType<ISODateTimeString | null | undefined>();
      });
    });
  });
  // </editor-fold>

  // <editor-fold desc="truncateToDate" defaultstate="collapsed">
  describe('truncateToDate', () => {
    it('truncates the time part of an ISODateTimeString', () => {
      const utcDate = '2021-07-01T12:34:56Z';

      const result = truncateToDate(utcDate);

      expect(result).toEqual('2021-07-01');
    });

    it('truncates the time part of a Date', () => {
      const utcDate = new Date('2021-07-01T12:34:56Z');

      const result = truncateToDate(utcDate);

      expect(result).toEqual('2021-07-01');
    });

    it('supports a ISO date string without time', () => {
      const utcDate = '2021-07-01' as any;

      const result = truncateToDate(utcDate);

      expect(result).toEqual('2021-07-01');
    });

    it.each([null, undefined])(
      'returns null/undefined for null/undefined input: %s',
      (input) => {
        const result = truncateToDate(input);

        expect(result).toEqual(input);
      },
    );

    it('throws an error for invalid input', () => {
      const input = 'asdfT123' as ISODateTimeString;

      expect(() => truncateToDate(input)).toThrowError();
    });

    describe('type-magic', () => {
      it('implements returning Non-Nullish ISODateString', () => {
        const input: ISODateTimeString = '2021-07-01T12:34:56Z' as any;

        const actual = truncateToDate(input);

        expect(actual).toHaveStrictType<ISODateString>();
      });

      it('returns a nullable ISODateString for nullable input', () => {
        const input: ISODateTimeString | null = '2021-07-01T12:34:56Z' as any;

        const actual = truncateToDate(input);

        expect(actual).toHaveStrictType<ISODateString | null>();
      });

      it('returns an undefinedable ISODateString for undefined input', () => {
        const input: ISODateTimeString | undefined =
          '2021-07-01T12:34:56Z' as any;

        const actual = truncateToDate(input);

        expect(actual).toHaveStrictType<ISODateString | undefined>();
      });

      it('returns null|undefined for null|undefined input', () => {
        const input: ISODateTimeString | null | undefined =
          '2021-07-01T12:34:56Z' as any;

        const actual = truncateToDate(input);

        expect(actual).toHaveStrictType<ISODateString | null | undefined>();
      });
    });
  });
  // </editor-fold>

  // <editor-fold desc="atStartOfDay" defaultstate="collapsed">
  describe('atStartOfDay', () => {
    it('returns the date at midnight', () => {
      const utcDate = '2021-07-01';

      const result = atStartOfDay(utcDate);

      expect(result).toEqual('2021-07-01T00:00:00.000Z');
    });

    it.each([null, undefined])(
      'returns null/undefined for null/undefined input: %s',
      (input) => {
        const result = atStartOfDay(input);

        expect(result).toEqual(input);
      },
    );

    it('accepts an ISO date with time and sets the time to midnight', () => {
      const utcDate = '2021-07-01T12:34:56Z' as any;

      const result = atStartOfDay(utcDate);

      expect(result).toEqual('2021-07-01T00:00:00.000Z');
    });

    describe('type-magic', () => {
      it('implements returning Non-Nullish ISODateTimeString', () => {
        const input: ISODateString = '2021-07-01' as any;
        const actual = atStartOfDay(input);

        expect(actual).toHaveStrictType<ISODateTimeString>();
      });

      it('returns a nullable ISODateTimeString for nullable input', () => {
        const input: ISODateString | null = '2021-07-01' as any;
        const actual = atStartOfDay(input);

        expect(actual).toHaveStrictType<ISODateTimeString | null>();
      });

      it('returns an undefinedable ISODateTimeString for undefined input', () => {
        const input: ISODateString | undefined = '2021-07-01' as any;
        const actual = atStartOfDay(input);

        expect(actual).toHaveStrictType<ISODateTimeString | undefined>();
      });

      it('returns null|undefined for null|undefined input', () => {
        const input: ISODateString | null | undefined = '2021-07-01' as any;
        const actual = atStartOfDay(input);

        expect(actual).toHaveStrictType<ISODateTimeString | null | undefined>();
      });
    });
  });
  // </editor-fold>
});
