import { Nullish } from '../nullish';
import { LogCallback } from './LogCallback';
import { isDark, isSeverityEnabled, parseSeverity } from './logging-helpers';
import { LogSeverity } from './LogSeverity';
import { parseColor } from './parseColor';

 
export class LogFactory {
  private static traceEnabledFor: string[] | Nullish;
  public static minSeverity: LogSeverity = LogSeverity.INFO;
  private static logCallback: LogCallback = () => {
    // default: do nothing
    // This might be useful to forward logging messages to a log-sink like Sentry
  };

  public static init(
    logSeverity: LogSeverity,
    traceEnabledFor: string[] | Nullish,
    logCallback?: LogCallback
  ): void {
    LogFactory.minSeverity = parseSeverity(logSeverity);
    LogFactory.traceEnabledFor = traceEnabledFor;
    LogFactory.logCallback =
      logCallback ??
      (() => {
        /* nop */
      });
  }

  constructor() {
    window.onerror = msg => {
      alert(`Unhandled internal error: ${String(msg)}`);
      // noinspection EmptyCatchBlockJS
      try {
        if (console.log) {
          console.log('Unhandled internal error', msg);
        }
      } catch (ignored) {
        // probably IE with a closed console
      }
    };
  }

  /**
   * Usage: const LOG = LogFactory.createLog("MyFooCompoment");
   */
  public static createLog(loggerName: string): Log {
    const bgColor = this.generateHexColorCode(loggerName);
    const color = parseColor(bgColor);
    const fgColor = isDark(color) ? 'white' : 'black';
    const loggerEnabled = (name: string, severity: LogSeverity): boolean =>
      this.isLoggerEnabled(name, severity);
    const logCallback: LogCallback = (severity, message, args) =>
      LogFactory.logCallback(severity, message, args);

    return new Log(loggerName, fgColor, bgColor, loggerEnabled, logCallback);
  }

  /**
   * output: #rrggbb
   */
  private static generateHexColorCode(name: string): string {
    let hash = 0;
    for (let i = 0; i < name.length; i++) {
       
      hash = name.charCodeAt(i) + ((hash << 5) - hash);
    }
    const c: string = Math.abs(hash).toString(16).toUpperCase();

    const hexNum = c.length > 6 ? c.substring(0, 6) : c.padStart(6, '0');

    const result = `#${hexNum}`;

    return result;
  }

  private static isLoggerEnabled(
    loggerName: string,
    severity: LogSeverity
  ): boolean {
    return this.isTraceEnabled(loggerName) || this.isSeverityEnabled(severity);
  }

  private static isSeverityEnabled(severity: LogSeverity): boolean {
    return isSeverityEnabled(severity, LogFactory.minSeverity);
  }

  private static isTraceEnabled(loggerName: string): boolean {
    return (
      (this.traceEnabledFor?.includes('*') ||
        this.traceEnabledFor?.includes(loggerName)) ??
      false
    );
  }
}

export class Log {
  constructor(
    private readonly name: string,
    private readonly fgColor: string,
    private readonly bgColor: string,
    private readonly loggerEnabled: (
      loggerName: string,
      severity: LogSeverity
    ) => boolean,
    private readonly logCallback: LogCallback
  ) {
    // nop
  }

  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  public info(message: any, ...args: any[]): void {
    this.doApply(console.info, LogSeverity.INFO, message, args);
  }

  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  public warn(message: any, ...args: any[]): void {
    this.doApply(console.warn, LogSeverity.WARN, message, args);
  }

  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  public error(message: any, ...args: any[]): void {
    this.doApply(console.error, LogSeverity.ERROR, message, args);
  }

  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  public debug(message: any, ...args: any[]): void {
    this.doApply(console.debug, LogSeverity.DEBUG, message, args);
  }

  /**
   * Trace must be explicitly enabled in environment.ts for specific loggers.
   */
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  public trace(message: any, ...args: any[]): void {
    this.doApply(console.debug, LogSeverity.TRACE, message, args);
  }

  private doApply(
    logFunc: (message?: unknown, ...optionalParams: unknown[]) => unknown,
    severity: LogSeverity,
    message: unknown,
    args: unknown[]
  ): void {
    if (!this.loggerEnabled(this.name, severity)) {
      return;
    }

    const callee = logFunc || console.log;
    if (!callee) {
      return;
    }

    let params = [];
    params.push(`%c${this.name}`);
    params.push(`color: ${this.fgColor}; background-color: ${this.bgColor}`);
    params.push(severity);
    params.push(message);
    params = params.concat(args);

    // noinspection EmptyCatchBlockJS
    try {
      callee.call(null, ...params);
    } catch (ignored) {
      // probably IE with a closed console
    }
    try {
      this.logCallback(severity, message, args);
    } catch (e) {
      try {
        console.error('Error: ' + JSON.stringify(e));
      } catch (ignore) {
        // dont break when the callback is failing
      }
    }
  }
}
