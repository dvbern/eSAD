import {formatEntryAsEsadId} from './kinder-esadid.util';

describe('formatEntryAsEsadId', () => {
  it('should format a 9-digit string into pattern xx xx xxx xx', () => {
    expect(formatEntryAsEsadId('123456789')).toBe('12 34 567 89');
  });

  it('should remove whitespaces and format correctly', () => {
    expect(formatEntryAsEsadId('123 456 789')).toBe('12 34 567 89');
    expect(formatEntryAsEsadId(' 123456789 ')).toBe('12 34 567 89');
    expect(formatEntryAsEsadId('12 34 56 78 9')).toBe('12 34 567 89');
  });

  it('should return the original input if not 9 digits', () => {
    expect(formatEntryAsEsadId('12345678')).toBe('12345678');
    expect(formatEntryAsEsadId('1234567890')).toBe('1234567890');
    expect(formatEntryAsEsadId('')).toBe('');
  });

  it('should return the original input if it contains non-digit characters', () => {
    expect(formatEntryAsEsadId('12345678a')).toBe('12345678a');
    expect(formatEntryAsEsadId('123-456-789')).toBe('123-456-789');
    expect(formatEntryAsEsadId('abc123xyz')).toBe('abc123xyz');
  });

  it('should handle already formatted input correctly', () => {
    expect(formatEntryAsEsadId('12 34 567 89')).toBe('12 34 567 89');
  });
});
