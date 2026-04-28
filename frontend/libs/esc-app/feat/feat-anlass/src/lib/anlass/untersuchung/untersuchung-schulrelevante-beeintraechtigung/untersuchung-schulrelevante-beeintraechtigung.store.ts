import {
  UntersuchungSchulrelevanteBeeintraechtigungJson,
  UntersuchungSchulrelevanteBeeintraechtigungService,
  UntersuchungSchulrelevanteBeeintraechtigungServiceGetUntersuchungSchulrelevanteBeeintraechtigungRequestParams,
  UntersuchungSchulrelevanteBeeintraechtigungServiceSaveUntersuchungSchulrelevanteBeeintraechtigungRequestParams,
} from "@dvb/data-access-backend";
import { toEinladungBackendEntityIdWithClass } from '@dvb/data-access-types';
import {inject, Injectable} from "@angular/core";
import {patchState, signalStore, withState} from "@ngrx/signals";
import {AppErrorHandlerService, AppNotificationStore, Loader} from "@dvb/util-angular";
import {UntersuchungStore} from "../untersuchung.store";
import {TranslocoService} from "@jsverse/transloco";
import {rxMethod} from "@ngrx/signals/rxjs-interop";
import {exhaustMap, map, pipe, switchMap, tap} from "rxjs";
import {tapResponse} from "@ngrx/operators";

type UntersuchungSchulrelevanteBeeintraechtigungState = {
  untersuchungSchulrelevanteBeeintraechtigung: UntersuchungSchulrelevanteBeeintraechtigungJson | undefined;
};

const initialState: UntersuchungSchulrelevanteBeeintraechtigungState = {
  untersuchungSchulrelevanteBeeintraechtigung: undefined,
};

@Injectable()
export class UntersuchungSchulrelevanteBeeintraechtigungStore extends signalStore({ protectedState: false },
  withState(initialState)) {
  readonly #service = inject(UntersuchungSchulrelevanteBeeintraechtigungService);
  readonly #appErrorHandlerService = inject(AppErrorHandlerService);
  readonly #untersuchungStore = inject(UntersuchungStore);
  readonly #appNotificationStore = inject(AppNotificationStore);
  readonly #transloco = inject(TranslocoService);

  readonly loader = new Loader();
  readonly saveLoader = new Loader();

  loadUntersuchungSchulrelevanteBeeintraechtigung = rxMethod<UntersuchungSchulrelevanteBeeintraechtigungServiceGetUntersuchungSchulrelevanteBeeintraechtigungRequestParams>(
    pipe(
      tap(() => patchState(this, { untersuchungSchulrelevanteBeeintraechtigung: undefined })),
      switchMap(params =>
        this.loader.tapLoading$(this.#service.getUntersuchungSchulrelevanteBeeintraechtigung$(params), error =>
          this.#appErrorHandlerService.handleError(error),
        ),
      ),
      tapResponse({
        next: untersuchungSchulrelevanteBeeintraechtigung => patchState(this, { untersuchungSchulrelevanteBeeintraechtigung }),
        error: console.error,
      }),
    ),
  );

  saveUntersuchungSchulrelevanteBeeintraechtigung = rxMethod<UntersuchungSchulrelevanteBeeintraechtigungServiceSaveUntersuchungSchulrelevanteBeeintraechtigungRequestParams>(
    pipe(
      exhaustMap(params =>
        this.saveLoader.tapLoading$(
          this.#service
            .saveUntersuchungSchulrelevanteBeeintraechtigung$(params)
            .pipe(map(untersuchungSchulrelevanteBeeintraechtigung => ({ untersuchungSchulrelevanteBeeintraechtigung, params }))),
          error => this.#appErrorHandlerService.handleError(error),
        ),
      ),
      tapResponse({
        next: ({ untersuchungSchulrelevanteBeeintraechtigung, params }) => {
          patchState(this, { untersuchungSchulrelevanteBeeintraechtigung });
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
