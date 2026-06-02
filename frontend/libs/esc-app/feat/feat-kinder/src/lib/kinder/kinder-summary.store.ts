import {withDevtools} from '@angular-architects/ngrx-toolkit';
import {computed, inject, Injectable} from '@angular/core';
import {
  EntityAktivStatusSaveJson,
  KinderService,
  KindJson,
  KindSaveJson,
  KindSaveModeDuplikat,
  SearchColumns,
  SearchJson,
  SearchService,
  SearchSummaryJson,
} from '@dvb/data-access-backend';
import {BackendEntityId} from '@dvb/data-access-types';
import {EventBusStore, getOptionWithSuffix} from '@dvb/feat-shared';
import {AppErrorHandlerService, DisplayConfigDTO, Loader, PaginationDTO} from '@dvb/util-angular';
import {isNotNullish, isNullishOrEmpty, requireValue} from '@dvb/util-shared';
import {TranslocoService} from '@jsverse/transloco';
import {tapResponse} from '@ngrx/operators';
import {EmptyFeatureResult, patchState, signalStore, SignalStoreFeature, withComputed, withState} from '@ngrx/signals';
import {EntityId, removeEntity, setAllEntities, setEntities, withEntities} from '@ngrx/signals/entities';
import {rxMethod} from '@ngrx/signals/rxjs-interop';
import {exhaustMap, map, mergeMap, pipe} from 'rxjs';
import {DEFAULT_AKTIVE_ONLY_FILTER, ErweiterteFilterJson} from './kinder-list/kinder-filter/kinder-filter.form';
import {
  mapErweiterteFilterToFilterJson,
  mapFilterToFilterJson,
  mapSortToSortJson,
} from './kinder-pagination-util/kinder-pagination-mapping-functions';

type KindSummmaryState = {
  totalFilteredKinderCount: number | undefined;
  totalKinderCount: number | undefined;
  backendFilteredListSize: number;
  currentLoadedPage: PaginationDTO | undefined;
  erweiterteFilterValues: ErweiterteFilterJson | undefined;
};

const initialState: KindSummmaryState = {
  totalFilteredKinderCount: undefined,
  totalKinderCount: undefined,
  backendFilteredListSize: 0,
  currentLoadedPage: undefined,
  erweiterteFilterValues: DEFAULT_AKTIVE_ONLY_FILTER,
};

@Injectable()
export class KinderSummaryStore extends signalStore(
  {protectedState: false},
  withDevtools('kinder-page-list') as SignalStoreFeature<EmptyFeatureResult, EmptyFeatureResult>,
  withEntities<SearchSummaryJson>(),
  withState<KindSummmaryState>(initialState),
  withComputed(({entities, erweiterteFilterValues}) => ({
    loadedRowCount: computed(() => entities().length),
    erweiterteFilter: computed(() => mapErweiterteFilterToFilterJson(erweiterteFilterValues())),
    // since we add the inaktiv == false filter by default we should remove it from the count of manually set filters
    manualErweiterteFilterCount: computed(() => {
      const currentFilters = mapErweiterteFilterToFilterJson(erweiterteFilterValues());
      const withoutInaktivFilter = currentFilters.filter(
        f => f.columnName !== SearchColumns.INAKTIV || (f.columnName
          === SearchColumns.INAKTIV
          && f.filterValue
          !== 'false'),
      );
      return withoutInaktivFilter.length;
    }),
  })),
) {
  readonly #kinderService = inject(KinderService);
  readonly #searchService = inject(SearchService);
  readonly #appErrorHandlerService = inject(AppErrorHandlerService);
  readonly #eventBusStore = inject(EventBusStore);
  readonly #transloco = inject(TranslocoService);

  readonly loader = new Loader();
  readonly saveLoader = new Loader();
  readonly changeStatusLoader = new Loader();

  kinderListWithSchuleMarkedInactive = computed(() =>
    this.entities().map((kind) =>
      kindMarkInactiveSchuleAndKlasse(this.#transloco.translate('gloabl.suffixInaktiv'), kind))
  );

  setErweiterteFilter(filter: ErweiterteFilterJson): void {
    patchState(this, {erweiterteFilterValues: filter})
  }

  resetErweiterteFilter(): void {
    patchState(this, {erweiterteFilterValues: DEFAULT_AKTIVE_ONLY_FILTER})
  }

  loadPaginatedKinderList = rxMethod<{
    config: DisplayConfigDTO<SearchSummaryJson>,
    replaceList: boolean
  }>(
    pipe(
      // mergeMap because for infinite scrolling, each request is important!
      mergeMap(({config, replaceList}) => {
        const searchJson = mapDisplayConfigToSearchJson(config, replaceList);
        // add the filters from the erweiterteFilter Popup
        searchJson.filters.push(...this.erweiterteFilter());

        return this.loader.tapLoading$(
          this.#searchService.findKinderPaginated$({searchJson}),
          error => this.#appErrorHandlerService.handleError(error),
        )
          .pipe(
            map((result) => ({
              result: result,
              replaceList: replaceList,
            })),
          );
      }),
      tapResponse({
        next: ({result, replaceList}) => {
          // backendFilteredListSize: required for infinite scroll, but only loaded once on first request
          patchState(this, {
            totalKinderCount : replaceList ? result.totalItemCount : this.totalKinderCount(),
            backendFilteredListSize: replaceList ? result.totalFilteredCount : this.backendFilteredListSize(),
            currentLoadedPage: {
              pageNr: result.page,
              pageSize: result.data.length,
            },
          });

          // update list
          const sizeBefore = replaceList ? 0 : this.entities().length;
          patchState(this, replaceList
            // sort&filter: replace list
            ? setAllEntities(result.data)
            // paginate: append list
            : setEntities(result.data));
          const sizeAfter = this.entities().length;
          console.debug('kinder summary store entities. page: '
            + result.page
            + '. size: before='
            + sizeBefore
            + ', after='
            + sizeAfter);

          // log if the result is empty unexpectedly
          if ((this.backendFilteredListSize() ?? 0) > sizeAfter) {
            if (result.data.length < 1) {
              console.error('The list is not complete but the last page request was empty', result, this);
            }
          }
        },
        error: console.error,
      }),
    ),
  );

  createKind = rxMethod<{
    kind: KindSaveJson;
    kindSaveMode: KindSaveModeDuplikat;
    arztId?: BackendEntityId;
    mpaId?: BackendEntityId;
    onSuccess: (result: KindJson) => void
  }>(
    pipe(
      exhaustMap(({kind, kindSaveMode, arztId, mpaId, onSuccess}) =>
        this.saveLoader
          .tapLoading$(this.#kinderService.createKindAndDossier$({
              kindSaveJson: kind,
              kindSaveMode: kindSaveMode,
              arztId: arztId,
              mpaId: mpaId,
            }), error => this.#appErrorHandlerService.handleError(error),
          )
          .pipe(
            tapResponse({
              next: result => {
                if (result.kindJson && (isNullishOrEmpty(result.idsOfPotentialDuplicates))) {
                  onSuccess(result.kindJson);
                } else {
                  this.#eventBusStore.emit('KIND_DUPLIKAT_VORHANDEN',
                    {kindTriedToSave: kind, kindIdsOfDuplicates: requireValue(result.idsOfPotentialDuplicates)});
                }
              },
              error: console.error,
            }),
          ),
      ),
    ),
  );

  changeKindAktivStatus = rxMethod<{ kindId: BackendEntityId, saveJson: EntityAktivStatusSaveJson }>(
    pipe(
      exhaustMap(({kindId, saveJson}) =>
        this.changeStatusLoader
          .tapLoading$(this.#kinderService.updateKindAktivStatus$({
              kindId: kindId,
              entityAktivStatusSaveJson: saveJson,
            }),
            error =>
              this.#appErrorHandlerService.handleError(error),
          )
          .pipe(
            tapResponse({
              next: () => {
                // since we 'removed' an element we have to also remove it from the filtered list size if we dont reload
                // the total size from the backend (which we currently do not)
                if(isNotNullish(this.backendFilteredListSize()) && this.backendFilteredListSize() > 0) {
                  patchState(this, {backendFilteredListSize: this.backendFilteredListSize() - 1});
                }
                patchState(this, removeEntity(kindId));
              },
              error: console.error,
            }),
          ),
      ),
    ),
  );
}

export function countEntities<T>(list: T[], accessor: (each: T) => EntityId | undefined): number {
  return new Set(list.map(accessor).filter(id => id)).size;
}

export function mapDisplayConfigToSearchJson(
  config: DisplayConfigDTO<SearchSummaryJson>,
  initialSearch: boolean,
): SearchJson {
  return {
    paginationRequest: {
      pageNr: config.pagination?.pageNr ?? 1,
      maxPageSize: config.pagination?.pageSize ?? 10,
      requestTotal: (config.needsOriginalSize ?? true) && initialSearch, // only request total size on first request
    },
    sorts: mapSortToSortJson(config.sort),
    filters: mapFilterToFilterJson(config.filter),
  };
}

function kindMarkInactiveSchuleAndKlasse(translatedSuffix: string, kind: SearchSummaryJson) {
  return {
    ...kind,
    schule: getOptionWithSuffix(translatedSuffix, kind.schule),
    klasse: getOptionWithSuffix(translatedSuffix, kind.klasse),
  };
}
