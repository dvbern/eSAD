import {SortUtil} from './SortUtil';

describe('SortUtil', () => {
  enum Land {
    CH = 'CH',
    AL = 'AL',
    DK = 'DK',
    DE = 'DE',
    DE_OLD = 'DE_OLD',
    FI = 'FI',
    FR = 'FR',
    GR = 'GR',
    GB = 'GB',
    IT = 'IT',
    ZZ = 'ZZ',
  }

  const locale = 'de';

  describe('usingTransformedValue', () => {
    it('should use the transformation function', () => {
      const input: Land[] = [Land.IT, Land.DE, Land.CH, Land.FR];

      const result = [...input].sort(
        SortUtil.usingTransformedValue((e) => `Land: ${e}`, locale),
      );

      expect(result).toEqual([Land.CH, Land.DE, Land.FR, Land.IT]);
    });
  });

  describe('usingPreferredValueFirst', () => {
    it('orders our preferred value first', () => {
      const input: Land[] = [Land.IT, Land.DE, Land.CH, Land.FR];

      const result = [...input].sort(
        SortUtil.usingPreferredValueFirst<Land>(Land.CH),
      );

      expect(result).toEqual([Land.CH, Land.IT, Land.DE, Land.FR]);
    });
  });

  describe('handleCompareStandard', () => {
    it('should compare values', () => {
      expect(
        SortUtil.handleCompareStandard(undefined, undefined, locale),
      ).toEqual(0);
      expect(SortUtil.handleCompareStandard('', '', locale)).toEqual(0);
      expect(SortUtil.handleCompareStandard(1, 1, locale)).toEqual(0);
      expect(SortUtil.handleCompareStandard('1', '1', locale)).toEqual(0);
      expect(SortUtil.handleCompareStandard('1', 1, locale)).toEqual(0);
      expect(SortUtil.handleCompareStandard(undefined, 2, locale)).toEqual(-1);
      expect(SortUtil.handleCompareStandard(1, 2, locale)).toEqual(-1);
      expect(SortUtil.handleCompareStandard('1', '2', locale)).toEqual(-1);
      expect(SortUtil.handleCompareStandard('1', 2, locale)).toEqual(-1);
      expect(SortUtil.handleCompareStandard(2, undefined, locale)).toEqual(1);
      expect(SortUtil.handleCompareStandard(2, 1, locale)).toEqual(1);
      expect(SortUtil.handleCompareStandard('2', '1', locale)).toEqual(1);
      expect(SortUtil.handleCompareStandard('2', 1, locale)).toEqual(1);
    });
  });

  describe('localCompareStr', () => {
    it('should compare string values', () => {
      expect(SortUtil.localeCompareStr('', '', locale)).toEqual(0);
      expect(SortUtil.localeCompareStr('1', '1', locale)).toEqual(0);
      expect(SortUtil.localeCompareStr('1', '2', locale)).toEqual(-1);
      expect(SortUtil.localeCompareStr('2', '1', locale)).toEqual(1);
    });
  });
});
