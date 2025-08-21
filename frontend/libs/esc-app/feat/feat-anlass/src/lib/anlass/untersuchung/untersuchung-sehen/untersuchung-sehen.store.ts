import {inject, Injectable} from '@angular/core';
import {
  UntersuchungSehenJson,
  UntersuchungSehenSaveJson,
  UntersuchungSehenService,
  UntersuchungSehenServiceGetUntersuchungSehenRequestParams,
  UntersuchungSehenServiceSaveUntersuchungSehenRequestParams,
} from '@dvb/data-access-backend';
import {AppErrorHandlerService, AppNotificationStore, Loader} from '@dvb/util-angular';
import {TranslocoService} from '@jsverse/transloco';
import {tapResponse} from '@ngrx/operators';
import {patchState, signalStore, withMethods, withState} from '@ngrx/signals';
import {rxMethod} from '@ngrx/signals/rxjs-interop';
import {exhaustMap, map, pipe, switchMap, tap} from 'rxjs';
import {AnlassEinladungenId} from '../anlass-einladungen-id';
import {UntersuchungStore} from '../untersuchung.store';

type UntersuchungSehenState = {
  sehen: UntersuchungSehenJson | undefined;
  defaultSehenInitial: UntersuchungSehenSaveJson | undefined;
  defaultSehenToApply: UntersuchungSehenSaveJson | undefined;
};

const initialState: UntersuchungSehenState = {
  sehen: undefined,
  defaultSehenInitial: undefined,
  defaultSehenToApply: undefined,
};

@Injectable()
export class UntersuchungSehenStore extends signalStore(
  {protectedState: false},
  withState(initialState),
  withMethods((store) => ({
    applyDefaultSehen(): void {
      patchState(store, {defaultSehenToApply: store.defaultSehenInitial()});
    },
    resetApplyDefaultSehen(): void {
      patchState(store, {defaultSehenToApply: undefined});
    },
  })),
) {
  readonly #sehenService = inject(UntersuchungSehenService);
  readonly #appErrorHandlerService = inject(AppErrorHandlerService);
  readonly #untersuchungStore = inject(UntersuchungStore);
  readonly #appNotificationStore = inject(AppNotificationStore);
  readonly #transloco = inject(TranslocoService);

  readonly loader = new Loader();
  readonly defaultLoader = new Loader();
  readonly saveLoader = new Loader();

  loadSehen = rxMethod<UntersuchungSehenServiceGetUntersuchungSehenRequestParams>(
    pipe(
      tap(() => patchState(this, {sehen: undefined})),
      switchMap(params => this.loader.tapLoading$(this.#sehenService.getUntersuchungSehen$(params),
        error => this.#appErrorHandlerService.handleError(error))),
      tapResponse({
        next: sehen => patchState(this, {sehen}),
        error: console.error,
      }),
    ),
  );

  loadDefaultSehen = rxMethod<AnlassEinladungenId>(
    pipe(
      switchMap(params => this.defaultLoader.tapLoading$(this.#sehenService.getDefaultUntersuchungSehenForAnlassConfig$(
          {
            anlassId: params.anlassId,
            einladungId: params.einladungIds[0],
          }),
        error => this.#appErrorHandlerService.handleError(error))),
      tapResponse({
        next: data => patchState(this, {defaultSehenInitial: data}),
        error: console.error,
      }),
    ),
  );

  saveSehen = rxMethod<UntersuchungSehenServiceSaveUntersuchungSehenRequestParams>(
    pipe(
      exhaustMap(params =>
        this.saveLoader.tapLoading$(
          this.#sehenService.saveUntersuchungSehen$(params).pipe(
            map(sehen => ({sehen, params})),
          ),
          error => this.#appErrorHandlerService.handleError(error),
        ),
      ),
      tapResponse({
        next: ({sehen, params}) => {
          patchState(this, {sehen});
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
