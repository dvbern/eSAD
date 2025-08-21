/**
 * Allow for a typesafe enum that is based on a union-type.
 * <p>
 * This is useful for e.g. Storybook-Dropdowns:
 * @example
 * // input:
 * export type AlertType = 'success' | 'info' | 'warning' | 'danger';
 * // outcome:
 * export const AlertTypeEnum: TypeEnum<AlertType> = {
 *   danger: 'danger',
 *   info: 'info',
 *   success: 'success',
 *   warning: 'warning',
 * };
 */
// eslint-disable-next-line @typescript-eslint/no-explicit-any
export type TypeEnum<K extends keyof any> = Record<K, K>;
