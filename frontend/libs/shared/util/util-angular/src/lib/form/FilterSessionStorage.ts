import {isNullish} from '@dvb/util-shared';

export abstract class FilterSessionStorage<T> {
  abstract readonly SESSIONSTORAGE_KEY: string;

  setSessionStorageFilter(filterForm: T): void {
    sessionStorage.setItem(this.SESSIONSTORAGE_KEY, JSON.stringify(filterForm));
  }

  getSessionStorageFilter(): T | undefined {
    const sessionStorageFilter = sessionStorage.getItem(this.SESSIONSTORAGE_KEY);
    if (isNullish(sessionStorageFilter)) {
      return undefined;
    }

    return this.mapSessionStorageStringToFilter(sessionStorageFilter);
  }

  abstract mapSessionStorageStringToFilter(sessionStorageFilter: string): T | undefined
}
