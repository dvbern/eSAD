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

import {computed} from '@angular/core';
import {patchState, signalStoreFeature, withComputed, withMethods, withState} from '@ngrx/signals';
import {DetailMode} from './detail-mode';

export type DetailModeState = {
  detailMode: DetailMode
};

export function withDetailModeState() {
  return signalStoreFeature(
    withState<DetailModeState>({detailMode: DetailMode.VIEW}),

    withComputed(({detailMode}) => ({
      detailIsEditMode: computed(() => {
        return detailMode() === DetailMode.EDIT
      }),
      detailIsViewMode: computed(() => {
        return detailMode() === DetailMode.VIEW
      }),
    })),
    withMethods(store => ({
      setDetailMode: (mode: DetailMode) => {
        patchState(store, {detailMode: mode})
      },
      setDetailModeView: () => {
        patchState(store, {detailMode: DetailMode.VIEW})
      },
      setDetailModeEdit: () => {
        patchState(store, {detailMode: DetailMode.EDIT})
      },
    })),
  );
}
