export type LogSeverity =
    | 'TRACE'
    | 'DEBUG'
    | 'INFO'
    | 'WARN'
    | 'ERROR'
    | 'OFF'
    ;
export const LogSeverity = {
    TRACE: 'TRACE' as LogSeverity,
    DEBUG: 'DEBUG' as LogSeverity,
    INFO: 'INFO' as LogSeverity,
    WARN: 'WARN' as LogSeverity,
    ERROR: 'ERROR' as LogSeverity,
    OFF: 'OFF' as LogSeverity,
} as const;
