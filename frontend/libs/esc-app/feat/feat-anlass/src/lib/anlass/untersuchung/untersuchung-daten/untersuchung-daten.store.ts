import {inject, Injectable} from '@angular/core';
import {
  EinladungenServiceGetUntersuchungKindDatenRequestParams,
  UntersuchungDurchfuehrungSaveJson,
  UntersuchungDurchfuehrungService,
  UntersuchungDurchfuehrungServiceSaveUntersuchungDurchfuehrungAbgeschlossenRequestParams,
  UntersuchungDurchfuehrungServiceSaveUntersuchungDurchfuehrungRequestParams,
  UntersuchungKindDatenJson,
} from '@dvb/data-access-backend';
import {AppErrorHandlerService, AppNotificationStore, Loader} from '@dvb/util-angular';
import {isNullish} from '@dvb/util-shared';
import {TranslocoService} from '@jsverse/transloco';
import {tapResponse} from '@ngrx/operators';
import {patchState, signalStore, withState} from '@ngrx/signals';
import {rxMethod} from '@ngrx/signals/rxjs-interop';
import {exhaustMap, map, pipe, switchMap, tap} from 'rxjs';
import {UntersuchungStore} from '../untersuchung.store';

type UntersuchungDatenState = {
  durchfuehrungDaten: UntersuchungDurchfuehrungSaveJson | undefined;
  kindDaten: UntersuchungKindDatenJson | undefined;
};

const initialState: UntersuchungDatenState = {
  durchfuehrungDaten: undefined,
  kindDaten: undefined,
};

@Injectable()
export class UntersuchungDatenStore extends signalStore(
  {protectedState: false},
  // withDevtools('untersuchungKindDaten'),
  withState(initialState),
) {
  readonly #untersuchungDurchfuehrungService = inject(UntersuchungDurchfuehrungService);
  readonly #appErrorHandlerService = inject(AppErrorHandlerService);
  readonly #untersuchungStore = inject(UntersuchungStore);
  readonly #appNotificationStore = inject(AppNotificationStore);
  readonly #transloco = inject(TranslocoService);

  readonly loader = new Loader();
  readonly saveLoader = new Loader();

  loadDurchfuehrungDaten = rxMethod<EinladungenServiceGetUntersuchungKindDatenRequestParams>(
    pipe(
      tap(() => patchState(this, {durchfuehrungDaten: undefined, kindDaten: undefined})),
      switchMap(params => this.loader.tapLoading$(
        this.#untersuchungDurchfuehrungService.getUntersuchungDurchfuehrung$(params),
        error => this.#appErrorHandlerService.handleError(error)),
      ),
      tapResponse({
        next: result => patchState(this,
          {
            durchfuehrungDaten: isNullish(result.untersuchungDurchfuehrung) ?
              result.initialValues :
              result.untersuchungDurchfuehrung?.data,
            kindDaten: result.kindDaten,
          }),
        error: console.error,
      }),
    ),
  );

  saveDurchfuehrungDaten = rxMethod<UntersuchungDurchfuehrungServiceSaveUntersuchungDurchfuehrungRequestParams>(
    pipe(
      exhaustMap(params => this.saveLoader.tapLoading$(
        this.#untersuchungDurchfuehrungService.saveUntersuchungDurchfuehrung$(params).pipe(
          map(result => ({result, params})),
        ),
        error => this.#appErrorHandlerService.handleError(error)),
      ),
      tapResponse({
        next: ({result, params}) => {
          patchState(this, {durchfuehrungDaten: result.data});
          this.#untersuchungStore.loadTabsSaved({
            anlassId: params.anlassId,
            einladungId: params.einladungId,
          });
          this.#appNotificationStore.addSuccessNotification(this.#transloco.translate(
            'untersuchung.daten.saved'));
        },
        error: console.error,
      }),
    ),
  );

  saveDurchfuehrungAbgeschlossen = rxMethod<UntersuchungDurchfuehrungServiceSaveUntersuchungDurchfuehrungAbgeschlossenRequestParams>(
    pipe(
      exhaustMap(params => this.saveLoader.tapLoading$(
        this.#untersuchungDurchfuehrungService.saveUntersuchungDurchfuehrungAbgeschlossen$(params),
        error => this.#appErrorHandlerService.handleError(error)),
      ),
      tapResponse({
        next: (result) => {
          patchState(this, {
            durchfuehrungDaten: result.untersuchungDurchfuehrung?.data,
            kindDaten: result.kindDaten,
          });
          this.#appNotificationStore.addSuccessNotification(this.#transloco.translate(
            'untersuchung.daten.saved'));
        },
        error: console.error,
      }),
    ),
  )
}
