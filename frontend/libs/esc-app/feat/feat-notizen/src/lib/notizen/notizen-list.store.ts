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

import {computed, inject, Injectable} from '@angular/core';
import {NotizenService, NotizJson, NotizSaveJson} from '@dvb/data-access-backend';
import {BackendEntityId} from '@dvb/data-access-types';
import {NotizJsonEnhanced} from '@dvb/feat-shared';
import {AppErrorHandlerService, Loader, withSelectedEntity} from '@dvb/util-angular';
import {compareByEntityInfoTimestampErstellt, isNotNullish, requireValue} from '@dvb/util-shared';
import {tapResponse} from '@ngrx/operators';
import {patchState, signalStore, withComputed} from '@ngrx/signals';
import {removeEntity, setAllEntities, setEntity, withEntities} from '@ngrx/signals/entities';
import {rxMethod} from '@ngrx/signals/rxjs-interop';
import {exhaustMap, pipe, switchMap} from 'rxjs';

@Injectable()
export class NotizenListStore extends signalStore(
  {protectedState: false},
  //withDevtools('kinder-notizen'),
  withEntities<NotizJsonEnhanced>(),
  withSelectedEntity<NotizJsonEnhanced>(),
  withComputed(({selectedEntity}) => ({
    notizenFiles: computed(
      () => selectedEntity()?.data?.files
        .sort(compareByEntityInfoTimestampErstellt) ?? [],
    ),
  })),
) {
  readonly loader = new Loader();
  readonly saveLoader = new Loader();
  readonly deleteLoader = new Loader();
  readonly #notizenService = inject(NotizenService);
  readonly #appErrorHandlerService = inject(AppErrorHandlerService);

  loadNotizen = rxMethod<void>(
    pipe(
      switchMap(() => this.loader.tapLoading$(
        this.#notizenService.findAll$(),
        error => this.#appErrorHandlerService.handleError(error))),
      tapResponse({
        next: listJson => patchState(this, setAllEntities(listJson.notizen.map(this.enhanceNotiz))),
        error: console.error,
      }),
    ),
  );

  removeNotizFile = rxMethod<{
    notizId: BackendEntityId,
    fileId: BackendEntityId,
    onSuccess?: (notiz: NotizJson) => void
  }>(
    pipe(
      exhaustMap(
        ({notizId, fileId, onSuccess}) => this.saveLoader.tapLoading$(
          this.#notizenService.removeNotizFile$(
            {
              notizId: notizId,
              fileId: fileId,
            }),
          error => this.#appErrorHandlerService.handleError(error),
        ).pipe(
          tapResponse({
            next: result => {
              patchState(this, setEntity(this.enhanceNotiz(result)));
              if (isNotNullish(onSuccess)) {
                onSuccess(result);
              }

            },
            error: console.error,
          }),
        ),
      ),
    ),
  );

  updateNotiz = rxMethod<{
    notiz: NotizSaveJson,
    fileUploads: File[];
    onSuccess: (notiz: NotizJson) => void
  }>(
    pipe(
      exhaustMap(
        ({notiz, fileUploads, onSuccess}) => {
          const [file, file2, file3, file4] = fileUploads;
          return this.saveLoader.tapLoading$(
            this.#notizenService.updateNotiz$({
              notizId: requireValue(this.selectedEntity()).id,
              notizSaveJson: notiz,
              file: file,
              file2: file2,
              file3: file3,
              file4: file4,
            }),
            error => this.#appErrorHandlerService.handleError(error),
          ).pipe(
            tapResponse({
              next: result => {
                patchState(this, setEntity(this.enhanceNotiz(result)));
                onSuccess(result);
              },
              error: console.error,
            }),
          );
        },
      ),
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

  private enhanceNotiz(notiz: NotizJson): NotizJsonEnhanced {
    return {
      ...notiz,
      labelIds: notiz.data.labels.map(each => each.id),
      hasAnhang: notiz.data.files.length > 0,
      kindFullName: notiz.kind.vorname + ' ' + notiz.kind.name,
    }
  }
}
