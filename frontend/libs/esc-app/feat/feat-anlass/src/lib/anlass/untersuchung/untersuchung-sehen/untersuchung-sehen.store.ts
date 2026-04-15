import {inject, Injectable} from '@angular/core';
import {
  UntersuchungMassnahmenService,
  UntersuchungMassnahmenServiceGetUntersuchungMassnahmenSehenRequestParams,
  UntersuchungMassnahmenServiceSaveUntersuchungMassnahmenSehenRequestParams,
  UntersuchungSehenBemerkungFragebogenJson,
  UntersuchungSehenBeurteilungJson,
  UntersuchungSehenBeurteilungSaveJson,
  UntersuchungSehenMassnahmenJson,
  UntersuchungSehenService,
  UntersuchungSehenServiceGetUntersuchungSehenBemerkungFragebogenRequestParams,
  UntersuchungSehenServiceGetUntersuchungSehenBeurteilungRequestParams,
  UntersuchungSehenServiceSaveUntersuchungSehenBemerkungFragebogenRequestParams,
  UntersuchungSehenServiceSaveUntersuchungSehenBeurteilungRequestParams,
} from '@dvb/data-access-backend';
import {AppErrorHandlerService, AppNotificationStore, Loader} from '@dvb/util-angular';
import {TranslocoService} from '@jsverse/transloco';
import {tapResponse} from '@ngrx/operators';
import {patchState, signalStore, withMethods, withState} from '@ngrx/signals';
import {rxMethod} from '@ngrx/signals/rxjs-interop';
import {exhaustMap, map, pipe, switchMap, tap} from 'rxjs';
import {UntersuchungStore} from '../untersuchung.store';

type UntersuchungSehenState = {
  sehenBeurteilung: UntersuchungSehenBeurteilungJson | undefined;
  defaultSehenBeurteilungInitial: UntersuchungSehenBeurteilungSaveJson | undefined;
  defaultSehenBeurteilungToApply: UntersuchungSehenBeurteilungSaveJson | undefined;
  sehenMassnahmen: UntersuchungSehenMassnahmenJson | undefined;
  sehenBemerkungFragebogen: UntersuchungSehenBemerkungFragebogenJson | undefined;
};

const initialState: UntersuchungSehenState = {
  sehenBeurteilung: undefined,
  defaultSehenBeurteilungInitial: undefined,
  defaultSehenBeurteilungToApply: undefined,
  sehenMassnahmen: undefined,
  sehenBemerkungFragebogen: undefined,
};

@Injectable()
export class UntersuchungSehenStore extends signalStore(
  {protectedState: false},
  withState(initialState),
  withMethods((store) => ({
    defaultSehenToApplyWithCorrectedVersion(): UntersuchungSehenBeurteilungSaveJson | undefined {
      // Ensure that the version is set correctly when applying the default Sehen
      // noinspection UnnecessaryLocalVariableJS
      const initialDefault = store.defaultSehenBeurteilungInitial();
      if (!initialDefault) {
        return undefined;
      }
      return  {
        ...initialDefault,
        version: store.sehenBeurteilung()?.data?.version ?? -1
      } as UntersuchungSehenBeurteilungSaveJson;
    },
    applyDefaultSehenBeurteilung(): void {
      patchState(store, {defaultSehenBeurteilungToApply: this.defaultSehenToApplyWithCorrectedVersion()});
    },
    resetApplyDefaultSehenBeurteilung(): void {
      patchState(store, {defaultSehenBeurteilungToApply: undefined});
    },
  })),
) {
  readonly #sehenService = inject(UntersuchungSehenService);
  readonly #massnahmenService = inject(UntersuchungMassnahmenService);
  readonly #appErrorHandlerService = inject(AppErrorHandlerService);
  readonly #untersuchungStore = inject(UntersuchungStore);
  readonly #appNotificationStore = inject(AppNotificationStore);
  readonly #transloco = inject(TranslocoService);

  readonly loader = new Loader();
  readonly defaultLoader = new Loader();
  readonly saveLoader = new Loader();

  loadSehenBeurteilung = rxMethod<UntersuchungSehenServiceGetUntersuchungSehenBeurteilungRequestParams>(
    pipe(
      tap(() => patchState(this, {sehenBeurteilung: undefined})),
      switchMap(params => this.loader.tapLoading$(this.#sehenService.getUntersuchungSehenBeurteilung$(params),
        error => this.#appErrorHandlerService.handleError(error))),
      tapResponse({
        next: sehenBeurteilung => patchState(this, {sehenBeurteilung}),
        error: console.error,
      }),
    ),
  );

  loadDefaultSehenBeurteilung = rxMethod<UntersuchungMassnahmenServiceGetUntersuchungMassnahmenSehenRequestParams>(
    pipe(
      switchMap(params => this.defaultLoader.tapLoading$(this.#sehenService.getDefaultUntersuchungSehenBeurteilungForAnlassConfig$(
          {
            anlassId: params.anlassId,
            einladungId: params.einladungId,
          }),
        error => this.#appErrorHandlerService.handleError(error))),
      tapResponse({
        next: data => patchState(this, {defaultSehenBeurteilungInitial: data}),
        error: console.error,
      }),
    ),
  );

  saveSehenBeurteilung = rxMethod<UntersuchungSehenServiceSaveUntersuchungSehenBeurteilungRequestParams>(
    pipe(
      exhaustMap(params =>
        this.saveLoader.tapLoading$(
          this.#sehenService.saveUntersuchungSehenBeurteilung$(params).pipe(
            map(sehenBeurteilung => ({sehenBeurteilung, params})),
          ),
          error => this.#appErrorHandlerService.handleError(error),
        ),
      ),
      tapResponse({
        next: ({sehenBeurteilung, params}) => {
          patchState(this, {sehenBeurteilung});
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

  loadSehenMassnahmen = rxMethod<UntersuchungMassnahmenServiceGetUntersuchungMassnahmenSehenRequestParams>(
    pipe(
      tap(() => patchState(this, {sehenMassnahmen: undefined})),
      switchMap(params => this.loader.tapLoading$(this.#massnahmenService.getUntersuchungMassnahmenSehen$(params),
        error => this.#appErrorHandlerService.handleError(error))),
      tapResponse({
        next: massnahmen => patchState(this, {sehenMassnahmen: massnahmen}),
        error: console.error,
      }),
    ),
  );

  saveSehenMassnahmen = rxMethod<UntersuchungMassnahmenServiceSaveUntersuchungMassnahmenSehenRequestParams>(
    pipe(
      exhaustMap(params =>
        this.saveLoader.tapLoading$(
          this.#massnahmenService.saveUntersuchungMassnahmenSehen$(params).pipe(
            map(massnahmen => ({massnahmen, params})),
          ),
          error => this.#appErrorHandlerService.handleError(error),
        ),
      ),
      tapResponse({
        next: ({massnahmen}) => {
          patchState(this, {sehenMassnahmen: massnahmen});
          this.#appNotificationStore.addSuccessNotification(this.#transloco.translate('global.data.saved'));
        },
        error: console.error,
      }),
    ),
  );

  loadSehenBemerkungFragebogen = rxMethod<UntersuchungSehenServiceGetUntersuchungSehenBemerkungFragebogenRequestParams>(
    pipe(
      tap(() => patchState(this, {sehenBemerkungFragebogen: undefined})),
      switchMap(params => this.loader.tapLoading$(this.#sehenService.getUntersuchungSehenBemerkungFragebogen$(params),
        error => this.#appErrorHandlerService.handleError(error))),
      tapResponse({
        next: sehenBemerkungFragebogen => patchState(this, {sehenBemerkungFragebogen}),
        error: console.error,
      }),
    ),
  );

  saveSehenBemerkungFragebogen = rxMethod<UntersuchungSehenServiceSaveUntersuchungSehenBemerkungFragebogenRequestParams>(
    pipe(
      exhaustMap(params =>
        this.saveLoader.tapLoading$(
          this.#sehenService.saveUntersuchungSehenBemerkungFragebogen$(params).pipe(
            map(sehenBemerkungFragebogen => ({sehenBemerkungFragebogen, params})),
          ),
          error => this.#appErrorHandlerService.handleError(error),
        ),
      ),
      tapResponse({
        next: ({sehenBemerkungFragebogen}) => {
          patchState(this, {sehenBemerkungFragebogen});
          this.#appNotificationStore.addSuccessNotification(this.#transloco.translate('global.data.saved'));
        },
        error: console.error,
      }),
    ),
  );
}
