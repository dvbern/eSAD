// https://stackoverflow.com/a/58436959

/**
 * Construct a string-template-type for properties of nested objects.
 */
// Stop recursion for primitives (and branded types, which extend string but look like objects to TypeScript)
export type Paths<T> = T extends string | number | boolean | bigint | symbol | null | undefined ? never : T extends object ? {
  [K in keyof T]:
  `${Exclude<K, symbol>}${'' | `.${Paths<T[K]>}`}`
}[keyof T] : never

/**
 * See {@link Paths} but shows only leaves, not intermediates.
 */
// Stop recursion for primitives (and branded types, which extend string but look like objects to TypeScript)
export type Leaves<T> = T extends string | number | boolean | bigint | symbol | null | undefined ? never : T extends object ? {
  [K in keyof T]:
  `${Exclude<K, symbol>}${Leaves<T[K]> extends never ? '' : `.${Leaves<T[K]>}`}`
}[keyof T] : never
