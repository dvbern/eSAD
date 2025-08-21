import {inject, Injectable} from '@angular/core';
import {
  UntersuchungMassnahmenJson,
  UntersuchungMassnahmenService,
  UntersuchungMassnahmenServiceGetUntersuchungMassnahmenRequestParams,
  UntersuchungMassnahmenServiceSaveUntersuchungMassnahmenRequestParams,
} from '@dvb/data-access-backend';
import {AppErrorHandlerService, AppNotificationStore, Loader} from '@dvb/util-angular';
import {TranslocoService} from '@jsverse/transloco';
import {tapResponse} from '@ngrx/operators';
import {patchState, signalStore, withState} from '@ngrx/signals';
import {rxMethod} from '@ngrx/signals/rxjs-interop';
import {exhaustMap, map, pipe, switchMap, tap} from 'rxjs';
import {UntersuchungStore} from '../untersuchung.store';

type UntersuchungMassnahmenState = {
  massnahmen: UntersuchungMassnahmenJson | undefined;
};

const initialState: UntersuchungMassnahmenState = {
  massnahmen: undefined,
};

@Injectable()
export class UntersuchungMassnahmenStore extends signalStore(
  {protectedState: false},
  withState(initialState),
) {
  readonly #massnahmenService = inject(UntersuchungMassnahmenService);
  readonly #appErrorHandlerService = inject(AppErrorHandlerService);
  readonly #untersuchungStore = inject(UntersuchungStore);
  readonly #appNotificationStore = inject(AppNotificationStore);
  readonly #transloco = inject(TranslocoService);

  readonly loader = new Loader();
  readonly saveLoader = new Loader();

  loadMassnahmen = rxMethod<UntersuchungMassnahmenServiceGetUntersuchungMassnahmenRequestParams>(
    pipe(
      tap(() => patchState(this, {massnahmen: undefined})),
      switchMap(params => this.loader.tapLoading$(this.#massnahmenService.getUntersuchungMassnahmen$(params),
        error => this.#appErrorHandlerService.handleError(error))),
      tapResponse({
        next: massnahmen => patchState(this, {massnahmen: massnahmen}),
        error: console.error,
      }),
    ),
  );

  saveMassnahmen = rxMethod<UntersuchungMassnahmenServiceSaveUntersuchungMassnahmenRequestParams>(
    pipe(
      exhaustMap(params =>
        this.saveLoader.tapLoading$(
          this.#massnahmenService.saveUntersuchungMassnahmen$(params).pipe(
            map(massnahmen => ({massnahmen, params})),
          ),
          error => this.#appErrorHandlerService.handleError(error),
        ),
      ),
      tapResponse({
        next: ({massnahmen, params}) => {
          patchState(this, {massnahmen: massnahmen});
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
