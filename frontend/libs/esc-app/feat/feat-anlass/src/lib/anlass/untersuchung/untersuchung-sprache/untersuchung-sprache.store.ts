import {inject, Injectable} from '@angular/core';
import {
  UntersuchungSpracheJson,
  UntersuchungSpracheSaveJson,
  UntersuchungSpracheService,
  UntersuchungSpracheServiceGetDefaultUntersuchungSpracheRequestParams,
  UntersuchungSpracheServiceGetUntersuchungSpracheRequestParams,
  UntersuchungSpracheServiceSaveUntersuchungSpracheRequestParams,
} from '@dvb/data-access-backend';
import {AppErrorHandlerService, AppNotificationStore, Loader} from '@dvb/util-angular';
import {TranslocoService} from '@jsverse/transloco';
import {tapResponse} from '@ngrx/operators';
import {patchState, signalStore, withState} from '@ngrx/signals';
import {rxMethod} from '@ngrx/signals/rxjs-interop';
import {exhaustMap, map, pipe, switchMap, tap} from 'rxjs';
import {withDefaultValues} from '../default-values.util';
import {UntersuchungStore} from '../untersuchung.store';

type UntersuchungSpracheState = {
  untersuchungSprache: UntersuchungSpracheJson | undefined;
};

const initialState: UntersuchungSpracheState = {
  untersuchungSprache: undefined,
};

@Injectable()
export class UntersuchungSpracheStore extends signalStore(
  { protectedState: false },
  withState(initialState),
  withDefaultValues<UntersuchungSpracheSaveJson, UntersuchungSpracheJson, UntersuchungSpracheState>({
    getCurrentEntity: (store) => store.untersuchungSprache(),
  }),
) {
  readonly #spracheService = inject(UntersuchungSpracheService);
  readonly #appErrorHandlerService = inject(AppErrorHandlerService);
  readonly #untersuchungStore = inject(UntersuchungStore);
  readonly #appNotificationStore = inject(AppNotificationStore);
  readonly #transloco = inject(TranslocoService);

  readonly loader = new Loader();
  readonly saveLoader = new Loader();

  loadUntersuchungSprache = rxMethod<UntersuchungSpracheServiceGetUntersuchungSpracheRequestParams>(
    pipe(
      tap(() => patchState(this, { untersuchungSprache: undefined })),
      switchMap(params =>
        this.loader.tapLoading$(this.#spracheService.getUntersuchungSprache$(params), error =>
          this.#appErrorHandlerService.handleError(error),
        ),
      ),
      tapResponse({
        next: untersuchungSprache => patchState(this, { untersuchungSprache }),
        error: console.error,
      }),
    ),
  );

  loadUntersuchungSpracheDefaults = rxMethod<UntersuchungSpracheServiceGetDefaultUntersuchungSpracheRequestParams>(
    pipe(
      switchMap(params => this.loader.tapLoading$(this.#spracheService.getDefaultUntersuchungSprache$(
          {
            anlassId: params.anlassId,
            einladungId: params.einladungId,
          }),
        error => this.#appErrorHandlerService.handleError(error))),
      tapResponse({
        next: data => patchState(this, {defaultInitial: data}),
        error: console.error,
      }),
    ),
  );

  saveUntersuchungSprache = rxMethod<UntersuchungSpracheServiceSaveUntersuchungSpracheRequestParams>(
    pipe(
      exhaustMap(params =>
        this.saveLoader.tapLoading$(
          this.#spracheService
            .saveUntersuchungSprache$(params)
            .pipe(map(untersuchungSprache => ({ untersuchungSprache, params }))),
          error => this.#appErrorHandlerService.handleError(error),
        ),
      ),
      tapResponse({
        next: ({ untersuchungSprache, params }) => {
          patchState(this, { untersuchungSprache });
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
