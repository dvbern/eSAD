import {BackendEntityId} from '@dvb/data-access-types';

export function compareFn<T extends { id: BackendEntityId }>(item: T, selected: T): boolean {
  return item.id === selected.id;
}
