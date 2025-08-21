import { ensureType } from '../ensure';
import { LogSeverity } from '.';
import { isSeverityEnabled } from './logging-helpers';

describe('logging-helpers', () => {
  describe('isSeverityEnabled', () => {
    type TestData = [boolean, LogSeverity, LogSeverity];

    ensureType<TestData[]>([
      [false, LogSeverity.TRACE, LogSeverity.ERROR],
      [false, LogSeverity.DEBUG, LogSeverity.ERROR],
      [false, LogSeverity.INFO, LogSeverity.ERROR],
      [false, LogSeverity.WARN, LogSeverity.ERROR],
      [true, LogSeverity.ERROR, LogSeverity.ERROR],

      [false, LogSeverity.TRACE, LogSeverity.WARN],
      [false, LogSeverity.DEBUG, LogSeverity.WARN],
      [false, LogSeverity.INFO, LogSeverity.WARN],
      [true, LogSeverity.WARN, LogSeverity.WARN],
      [true, LogSeverity.ERROR, LogSeverity.WARN],

      [false, LogSeverity.TRACE, LogSeverity.INFO],
      [false, LogSeverity.DEBUG, LogSeverity.INFO],
      [true, LogSeverity.INFO, LogSeverity.INFO],
      [true, LogSeverity.WARN, LogSeverity.INFO],
      [true, LogSeverity.ERROR, LogSeverity.INFO],

      [false, LogSeverity.TRACE, LogSeverity.DEBUG],
      [true, LogSeverity.DEBUG, LogSeverity.DEBUG],
      [true, LogSeverity.INFO, LogSeverity.DEBUG],
      [true, LogSeverity.WARN, LogSeverity.DEBUG],
      [true, LogSeverity.ERROR, LogSeverity.DEBUG],

      [true, LogSeverity.TRACE, LogSeverity.TRACE],
      [true, LogSeverity.DEBUG, LogSeverity.TRACE],
      [true, LogSeverity.INFO, LogSeverity.TRACE],
      [true, LogSeverity.WARN, LogSeverity.TRACE],
      [true, LogSeverity.ERROR, LogSeverity.TRACE],
    ]).forEach(([expected, severity, minLevel]) =>
      it(`should return ${String(expected)} for LogLevel: ${severity}, minLevel: ${minLevel}`, () => {
        const actual = isSeverityEnabled(severity, minLevel);

        expect(actual).toEqual(expected);
      })
    );
  });
});
