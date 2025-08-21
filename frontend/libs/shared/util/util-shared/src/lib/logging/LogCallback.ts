import {LogSeverity} from './LogSeverity';

// eslint-disable-next-line @typescript-eslint/no-explicit-any
export type LogCallback = (severity: LogSeverity, message: any, args: any[]) => void;
