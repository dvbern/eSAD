import {colMatchesFilter} from './filter-functions';

describe('filter-functions', () => {
  describe('filtering string cols', () => {
    // STRING
    test('find substring', () => {
      expect(colMatchesFilter('heinz.test@hin.ch', 'hin', 'SUBSTRING')).toBe(
        true
      );
    });
    // FIXME: what do we do with umlauts in search? we want to be able to type gnägi, gnaegi oder gnagi
    test.skip('find substring with umlaut', () => {
      expect(colMatchesFilter('Stephanie Gnaegi', 'Gnägi', 'SUBSTRING')).toBe(
        true
      );
    });
    test('does not find', () => {
      expect(colMatchesFilter('heinz.test@hin.ch', 'dvbern', 'SUBSTRING')).toBe(
        false
      );
    });
    test('null col value', () => {
      expect(colMatchesFilter(undefined, 'hin', 'SUBSTRING')).toBe(false);
    });
    test('empty col value', () => {
      expect(colMatchesFilter('', 'hin', 'SUBSTRING')).toBe(false);
    });

  });

  describe('filtering enum cols', () => {
    // ENUM
    test('enum found', () => {
      expect(
        colMatchesFilter(DummyEnum.ONE, DummyEnum.ONE, 'ENUM')
      ).toBe(true);
    });
    test('enum not found', () => {
      expect(
        colMatchesFilter(DummyEnum.TWO, DummyEnum.ONE, 'ENUM')
      ).toBe(false);
    });
    test('enum empty col value', () => {
      expect(colMatchesFilter(undefined, DummyEnum.ONE, 'ENUM')).toBe(
        false
      );
    });
    test('enum empty string col value', () => {
      expect(colMatchesFilter('', DummyEnum.ONE, 'ENUM')).toBe(false);
    });
    test('enum empty string search value', () => {
      expect(colMatchesFilter(DummyEnum.ONE, '', 'ENUM')).toBe(false);
    });

  });

  describe('filtering enum in array cols', () => {
    // ENUM IN ARRAY (e.g. Rollen)
    test('enum found', () => {
      expect(
        colMatchesFilter([DummyEnum.TWO, DummyEnum.ONE], DummyEnum.ONE, 'ENUM')
      ).toBe(true);
    });
    test('enum not found', () => {
      expect(
        colMatchesFilter([DummyEnum.TWO], DummyEnum.ONE, 'ENUM')
      ).toBe(false);
    });
  });


  describe('filtering bool cols', () => {
    // BOOL
    test('true found', () => {
      expect(colMatchesFilter(true, 'true', 'BOOL')).toBe(true);
    });
    test('true not found', () => {
      expect(colMatchesFilter(false, 'true', 'BOOL')).toBe(false);
    });
    test('true not found', () => {
      expect(colMatchesFilter(undefined, 'true', 'BOOL')).toBe(false);
    });

    test('false found', () => {
      expect(colMatchesFilter(false, 'false', 'BOOL')).toBe(true);
    });
    test('false not found', () => {
      expect(colMatchesFilter(true, 'false', 'BOOL')).toBe(false);
    });
    test('false not found', () => {
      expect(colMatchesFilter(undefined, 'false', 'BOOL')).toBe(false);
    });
  });

  describe('filtering dateRange cols', () => {
    // DATE

    test('no value', () => {
      expect(
        colMatchesFilter(undefined, '1999-01-01,2001-01-01', 'DATERANGE')
      ).toBe(false);
    });

    // range
    test('in range', () => {
      expect(
        colMatchesFilter('2000-01-01', '1999-01-01,2001-01-01', 'DATERANGE')
      ).toBe(true);
    });
    test('before range', () => {
      expect(
        colMatchesFilter('1998-12-31', '1999-01-01,2001-01-01', 'DATERANGE')
      ).toBe(false);
    });
    test('after range', () => {
      expect(
        colMatchesFilter('2001-01-02', '1999-01-01,2001-01-01', 'DATERANGE')
      ).toBe(false);
    });

    // from
    test('after from filter', () => {
      expect(colMatchesFilter('2000-01-01', '1999-01-01,', 'DATERANGE')).toBe(
        true
      );
    });
    test('before from filter', () => {
      expect(colMatchesFilter('1998-12-31', '1999-01-01,', 'DATERANGE')).toBe(
        false
      );
    });
    // to
    test('before to filter', () => {
      expect(colMatchesFilter('2000-01-01', ',2000-01-02', 'DATERANGE')).toBe(
        true
      );
    });
    test('after to filter', () => {
      expect(colMatchesFilter('2000-01-01', ',1999-12-31', 'DATERANGE')).toBe(
        false
      );
    });

    // invalid input
    test('invalid date filter: only 1 date', () => {
      expect(colMatchesFilter('2000-01-01', '1999-12-31', 'DATERANGE')).toBe(
        false
      );
    });
    test('invalid date filter: invalid date-to -> is ignored', () => {
      expect(
        colMatchesFilter('2000-01-01', '1999-12-31,2000-02-31', 'DATERANGE')
      ).toBe(true);
    });
    test('invalid col value: invalid date', () => {
      expect(
        colMatchesFilter('2000-02-31', '2000-01-01,2000-12-31', 'DATERANGE')
      ).toBe(false);
    });
    test('invalid col value: no date', () => {
      expect(
        colMatchesFilter('abracadabra', '2000-01-01,2000-12-31', 'DATERANGE')
      ).toBe(false);
    });
  });

  describe('filtering date cols', () => {
    // DATE
    test.each([undefined, null, ''])('no value', (input) => {
      expect(colMatchesFilter(input, '1999-01-01', 'DATE')).toBe(false);
    });

    test('same Date', () => {
      expect(colMatchesFilter('2000-01-01', '2000-01-01', 'DATE')).toBe(true);
    });

    test('same Date with Time', () => {
      expect(
        colMatchesFilter('2000-01-01T18:00:00', '2000-01-01T12:00:00', 'DATE')
      ).toBe(true);
    });

    test('different Date', () => {
      expect(colMatchesFilter('2000-01-02', '2000-01-01', 'DATE')).toBe(false);
    });

    test('different Date', () => {
      expect(
        colMatchesFilter('2000-01-02T00:00:00', '2000-01-01T23:59:59', 'DATE')
      ).toBe(false);
    });

    // invalid input
    test('invalid date filter: invalid date-to -> is ignored', () => {
      expect(colMatchesFilter('2000-01-01', '2000-02-31', 'DATE')).toBe(true);
    });
    test('invalid col value: invalid date', () => {
      expect(colMatchesFilter('2000-02-31', '2000-12-31', 'DATE')).toBe(false);
    });
    test('invalid col value: no date', () => {
      expect(colMatchesFilter('abracadabra', '2000-01-01', 'DATE')).toBe(false);
    });
  });

  enum DummyEnum {
    ONE = 'ONE',
    TWO = 'TWO',
  }
});

