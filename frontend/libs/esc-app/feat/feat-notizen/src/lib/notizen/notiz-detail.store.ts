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

import {computed, inject, Injectable} from '@angular/core';
import {NotizenService, NotizJson, NotizSaveJson} from '@dvb/data-access-backend';
import {BackendEntityId} from '@dvb/data-access-types';
import {AppErrorHandlerService, Loader} from '@dvb/util-angular';
import {compareByEntityInfoTimestampErstellt, isNotNullish} from '@dvb/util-shared';
import {tapResponse} from '@ngrx/operators';
import {patchState, signalState} from '@ngrx/signals';
import {rxMethod} from '@ngrx/signals/rxjs-interop';
import {exhaustMap, pipe, switchMap} from 'rxjs';
import {NotizenListStore} from "./notizen-list.store";

interface NotizDetailState {
  notiz: NotizJson | null;
}

const initialState: NotizDetailState = {
  notiz: null,
};

/**
 * Detail-Store für eine einzelne {@link NotizJson}.
 *
 * Wird in den Detail-/Hochladen-Pages eingesetzt, wo das vollständige Notiz-Objekt
 * (inkl. Dateien, `bemerkungen`, `kind` etc.) gebraucht wird. Lädt die Notiz separat
 * via {@link NotizenService#getNotiz$}, unabhängig vom schlanken Listen-Store.
 */
@Injectable()
export class NotizDetailStore {

  readonly loader = new Loader();
  readonly saveLoader = new Loader();
  readonly #notizenService = inject(NotizenService);
  readonly #appErrorHandlerService = inject(AppErrorHandlerService);
  readonly notizListStore = inject(NotizenListStore);

  readonly #state = signalState<NotizDetailState>(initialState);

  readonly notiz = this.#state.notiz;
  readonly kind = computed(() => this.notiz()?.kind);
  readonly notizenFiles = computed(
    () => this.notiz()?.data?.files
      .slice()
      .sort(compareByEntityInfoTimestampErstellt) ?? [],
  );

  loadNotiz = rxMethod<BackendEntityId>(
    pipe(
      switchMap((notizId) => this.loader.tapLoading$(
        this.#notizenService.getNotiz$({notizId}),
        error => this.#appErrorHandlerService.handleError(error))),
      tapResponse({
        next: notiz => patchState(this.#state, {notiz}),
        error: console.error,
      }),
    ),
  );

  updateNotiz = rxMethod<{
    notiz: NotizSaveJson,
    fileUploads: File[];
    onSuccess: (notiz: NotizJson) => void
  }>(
    pipe(
      exhaustMap(({notiz, fileUploads, onSuccess}) => {
        const currentNotiz = this.notiz();
        if (currentNotiz === null) {
          throw new Error('Keine Notiz geladen; updateNotiz kann nicht aufgerufen werden.');
        }
        const [file, file2, file3, file4] = fileUploads;
        return this.saveLoader.tapLoading$(
          this.#notizenService.updateNotiz$({
            notizId: currentNotiz.id,
            notizSaveJson: notiz,
            file,
            file2,
            file3,
            file4,
          }),
          error => this.#appErrorHandlerService.handleError(error),
        ).pipe(
          tapResponse({
            next: result => {
              patchState(this.#state, {notiz: result});
              this.notizListStore.addNotizJsonToList(result);
              onSuccess(result);
            },
            error: console.error,
          }),
        );
      }),
    ),
  );

  removeNotizFile = rxMethod<{
    notizId: BackendEntityId,
    fileId: BackendEntityId,
    onSuccess?: (notiz: NotizJson) => void
  }>(
    pipe(
      exhaustMap(({notizId, fileId, onSuccess}) => this.saveLoader.tapLoading$(
        this.#notizenService.removeNotizFile$({notizId, fileId}),
        error => this.#appErrorHandlerService.handleError(error),
      ).pipe(
        tapResponse({
          next: result => {
            patchState(this.#state, {notiz: result});
            this.notizListStore.addNotizJsonToList(result);
            if (isNotNullish(onSuccess)) {
              onSuccess(result);
            }
          },
          error: console.error,
        }),
      )),
    ),
  );

  reset(): void {
    patchState(this.#state, initialState);
  }
}
