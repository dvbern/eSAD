import {computed} from '@angular/core';
import {isNullish, Nullish} from '@dvb/util-shared';
import {patchState, signalStoreFeature, type, withComputed, withMethods, withState} from '@ngrx/signals';
import {EntityId, EntityState} from '@ngrx/signals/entities';
import {DetailMode} from './detail-mode';



export type SelectedEntityState = {
  selectedEntityId: EntityId | Nullish;
  detailMode: DetailMode
};

export function withSelectedEntity<Entity>() {
  return signalStoreFeature(
    {state: type<EntityState<Entity>>()},
    withState<SelectedEntityState>({selectedEntityId: null, detailMode: DetailMode.VIEW}),

    withComputed(({entityMap, selectedEntityId, detailMode}) => ({
      selectedEntity: computed(() => {
        const selectedId = selectedEntityId();
        return selectedId ? entityMap()[selectedId] : null;
      }),
      selectedIsNew: computed(() => {
        const selectedId = selectedEntityId();
        return isNullish(selectedId);
      }),
      detailIsEditMode: computed(() => {
        return detailMode() === DetailMode.EDIT
      }),
      detailIsViewMode: computed(() => {
        return detailMode() === DetailMode.VIEW
      }),
    })),
    withMethods(store => ({
      setSelectedEntityId: (id: EntityId | Nullish) => {
        patchState(store, {selectedEntityId: id});
      },
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
