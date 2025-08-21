import {Nullish} from './nullish';
import {isFilledIn, requireFilledIn, requireNumber, trimToUndefined} from './string-utils';
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
