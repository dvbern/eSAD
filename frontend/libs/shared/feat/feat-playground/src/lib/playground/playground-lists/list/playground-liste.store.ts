import {withDevtools} from '@angular-architects/ngrx-toolkit';
import {inject} from '@angular/core';
import {AppErrorHandlerService, DisplayConfigDTO, Loader, PaginationDTO} from '@dvb/util-angular';
import {tapResponse} from '@ngrx/operators';
import {EmptyFeatureResult, patchState, signalStore, SignalStoreFeature, withState} from '@ngrx/signals';
import {rxMethod} from '@ngrx/signals/rxjs-interop';
import {map, mergeMap, pipe, switchMap} from 'rxjs';
import {FakeBackend} from './playground-liste.fake-backend';

export interface MyDummyItem {
  name: string;
  vorname: string;
}

type DummyState = {
  items: MyDummyItem[],
  filteredCount: number,
  currentLoadedPage: PaginationDTO | undefined
};

const initialState: DummyState = {
  items: [],
  filteredCount: 0,
  currentLoadedPage: undefined,
};

export class PlaygroundListsStore extends signalStore(
  {protectedState: false},
  withDevtools('playground-list') as SignalStoreFeature<EmptyFeatureResult, EmptyFeatureResult>,
  withState<DummyState>(initialState),
) {
  readonly backend = inject(FakeBackend);
  readonly loader = new Loader();
  readonly #appErrorHandlerService = inject(AppErrorHandlerService);

  loadCompleteList = rxMethod<void>(
    pipe(
      switchMap(() => {
        return this.loader.tapLoading$(
          this.backend.loadDummyListFull$(),
          error => this.#appErrorHandlerService.handleError(error),
        );
      }),
      tapResponse({
        next: result => {
          patchState(this, {filteredCount: result.length});
          patchState(this, {items: result});
        },
        error: console.error,
      }),
    ),
  );

  loadPaginated = rxMethod<{
    config: DisplayConfigDTO<MyDummyItem>,
    replaceList: boolean
  }>(
    pipe(
      // mergeMap because for infinite scrolling, each request is important!
      mergeMap(({config, replaceList}) => {
        return this.loader.tapLoading$(
          this.backend.loadDummyListPaginated$(config),
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
          patchState(this, (state) => ({
            filteredCount: result.totalItemCount,
            // update current pagination
            currentLoadedPage: {
              pageNr: result.page,
              pageSize: result.pageSize,
            },
            // filter/sort: Liste ersetzen. page: Liste ergaenzen
            items: replaceList
              ? result.data
              : [...state.items, ...result.data],
          }));

          // log if the result is empty unexpectedly
          if ((result.totalItemCount ?? 0) > this.items.length) {
            if (result.data.length < 1) {
              console.error('The list is not complete but the last page request was empty', result, this);
            }
          }
        },
        error: console.error,
      }),
    ),
  );
}
