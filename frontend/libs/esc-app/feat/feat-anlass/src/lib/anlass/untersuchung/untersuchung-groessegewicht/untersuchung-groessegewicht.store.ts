import {inject, Injectable} from '@angular/core';
import {
  UntersuchungGroesseGewichtDataJson,
  UntersuchungGroesseGewichtService,
  UntersuchungGroesseGewichtServiceGetUntersuchungGroesseGewichtRequestParams,
  UntersuchungGroesseGewichtServiceSaveUntersuchungGroesseGewichtRequestParams,
} from '@dvb/data-access-backend';
import { toEinladungBackendEntityIdWithClass } from '@dvb/data-access-types';
import {AppErrorHandlerService, AppNotificationStore, Loader} from '@dvb/util-angular';
import {TranslocoService} from '@jsverse/transloco';
import {tapResponse} from '@ngrx/operators';
import {patchState, signalStore, withState} from '@ngrx/signals';
import {rxMethod} from '@ngrx/signals/rxjs-interop';
import {exhaustMap, map, pipe, switchMap, tap} from 'rxjs';
import {UntersuchungStore} from '../untersuchung.store';

type UntersuchungGroesseGewichtState = {
  groesseGewicht: UntersuchungGroesseGewichtDataJson | undefined;
};

const initialState: UntersuchungGroesseGewichtState = {
  groesseGewicht: undefined,
};

@Injectable()
export class UntersuchungGroesseGewichtStore extends signalStore(
  {protectedState: false},
  withState(initialState),
) {
  readonly #groesseGewichtService = inject(UntersuchungGroesseGewichtService);
  readonly #appErrorHandlerService = inject(AppErrorHandlerService);
  readonly #untersuchungStore = inject(UntersuchungStore);
  readonly #appNotificationStore = inject(AppNotificationStore);
  readonly #transloco = inject(TranslocoService);

  readonly loader = new Loader();
  readonly saveLoader = new Loader();

  loadGroesseGewicht = rxMethod<UntersuchungGroesseGewichtServiceGetUntersuchungGroesseGewichtRequestParams>(
    pipe(
      tap(() => patchState(this, {groesseGewicht: undefined})),
      switchMap(params => this.loader.tapLoading$(this.#groesseGewichtService.getUntersuchungGroesseGewicht$(params),
        error => this.#appErrorHandlerService.handleError(error))),
      tapResponse({
        next: groesseGewicht => patchState(this, {groesseGewicht}),
        error: console.error,
      }),
    ),
  );

  saveGroesseGewicht = rxMethod<UntersuchungGroesseGewichtServiceSaveUntersuchungGroesseGewichtRequestParams>(
    pipe(
      exhaustMap(params =>
        this.saveLoader.tapLoading$(
          this.#groesseGewichtService.saveUntersuchungGroesseGewicht$(params).pipe(
            map(groesseGewicht => ({groesseGewicht, params})),
          ),
          error => this.#appErrorHandlerService.handleError(error),
        ),
      ),
      tapResponse({
        next: ({groesseGewicht, params}) => {
          patchState(this, {groesseGewicht});
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
