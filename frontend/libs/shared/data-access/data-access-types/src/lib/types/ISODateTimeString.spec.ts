import {isISODateTimeString} from './ISODateTimeString';

describe('isISODateTimeString', () => {
  it.each([
    '2024-01-23T12:34:56',
    '2024-01-23T12:34:56Z',
    '2024-01-23T12:34:56.000',
    '2024-01-23T12:34:56.000Z',
  ])('accepts a valid ISO 8601 date time string', (input) => {
    const actual = isISODateTimeString(input);

    expect(actual).toBe(true);
  });

  it.each([
    '2024-01-23T99:99:99',
    ' 2024-01-23T12:34:56',
    '2024-01-23T12:34:56 ',
    '2024-01-23',
  ])('rejects invalid formats: %s', (input) => {
    const actual = isISODateTimeString(input);

    expect(actual).toBe(false);
  });

  it.each([null, undefined, 0, 1, NaN, '', 'hello', new Date(), Symbol(), {}])(
    'rejects everything else: %s',
    (input) => {
      const actual = isISODateTimeString(input);

      expect(actual).toBe(false);
    },
  );
});
