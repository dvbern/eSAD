import { isValidBackendEntityIdWithClass } from './BackendEntityIdWithClass';

describe('BackendEntityIdWithClass', () => {
  describe('isValidBackendEntityIdWithClass', () => {
    it.each([
      '00000000-0000-4000-8000-000000000000-anonym',
      '00000000-0000-4000-8000-000000000001-einladung',
      'aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa-anonym',
      '3e9e67e2-08af-440b-aed0-ce57528297de-einladung',
    ])('returns true for a valid ID: %p', (input) => {
      const actual = isValidBackendEntityIdWithClass(input);
      expect(actual).toBe(true);
    });

    it.each([
      '00000000-0000-4000-8000-000000000000', // missing mandatory postfix
      '00000000-0000-0000-0000-00000000000', // too short
      '00000000-0000-0000-0000-000000000000-invalid',
      '00000000-0000-0000-0000-000000000000-anonym-extra',
      null,
      undefined,
      1,
      '',
    ])('returns false for invalid input: %p', (input) => {
      const actual = isValidBackendEntityIdWithClass(input);
      expect(actual).toBe(false);
    });
  });
});
