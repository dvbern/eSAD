import {
  UntersuchungSignaturJson,
  UntersuchungSignaturService,
  UntersuchungSignaturServiceGetUntersuchungSignaturRequestParams,
  UntersuchungSignaturServiceResetSignaturRequestParams,
  UntersuchungSignaturServiceSignierenRequestParams
} from "@dvb/data-access-backend";
import {computed, inject, Injectable} from "@angular/core";
import {patchState, signalStore, withComputed, withState} from "@ngrx/signals";
import {AppErrorHandlerService, AppNotificationStore, Loader} from "@dvb/util-angular";
import {TranslocoService} from "@jsverse/transloco";
import {rxMethod} from "@ngrx/signals/rxjs-interop";
import {exhaustMap, map, pipe, switchMap, tap} from "rxjs";
import {tapResponse} from "@ngrx/operators";
import {UntersuchungStore} from "../../untersuchung.store";

type UntersuchungSignaturState = {
  untersuchungSignatur: UntersuchungSignaturJson | undefined;
};

const initialState: UntersuchungSignaturState = {
  untersuchungSignatur: undefined,
};

@Injectable()
export class UntersuchungSignaturStore extends signalStore(
  {protectedState: false},
  withState(initialState),
  withComputed(({ untersuchungSignatur }) => ({
    isSigniert: computed(() =>
      !!untersuchungSignatur()?.data.person && !!untersuchungSignatur()?.data.timestampVisiert,
    ),
  })),
) {
  readonly #signaturService = inject(UntersuchungSignaturService);
  readonly #untersuchungStore = inject(UntersuchungStore);
  readonly #appErrorHandlerService = inject(AppErrorHandlerService);
  readonly #appNotificationStore = inject(AppNotificationStore);
  readonly #transloco = inject(TranslocoService);

  readonly loader = new Loader();
  readonly saveLoader = new Loader();

  loadUntersuchungSignatur = rxMethod<UntersuchungSignaturServiceGetUntersuchungSignaturRequestParams>(
    pipe(
      tap(() => patchState(this, {untersuchungSignatur: undefined})),
      switchMap(params =>
        this.loader.tapLoading$(this.#signaturService.getUntersuchungSignatur$(params), error =>
          this.#appErrorHandlerService.handleError(error),
        ),
      ),
      tapResponse({
        next: untersuchungSignatur => patchState(this, {untersuchungSignatur}),
        error: console.error,
      }),
    ),
  );

  signieren = rxMethod<UntersuchungSignaturServiceSignierenRequestParams>(
    pipe(
      exhaustMap(params =>
        this.saveLoader.tapLoading$(
          this.#signaturService
            .signieren$(params)
            .pipe(map(untersuchungSignatur => ({untersuchungSignatur, params}))),
          error => this.#appErrorHandlerService.handleError(error),
        ),
      ),
      tapResponse({
        next: ({untersuchungSignatur, params}) => {
          patchState(this, {untersuchungSignatur});
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

  resetSignatur = rxMethod<UntersuchungSignaturServiceResetSignaturRequestParams>(
    pipe(
      exhaustMap(params =>
        this.saveLoader.tapLoading$(
          this.#signaturService
            .resetSignatur$(params)
            .pipe(map(untersuchungSignatur => ({untersuchungSignatur, params}))),
          error => this.#appErrorHandlerService.handleError(error),
        ),
      ),
      tapResponse({
        next: ({untersuchungSignatur, params}) => {
          patchState(this, {untersuchungSignatur});
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
