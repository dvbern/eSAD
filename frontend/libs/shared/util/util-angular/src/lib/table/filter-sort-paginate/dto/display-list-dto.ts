/**
 * The result of filter/sort/paginate a list
 */
export interface DisplayListDTO {
  /**
   * The sorted, filtered and paginated list. If there is pagination, these are the items on this page only.
   */
  displayedList?: object[];
  /**
   * The size of the list after filtering but before pagination. Required if we want pagination.
   */
  filteredSize?: number;
  /**
   * The size of the original list. Only provided if requested.
   */
  originalSize?: number;
  /**
   * pageNr, starting with 1. Can be different to the one that was requested e.g. when the filtered list is shorter.
   */
  pageNr: number;
}
