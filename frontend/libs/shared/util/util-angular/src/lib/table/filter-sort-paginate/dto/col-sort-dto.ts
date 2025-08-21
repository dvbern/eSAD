import { Leaves } from '@dvb/util-shared';
import type { SortDirection } from './sort-direction';

export interface ColSortDTO<T extends object> {
  colName: Leaves<T>;
  sortDirection?: SortDirection;
}
