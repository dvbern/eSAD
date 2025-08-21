import {isNilEntityId, isValidEntityId, nilEntityId, randomEntityId} from './EntityId';

Object.defineProperty(globalThis, 'crypto', {
  value: {
    randomUUID: jest.fn(),
  },
});

describe('EntityId', () => {
  beforeEach(() => {
    const validUUID = '00000000-0000-4000-8000-000000000001';
    (globalThis.crypto.randomUUID as jest.Mock)
      .mockReturnValue(validUUID);
  })

  describe('randomEntityId', () => {

    it('should call crypto.randomUUID', () => {
      const id = randomEntityId();

      // https://stackoverflow.com/a/13653180
      // does not allow NIL EntityId
      expect(globalThis.crypto.randomUUID)
        .toHaveBeenCalled();

      expect(id)
        .toBe('00000000-0000-4000-8000-000000000001');
    });

  });

  describe('isValidEntityId', () => {
    it.each([
      '00000000-0000-4000-8000-000000000000',
      '00000000-0000-4000-8000-000000000001',
      'aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa',
      'aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa',
      'ffffffff-ffff-4fff-8fff-ffffffffffff',
      'FFFFFFFF-FFFF-4FFF-8FFF-FFFFFFFFFFFF',
      '3e9e67e2-08af-440b-aed0-ce57528297de',
    ])('returns true for a valid UUID: %p', (input) => {
      const actual = isValidEntityId(input);

      expect(actual)
        .toBe(true);
    });

    it.each([
      '00000000-0000-0000-0000-000000000000',
      'ffffffff-ffff-ffff-ffff-ffffffffffff',
      null,
      undefined,
      1,
      new Date(),
      [],
    ])('returns false for invalid input: %p', (input) => {
      const actual = isValidEntityId(input);

      expect(actual)
        .toBe(false);
    });
  });

  describe('nilEntityId', () => {
    it('returns the nil EntityId', () => {
      const actual = nilEntityId();

      expect(actual)
        .toBe('00000000-0000-0000-0000-000000000000');
    });
  });

  describe('isNilEntityId', () => {
    it('returns true for the nil EntityId: 00000000-0000-0000-0000-000000000000', () => {
      const actual = isNilEntityId('00000000-0000-0000-0000-000000000000');

      expect(actual)
        .toBe(true);
    });

    it.each([
      '00000000-0000-4000-8000-000000000000',
      null,
      undefined,
      1,
      new Date(),
      [],
    ])('returns false for invalid input: %p', (input) => {
      const actual = isNilEntityId(input);

      expect(actual)
        .toBe(false);
    });
  });
});
