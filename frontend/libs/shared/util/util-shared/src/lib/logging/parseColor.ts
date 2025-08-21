export type RGBColor = [number, number, number];

/**
 * Parse a color value in Hex format: #rrggbb
 */
export function parseColor(hex: string): RGBColor {
    if (hex.length !== 7) {
        throw Error(`Cannot parse color: ${hex}`);
    }

    return [
        parseInt(hex.substring(1, 3), 16),
        parseInt(hex.substring(3, 5), 16),
        parseInt(hex.substring(5, 7), 16),
    ];
}
