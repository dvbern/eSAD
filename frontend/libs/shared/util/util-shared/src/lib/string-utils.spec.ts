import {Nullish} from './nullish';
import {isFilledIn, isNotBlank, joinNotBlank, requireFilledIn, requireNumber, trimToUndefined} from './string-utils';
import '@lukemorales/jest-type-matchers';

describe('string-utils', () => {
  describe('isFilledIn', () => {
    ['', null, undefined].forEach((value) => {
      it(`returns false for nullish/emptystring: ${value}`, () => {
        expect(isFilledIn(value)).toEqual(false);
      });
    });

    ['test', true, 5, new Date()].forEach((value) => {
      it(`returns true for non-empty strings: ${value}`, () => {
        expect(isFilledIn(value)).toEqual(true);
      });
    });

    describe('type-assertion', () => {
      enum Foo {
        'A' = 'A',
        'B' = 'B',
      }

      it('asserts the enum type for an input with exact type', () => {
        const input: Foo = Foo.A as any;

        const actual = isFilledIn(input);
        // noinspection IfStatementWithIdenticalBranchesJS
        if (actual) {
          expect(input).toHaveStrictType<Foo>();
        } else {
          expect(input).toHaveStrictType<never>();
        }
      });

      it('asserts the enum type for an input with other types', () => {
        const input: Foo | '' | Nullish = Foo.A as any;

        const actual = isFilledIn(input);
        // noinspection IfStatementWithIdenticalBranchesJS
        if (actual) {
          expect(input).toHaveStrictType<Foo>();
        } else {
          expect(input).toHaveStrictType<'' | Nullish>();
        }
      });
    });
  });

  describe('isNotBlank', () => {
    ['', null, undefined].forEach((value) => {
      it(`returns false for nullish/blank string: ${value}`, () => {
        expect(isNotBlank(value)).toEqual(false);
      });
    });

    ['test', true, 5].forEach((value) => {
      it(`returns true for non-blank values: ${value}`, () => {
        expect(isNotBlank(value)).toEqual(true);
      });
    });
  });

  describe('joinNotBlank', () => {
    it('joins non-blank parts with a space by default', () => {
      expect(joinNotBlank(['a', 'b', 'c'])).toBe('a b c');
    });

    it('filters out empty strings, null, and undefined', () => {
      expect(joinNotBlank(['a', '', null, 'b', undefined, 'c'])).toBe('a b c');
    });

    it('uses a custom separator', () => {
      expect(joinNotBlank(['a', 'b', 'c'], ', ')).toBe('a, b, c');
    });

    it('returns undefined if all parts are blank', () => {
      expect(joinNotBlank(['', null, undefined])).toBeUndefined();
    });

    it('returns undefined for an empty array', () => {
      expect(joinNotBlank([])).toBeUndefined();
    });

    it('returns a single part without separator if only one part is non-blank', () => {
      expect(joinNotBlank(['', 'a', null])).toBe('a');
    });
  });

  describe('trimToUndefined', () => {
    it.each(['', ' ', null, undefined])(
      'returns empty string for nullish/emptystring: %s',
      (input) => {
        expect(trimToUndefined(input)).toBeUndefined();
      },
    );

    it.each([
      ['test', 'test'],
      [' Hello World ', 'Hello World'],
    ])('returns the value for non-empty strings: %s', (input, expected) => {
      expect(trimToUndefined(input)).toEqual(expected);
    });
  });

  describe('requireFilledIn', () => {
    it('throws an error for empty strings', () => {
      expect(() => requireFilledIn('')).toThrow();
    });

    it('returns the value for non-empty strings', () => {
      expect(requireFilledIn('test')).toEqual('test');
    });

    describe('for enums', () => {
      enum Foo {
        A = 'A',
        B = 'B',
      }

      it('returns an enum value for enums', () => {
        const value: Foo | '' = Foo.A as any;

        const filledIn: Foo = requireFilledIn(value);

        expect(filledIn).toEqual(Foo.A);
      });
    });
  });

  describe('requireNumber', () => {
    it('throws an error for empty strings', () => {
      expect(() => requireNumber('')).toThrow();
    });
    it('throws an error for nullish strings', () => {
      expect(() => requireNumber(null)).toThrow();
      expect(() => requireNumber(undefined)).toThrow();
    });

    // noinspection MagicNumberJS
    it.each([
      ['123.4', 123.4],
      ['-100', -100],
      ['0', 0],
    ])('returns the value for non-empty strings: %s', (input, expected) => {
      expect(requireNumber(input)).toEqual(expected);
    });

    it.each([['test'], ['true'], ['false']])(
      'returns the value for non-empty strings: %s',
      (input) => {
        expect(() => requireNumber(input)).toThrow();
      },
    );
  });
});
