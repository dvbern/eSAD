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

import {computed, inject, Injectable} from "@angular/core";
import {patchState, signalStore, withComputed, withMethods, withState} from "@ngrx/signals";
import {withEntities} from "@ngrx/signals/entities";
import {DocumentQueueMassenbriefSaveJson, DocumentQueueService, SearchSummaryJson} from "@dvb/data-access-backend";
import {BackendEntityId} from "@dvb/data-access-types";
import {exhaustMap, pipe} from "rxjs";
import {tapResponse} from "@ngrx/operators";
import {AppErrorHandlerService, Loader} from "@dvb/util-angular";
import {rxMethod} from "@ngrx/signals/rxjs-interop";
import {withCallState} from "@angular-architects/ngrx-toolkit";

type KinderMassenbriefState = {
  massenbriefMode: boolean;
  selectedKinderIds: BackendEntityId[];
  limit: number
};

const initialState: KinderMassenbriefState = {
  massenbriefMode: false,
  selectedKinderIds: [],
  limit: 500
};

@Injectable({providedIn: 'root'})
export class KinderMassenbriefStore extends signalStore(
  {protectedState: false},
  //withDevtools('kinder-massenbrief-store') as SignalStoreFeature<EmptyFeatureResult, EmptyFeatureResult>,
  withEntities<SearchSummaryJson>(),
  withState<KinderMassenbriefState>(initialState),
  withCallState(),
  withMethods(store => ({
    changeMassenbriefmode(): void {
      if (store.massenbriefMode()) {
        patchState(store, {massenbriefMode: false, selectedKinderIds: []});
      } else {
        patchState(store, {massenbriefMode: true})
      }
    },
    isSelected(item: SearchSummaryJson): boolean {
      return store.selectedKinderIds().includes(item.id);
    },
    isLimitReached(): boolean {
      return store.selectedKinderIds().length >= store.limit()
    },
    isDisabled(item: SearchSummaryJson): boolean {
      return !this.isSelected(item) && this.isLimitReached();
    },
    toggleSelected(item: SearchSummaryJson): void {
      if (this.isSelected(item)) {
        patchState(store, {
          selectedKinderIds: store.selectedKinderIds().filter(kindId => kindId !== item.id),
        });
      } else {
        patchState(store, {selectedKinderIds: [...store.selectedKinderIds(), item.id]});
      }
    },
    resetState(): void {
      patchState(store, initialState);
    },
    getLimit(): number {
      return store.limit();
    }
  })),
  withComputed(store => ({
    selectedKinderCount: computed(() =>
      store.selectedKinderIds().length
    ),
  }))
) {
  readonly #documentQueueService = inject(DocumentQueueService);
  readonly #appErrorHandlerService = inject(AppErrorHandlerService);
  readonly loader = new Loader();

  triggerMassenbriefgenerierung = rxMethod<{
    massenbriefParams: DocumentQueueMassenbriefSaveJson,
    onSuccess: () => void
  }>(
    pipe(
      exhaustMap(({massenbriefParams, onSuccess}) => this.loader.tapLoading$(
        this.#documentQueueService.createMassenbrief$({
          documentQueueMassenbriefSaveJson: massenbriefParams
        }),
        error => this.#appErrorHandlerService.handleError(error),
      ).pipe(
        tapResponse({
          next: () => {
            onSuccess();
          },
          error: console.error,
        }),
      )),
    ),
  )
}
