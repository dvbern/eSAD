import {inject, Injectable} from '@angular/core';
import {EntityAktivStatusSaveJson, SchuleJson, SchulenService, SchuleSaveJson} from '@dvb/data-access-backend';
import {BackendEntityId} from '@dvb/data-access-types';
import {AppErrorHandlerService, Loader, withSelectedEntity} from '@dvb/util-angular';
import {requireValue} from '@dvb/util-shared';
import {tapResponse} from '@ngrx/operators';
import {patchState, signalStore} from '@ngrx/signals';
import {addEntity, removeEntity, setAllEntities, setEntity, withEntities} from '@ngrx/signals/entities';
import {rxMethod} from '@ngrx/signals/rxjs-interop';
import {exhaustMap, pipe, switchMap, tap} from 'rxjs';

@Injectable({providedIn: 'root'})
export class SchulenStore extends signalStore(
  {protectedState: false},
  // withDevtools('schulen'),
  withEntities<SchuleJson>(),
  withSelectedEntity<SchuleJson>(),
) {
  readonly loader = new Loader();
  readonly saveLoader = new Loader();
  readonly deleteLoader = new Loader();
  readonly loadEntityLoader = new Loader();
  readonly changeStatusLoader = new Loader();
  readonly #schulenService = inject(SchulenService);
  readonly #appErrorHandlerService = inject(AppErrorHandlerService);

  loadSchulenList = rxMethod<void>(
    pipe(
      tap(() => patchState(this, setAllEntities([] as SchuleJson[]))),
      switchMap(() => this.loader.tapLoading$(this.#schulenService.findAllSchulen$(),
        error => this.#appErrorHandlerService.handleError(error))),
      tapResponse({
        next: listJson => patchState(this, setAllEntities(listJson.schulenList)),
        error: console.error,
      }),
    ),
  );

  loadEntity = rxMethod<BackendEntityId>(
    pipe(
      switchMap((entityId) => this.loadEntityLoader
        .tapLoading$(this.#schulenService.getSchule$({schuleId: entityId})
            .pipe(
              tapResponse({
                next: result => {
                  patchState(this, setEntity(result));
                },
                error: error => this.#appErrorHandlerService.handleError(error),
              }),
            ),
          error => this.#appErrorHandlerService.handleError(error),
        )),
    ),
  );

  createSchule = rxMethod<{ schule: SchuleSaveJson, onSuccess: (result: SchuleJson) => void }>(
    pipe(
      exhaustMap(
        ({schule, onSuccess}) => this.saveLoader.tapLoading$(
          this.#schulenService.createSchule$({schuleSaveJson: schule}),
          error => this.#appErrorHandlerService.handleError(error),
        ).pipe(
          tap({
              next: result => {
                patchState(this, addEntity(result));
                onSuccess(result);
              },
              error: error => this.#appErrorHandlerService.handleError(error),
            },
          ),
        ),
      )),
  );

  updateSchule = rxMethod<{ schule: SchuleSaveJson, onSuccess: () => void }>(
    pipe(
      exhaustMap(({schule, onSuccess}) => this.saveLoader.tapLoading$(
          this.#schulenService.updateSchule$({
            schuleId: requireValue(this.selectedEntity()?.id),
            schuleSaveJson: schule,
          }),
          error => this.#appErrorHandlerService.handleError(error),
        )
          .pipe(
            tap({
              next: result => {
                patchState(this, setEntity(result)); // wichtig: beim updaten immer setEntity verwenden
                onSuccess();
              },
              error: error => this.#appErrorHandlerService.handleError(error),
            }),
          ),
      )),
  );

  changeSchuleAktivStatus = rxMethod<{ schuleId: BackendEntityId, saveJson: EntityAktivStatusSaveJson }>(
    pipe(
      exhaustMap(({schuleId, saveJson}) =>
        this.changeStatusLoader
          .tapLoading$(this.#schulenService.updateSchuleAktivStatus$({
              schuleId: schuleId,
              entityAktivStatusSaveJson: saveJson,
            }),
            error =>
              this.#appErrorHandlerService.handleError(error),
          )
          .pipe(
            tapResponse({
              next: result => patchState(this, setEntity(result)),
              error: console.error,
            }),
          ),
      ),
    ),
  );

  removeSchule = rxMethod<BackendEntityId>(
    pipe(
      exhaustMap(idToDelete => this.deleteLoader.tapLoading$(
        this.#schulenService.removeSchule$({schuleId: idToDelete}),
        error => this.#appErrorHandlerService.handleError(error),
      ).pipe(
        tapResponse({
          next: () => patchState(this, removeEntity(idToDelete)),
          error: console.error,
        }),
      )),
    ),
  );
}
