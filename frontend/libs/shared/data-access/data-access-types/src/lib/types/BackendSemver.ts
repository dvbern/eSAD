// this is very simplified definition of suffixes!
// See https://semver.org/#backusnaur-form-grammar-for-valid-semver-versions for more details
export type BackendSemver =
  | `${number}.${number}.${number}`
  | `${number}.${number}.${number}+${string}`
  | `${number}.${number}.${number}-${string}`
  ;

export function isBackendSemver(value: unknown): value is BackendSemver {
  if (typeof value !== 'string') {
    return false;
  }

  // regexp taken from https://semver.org/#is-there-a-suggested-regular-expression-regex-to-check-a-semver-string
  const matches = value.match(
    /^(0|[1-9]\d*)\.(0|[1-9]\d*)\.(0|[1-9]\d*)(?:-((?:0|[1-9]\d*|\d*[a-zA-Z-][0-9a-zA-Z-]*)(?:\.(?:0|[1-9]\d*|\d*[a-zA-Z-][0-9a-zA-Z-]*))*))?(?:\+([0-9a-zA-Z-]+(?:\.[0-9a-zA-Z-]+)*))?$/);

  const valid = Array.isArray(matches) && matches.length > 0;

  return valid;
}
