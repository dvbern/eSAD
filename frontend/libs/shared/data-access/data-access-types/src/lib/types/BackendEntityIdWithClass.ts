import { BackendEntityId } from './EntityId';

declare const __brand: unique symbol;

export type BackendEntityIdWithClass =
  `${string}-${string}-${string}-${string}-${string}${'-anonym' | '-einladung'}` & { [__brand]: 'withClass' };

export function toEinladungBackendEntityIdWithClass(id: BackendEntityId): BackendEntityIdWithClass {
  return requireValidBackendEntityIdWithClass(`${id}-einladung`);
}

export function toAnonymerEintragBackendEntityIdWithClass(id: BackendEntityId): BackendEntityIdWithClass {
  return requireValidBackendEntityIdWithClass(`${id}-anonym`);
}

export function requireValidBackendEntityIdWithClass(value: unknown): BackendEntityIdWithClass {
  if (!isValidBackendEntityIdWithClass(value)) {
    throw new Error(`Invalid BackendEntityIdWithClass: ${value}`);
  }
  return value;
}

export function isValidBackendEntityIdWithClass(value: unknown): value is BackendEntityIdWithClass {
  if (typeof value !== 'string') {
    return false;
  }
  // Format: UUID (8-4-4-4-12) followed by -anonym or -einladung
  const isValid = /^[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}-(anonym|einladung)$/i.test(value);
  return isValid;
}
