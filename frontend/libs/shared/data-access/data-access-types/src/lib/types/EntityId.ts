declare const __brand: unique symbol;

export type BackendEntityId =
  `${string}-${string}-${string}-${string}-${string}` & { [__brand]: 'plainId' };

export function randomEntityId(): BackendEntityId {
  return requireValidBackendEntityId(crypto.randomUUID())
}

export function nilEntityId(): '00000000-0000-0000-0000-000000000000' {
  return '00000000-0000-0000-0000-000000000000';
}

export function requireValidBackendEntityIds(value: unknown[]): BackendEntityId[] {
  if (!areValidBackendEntityIds(value)) {
    throw new Error(`Invalid BackendEntityIds: ${value}`);
  }
  return value;
}

export function areValidBackendEntityIds(value: unknown[]): value is BackendEntityId[] {
  for (const valueElement of value) {
    if (!isValidBackendEntityId(valueElement)) {
      return false;
    }
  }
  return true;
}

export function requireValidBackendEntityId(value: unknown): BackendEntityId {
  if (!isValidBackendEntityId(value)) {
    throw new Error(`Invalid BackendEntityId: ${value}`);
  }
  return value;
}

export function isValidBackendEntityId(value: unknown): value is BackendEntityId {
  if (typeof value !== 'string') {
    return false;
  }
  // this matcher is a less strict uuid matcher that allows for 'hand-made' uuids that do not conform to spec exactly
  return /^[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}$/i.test(value);
}

export function isValidEntityId(value: unknown): value is BackendEntityId {
  if (typeof value !== 'string') {
    return false;
  }
  // https://stackoverflow.com/a/13653180
  const isValidNonNILEntityId = /^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i.test(
    value,
  );

  return isValidNonNILEntityId;
}

export function isNilEntityId(value: unknown): value is '00000000-0000-0000-0000-000000000000' {
  return value === '00000000-0000-0000-0000-000000000000';
}
