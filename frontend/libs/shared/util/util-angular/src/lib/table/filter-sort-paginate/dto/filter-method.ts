/**
 * Filter methods, add when needed
 */
export type FilterMethod = 'ENUM' | 'SUBSTRING' | 'DATERANGE' | 'BOOL' | 'PHONE' | 'DATE';

export const FilterMethod = {
  ENUM: 'ENUM' as FilterMethod,
  SUBSTRING: 'SUBSTRING' as FilterMethod,
  DATE: 'DATE' as FilterMethod,
  DATERANGE: 'DATERANGE' as FilterMethod,
  BOOL: 'BOOL' as FilterMethod,
  PHONE: 'PHONE' as FilterMethod,
};


// space only because then we can parse a text search field where the user just uses space as a separator
export const MULTI_FILTER_SEPARATOR = ' ';
