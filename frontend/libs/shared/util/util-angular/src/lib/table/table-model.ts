import {computed, effect, Signal, signal, untracked} from '@angular/core';
import {toSignal} from '@angular/core/rxjs-interop';
import {Leaves, requireValue} from '@dvb/util-shared';
import {distinctUntilChanged, Subject} from 'rxjs';
import {
  clearFilters,
  ColSortDTO,
  DisplayConfigDTO,
  DisplayList,
  FilterDTO,
  findColSort,
  gotoFirstPage,
  prepareDisplayList,
  SimpleFilterDTO,
  SortDirection,
  SortDTO,
  updateFilter,
  updateSingleColSort,
  updateSort,
} from './filter-sort-paginate';
import {PaginationDTO} from './filter-sort-paginate/dto/pagination-dto';

const ORIGINAL_PAGE_SIZE = 10;

export type PageOrScrollMode = 'paginate' | 'scroll' | undefined;
export type FrontendOrBackendLogic = 'frontend' | 'backend';

// Output type: same as input type but we keep the alias for clarity
export type PageChanged = PaginationDTO;

export interface TableModelConfig {
  frontendOrBackend: FrontendOrBackendLogic; // frontend vs. backend logic
  pageOrScrollOrNothing: PageOrScrollMode;
  originalPageSize?: number; // set a page size if you want a specific one
}

export interface TableInputOutput<T extends object> {
  rawListSignal: Signal<T[] | undefined>;
  backendFilteredSize?: Signal<number>;
  backendLastLoadedPageSignal?: Signal<PaginationDTO | undefined>;
  lazyLoadOnSortOrFilter?: (d: DisplayConfigDTO<T>) => void;
  lazyLoadOnPaginateOrScroll?: (d: DisplayConfigDTO<T>) => void;
}

export class TableModel<T extends object> {

  // INPUTS
  // INPUT: table config (statically set by the developer that specify how each table should behave)
  readonly tableConfig: TableModelConfig;

  // INPUT: raw list
  private readonly rawList: Signal<T[] | undefined>;
  private readonly backendFilteredSize: Signal<number> | undefined;
  private readonly backendLastLoadedPageSignal: Signal<PaginationDTO | undefined> | undefined;

  // WRITABLE SIGNAL: sort/filter/paginate config (updated by the user during interacting with the table)
  readonly displayConfig = signal<DisplayConfigDTO<T>>({});

  // COMPUTE
  private readonly displayList: Signal<DisplayList<T>> = computed(() => {
    // skip the logic:
    if (this.tableConfig.frontendOrBackend === 'backend') {
      return {
        pageNr: 1,
        displayedList: this.rawList(),
        filteredSize: requireValue(this.backendFilteredSize,
          'if we skip the frontend logic, we need the filtered size from the backend logic')(),
      };
    }
    // do the logic:
    return prepareDisplayList(this.rawList(), this.displayConfig());
  });

  // GETTERS

  // GET items on currently displayed page
  displayedList: Signal<T[] | undefined> = computed(() => this.displayList().displayedList);

  pagination: Signal<PaginationDTO | undefined> = computed(() => this.displayConfig().pagination);
  filteredSortedList: Signal<T[]> = computed(() => this.displayList()?.filteredSortedList ?? []);
  filteredListSize: Signal<number> = computed(() => this.displayList()?.filteredSize ?? 0);
  numberOfFilters: Signal<number> = computed(() => this.displayConfig().filter?.filters?.length ?? 0);
  rawListSize: Signal<number> = computed(() => this.rawList()?.length ?? 0);
  lastLoadedPage: Signal<PaginationDTO | undefined> = computed(() =>
    this.backendLastLoadedPageSignal ? this.backendLastLoadedPageSignal() : undefined);

  // "EVENTS"
  private readonly pageChangedSubject = new Subject<PaginationDTO>();
  private readonly sortChangedSubject = new Subject<SortDTO<T>>();
  private readonly filterChangedSubject = new Subject<FilterDTO<T>>();
  pageChanged = toSignal(this.pageChangedSubject.pipe(distinctUntilChanged()));
  sortChanged = toSignal(this.sortChangedSubject.pipe(distinctUntilChanged()));
  filterChanged = toSignal(this.filterChangedSubject.pipe(distinctUntilChanged()));

  private needsPaginationConfig(tableConfig: TableModelConfig): boolean {
    switch (tableConfig.pageOrScrollOrNothing) {
      case 'paginate':
        return true;
      case 'scroll':
        return tableConfig.frontendOrBackend === 'backend';
      default:
        return false;
    }
  }

  constructor(
    inputOutput: TableInputOutput<T>,
    tableConfig: TableModelConfig,
  ) {
    this.rawList = inputOutput.rawListSignal;
    this.backendFilteredSize = inputOutput.backendFilteredSize;
    this.backendLastLoadedPageSignal = inputOutput.backendLastLoadedPageSignal;
    this.tableConfig = tableConfig;

    // check required inputOutputs for infinite scrolling
    if (tableConfig.frontendOrBackend === 'backend' && tableConfig.pageOrScrollOrNothing === 'scroll') {
      requireValue(inputOutput.backendFilteredSize,
        'missing backendFilteredSize: for infinite scrolling we need to know if there is more');
      requireValue(inputOutput.backendLastLoadedPageSignal,
        'missing backendLastLoadedPageSignal: for infinite scrolling we need to know the last _successfully_ loaded page nr');
    }

    // initialize pagination
    if (this.needsPaginationConfig(tableConfig)) {
      this.displayConfig.update(displayConfig => ({
        ...displayConfig,
        pagination: {
          pageSize: tableConfig.originalPageSize ?? ORIGINAL_PAGE_SIZE,
          pageNr: 1,
        },
      }));
    }

    if (inputOutput.lazyLoadOnSortOrFilter) {
      // on sort and filter only: reset list
      effect(() => {
        const _changedSort = this.sortChanged();
        const _changedFilter = this.filterChanged();
        untracked(() => {
          // untracked because otherwise the reload is done on each displayConfig() change!!!
          inputOutput.lazyLoadOnSortOrFilter!(this.displayConfig());
        });
      });
    }

    if (inputOutput.lazyLoadOnPaginateOrScroll) {
      // on paginate only: append list
      effect(() => {
        const _changedPagination = this.pageChanged();
        untracked(() => {
          // untracked because otherwise the reload is done on each displayConfig() change!!!
          inputOutput.lazyLoadOnPaginateOrScroll!(this.displayConfig());
        });
      });
    }
  }

  // page changed
  onPageChanged($event: PageChanged): void {
    const newDisplayConfig: DisplayConfigDTO<T> = {
      ...this.displayConfig(),
      pagination: {
        pageNr: $event.pageNr,
        pageSize: $event.pageSize,
      },
    };
    this.displayConfig.set(newDisplayConfig);
    this.pageChangedSubject.next(newDisplayConfig.pagination!);
  }

  // sort: clicked one col
  onColSort($event: SortDirection, colName: Leaves<T>): void {
    const newDisplayConfig: DisplayConfigDTO<T> = updateSingleColSort(
      {
        colName,
        sortDirection: $event,
      },
      this.displayConfig(),
    );
    this.displayConfig.set(newDisplayConfig);
    this.sortChangedSubject.next(newDisplayConfig.sort!);
  }

  onSort(sort: SortDTO<T>): void {
    const newDisplayConfig: DisplayConfigDTO<T> = updateSort(
      sort,
      this.displayConfig(),
    );
    this.displayConfig.set(newDisplayConfig);
    this.sortChangedSubject.next(newDisplayConfig.sort!);
  }

  // get col sort state
  findColSortDTO(colName: Leaves<T>): ColSortDTO<T> | undefined {
    return findColSort(colName, this.displayConfig());
  }

  // filter changed
  onFilterChanged(filter: SimpleFilterDTO<T>): void {
    const newDisplayConfig: DisplayConfigDTO<T> = updateFilter(
      filter.filterName,
      filter.filterValue,
      filter.filterMethod,
      filter.filterCombinationMethod,
      this.displayConfig(),
    );
    this.displayConfig.set(newDisplayConfig);
    this.filterChangedSubject.next(newDisplayConfig.filter!);
  }

  resetPaginationToFirstPage(): void {
    const newDisplayConfig: DisplayConfigDTO<T> = {
      ...this.displayConfig(),
      pagination: gotoFirstPage(this.displayConfig().pagination),
    };
    this.displayConfig.set(newDisplayConfig);
  }

  resetFilters(): void {
    const newDisplayConfig: DisplayConfigDTO<T> = clearFilters(this.displayConfig());
    this.displayConfig.set(newDisplayConfig);
    this.filterChangedSubject.next(newDisplayConfig.filter!);
  }

}
