import {inject, Injectable} from '@angular/core';
import {
  UntersuchungTriageJson,
  UntersuchungTriageService,
  UntersuchungTriageServiceGetUntersuchungTriageRequestParams,
  UntersuchungTriageServiceSaveUntersuchungTriageRequestParams,
} from '@dvb/data-access-backend';
import {AppErrorHandlerService, AppNotificationStore, Loader} from '@dvb/util-angular';
import {TranslocoService} from '@jsverse/transloco';
import {tapResponse} from '@ngrx/operators';
import {patchState, signalStore, withState} from '@ngrx/signals';
import {rxMethod} from '@ngrx/signals/rxjs-interop';
import {exhaustMap, map, pipe, switchMap, tap} from 'rxjs';
import {UntersuchungStore} from '../untersuchung.store';

type UntersuchungTriageState = {
  triage: UntersuchungTriageJson | undefined;
};

const initialState: UntersuchungTriageState = {
  triage: undefined,
};

@Injectable()
export class UntersuchungTriageStore extends signalStore(
  {protectedState: false},
  // withDevtools('untersuchungTriage'),
  withState(initialState),
) {
  readonly #triageService = inject(UntersuchungTriageService);
  readonly #appErrorHandlerService = inject(AppErrorHandlerService);
  readonly #untersuchungStore = inject(UntersuchungStore);
  readonly #appNotificationStore = inject(AppNotificationStore);
  readonly #transloco = inject(TranslocoService);

  readonly loader = new Loader();
  readonly saveLoader = new Loader();

  loadTriage = rxMethod<UntersuchungTriageServiceGetUntersuchungTriageRequestParams>(
    pipe(
      tap(() => patchState(this, {triage: undefined})),
      switchMap(params => this.loader.tapLoading$(this.#triageService.getUntersuchungTriage$(params),
        error => this.#appErrorHandlerService.handleError(error))),
      tapResponse({
        next: triage => patchState(this, {triage}),
        error: console.error,
      }),
    ),
  );

  saveTriage = rxMethod<UntersuchungTriageServiceSaveUntersuchungTriageRequestParams>(
    pipe(
      exhaustMap(params =>
        this.saveLoader.tapLoading$(
          this.#triageService.saveUntersuchungTriage$(params).pipe(
            map(triage => ({triage, params})),
          ),
          error => this.#appErrorHandlerService.handleError(error),
        ),
      ),
      tapResponse({
        next: ({triage, params}) => {
          patchState(this, {triage});
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
