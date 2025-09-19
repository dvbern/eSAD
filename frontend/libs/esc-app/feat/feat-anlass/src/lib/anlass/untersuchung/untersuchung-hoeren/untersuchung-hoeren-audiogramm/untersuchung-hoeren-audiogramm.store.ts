import {inject, Injectable} from '@angular/core';
import {
  UntersuchungHoerenAudiogrammJson,
  UntersuchungHoerenAudiogrammSaveJson,
  UntersuchungHoerenAudiogrammService,
  UntersuchungHoerenAudiogrammServiceGetUntersuchungHoerenAudiogrammRequestParams,
  UntersuchungHoerenAudiogrammServiceSaveUntersuchungHoerenAudiogrammRequestParams,
} from '@dvb/data-access-backend';
import {globalConstants} from '@dvb/feat-shared';
import {AppErrorHandlerService, AppNotificationStore, Loader} from '@dvb/util-angular';
import {requireValue} from '@dvb/util-shared';
import {TranslocoService} from '@jsverse/transloco';
import {tapResponse} from '@ngrx/operators';
import {patchState, signalStore, withMethods, withState} from '@ngrx/signals';
import {rxMethod} from '@ngrx/signals/rxjs-interop';
import {exhaustMap, map, pipe, switchMap, tap} from 'rxjs';
import {AnlassEinladungenId} from '../../anlass-einladungen-id';
import {UntersuchungStore} from '../../untersuchung.store';

type UntersuchungHoerenAudiogrammState = {
  hoerenAudiogramm: UntersuchungHoerenAudiogrammJson | undefined;
  defaultHoerenAudiogrammInitial: UntersuchungHoerenAudiogrammSaveJson | undefined;
  defaultHoerenAudiogrammToApply: UntersuchungHoerenAudiogrammSaveJson | undefined;
};

const initialState: UntersuchungHoerenAudiogrammState = {
  hoerenAudiogramm: undefined,
  defaultHoerenAudiogrammInitial: undefined,
  defaultHoerenAudiogrammToApply: undefined,
};

@Injectable()
export class UntersuchungHoerenAudiogrammStore extends signalStore(
  {protectedState: false},
  withState(initialState),
  withMethods((store) => ({
    defaultHoerenAudiogrammToApplyWithCorrectedVersion(): UntersuchungHoerenAudiogrammSaveJson {
      // Ensure that the version is set correctly when applying the default Sehen
      const defaultsWithCorrectedVersion = {
        ...store.defaultHoerenAudiogrammInitial(),
        version: store.hoerenAudiogramm()?.audiogrammData.version ?? globalConstants.entityPrePersistVersion,
        audiogrammLinks: {
          ...requireValue(store.defaultHoerenAudiogrammInitial()?.audiogrammLinks),
          version:  store.hoerenAudiogramm()?.audiogrammData?.audiogrammLinks?.version ?? globalConstants.entityPrePersistVersion
        },
        audiogrammRechts: {
          ...requireValue(store.defaultHoerenAudiogrammInitial()?.audiogrammRechts),
          version: store.hoerenAudiogramm()?.audiogrammData.audiogrammRechts?.version ?? globalConstants.entityPrePersistVersion
        }
      };
      return defaultsWithCorrectedVersion;
    },
    applyDefaultHoeren(): void {
      patchState(store, {defaultHoerenAudiogrammToApply: this.defaultHoerenAudiogrammToApplyWithCorrectedVersion()});
    },
    resetApplyDefaultHoeren(): void {
      patchState(store, {defaultHoerenAudiogrammToApply: undefined});
    },
  })),
) {
  readonly #hoerenService = inject(UntersuchungHoerenAudiogrammService);
  readonly #appErrorHandlerService = inject(AppErrorHandlerService);
  readonly #untersuchungStore = inject(UntersuchungStore);
  readonly #appNotificationStore = inject(AppNotificationStore);
  readonly #transloco = inject(TranslocoService);

  readonly loader = new Loader();
  readonly defaultLoader = new Loader();
  readonly saveLoader = new Loader();

  loadHoerenAudiogramm = rxMethod<UntersuchungHoerenAudiogrammServiceGetUntersuchungHoerenAudiogrammRequestParams>(
    pipe(
      tap(() => patchState(this, {hoerenAudiogramm: undefined})),
      switchMap(params => this.loader.tapLoading$(this.#hoerenService.getUntersuchungHoerenAudiogramm$(params),
        error => this.#appErrorHandlerService.handleError(error))),
      tapResponse({
        next: hoeren => patchState(this, {hoerenAudiogramm: hoeren}),
        error: console.error,
      }),
    ),
  );

  loadDefaultHoerenAudiogramm = rxMethod<AnlassEinladungenId>(
    pipe(
      switchMap(params => this.defaultLoader.tapLoading$(this.#hoerenService.getDefaultUntersuchungHoerenAudiogrammForAnlassConfig$(
          {
            anlassId: params.anlassId,
            einladungId: params.einladungIds[0],
          }),
        error => this.#appErrorHandlerService.handleError(error))),
      tapResponse({
        next: data => patchState(this, {defaultHoerenAudiogrammInitial: data}),
        error: console.error,
      }),
    ),
  );

  saveHoerenAudiogramm = rxMethod<UntersuchungHoerenAudiogrammServiceSaveUntersuchungHoerenAudiogrammRequestParams>(
    pipe(
      exhaustMap(params =>
        this.saveLoader.tapLoading$(
          this.#hoerenService.saveUntersuchungHoerenAudiogramm$(params).pipe(
            map(hoeren => ({hoeren, params})),
          ),
          error => this.#appErrorHandlerService.handleError(error),
        ),
      ),
      tapResponse({
        next: ({hoeren, params}) => {
          patchState(this, {hoerenAudiogramm: hoeren});
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
