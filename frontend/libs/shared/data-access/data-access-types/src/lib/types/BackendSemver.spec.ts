import {isBackendSemver} from './BackendSemver';

describe('BackendSemver', () => {
  describe('isBackendSemver', () => {
    it.each([
      '1.0.0',
      '1.0.0-alpha',
      '1.0.0+alpha',
    ])(`accepts valid semver: %p`, (value) => {
      expect(isBackendSemver(value))
        .toBe(true);
    });

    it.each([
      null,
      undefined,
      123,
      new Date(),
      {},
      '',
      '1',
      '1.0',
      '1.0.0.',
      '1.0.0-alpha.',
      '1.0.0+alpha+alpha',
    ])('rejects invalid semver: %p', (value) => {
      expect(isBackendSemver(value))
        .toBe(false);
    });
  });

});
