import {computed, inject, Injectable} from '@angular/core';
import {KindSearchJson, SearchService} from '@dvb/data-access-backend';
import {AppErrorHandlerService, Loader, withSelectedEntity} from '@dvb/util-angular';
import {tapResponse} from '@ngrx/operators';
import {patchState, signalStore, withComputed, withState} from '@ngrx/signals';
import {setAllEntities, withEntities} from '@ngrx/signals/entities';
import {rxMethod} from '@ngrx/signals/rxjs-interop';
import {debounceTime, distinctUntilChanged, pipe, switchMap} from 'rxjs';

type MoreResultsState = {
  hasMoreResults: boolean;
};

@Injectable()
export class KindSearchStore extends signalStore(
  {protectedState: false},
  // withDevtools('kind-search'),
  withEntities<KindSearchJson>(),
  withState<MoreResultsState>({hasMoreResults: false}),
  withSelectedEntity<KindSearchJson>(),
  withComputed(({entities}) => ({
    countAll: computed(() => entities().length),
  })),
) {
  readonly #searchService = inject(SearchService);
  readonly #appErrorHandlerService = inject(AppErrorHandlerService);
  readonly loader = new Loader();

  searchKinder = rxMethod<string>(
    pipe(
      distinctUntilChanged(),
      debounceTime(150),
      switchMap(term =>
        this.loader.tapLoading$(this.#searchService.searchKinder$({searchTerm: term}), error =>
          this.#appErrorHandlerService.handleError(error),
        ),
      ),
      tapResponse({
        next: result => {
          patchState(this, {hasMoreResults: result.moreResultsAvailable});
          patchState(this, setAllEntities(result.kinderList));
        },
        error: console.error,
      }),
    ),
  );
}
