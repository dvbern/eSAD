import { inject, Injectable } from '@angular/core';
import {
  UntersuchungLaeusekontrolleJson,
  UntersuchungLaeusekontrolleService,
  UntersuchungLaeusekontrolleServiceGetUntersuchungLaeusekontrolleRequestParams,
  UntersuchungLaeusekontrolleServiceSaveUntersuchungLaeusekontrolleRequestParams,
} from '@dvb/data-access-backend';
import { AppErrorHandlerService, AppNotificationStore, Loader } from '@dvb/util-angular';
import { TranslocoService } from '@jsverse/transloco';
import { tapResponse } from '@ngrx/operators';
import { patchState, signalStore, withState } from '@ngrx/signals';
import { rxMethod } from '@ngrx/signals/rxjs-interop';
import { exhaustMap, map, pipe, switchMap, tap } from 'rxjs';
import { UntersuchungStore } from '../untersuchung.store';

type UntersuchungLaeusekontrolleState = {
  laeusekontrolle: UntersuchungLaeusekontrolleJson | undefined;
};

const initialState: UntersuchungLaeusekontrolleState = {
  laeusekontrolle: undefined,
};

@Injectable()
export class UntersuchungLaeusekontrolleStore extends signalStore(
  { protectedState: false },
  // withDevtools('untersuchungTriage'),
  withState(initialState),
) {
  readonly #laeusekontrolleService = inject(UntersuchungLaeusekontrolleService);
  readonly #appErrorHandlerService = inject(AppErrorHandlerService);
  readonly #untersuchungStore = inject(UntersuchungStore);
  readonly #appNotificationStore = inject(AppNotificationStore);
  readonly #transloco = inject(TranslocoService);

  readonly loader = new Loader();
  readonly saveLoader = new Loader();

  loadLaeusekontrolle = rxMethod<UntersuchungLaeusekontrolleServiceGetUntersuchungLaeusekontrolleRequestParams>(
    pipe(
      tap(() => patchState(this, { laeusekontrolle: undefined })),
      switchMap(params =>
        this.loader.tapLoading$(this.#laeusekontrolleService.getUntersuchungLaeusekontrolle$(params), error =>
          this.#appErrorHandlerService.handleError(error),
        ),
      ),
      tapResponse({
        next: laeusekontrolle => patchState(this, { laeusekontrolle }),
        error: console.error,
      }),
    ),
  );

  saveLaeusekontrolle = rxMethod<UntersuchungLaeusekontrolleServiceSaveUntersuchungLaeusekontrolleRequestParams>(
    pipe(
      exhaustMap(params =>
        this.saveLoader.tapLoading$(
          this.#laeusekontrolleService
            .saveUntersuchungLaeusekontrolle$(params)
            .pipe(map(laeusekontrolle => ({ laeusekontrolle: laeusekontrolle, params }))),
          error => this.#appErrorHandlerService.handleError(error),
        ),
      ),
      tapResponse({
        next: ({ laeusekontrolle, params }) => {
          patchState(this, { laeusekontrolle });
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
