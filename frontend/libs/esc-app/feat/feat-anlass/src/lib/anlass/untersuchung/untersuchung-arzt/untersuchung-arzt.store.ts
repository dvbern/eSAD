/*
 *
 * Copyright (C) 2025 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

import {inject, Injectable} from '@angular/core';
import {
  UntersuchungArztBemerkungJson,
  UntersuchungArztBeurteilungJson,
  UntersuchungArztBeurteilungSaveJson,
  UntersuchungArztService,
  UntersuchungArztServiceGetUntersuchungArztBemerkungRequestParams,
  UntersuchungArztServiceGetUntersuchungArztBeurteilungRequestParams,
  UntersuchungArztServiceSaveUntersuchungArztBemerkungRequestParams,
  UntersuchungArztServiceSaveUntersuchungArztBeurteilungRequestParams,
} from '@dvb/data-access-backend';
import {globalConstants} from '@dvb/feat-shared';
import {AppErrorHandlerService, AppNotificationStore, Loader} from '@dvb/util-angular';
import {TranslocoService} from '@jsverse/transloco';
import {tapResponse} from '@ngrx/operators';
import {patchState, signalStore, withMethods, withState} from '@ngrx/signals';
import {rxMethod} from '@ngrx/signals/rxjs-interop';
import {exhaustMap, map, pipe, switchMap, tap} from 'rxjs';
import {AnlassEinladungenId} from '../anlass-einladungen-id';
import {UntersuchungStore} from '../untersuchung.store';

type UntersuchungArztState = {
  untersuchungArztBeurteilung: UntersuchungArztBeurteilungJson | undefined;
  defaultArztInitial: UntersuchungArztBeurteilungSaveJson | undefined;
  defaultArztToApply: UntersuchungArztBeurteilungSaveJson | undefined;
  untersuchungArztBemerkung: UntersuchungArztBemerkungJson | undefined;
};

const initialState: UntersuchungArztState = {
  untersuchungArztBeurteilung: undefined,
  defaultArztInitial: undefined,
  defaultArztToApply: undefined,
  untersuchungArztBemerkung: undefined,
};

@Injectable()
export class UntersuchungArztStore extends signalStore(
  {protectedState: false},
  withState(initialState),
  withMethods((store) => ({
    defaultArztToApplyWithCorrectedVersion(): UntersuchungArztBeurteilungSaveJson {
      // Ensure that the version is set correctly when applying the default Sehen
      const defaultsWithCorrectedVersion = {
        ...store.defaultArztInitial(),
        version: store.untersuchungArztBeurteilung()?.data.version ?? globalConstants.entityPrePersistVersion,
      };
      return defaultsWithCorrectedVersion;
    },
    applyDefaultUntersuchungArzt(): void {
      patchState(store, {defaultArztToApply: this.defaultArztToApplyWithCorrectedVersion()});
    },
    resetApplyDefaultUntersuchungArzt(): void {
      patchState(store, {defaultArztToApply: undefined});
    },
  })),
) {
  readonly #untersuchungArztService = inject(UntersuchungArztService);
  readonly #appErrorHandlerService = inject(AppErrorHandlerService);
  readonly #untersuchungStore = inject(UntersuchungStore);
  readonly #appNotificationStore = inject(AppNotificationStore);
  readonly #transloco = inject(TranslocoService);

  readonly loader = new Loader();
  readonly saveLoader = new Loader();

  loadUntersuchungArzt = rxMethod<UntersuchungArztServiceGetUntersuchungArztBeurteilungRequestParams>(
    pipe(
      tap(() => patchState(this, {untersuchungArztBeurteilung: undefined})),
      switchMap(params => this.loader.tapLoading$(this.#untersuchungArztService.getUntersuchungArztBeurteilung$(params),
        error => this.#appErrorHandlerService.handleError(error))),
      tapResponse({
        next: untersuchungArztBeurteilung => patchState(this, {untersuchungArztBeurteilung: untersuchungArztBeurteilung}),
        error: console.error,
      }),
    ),
  );

  loadDefaultUntersuchungArzt = rxMethod<AnlassEinladungenId>(
    pipe(
      switchMap(params => this.loader.tapLoading$(this.#untersuchungArztService.getDefaultUntersuchungArztBeurteilungForAnlassConfig$(
          {
            anlassId: params.anlassId,
            einladungId: params.einladungIds[0],
          }),
        error => this.#appErrorHandlerService.handleError(error))),
      tapResponse({
        next: data => patchState(this, {defaultArztInitial: data}),
        error: console.error,
      }),
    ),
  );

  saveUntersuchungArzt = rxMethod<UntersuchungArztServiceSaveUntersuchungArztBeurteilungRequestParams>(
    pipe(
      exhaustMap(params =>
        this.saveLoader.tapLoading$(
          this.#untersuchungArztService.saveUntersuchungArztBeurteilung$(params).pipe(
            map(untersuchungArztBeurt => ({untersuchungArzt: untersuchungArztBeurt, params}))
          ),
          error => this.#appErrorHandlerService.handleError(error),
        ),
      ),
      tapResponse({
        next: ({untersuchungArzt, params}) => {
          patchState(this, {untersuchungArztBeurteilung: untersuchungArzt});
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

  loadUntersuchungArztBemerkung = rxMethod<UntersuchungArztServiceGetUntersuchungArztBemerkungRequestParams>(
    pipe(
      tap(() => patchState(this, {untersuchungArztBeurteilung: undefined})),
      switchMap(params => this.loader.tapLoading$(this.#untersuchungArztService.getUntersuchungArztBemerkung$(params),
        error => this.#appErrorHandlerService.handleError(error))),
      tapResponse({
        next: untersuchungArzt => patchState(this, {untersuchungArztBemerkung: untersuchungArzt}),
        error: console.error,
      }),
    ),
  );

  saveUntersuchungArztBemerkung = rxMethod<UntersuchungArztServiceSaveUntersuchungArztBemerkungRequestParams>(
    pipe(
      exhaustMap(params =>
        this.saveLoader.tapLoading$(
          this.#untersuchungArztService.saveUntersuchungArztBemerkung$(params).pipe(
            map(untersuchungArzt => ({untersuchungArzt, params}))
          ),
          error => this.#appErrorHandlerService.handleError(error),
        ),
      ),
      tapResponse({
        next: ({untersuchungArzt}) => {
          patchState(this, {untersuchungArztBemerkung: untersuchungArzt});
          this.#appNotificationStore.addSuccessNotification(this.#transloco.translate('global.data.saved'));
        },
        error: console.error,
      }),
    ),
  );
}
