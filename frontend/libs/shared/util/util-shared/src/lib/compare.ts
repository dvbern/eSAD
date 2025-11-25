import {EntityInfoJson} from '@dvb/data-access-backend';
import {BackendEntityId} from '@dvb/data-access-types';

export function compareFn<T extends { id: BackendEntityId }>(item: T, selected: T): boolean {
  return item.id === selected.id;
}

export function compareByEntityInfoTimestampErstellt(a: {entityInfo: EntityInfoJson}, b: {entityInfo: EntityInfoJson}) {
  return b.entityInfo.timestampErstellt.localeCompare(a.entityInfo.timestampErstellt)
}