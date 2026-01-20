import {TestBed} from '@angular/core/testing';
import {NonNullableFormBuilder} from '@angular/forms';
import {toDateStr, toISODateString} from '@dvb/util-shared';
import {AppValidatorKeys, AppValidators} from './AppValidators';
import {ISODateString} from "@dvb/data-access-types";

describe('AppValidators', () => {
  let fb: NonNullableFormBuilder;

  beforeAll(async () => {
    fb = TestBed.inject(NonNullableFormBuilder);
  });

  describe('numberOrNumericChars', () => {
    it.each([undefined, null, ''])('accepts for empty input', (input) => {
      const control = fb.control(input, {
        validators: [AppValidators.numberOrNumericChars()],
      });

      expect(control.valid).toBe(true);
      expect(control.errors).toBeNull();
    });

    it.each(['0', '123', 123])('accepts for valid input', (input) => {
      const control = fb.control(input, {
        validators: [AppValidators.numberOrNumericChars()],
      });

      expect(control.valid).toBe(true);
      expect(control.errors).toBeNull();
    });

    it.each(['', null, undefined])('accepts for empty input: %s', (input) => {
      const control = fb.control(input, {
        validators: [AppValidators.numberOrNumericChars()],
      });

      expect(control.valid).toBe(true);
      expect(control.errors).toBeNull();
    });

    it.each(['asdf', '-123', '-0', ' ', '     ', new Date()])(
      'recjects for invalid input: %s',
      (input) => {
        const control = fb.control(input, {
          validators: [AppValidators.numberOrNumericChars()],
        });

        expect(control.valid).toBe(false);
        expect(control.errors).toEqual({
          numberOrNumericChars: {
            actualValue: input,
          },
        });
      },
    );
  });

  describe('nonNegativNumber', () => {
    it.each([undefined, null])('accepts for nullish input', (input) => {
      const control = fb.control(input, {
        validators: [AppValidators.nonNegativNumber()],
      });

      expect(control.valid).toBe(true);
      expect(control.errors).toBeNull();
    });

    it.each([0, 1, 4.2, '0', '99'])('accepts for valid input', (input) => {
      const control = fb.control(input, {
        validators: [AppValidators.nonNegativNumber()],
      });

      expect(control.valid).toBe(true);
      expect(control.errors).toBeNull();
    });

    it.each([-1, -2.5, '-1', '-99'])('reject for invalid input', (input) => {
      const control = fb.control(input, {
        validators: [AppValidators.nonNegativNumber()],
      });

      expect(control.valid).toBe(false);
      expect(control.errors).toEqual({
        nonNegativeNumber: {
          actualValue: input,
        },
      });
    });
  });

  describe('date', () => {
    it.each([undefined, null])('accepts for nullish input', (input) => {
      const control = fb.control(input, {
        validators: [AppValidators.date()],
      });

      expect(control.valid).toBe(false);
      expect(control.errors).toEqual({
        date: {},
      });
    });

    it.each(['01/03/2024', '1/2/2024', '12/31/1999', new Date()])(
      'accepts for valid input',
      (input) => {
        const control = fb.control(input, {
          validators: [AppValidators.date()],
        });

        expect(control.valid).toBe(true);
        expect(control.errors).toBeNull();
      },
    );

    it.each(['13/01/2024', '13/01/100'])('recjects for input: %s', (input) => {
      const control = fb.control(input, {
        validators: [AppValidators.date()],
      });

      expect(control.valid).toBe(false);
      expect(control.errors).toEqual({
        date: {},
      });
    });
  });

  describe('dateRange', () => {
    it.each([undefined, null])('rejects for nullish input', (input) => {
      const afterDate = new Date(1970);
      const beforeDate = new Date(new Date().getFullYear() + 1);
      const control = fb.control(input, {
        validators: [AppValidators.dateRange(afterDate, beforeDate)],
      });

      expect(control.valid).toBe(false);
      expect(control.errors).toEqual({
        date: {},
      });
    });

    it.each([new Date(2000, 0, 2)])('accepts for valid input', (input) => {
      const afterDate = new Date(2000, 0, 1);
      const beforeDate = new Date(2000, 0, 3);
      const control = fb.control(input, {
        validators: [AppValidators.dateRange(afterDate, beforeDate)],
      });

      expect(control.valid).toBe(true);
      expect(control.errors).toBeNull();
    });

    it.each([new Date(1970, 0, 1)])('recjects for input', (input) => {
      const afterDate = new Date(2000, 0, 1);
      const beforeDate = new Date(2000, 0, 3);
      const control = fb.control(input, {
        validators: [AppValidators.dateRange(afterDate, beforeDate)],
      });

      expect(control.valid).toBe(false);
      expect(control.errors).toEqual({
        dateRange: {
          afterDate: toDateStr(beforeDate),
          beforeDate: toDateStr(afterDate),
        },
      });
    });
  });

  describe('dbMaxLength', () => {
    it.each([undefined, null])('accepts for nullish input', (input) => {
      const control = fb.control(input, {
        validators: [AppValidators.dbMaxLength()],
      });

      expect(control.valid).toBe(true);
      expect(control.errors).toBeNull();
    });

    it.each(['', 'a', 'a'.repeat(255)])('accepts for valid input', (input) => {
      const control = fb.control(input, {
        validators: [AppValidators.dbMaxLength()],
      });

      expect(control.valid).toBe(true);
      expect(control.errors).toBeNull();
    });

    it.each(['a'.repeat(256)])('reject for invalid input', (input) => {
      const control = fb.control(input, {
        validators: [AppValidators.dbMaxLength()],
      });

      expect(control.valid).toBe(false);
      expect(control.errors).toEqual({
        dbMaxLength: {
          actualLength: 256,
          requiredLength: 255,
        },
      });
    });
  });

  describe('textareaMaxLength', () => {
    it.each([undefined, null])('accepts for nullish input', (input) => {
      const control = fb.control(input, {
        validators: [AppValidators.textareaMaxLength2000()],
      });

      expect(control.valid).toBe(true);
      expect(control.errors).toBeNull();
    });

    it.each(['', 'a', 'a'.repeat(2000)])('accepts for valid input', (input) => {
      const control = fb.control(input, {
        validators: [AppValidators.textareaMaxLength2000()],
      });

      expect(control.valid).toBe(true);
      expect(control.errors).toBeNull();
    });

    it.each(['a'.repeat(2001)])('reject for invalid input', (input) => {
      const control = fb.control(input, {
        validators: [AppValidators.textareaMaxLength2000()],
      });

      expect(control.valid).toBe(false);
      expect(control.errors).toEqual({
        [AppValidatorKeys.textareaMaxLength]: {
          actualLength: 2001,
          requiredLength: 2000,
        },
      });
    });
  });

  describe('emailValidator', () => {
    it.each([undefined, null])('accepts for nullish input', (input) => {
      const control = fb.control(input, {
        validators: [AppValidators.emailValidator()],
      });

      expect(control.valid).toBe(true);
      expect(control.errors).toBeNull();
    });

    it.each(['test@mail.ch', 'blabla@mailbucket.dvbern.ch'])('accepts for valid input: %s', (input) => {
      const control = fb.control(input, {
        validators: [AppValidators.emailValidator()],
      });

      expect(control.valid).toBe(true);
      expect(control.errors).toBeNull();
    });

    it.each(['falsch@mailbucket', 'invalicMAil.ch'])('rejects invalid input: %s', (input) => {
      const control = fb.control(input, {
        validators: [AppValidators.emailValidator()],
      });

      expect(control.valid).toBe(false);
      expect(control.errors).toEqual({[AppValidatorKeys.email]: true});
    });
  });

  describe('phoneValidator', () => {
    it.each([undefined, null])('accepts for nullish input', (input) => {
      const control = fb.control(input, {
        validators: [AppValidators.phoneValidator()],
      });

      expect(control.valid).toBe(true);
      expect(control.errors).toBeNull();
    });

    it.each(
      [
        '0791231212',
        '00321231212',
        '+41791231212',
        '032 123 12 12',
        '+44 79 123 23 45',
      ])(
      'accepts for valid input: %s',
      (input) => {
        const control = fb.control(input, {
          validators: [AppValidators.phoneValidator()],
        });

        expect(control.valid).toBe(true);
        expect(control.errors).toBeNull();
      });

    it.each(
      ['032', 'telefonNummer', '79 123 12 12', '032 123'],
    )('rejects invalid input: %s', (input) => {
      const control = fb.control(input, {
        validators: [AppValidators.phoneValidator()],
      });

      expect(control.valid).toBe(false);
      expect(control.errors).toEqual({[AppValidatorKeys.phone]: true});
    });
  });

  describe('nonPastDates', () => {
    it.each([undefined, null])('accepts for nullish input', (input) => {
      const control = fb.control(input, {
        validators: [AppValidators.nonPastDates(new Date())],
      });

      expect(control.valid).toBe(true);
      expect(control.errors).toBeNull();
    });

    it.each(
      [new Date('2024-01-02'), new Date('2024-06-06')]
    )('accepts for valid input: %s', (input) => {
      const date = new Date('2024-01-01');
      jest.useFakeTimers().setSystemTime(date);
      const control = fb.control(input, {
        validators: [AppValidators.nonPastDates(date)],
      });

      expect(control.valid).toBe(true);
      expect(control.errors).toBeNull();
    });

    it.each(
      [new Date('2024-01-01')]
    )('accepts for equal input: %s', (input) => {
      const date = new Date('2024-01-01');
      jest.useFakeTimers().setSystemTime(date);
      const control = fb.control(input, {
        validators: [AppValidators.nonPastDates(date)],
      });

      expect(control.valid).toBe(true);
      expect(control.errors).toBeNull();
    });

    it.each(
      [new Date('2023-12-31'), new Date('2023-06-06')]
    )('rejects for invalid input: %s', (input) => {
      const date = new Date('2024-01-01');
      jest.useFakeTimers().setSystemTime(date);
      const control = fb.control(input, {
        validators: [AppValidators.nonPastDates(date)],
      });

      expect(control.valid).toBe(false);
      expect(control.errors).toEqual({[AppValidatorKeys.nonPastDates]: true});
    });

  });

  describe('zeitBisIsAfterZeitVon', () => {
    it('works with undefined', () => {
      const group = fb.group({
          zeitVon: fb.control<`${number}:${number}` | undefined>(undefined),
          zeitBis: fb.control<`${number}:${number}` | undefined>(undefined)
        },
        {validators: [AppValidators.zeitBisIsAfterZeitVon('zeitVon', 'zeitBis')]});

      group.updateValueAndValidity();

      expect(group.valid).toBe(true);
      expect(group.errors).toBeNull();
    });

    it('works with valid input: 10:00, 12:00', () => {
      const group = fb.group({
          zeitVon: fb.control<`${number}:${number}` | undefined>('10:00'),
          zeitBis: fb.control<`${number}:${number}` | undefined>('12:00')
        },
        {validators: [AppValidators.zeitBisIsAfterZeitVon('zeitVon', 'zeitBis')]});

      group.updateValueAndValidity();

      expect(group.valid).toBe(true);
      expect(group.errors).toBeNull();
    });

    it('fails with invalid input: 12:00, 10:00', () => {
      const group = fb.group({
          zeitVon: fb.control<`${number}:${number}` | undefined>('12:00'),
          zeitBis: fb.control<`${number}:${number}` | undefined>('10:00')
        },
        {validators: [AppValidators.zeitBisIsAfterZeitVon('zeitVon', 'zeitBis')]});

      group.updateValueAndValidity();

      expect(group.valid).toBe(false);
      expect(group.errors).toEqual({[AppValidatorKeys.zeitVonAfterZeitBis]: true});
    });

    it('fails with equal input: 12:00, 10:00', () => {
      const group = fb.group({
          zeitVon: fb.control<`${number}:${number}` | undefined>('10:00'),
          zeitBis: fb.control<`${number}:${number}` | undefined>('10:00')
        },
        {validators: [AppValidators.zeitBisIsAfterZeitVon('zeitVon', 'zeitBis')]});

      group.updateValueAndValidity();

      expect(group.valid).toBe(false);
      expect(group.errors).toEqual({[AppValidatorKeys.zeitVonAfterZeitBis]: true});
    });

    it('works if only 1 zeitVon is provided', () => {
      const group = fb.group({
          zeitVon: fb.control<`${number}:${number}` | undefined>('10:00'),
          zeitBis: fb.control<`${number}:${number}` | undefined>(undefined)
        },
        {validators: [AppValidators.zeitBisIsAfterZeitVon('zeitVon', 'zeitBis')]});

      group.updateValueAndValidity();

      expect(group.valid).toBe(true);
      expect(group.errors).toBeNull();
    });

    it('works if only 1 zeitBis is provided', () => {
      const group = fb.group({
          zeitVon: fb.control<`${number}:${number}` | undefined>(undefined),
          zeitBis: fb.control<`${number}:${number}` | undefined>('10:00')
        },
        {validators: [AppValidators.zeitBisIsAfterZeitVon('zeitVon', 'zeitBis')]});

      group.updateValueAndValidity();

      expect(group.valid).toBe(true);
      expect(group.errors).toBeNull();
    });

  });

  describe('datumBisIsAfterDatumVon', () => {
    it('works with undefined', () => {
      const group = fb.group({
          datumVon: fb.control<ISODateString | undefined>(undefined),
          datumBis: fb.control<ISODateString | undefined>(undefined)
        },
        {validators: [AppValidators.datumBisIsAfterDatumVon('datumVon', 'datumBis')]});

      group.updateValueAndValidity();

      expect(group.valid).toBe(true);
      expect(group.errors).toBeNull();
    });

    it('works with valid input: 01.01.2025, 31.05.2025', () => {
      const group = fb.group({
          datumVon: fb.control<ISODateString | undefined>(toISODateString(new Date(2025,1,1))),
          datumBis: fb.control<ISODateString | undefined>(toISODateString(new Date(2025,5,31))),
        },
        {validators: [AppValidators.datumBisIsAfterDatumVon('datumVon', 'datumBis')]});

      group.updateValueAndValidity();

      expect(group.valid).toBe(true);
      expect(group.errors).toBeNull();
    });

    it('fails with invalid input: 31.05.2025, 01.01.2025', () => {
      const group = fb.group({
          datumVon: fb.control<ISODateString | undefined>(toISODateString(new Date(2025,5,31))),
          datumBis: fb.control<ISODateString | undefined>(toISODateString(new Date(2025,1,1))),
        },
        {validators: [AppValidators.datumBisIsAfterDatumVon('datumVon', 'datumBis')]});

      group.updateValueAndValidity();

      expect(group.valid).toBe(false);
      expect(group.errors).toEqual({[AppValidatorKeys.datumVonAfterDatumBis]: true});
    });

    it('fails with equal input: 31.05.2025', () => {
      const group = fb.group({
          datumVon: fb.control<ISODateString | undefined>(toISODateString(new Date(2025,5,31))),
          datumBis: fb.control<ISODateString | undefined>(toISODateString(new Date(2025,5,31))),
        },
        {validators: [AppValidators.datumBisIsAfterDatumVon('datumVon', 'datumBis')]});

      group.updateValueAndValidity();

      expect(group.valid).toBe(false);
      expect(group.errors).toEqual({[AppValidatorKeys.datumVonAfterDatumBis]: true});
    });

    it('works if only datumVon is provided', () => {
      const group = fb.group({
          datumVon: fb.control<ISODateString | undefined>(toISODateString(new Date(2025,5,31))),
          datumBis: fb.control<ISODateString | undefined>(undefined),
        },
        {validators: [AppValidators.datumBisIsAfterDatumVon('datumVon', 'datumBis')]});

      group.updateValueAndValidity();

      expect(group.valid).toBe(true);
      expect(group.errors).toBeNull();
    });

    it('works if only datumBis is provided', () => {
      const group = fb.group({
          datumVon: fb.control<ISODateString | undefined>(undefined),
          datumBis: fb.control<ISODateString | undefined>(toISODateString(new Date(2025,5,31))),
        },
        {validators: [AppValidators.datumBisIsAfterDatumVon('datumVon', 'datumBis')]});

      group.updateValueAndValidity();

      expect(group.valid).toBe(true);
      expect(group.errors).toBeNull();
    });

    it('works with Invalid Inputs 31.02.2025', () => {
      const group = fb.group({
          datumVon: fb.control<ISODateString | undefined>(toISODateString(new Date(2025,2,31))),
          datumBis: fb.control<ISODateString | undefined>(toISODateString(new Date(2025,5,31))),
        },
        {validators: [AppValidators.datumBisIsAfterDatumVon('datumVon', 'datumBis')]});

      group.updateValueAndValidity();

      expect(group.valid).toBe(true);
      expect(group.errors).toBeNull();
    });
  });

  describe('dateRangeIsMaximumNumberOfYears', () => {
    it('works with undefined', () => {
      const group = fb.group({
          datumVon: fb.control<ISODateString | undefined>(undefined),
          datumBis: fb.control<ISODateString | undefined>(undefined)
        },
        {validators: [AppValidators.dateRangeIsMaximumNumberOfYears('datumVon', 'datumBis', 5)]});

      group.updateValueAndValidity();

      expect(group.valid).toBe(true);
      expect(group.errors).toBeNull();
    });

    it('works with valid input: 01.01.2025, 31.05.2025', () => {
      const group = fb.group({
          datumVon: fb.control<ISODateString | undefined>(toISODateString(new Date(2025,1,1))),
          datumBis: fb.control<ISODateString | undefined>(toISODateString(new Date(2025,5,31)))
        },
        {validators: [AppValidators.dateRangeIsMaximumNumberOfYears('datumVon', 'datumBis', 5)]});

      group.updateValueAndValidity();

      expect(group.valid).toBe(true);
      expect(group.errors).toBeNull();
    });

    it('fails with invalid input: 01.01.2025, 31.05.2030', () => {
      const group = fb.group({
          datumVon: fb.control<ISODateString | undefined>(toISODateString(new Date(2025,1,1))),
          datumBis: fb.control<ISODateString | undefined>(toISODateString(new Date(2030,5,31)))
        },
        {validators: [AppValidators.dateRangeIsMaximumNumberOfYears('datumVon', 'datumBis', 5)]});

      group.updateValueAndValidity();

      group.updateValueAndValidity();

      expect(group.valid).toBe(false);
      expect(group.errors).toEqual({[AppValidatorKeys.dateRangeIsMaximumNumberOfYears]: {"maximalYears": 5}});
    });

    it('works with dates wxactly 5 years apart  input: 31.05.2025, 31.05.2030', () => {
      const group = fb.group({
          datumVon: fb.control<ISODateString | undefined>(toISODateString(new Date(2025,5,31))),
          datumBis: fb.control<ISODateString | undefined>(toISODateString(new Date(2030,5,31))),
        },
        {validators: [AppValidators.dateRangeIsMaximumNumberOfYears('datumVon', 'datumBis', 5)]});

      group.updateValueAndValidity();

      expect(group.valid).toBe(true);
      expect(group.errors).toBeNull();
    });

    it('works if only datumVon is provided', () => {
      const group = fb.group({
          datumVon: fb.control<ISODateString | undefined>(toISODateString(new Date(2025,5,31))),
          datumBis: fb.control<ISODateString | undefined>(undefined),
        },
        {validators: [AppValidators.dateRangeIsMaximumNumberOfYears('datumVon', 'datumBis', 5)]});

      group.updateValueAndValidity();

      expect(group.valid).toBe(true);
      expect(group.errors).toBeNull();
    });

    it('works if only datumBis is provided', () => {
      const group = fb.group({
          datumVon: fb.control<ISODateString | undefined>(undefined),
          datumBis: fb.control<ISODateString | undefined>(toISODateString(new Date(2025,5,31))),
        },
        {validators: [AppValidators.dateRangeIsMaximumNumberOfYears('datumVon', 'datumBis', 5)]});

      group.updateValueAndValidity();

      expect(group.valid).toBe(true);
      expect(group.errors).toBeNull();
    });

    it('works with Invalid Inputs 31.02.2025', () => {
      const group = fb.group({
          datumVon: fb.control<ISODateString | undefined>(toISODateString(new Date(2025,2,31))),
          datumBis: fb.control<ISODateString | undefined>(toISODateString(new Date(2025,2,31))),
        },
        {validators: [AppValidators.dateRangeIsMaximumNumberOfYears('datumVon', 'datumBis', 5)]});

      group.updateValueAndValidity();

      expect(group.valid).toBe(true);
      expect(group.errors).toBeNull();
    });
  });
});
