 import {colMatchesFilterMulti} from './filter-functions';

describe('filter-functions multi', () => {
  describe('filtering string cols', () => {

    // MULTI STRINGS
    test('find substring AND', () => {
      expect(colMatchesFilterMulti('heinz.test@hin.ch', 'heinz hin test', 'SUBSTRING', 'AND')).toBe(
        true
      );
    });
    test('find substring OR', () => {
      expect(colMatchesFilterMulti('heinz.test@hin.ch', 'heinz hin test', 'SUBSTRING', 'OR')).toBe(
        true
      );
    });
    test('find substring SINGLE', () => {
      expect(colMatchesFilterMulti('heinz.test@hin.ch', 'heinz hin test', 'SUBSTRING', 'SINGLE')).toBe(
        false
      );
    });
    test('find substring SINGLE', () => {
      expect(colMatchesFilterMulti('heinz.test@hin.ch', 'st@hin', 'SUBSTRING', 'SINGLE')).toBe(
        true
      );
    });
    // FIXME: what do we do with umlauts in search? we want to be able to type gnägi, gnaegi oder gnagi
    test.skip('find multi substring with umlaut', () => {
      expect(colMatchesFilterMulti('Stephanie Gnaegi', 'Gnägi gnä gnagi steph', 'SUBSTRING', 'AND')).toBe(
        true
      );
    });
  });
});
