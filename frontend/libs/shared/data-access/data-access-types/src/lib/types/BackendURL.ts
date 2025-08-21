export type BackendURL = `https://${string}` | `http://${string}`;

export function isBackendURL(value: unknown): value is BackendURL {
  if (typeof value !== 'string' || (!value.startsWith('https://') && !value.startsWith('http://'))) {
    return false;
  }

  // validate that the string is a valid URL
  try {
    new URL(value);
    return true;
  } catch (_error) {
    return false;
  }
}
