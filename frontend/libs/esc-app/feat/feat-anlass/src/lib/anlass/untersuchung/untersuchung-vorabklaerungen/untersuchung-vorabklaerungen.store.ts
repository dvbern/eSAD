import {
  UntersuchungVorabklaerungenJson,
  UntersuchungVorabklaerungenService,
  UntersuchungVorabklaerungenServiceGetUntersuchungVorabklaerungenRequestParams,
  UntersuchungVorabklaerungenServiceSaveUntersuchungVorabklaerungenRequestParams
} from "@dvb/data-access-backend";
import {inject, Injectable} from "@angular/core";
import {patchState, signalStore, withState} from "@ngrx/signals";
import {AppErrorHandlerService, AppNotificationStore, Loader} from "@dvb/util-angular";
import {UntersuchungStore} from "../untersuchung.store";
import {TranslocoService} from "@jsverse/transloco";
import {rxMethod} from "@ngrx/signals/rxjs-interop";
import {exhaustMap, map, pipe, switchMap, tap} from "rxjs";
import {tapResponse} from "@ngrx/operators";

type UntersuchungVorabklaerungenState = {
  untersuchungVorabklaerungen: UntersuchungVorabklaerungenJson | undefined;
};

const initialState: UntersuchungVorabklaerungenState = {
  untersuchungVorabklaerungen: undefined,
};

@Injectable()
export class UntersuchungVorabklaerungenStore extends signalStore({protectedState: false}, withState(initialState)) {
  readonly #vorabklaerungenService = inject(UntersuchungVorabklaerungenService);
  readonly #appErrorHandlerService = inject(AppErrorHandlerService);
  readonly #untersuchungStore = inject(UntersuchungStore);
  readonly #appNotificationStore = inject(AppNotificationStore);
  readonly #transloco = inject(TranslocoService);

  readonly loader = new Loader();
  readonly saveLoader = new Loader();

  loadUntersuchungVorabklaerungen = rxMethod<UntersuchungVorabklaerungenServiceGetUntersuchungVorabklaerungenRequestParams>(
    pipe(
      tap(() => patchState(this, {untersuchungVorabklaerungen: undefined})),
      switchMap(params =>
        this.loader.tapLoading$(this.#vorabklaerungenService.getUntersuchungVorabklaerungen$(params), error =>
          this.#appErrorHandlerService.handleError(error),
        ),
      ),
      tapResponse({
        next: untersuchungVorabklaerungen => patchState(this, {untersuchungVorabklaerungen}),
        error: console.error,
      }),
    ),
  );

  saveUntersuchungVorabklaerungen = rxMethod<UntersuchungVorabklaerungenServiceSaveUntersuchungVorabklaerungenRequestParams>(
    pipe(
      exhaustMap(params =>
        this.saveLoader.tapLoading$(
          this.#vorabklaerungenService
            .saveUntersuchungVorabklaerungen$(params)
            .pipe(map(untersuchungVorabklaerungen => ({untersuchungVorabklaerungen, params}))),
          error => this.#appErrorHandlerService.handleError(error),
        ),
      ),
      tapResponse({
        next: ({untersuchungVorabklaerungen, params}) => {
          patchState(this, {untersuchungVorabklaerungen});
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
