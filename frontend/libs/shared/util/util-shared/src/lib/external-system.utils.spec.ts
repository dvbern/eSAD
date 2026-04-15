import { ImportTyp } from '@dvb/data-access-backend';
import { isActiveInExternalSystem, isExternalObject } from './external-system.utils';

describe('external-system.utils', () => {
  describe('isActiveInExternalSystem', () => {
    it.each(
      [
        {
          input: { externalImportInfoJson: { aktivInExternalSystem: true, importTyp: ImportTyp.ESCADA } },
          expected: true,
          description: 'active ESCADA'
        },
        {
          input: { externalImportInfoJson: { aktivInExternalSystem: true, importTyp: ImportTyp.PUPIL } },
          expected: true,
          description: 'active PUPIL'
        },
        {
          input: { externalImportInfoJson: { aktivInExternalSystem: true, importTyp: ImportTyp.EXCEL_MASSENIMPORT } },
          expected: true,
          description: 'active EXCEL_MASSENIMPORT'
        },
        {
          input: { externalImportInfoJson: { aktivInExternalSystem: false, importTyp: ImportTyp.ESCADA } },
          expected: false,
          description: 'inactive ESCADA'
        },
        {
          input: { externalImportInfoJson: { aktivInExternalSystem: false, importTyp: ImportTyp.PUPIL } },
          expected: false,
          description: 'inactive PUPIL'
        },
        {
          input: { externalImportInfoJson: { aktivInExternalSystem: false, importTyp: ImportTyp.EXCEL_MASSENIMPORT } },
          expected: false,
          description: 'inactive EXCEL_MASSENIMPORT'
        },
        {
          input: { externalImportInfoJson: undefined },
          expected: false,
          description: 'exportinfo is undefined'
        },
        {
          input: null,
          expected: false,
          description: 'object is null'
        },
        {
          input: undefined,
          expected: false,
          description: 'object is undefined'
        },
      ]
    )('should return $expected for input $input', ({ input, expected }) => {
        expect(isActiveInExternalSystem(input as any)).toBe(expected);
    });
  });

  describe('isExternalObject', () => {
    it.each([
      {
        input: { externalImportInfoJson: { aktivInExternalSystem: true, importTyp: ImportTyp.ESCADA } },
        expected: true,
        description: 'active ESCADA'
      },
      {
        input: { externalImportInfoJson: { aktivInExternalSystem: true, importTyp: ImportTyp.PUPIL } },
        expected: true,
        description: 'active PUPIL'
      },
      {
        input: { externalImportInfoJson: { aktivInExternalSystem: true, importTyp: ImportTyp.EXCEL_MASSENIMPORT } },
        expected: false,
        description: 'active EXCEL_MASSENIMPORT'
      },
      {
        input: { externalImportInfoJson: { aktivInExternalSystem: false, importTyp: ImportTyp.ESCADA } },
        expected: false,
        description: 'inactive ESCADA'
      },
      {
        input: { externalImportInfoJson: undefined },
        expected: false,
        description: 'undefined externalImportInfoJson'
      },
      {
        input: null,
        expected: false,
        description: 'null input'
      },
      {
        input: undefined,
        expected: false,
        description: 'undefined input'
      },
    ])( 'should return $expected for $description', ({ expected,  input }) => {
        expect(isExternalObject(input as any)).toBe(expected);
    });
  });
});
