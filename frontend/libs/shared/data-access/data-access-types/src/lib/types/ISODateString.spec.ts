import {isISODateString} from './ISODateString';

describe('isISODateString', () => {
  it('accepts a valid ISO 8601 date string', () => {
    const actual = isISODateString('2024-01-23');

    expect(actual).toBe(true);
  });

  it.each(['2024-01-23T12:34:56'])(
    'rejects ISO 8601 string with more components: %s',
    (input) => {
      const actual = isISODateString(input);

      expect(actual).toBe(false);
    },
  );

  it.each([
    null,
    undefined,
    0,
    1,
    NaN,
    '',
    ' 2024-01-23',
    '2024-01-23 ',
    'hello',
    new Date(),
    Symbol(),
    {},
  ])('rejects everything else: %s', (input) => {
    const actual = isISODateString(input);

    expect(actual).toBe(false);
  });
});
