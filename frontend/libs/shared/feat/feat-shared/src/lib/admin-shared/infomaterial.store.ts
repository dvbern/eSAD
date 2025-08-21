import {inject, Injectable} from '@angular/core';
import {InfomaterialJson, InfomaterialSaveJson, InfomaterialService} from '@dvb/data-access-backend';
import {BackendEntityId} from '@dvb/data-access-types';
import {AppErrorHandlerService, Loader, withSelectedEntity} from '@dvb/util-angular';
import {requireValue} from '@dvb/util-shared';
import {tapResponse} from '@ngrx/operators';
import {patchState, signalStore, withMethods} from '@ngrx/signals';
import {
  addEntity,
  removeAllEntities,
  removeEntity,
  setAllEntities,
  setEntity,
  withEntities,
} from '@ngrx/signals/entities';
import {rxMethod} from '@ngrx/signals/rxjs-interop';
import {exhaustMap, pipe, switchMap, tap} from 'rxjs';

@Injectable({providedIn: 'root'})
export class InfomaterialStore extends signalStore(
  {protectedState: false},
  // withDevtools('infomaterial'),
  withEntities<InfomaterialJson>(),
  withSelectedEntity<InfomaterialJson>(),
  withMethods((store) => ({
    patchInfomaterialOptions(savedInfomaterial: InfomaterialJson[] = []): InfomaterialJson[] {
      const loadedInfomaterialOptions = store.entities();
      // Add only those from savedInfomaterial not already in loadedInfomaterialOptions
      const missingItems = savedInfomaterial.filter(
        current => !loadedInfomaterialOptions.some(existing => existing.id === current.id),
      );
      return [
        ...loadedInfomaterialOptions,
        ...missingItems
      ];
    },
  })),
) {
  readonly #infomaterialService = inject(InfomaterialService);
  readonly #appErrorHandlerService = inject(AppErrorHandlerService);
  readonly loader = new Loader();
  readonly deleteLoader = new Loader();
  readonly saveLoader = new Loader();

  loadInfomaterialList = rxMethod<{ onlyActive: boolean }>(
    pipe(
      tap(() => patchState(this, removeAllEntities())),
      switchMap((onlyActive) => this.loader.tapLoading$(
        this.#infomaterialService.findAllInfomaterialien$(onlyActive),
        error => this.#appErrorHandlerService.handleError(error))),
      tapResponse({
        next: infomaterialList => patchState(this, setAllEntities(infomaterialList.infomaterialienList)),
        error: console.error,
      }),
    ),
  );

  createInfomaterial = rxMethod<{ newInfomaterial: InfomaterialSaveJson, onSuccess: () => void; }>(
    pipe(
      exhaustMap(({newInfomaterial, onSuccess}) => this.saveLoader.tapLoading$(
        this.#infomaterialService.createInfomaterial$({infomaterialSaveJson: newInfomaterial}),
        error => this.#appErrorHandlerService.handleError(error))
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

  updateInfomaterial = rxMethod<{ infomaterialToUpdate: InfomaterialSaveJson, onSuccess: () => void; }>(
    pipe(
      exhaustMap(({infomaterialToUpdate, onSuccess}) => this.saveLoader.tapLoading$(
        this.#infomaterialService.updateInfomaterial$({
          infomaterialId: requireValue(this.selectedEntity()?.id),
          infomaterialSaveJson: infomaterialToUpdate,
        }),
        error => this.#appErrorHandlerService.handleError(error))
        .pipe(
          tapResponse({
            next: result => {
              patchState(this, setEntity(result));
              onSuccess();
            },
            error: console.error,
          }),
        )),
    ),
  );

  removeInfomaterial = rxMethod<BackendEntityId>(
    pipe(
      exhaustMap(idToDelete =>
        this.deleteLoader.tapLoading$(
          this.#infomaterialService.removeInfomaterial$({infomaterialId: idToDelete}),
          error => this.#appErrorHandlerService.handleError(error),
        )
          .pipe(
            tapResponse({
              next: () => patchState(this, removeEntity(idToDelete)),
              error: console.error,
            }),
          ),
      ),
    ),
  );

  toggleActiveOnInfomaterial = rxMethod<InfomaterialJson>(
    pipe(
      exhaustMap((infomaterialJson) => this.saveLoader
        .tapLoading$(
          this.#infomaterialService.updateInfomaterial$({
            infomaterialId: infomaterialJson.id,
            infomaterialSaveJson: {...infomaterialJson.data, aktiv: !infomaterialJson.data.aktiv},
          }), error => this.#appErrorHandlerService.handleError(error))
        .pipe(
          tapResponse({
            next: result => {
              patchState(this, setEntity(result));
            },
            error: console.error,
          }),
        ),
      )),
  );
}
