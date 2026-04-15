import {
  UntersuchungAbschlussJson,
  UntersuchungAbschlussService,
  UntersuchungAbschlussServiceGetUntersuchungAbschlussRequestParams,
  UntersuchungAbschlussServiceSaveUntersuchungAbschlussRequestParams
} from "@dvb/data-access-backend";
import {inject, Injectable} from "@angular/core";
import {patchState, signalStore, withState} from "@ngrx/signals";
import {AppErrorHandlerService, AppNotificationStore, Loader} from "@dvb/util-angular";
import {UntersuchungStore} from "../../untersuchung.store";
import {TranslocoService} from "@jsverse/transloco";
import {rxMethod} from "@ngrx/signals/rxjs-interop";
import {exhaustMap, map, pipe, switchMap, tap} from "rxjs";
import {tapResponse} from "@ngrx/operators";

type UntersuchungAbschlussState = {
  untersuchungAbschluss: UntersuchungAbschlussJson | undefined;
};

const initialState: UntersuchungAbschlussState = {
  untersuchungAbschluss: undefined,
};

@Injectable()
export class UntersuchungAbschlussStore extends signalStore({protectedState: false}, withState(initialState)) {
  readonly #AbschlussService = inject(UntersuchungAbschlussService);
  readonly #appErrorHandlerService = inject(AppErrorHandlerService);
  readonly #untersuchungStore = inject(UntersuchungStore);
  readonly #appNotificationStore = inject(AppNotificationStore);
  readonly #transloco = inject(TranslocoService);

  readonly loader = new Loader();
  readonly saveLoader = new Loader();

  loadUntersuchungAbschluss = rxMethod<UntersuchungAbschlussServiceGetUntersuchungAbschlussRequestParams>(
    pipe(
      tap(() => patchState(this, {untersuchungAbschluss: undefined})),
      switchMap(params =>
        this.loader.tapLoading$(this.#AbschlussService.getUntersuchungAbschluss$(params), error =>
          this.#appErrorHandlerService.handleError(error),
        ),
      ),
      tapResponse({
        next: untersuchungAbschluss => patchState(this, {untersuchungAbschluss}),
        error: console.error,
      }),
    ),
  );

  saveUntersuchungAbschluss = rxMethod<UntersuchungAbschlussServiceSaveUntersuchungAbschlussRequestParams>(
    pipe(
      exhaustMap(params =>
        this.saveLoader.tapLoading$(
          this.#AbschlussService
            .saveUntersuchungAbschluss$(params)
            .pipe(map(untersuchungAbschluss => ({untersuchungAbschluss, params}))),
          error => this.#appErrorHandlerService.handleError(error),
        ),
      ),
      tapResponse({
        next: ({untersuchungAbschluss, params}) => {
          patchState(this, {untersuchungAbschluss});
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
