import {PaginationDTO} from './pagination-dto';
import type {FilterDTO} from './filter-dto';
import type {SortDTO} from './sort-dto';

/**
 * Object that contains the filter, sort and pagination that can be applied to a list.
 */
export interface DisplayConfigDTO<T extends object> {
  filter?: FilterDTO<T>;
  /**
   * If this property is true, the response should include the original (unfiltered) size
   */
  needsOriginalSize?: boolean;
  pagination?: PaginationDTO;
  sort?: SortDTO<T>;
}
