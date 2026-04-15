import {inject, Injectable} from '@angular/core';
import {
  UntersuchungHoerenOtoskopieJson,
  UntersuchungHoerenOtoskopieSaveJson,
  UntersuchungHoerenOtoskopieService,
  UntersuchungHoerenOtoskopieServiceGetUntersuchungHoerenOtoskopieRequestParams,
  UntersuchungHoerenOtoskopieServiceSaveUntersuchungHoerenOtoskopieRequestParams,
} from '@dvb/data-access-backend';

import {AppErrorHandlerService, AppNotificationStore, Loader} from '@dvb/util-angular';
import {TranslocoService} from '@jsverse/transloco';
import {tapResponse} from '@ngrx/operators';
import {patchState, signalStore, withMethods, withState} from '@ngrx/signals';
import {rxMethod} from '@ngrx/signals/rxjs-interop';
import {exhaustMap, map, pipe, switchMap, tap} from 'rxjs';
import {UntersuchungStore} from '../../untersuchung.store';

type UntersuchungHoerenOtoskopieState = {
  hoerenOtoskopie: UntersuchungHoerenOtoskopieJson | undefined;
  defaultHoerenOtoskopieInitial: UntersuchungHoerenOtoskopieSaveJson | undefined;
  defaultHoerenOtoskopieToApply: UntersuchungHoerenOtoskopieSaveJson | undefined;
};

const initialState: UntersuchungHoerenOtoskopieState = {
  hoerenOtoskopie: undefined,
  defaultHoerenOtoskopieInitial: undefined,
  defaultHoerenOtoskopieToApply: undefined,
};

@Injectable()
export class UntersuchungHoerenOtoskopieStore extends signalStore(
  {protectedState: false},
  withState(initialState),
  withMethods((store) => ({
    applyDefaultHoerenOtoskopie(): void {
      patchState(store, {defaultHoerenOtoskopieToApply: store.defaultHoerenOtoskopieInitial()});
    },
    resetApplyDefaultHoerenOtoskopie(): void {
      patchState(store, {defaultHoerenOtoskopieToApply: undefined})
    },
  })),
) {
  readonly #hoerenOtoskopieService = inject(UntersuchungHoerenOtoskopieService);
  readonly #appErrorHandlerService = inject(AppErrorHandlerService);
  readonly #untersuchungStore = inject(UntersuchungStore);
  readonly #appNotificationStore = inject(AppNotificationStore);
  readonly #transloco = inject(TranslocoService);

  readonly loader = new Loader();
  readonly defaultLoader = new Loader();
  readonly saveLoader = new Loader();

  loadHoerenOtoskopie = rxMethod<UntersuchungHoerenOtoskopieServiceGetUntersuchungHoerenOtoskopieRequestParams>(
    pipe(
      tap(() => patchState(this, {hoerenOtoskopie: undefined})),
      switchMap(params => this.loader.tapLoading$(this.#hoerenOtoskopieService.getUntersuchungHoerenOtoskopie$(params),
        error => this.#appErrorHandlerService.handleError(error))),
      tapResponse({
        next: hoeren => patchState(this, {hoerenOtoskopie: hoeren}),
        error: console.error,
      }),
    ),
  );

  loadDefaultHoerenOtoskopie = rxMethod<UntersuchungHoerenOtoskopieServiceGetUntersuchungHoerenOtoskopieRequestParams>(
    pipe(
      switchMap(params => this.defaultLoader.tapLoading$(
        this.#hoerenOtoskopieService.getDefaultUntersuchungHoerenOtoskopieForAnlassConfig$({
          anlassId: params.anlassId,
          einladungId: params.einladungId,
        }),
        error => this.#appErrorHandlerService.handleError(error))),
      tapResponse({
        next: data => patchState(this, {defaultHoerenOtoskopieInitial: data}),
        error: console.error,
      }),
    ),
  );

  saveHoerenOtoskopie = rxMethod<UntersuchungHoerenOtoskopieServiceSaveUntersuchungHoerenOtoskopieRequestParams>(
    pipe(
      exhaustMap(params =>
        this.saveLoader.tapLoading$(
          this.#hoerenOtoskopieService.saveUntersuchungHoerenOtoskopie$(params).pipe(
            map(hoeren => ({hoeren, params})),
          ),
          error => this.#appErrorHandlerService.handleError(error),
        ),
      ),
      tapResponse({
        next: ({hoeren, params}) => {
          patchState(this, {hoerenOtoskopie: hoeren});
          this.#untersuchungStore.loadTabsSaved({
            anlassId: params.anlassId,
            einladungId: params.einladungId,
          });
          this.#appNotificationStore.addSuccessNotification(this.#transloco.translate('global.data.saved'));
        },
        error: console.error,
      }),
    ),
  );
}
