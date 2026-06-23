/*
 *
 * Copyright (C) 2026 DV Bern AG, Switzerland
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
import {NotizenService, NotizJson, NotizListItemJson} from '@dvb/data-access-backend';
import {BackendEntityId} from '@dvb/data-access-types';
import {enhanceNotizJson, NotizJsonEnhanced} from '@dvb/feat-shared';
import {AppErrorHandlerService, Loader} from '@dvb/util-angular';
import {ensurePropsFlat, joinNotBlank, requireValue} from '@dvb/util-shared';
import {tapResponse} from '@ngrx/operators';
import {patchState, signalStore} from '@ngrx/signals';
import {
  removeEntity,
  setAllEntities,
  setEntity,
  withEntities
} from '@ngrx/signals/entities';
import {rxMethod} from '@ngrx/signals/rxjs-interop';
import {exhaustMap, pipe, switchMap} from 'rxjs';

@Injectable()
export class NotizenListStore extends signalStore(
  {protectedState: false},
  //withDevtools('kinder-notizen'),
  withEntities<NotizJsonEnhanced>(),
) {
  readonly loader = new Loader();
  readonly deleteLoader = new Loader();
  readonly #notizenService = inject(NotizenService);
  readonly #appErrorHandlerService = inject(AppErrorHandlerService);

  loadNotizen = rxMethod<void>(
    pipe(
      switchMap(() => this.loader.tapLoading$(
        this.#notizenService.findAllBerechtigt$(),
        error => this.#appErrorHandlerService.handleError(error))),
      tapResponse({
        next: listJson => patchState(this, setAllEntities(listJson.notizen.map(this.enhanceNotiz))),
        error: console.error,
      }),
    ),
  );

  removeNotiz = rxMethod<{
    notizId: BackendEntityId
  }>(
    pipe(
      exhaustMap(({notizId}) => this.deleteLoader.tapLoading$(
          this.#notizenService.deleteNotiz$({
            notizId: requireValue(notizId),
          }),
          error => this.#appErrorHandlerService.handleError(error),
        ).pipe(
          tapResponse({
            next: () => {
              patchState(this, removeEntity(notizId));
            },
            error: console.error,
          }),
        ),
      ),
    ),
  );

  public addNotizJsonToList(notiz: NotizJson): void {
    patchState(this, setEntity(enhanceNotizJson(notiz)));
  }

  private enhanceNotiz(notiz: NotizListItemJson): NotizJsonEnhanced {
    return {
      ...notiz,
      labelIds: notiz.labels.map(each => each.id),
    };
  }
}
