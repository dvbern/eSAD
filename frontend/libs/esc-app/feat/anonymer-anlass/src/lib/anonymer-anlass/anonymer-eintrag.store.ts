/*
 * Copyright (C) 2026 DV Bern AG, Switzerland
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

import {inject, Injectable} from '@angular/core';
import {AnonymerEintragCreateJson, AnonymerEintragJson, AnonymerEintragService,} from '@dvb/data-access-backend';
import {BackendEntityId, BackendEntityIdWithClass} from '@dvb/data-access-types';
import {AppErrorHandlerService, Loader, withSelectedEntity} from '@dvb/util-angular';
import {TranslocoService} from '@jsverse/transloco';
import {tapResponse} from '@ngrx/operators';
import {patchState, signalStore} from '@ngrx/signals';
import {addEntity, setEntity, withEntities} from '@ngrx/signals/entities';
import {rxMethod} from '@ngrx/signals/rxjs-interop';
import {exhaustMap, pipe, switchMap} from 'rxjs';
import {withDevtools} from '@angular-architects/ngrx-toolkit';

@Injectable()
export class AnonymerEintragStore extends signalStore(
  {protectedState: false},
  withDevtools('anonymer-eintrag'),
  withEntities<AnonymerEintragJson>(),
  withSelectedEntity<AnonymerEintragJson>(),
) {
  readonly #anonymerEintragService = inject(AnonymerEintragService);
  readonly #appErrorHandlerService = inject(AppErrorHandlerService);
  readonly transloco = inject(TranslocoService);

  readonly loader = new Loader();
  readonly saveLoader = new Loader();

  loadAnonymerEintrag = rxMethod<BackendEntityId>(
    pipe(
      switchMap((entityId) => this.loader
        .tapLoading$(this.#anonymerEintragService
            .getAnonymerEintrag$({anonymerEintragId: entityId}),
          error => this.#appErrorHandlerService.handleError(error),
        ),
      ),
      tapResponse({
        next: result => patchState(this, setEntity(result)),
        error: console.error,
      }),
    ),
  );

  createAnonymerEintrag = rxMethod<{
    createJson: AnonymerEintragCreateJson,
    onSuccess: (result: AnonymerEintragJson) => void
  }>(
    pipe(
      exhaustMap(
        ({createJson, onSuccess}) => this.saveLoader.tapLoading$(
          this.#anonymerEintragService.createAnonymerEintrag$({anonymerEintragCreateJson: createJson}),
          error => this.#appErrorHandlerService.handleError(error),
        ).pipe(
          tapResponse({
            next: result => {
              patchState(this, addEntity(result));
              onSuccess(result);
            },
            error: console.error,
          }),
        ),
      )),
  );

  abschliessenAnonymerEintrag = rxMethod<{
    anlassId: BackendEntityId,
    anonymerEintragId: BackendEntityId,
    onSuccess: (anonymerEintragId: BackendEntityId) => void
  }>(
    pipe(
      exhaustMap(
        (params) => this.saveLoader.tapLoading$(
          this.#anonymerEintragService.abschlussAnonymerEintrag$({
            anlassId: params.anlassId,
            anonymerEintragId: params.anonymerEintragId,
          }),
          error => this.#appErrorHandlerService.handleError(error),
        ).pipe(
          tapResponse({
            next: () => params.onSuccess(params.anonymerEintragId),
            error: console.error,
          }),
        ),
      )),
  );
}
