/*
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

import { inject, Injectable } from '@angular/core';
import { AerzteService, ArztJson, ArztSaveJson } from '@dvb/data-access-backend';
import { BackendEntityId } from '@dvb/data-access-types';
import { AppErrorHandlerService, handleBlobResponseDownload, Loader, withSelectedEntity } from '@dvb/util-angular';
import { requireValue } from '@dvb/util-shared';
import { tapResponse } from '@ngrx/operators';
import { patchState, signalStore } from '@ngrx/signals';
import {
  addEntity,
  removeAllEntities,
  removeEntity,
  setAllEntities,
  setEntity,
  withEntities,
} from '@ngrx/signals/entities';
import { rxMethod } from '@ngrx/signals/rxjs-interop';
import { exhaustMap, pipe, switchMap, tap } from 'rxjs';

@Injectable({providedIn: 'root'})
export class AerzteStore extends signalStore(
  {protectedState: false},
  // withDevtools('aerzte') as SignalStoreFeature<EmptyFeatureResult, EmptyFeatureResult>,
  withEntities<ArztJson>(),
  withSelectedEntity<ArztJson>(),
) {
  readonly #aerzteService = inject(AerzteService);
  readonly #appErrorHandlerService = inject(AppErrorHandlerService);
  readonly loader = new Loader();
  readonly saveLoader = new Loader();
  readonly removeLoader = new Loader();
  readonly exportLoader = new Loader();

  loadAerzteList = rxMethod<void>(
    pipe(
      tap(() => patchState(this, removeAllEntities())),
      switchMap(() =>
        this.loader.tapLoading$(this.#aerzteService.findAllArzte$(), error =>
          this.#appErrorHandlerService.handleError(error),
        ),
      ),
      tapResponse({
        next: listJson => patchState(this, setAllEntities(listJson.arztList)),
        error: console.error,
      }),
    ),
  );

  createArzt = rxMethod<{
    arzt: ArztSaveJson;
    onSuccess: () => void;
  }>(
    pipe(
      exhaustMap(({arzt, onSuccess}) =>
        this.saveLoader
          .tapLoading$(this.#aerzteService.createArzt$({arztSaveJson: arzt}), error =>
            this.#appErrorHandlerService.handleError(error),
          )
          .pipe(
            tapResponse({
              next: result => {
                patchState(this, addEntity(result));
                onSuccess();
              },
              error: console.error,
            }),
          ),
      ),
    ),
  );

  updateArzt = rxMethod<{
    arzt: ArztSaveJson;
    onSuccess: () => void;
  }>(
    pipe(
      exhaustMap(({arzt, onSuccess}) =>
        this.saveLoader
          .tapLoading$(
            this.#aerzteService.updateArzt$({
              arztSaveJson: arzt,
              arztId: requireValue(this.selectedEntity()?.id),
            }),
            error => this.#appErrorHandlerService.handleError(error),
          )
          .pipe(
            tapResponse({
              next: result => {
                patchState(this, setEntity(result));
                onSuccess();
              },
              error: console.error,
            }),
          ),
      ),
    ),
  );

  removeArzt = rxMethod<BackendEntityId>(
    pipe(
      exhaustMap(arztId =>
        this.removeLoader
          .tapLoading$(this.#aerzteService.removeArzt$({arztId}), error =>
            this.#appErrorHandlerService.handleError(error),
          )
          .pipe(
            tapResponse({
              next: () => {
                patchState(this, removeEntity(arztId));
              },
              error: console.error,
            }),
          ),
      ),
    ),
  );

  exportAerzteAsDoc = rxMethod<
    {
      aerzteIds: BackendEntityId[],
      onSuccess: () => void
    }
  >(
    pipe(
      exhaustMap(({aerzteIds, onSuccess}) =>
      this.exportLoader.tapLoading$(
        this.#aerzteService.exportAerzte$({ aerzteIds }, 'response'), error =>
          this.#appErrorHandlerService.handleError(error),
      ).pipe(
        tapResponse({
            next: (result) => {
              handleBlobResponseDownload(result, true);
              onSuccess();
            },
            error: console.error,
          }
        )
      )
      )
    )
  )
}
