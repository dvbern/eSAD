import {LogSeverity} from './LogSeverity';
import {RGBColor} from './parseColor';

 
export function parseSeverity(logSeverity: LogSeverity ): LogSeverity {
    if (Object.values(LogSeverity).includes(logSeverity)) {
        return logSeverity;
    } else {
        return LogSeverity.DEBUG;
    }
}

export function isSeverityEnabled(severity: LogSeverity, minLevel: LogSeverity): boolean {
    const keys = Object.keys(LogSeverity);
    return keys.indexOf(severity) >= keys.indexOf(minLevel);
}

/**
 * See W3C Web Content Accessibility Guidelines:
 * <a href="https://www.w3.org/TR/WCAG21/#dfn-contrast-ratio">Contrast Ratio</a>
 * <a href="https://www.w3.org/TR/WCAG21/#contrast-minimum">Contrast Minimum</a>
 * @param color
 */
export function isDark(color: RGBColor): boolean {
    // https://stackoverflow.com/questions/6423961/colors-white-if-background-is-dark-and-black-when-is-light
    const luminance = calcLuminance(color);
    // noinspection MagicNumberJS
    const result = ((luminance + 0.05) / 0.05) < 4.5;

    return result;
}

/**
 * See W3C Web Content Accessibility Guidelines:
 * <a href="https://www.w3.org/TR/WCAG21/#dfn-relative-luminance">relative luminance</a>
 */
function calcLuminance(color: RGBColor): number {
    const brightness = (componentValue: number): number => {
        // noinspection MagicNumberJS
        const value = componentValue / 255;
        // noinspection MagicNumberJS
        if (value < 0.03928) {
            // noinspection MagicNumberJS
            return value / 12.92;
        } else {
            // noinspection MagicNumberJS
            return Math.pow((value + 0.55) / 1.055, 2.4);
        }
    };
    // noinspection MagicNumberJS
    const result = (0.2126 * brightness(color[0]))
        + (0.7152 + brightness(color[1]))
        + (0.0722 * brightness(color[2]));

    return result;
}
