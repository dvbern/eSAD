import {
  UntersuchungBewegungsapparatJson, UntersuchungBewegungsapparatService,
  UntersuchungBewegungsapparatServiceGetUntersuchungBewegungsapparatRequestParams,
  UntersuchungBewegungsapparatServiceSaveUntersuchungBewegungsapparatRequestParams,
} from '@dvb/data-access-backend';
import {inject, Injectable} from '@angular/core';
import {patchState, signalStore, withState} from '@ngrx/signals';
import {AppErrorHandlerService, AppNotificationStore, Loader, rxMethod} from '@dvb/util-angular';
import {UntersuchungStore} from '../untersuchung.store';
import {TranslocoService} from '@jsverse/transloco';
import {exhaustMap, map, pipe, switchMap, tap} from 'rxjs';
import {tapResponse} from '@ngrx/operators';

type UntersuchungBewegungsapparatState = {
  bewegungsapparat: UntersuchungBewegungsapparatJson | undefined;
};

const initialState: UntersuchungBewegungsapparatState = {
  bewegungsapparat: undefined,
};

@Injectable()
export class UntersuchungBewegungsapparatStore extends signalStore(
  {protectedState: false},
  withState(initialState),
) {
  readonly #bewegungsapparatService = inject(UntersuchungBewegungsapparatService);
  readonly #appErrorHandlerService = inject(AppErrorHandlerService);
  readonly #untersuchungStore = inject(UntersuchungStore);
  readonly #appNotificationStore = inject(AppNotificationStore);
  readonly #transloco = inject(TranslocoService);

  readonly loader = new Loader();
  readonly saveLoader = new Loader();

  loadBewegungsapparat = rxMethod<UntersuchungBewegungsapparatServiceGetUntersuchungBewegungsapparatRequestParams>(
    pipe(
      tap(() => patchState(this, {bewegungsapparat: undefined})),
      switchMap(params => this.loader.tapLoading$(
          this.#bewegungsapparatService.getUntersuchungBewegungsapparat$(params),
          error => this.#appErrorHandlerService.handleError(error),
        )
      ),
      tapResponse({
        next: bewegungsapparat => patchState(this, {bewegungsapparat}),
        error: console.error
      }),
    ),
  );

  saveBewegungsapparat = rxMethod<UntersuchungBewegungsapparatServiceSaveUntersuchungBewegungsapparatRequestParams>(
    pipe(
      exhaustMap(params =>
        this.saveLoader.tapLoading$(
          this.#bewegungsapparatService.saveUntersuchungBewegungsapparat$(params)
            .pipe(map(bewegungsapparat => ({bewegungsapparat: bewegungsapparat, params}))),
          error => this.#appErrorHandlerService.handleError(error),
        ),
      ),
      tapResponse({
        next: ({bewegungsapparat, params}) => {
          patchState(this, {bewegungsapparat});
          this.#untersuchungStore.loadTabsSaved(
            {
              anlassId: params.anlassId,
              einladungId: params.einladungId,
            }
          );
          this.#appNotificationStore.addSuccessNotification(this.#transloco.translate('global.data.saved'));
        },
        error: console.error,
      }),
    ),
  );
}
