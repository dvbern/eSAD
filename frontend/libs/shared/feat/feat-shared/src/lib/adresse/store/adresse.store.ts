import {withDevtools} from '@angular-architects/ngrx-toolkit';
import {inject, Injectable} from '@angular/core';
import {PLZDataListJson, PlzService} from '@dvb/data-access-backend';
import {AppErrorHandlerService, Loader} from '@dvb/util-angular';
import {tapResponse} from '@ngrx/operators';
import {EmptyFeatureResult, patchState, signalStore, SignalStoreFeature, withState} from '@ngrx/signals';
import {rxMethod} from '@ngrx/signals/rxjs-interop';
import {exhaustMap, of, pipe} from 'rxjs';

interface SystemAdminState {
  plzMap: { [key: string]: PLZDataListJson };
  searchResults: { [key: string]: PLZDataListJson }
}

const initialState: SystemAdminState = {
  plzMap: {},
  searchResults: {},
};

@Injectable({ providedIn: 'root' })
export class AdresseStore extends signalStore(
  { protectedState: false },
  withDevtools('adresseStore') as SignalStoreFeature<EmptyFeatureResult, EmptyFeatureResult>,
  withState(initialState),
) {
  readonly loader = new Loader();
  readonly #plzService = inject(PlzService);
  readonly #appErrorHandlerService = inject(AppErrorHandlerService);

  onReadPlzData = rxMethod<{ searchString: string, resultFor: string }>(
    pipe(
      exhaustMap(({ searchString, resultFor }) => {
          const plzDataListJson: PLZDataListJson = this.plzMap()[searchString];
          if (plzDataListJson) {
            return of(patchState(this, { searchResults: { ...this.searchResults(), [resultFor]: plzDataListJson } }));
          }
          return this.loader
            .tapLoading$(
              this.#plzService.findPLZData$({ plzdata: searchString }),
              error => this.#appErrorHandlerService.handleError(error))
            .pipe(
              tapResponse({
                next: (result) => {
                  patchState(this, { plzMap: { ...this.plzMap(), [searchString]: result } });
                  patchState(this, { searchResults: { ...this.searchResults(), [resultFor]: result } })
                },
                error: console.error,
              }),
            );
        },
      )),
  );

  /**
   * Cleans out the searchResults, called OnDestroy of the AdressComponent.
   */
  resetSearchResult(resultFor: string): void {
    const searchResults = this.searchResults();
    delete searchResults[resultFor];
    patchState(this, {searchResults: searchResults});
  }
}
