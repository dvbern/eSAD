import {isBackendURL} from './BackendURL';

describe('BackendURL', () => {

  it.each([
    'example.com',
    'ftp://example.com',
    null,
    undefined,
    '',
    new Date(),
  ])(`rejects invalid URLs`, (value) => {
    expect(isBackendURL(value))
      .toBe(false);
  });

  describe('isBackendURL', () => {
    it.each([
      'https://example.com',
      'https://example.com#foo',
      'https://example.com?foo',
      'https://example.com/banana',
      'https://example.com/banana#foo',
      'https://example.com/banana?foo',
      'http://example.com',
      'http://example.com#foo',
      'http://example.com?foo',
      'http://example.com/banana',
      'http://example.com/banana#foo',
      'http://example.com/banana?foo',
    ])('accepts valid URLs', (value) => {
      expect(isBackendURL(value))
        .toBe(true);
    });
  });

});
