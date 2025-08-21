import type {SimpleFilterDTO} from './simple-filter-dto';

/**
 * Set of SimpleFilters that need to be apply (combined with AND)
 */
export interface FilterDTO<T extends object> {
  filters?: SimpleFilterDTO<T>[];
}
