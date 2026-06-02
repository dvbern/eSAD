import {
  combineMonthsAndYearsIntoAnzahlMonths,
  splitAnzahlMonateIntoMonthsAndYears,
} from './untersuchung-schulrelevante-beeintraechtigung.utils';

describe('untersuchung-schulrelevante-beeintraechtigung.utils', () => {
  describe('splitAnzahlMonateIntoMonthsAndYears', () => {
    it.each([
      {input: 0, expected: {jahre: 0, monate: 0}, description: '0 Monate'},
      {input: 1, expected: {jahre: 0, monate: 1}, description: '1 Monat'},
      {input: 11, expected: {jahre: 0, monate: 11}, description: '11 Monate'},
      {input: 12, expected: {jahre: 1, monate: 0}, description: 'exakt 1 Jahr'},
      {input: 13, expected: {jahre: 1, monate: 1}, description: '1 Jahr + 1 Monat'},
      {input: 24, expected: {jahre: 2, monate: 0}, description: 'exakt 2 Jahre'},
      {input: 25, expected: {jahre: 2, monate: 1}, description: '2 Jahre + 1 Monat'},
      {input: 119, expected: {jahre: 9, monate: 11}, description: 'Maximalwert 9 Jahre 11 Monate'},
    ])('should split $input months into $expected ($description)', ({input, expected}) => {
      expect(splitAnzahlMonateIntoMonthsAndYears(input)).toEqual(expected);
    });

    it('should return null for undefined input', () => {
      expect(splitAnzahlMonateIntoMonthsAndYears(undefined)).toBeNull();
    });

    it('should return null for null input', () => {
      expect(splitAnzahlMonateIntoMonthsAndYears(null as unknown as undefined)).toBeNull();
    });
  });

  describe('combineMonthsAndYearsIntoAnzahlMonths', () => {
    it.each([
      {jahre: 0, monate: 0, expected: 0, description: 'beide 0'},
      {jahre: 1, monate: 0, expected: 12, description: '1 Jahr'},
      {jahre: 0, monate: 5, expected: 5, description: '5 Monate'},
      {jahre: 1, monate: 1, expected: 13, description: '1 Jahr + 1 Monat'},
      {jahre: 2, monate: 6, expected: 30, description: '2 Jahre + 6 Monate'},
      {jahre: 9, monate: 11, expected: 119, description: 'Maximalwerte'},
    ])('should combine jahre=$jahre, monate=$monate to $expected ($description)', ({jahre, monate, expected}) => {
      expect(combineMonthsAndYearsIntoAnzahlMonths(jahre, monate)).toBe(expected);
    });

    it('should return undefined when both jahre and monate are undefined', () => {
      expect(combineMonthsAndYearsIntoAnzahlMonths(undefined, undefined)).toBeUndefined();
    });

    it('should return undefined when both jahre and monate are null', () => {
      expect(combineMonthsAndYearsIntoAnzahlMonths(null, null)).toBeUndefined();
    });

    it('should return only monate when jahre is nullish', () => {
      expect(combineMonthsAndYearsIntoAnzahlMonths(undefined, 7)).toBe(7);
      expect(combineMonthsAndYearsIntoAnzahlMonths(null, 7)).toBe(7);
    });

    it('should return only jahre*12 when monate is nullish', () => {
      expect(combineMonthsAndYearsIntoAnzahlMonths(3, undefined)).toBe(36);
      expect(combineMonthsAndYearsIntoAnzahlMonths(3, null)).toBe(36);
    });

    it('should coerce numeric strings to numbers', () => {
      expect(combineMonthsAndYearsIntoAnzahlMonths('2' as unknown as number, '3' as unknown as number)).toBe(27);
    });

    it('should be the inverse of splitAnzahlMonateIntoMonthsAndYears for valid inputs', () => {
      for (const anzahl of [0, 1, 5, 11, 12, 13, 24, 50, 119]) {
        const split = splitAnzahlMonateIntoMonthsAndYears(anzahl);
        expect(combineMonthsAndYearsIntoAnzahlMonths(split?.jahre, split?.monate)).toBe(anzahl);
      }
    });
  });
});
