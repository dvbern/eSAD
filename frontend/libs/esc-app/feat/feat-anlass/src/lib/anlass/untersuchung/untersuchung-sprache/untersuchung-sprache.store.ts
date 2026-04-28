import {inject, Injectable} from '@angular/core';
import {
  UntersuchungSpracheJson,
  UntersuchungSpracheService,
  UntersuchungSpracheServiceGetUntersuchungSpracheRequestParams,
  UntersuchungSpracheServiceSaveUntersuchungSpracheRequestParams,
} from '@dvb/data-access-backend';
import {AppErrorHandlerService, AppNotificationStore, Loader} from '@dvb/util-angular';
import {TranslocoService} from '@jsverse/transloco';
import {tapResponse} from '@ngrx/operators';
import {patchState, signalStore, withState} from '@ngrx/signals';
import {rxMethod} from '@ngrx/signals/rxjs-interop';
import {exhaustMap, map, pipe, switchMap, tap} from 'rxjs';
import {UntersuchungStore} from '../untersuchung.store';

type UntersuchungSpracheState = {
  untersuchungSprache: UntersuchungSpracheJson | undefined;
};

const initialState: UntersuchungSpracheState = {
  untersuchungSprache: undefined,
};

@Injectable()
export class UntersuchungSpracheStore extends signalStore({ protectedState: false }, withState(initialState)) {
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
