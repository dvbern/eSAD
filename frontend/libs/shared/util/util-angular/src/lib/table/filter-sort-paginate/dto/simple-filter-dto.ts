import { Leaves } from '@dvb/util-shared';
import {FilterCombinationMethod} from './filter-combination-method';
import type { FilterMethod } from './filter-method';

export interface SimpleFilterDTO<T extends object> {
  /**
   * Can be the colName or “global” or any other name
   */
  filterName: Leaves<T>;
  /**
   * one or multiple values of any type (string | date | dateRange | List<string> …) (needs to be parsed depending on
   * the filterMethod and the multiValueMethod)
   */
  filterValue: string;
  /**
   * matching algorithm
   */
  filterMethod: FilterMethod;
  /**
   * examples:
   * - name search field -> AND
   * - multiselect -> OR
   * - select -> NONE
   */
  filterCombinationMethod: FilterCombinationMethod;

}
