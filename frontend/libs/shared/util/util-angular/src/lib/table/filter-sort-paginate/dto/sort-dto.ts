import type {ColSortDTO} from './col-sort-dto';

/**
 * Ordered List of ColSorts that need to be applied in the specified order
 */
export interface SortDTO<T extends object> {
  colSorts?: ColSortDTO<T>[];
}
