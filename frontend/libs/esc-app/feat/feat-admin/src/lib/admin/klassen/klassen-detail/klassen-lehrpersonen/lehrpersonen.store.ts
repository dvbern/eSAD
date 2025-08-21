import {inject, Injectable} from '@angular/core';
import {LehrpersonJson, LehrpersonSaveJson, LehrpersonService} from '@dvb/data-access-backend';
import {BackendEntityId} from '@dvb/data-access-types';
import {AppErrorHandlerService, Loader, withSelectedEntity} from '@dvb/util-angular';
import {requireValue} from '@dvb/util-shared';
import {tapResponse} from '@ngrx/operators';
import {patchState, signalStore} from '@ngrx/signals';
import {addEntity, removeEntity, setAllEntities, setEntity, withEntities} from '@ngrx/signals/entities';
import {rxMethod} from '@ngrx/signals/rxjs-interop';
import {exhaustMap, pipe, switchMap} from 'rxjs';

@Injectable()
export class LehrpersonenStore extends signalStore(
  {protectedState: false},
  // withDevtools('Lehrpersonen'),
  withEntities<LehrpersonJson>(),
  withSelectedEntity<LehrpersonJson>(),
) {
  readonly loader = new Loader();
  readonly entityLoader = new Loader();
  readonly saveLoader = new Loader();
  readonly deleteLoader = new Loader();
  readonly #lehrpersonenService = inject(LehrpersonService);
  readonly #appErrorHandlerService = inject(AppErrorHandlerService);

  loadLehrpersonById = rxMethod<{ klasseId: BackendEntityId, lehrpersonId: BackendEntityId }>(
    pipe(
      switchMap(({klasseId, lehrpersonId}) => this.entityLoader.tapLoading$(
        this.#lehrpersonenService.getLehrperson$({klasseID: klasseId, lehrpersonID: lehrpersonId}),
        error => this.#appErrorHandlerService.handleError(error))),
      tapResponse({
        next: lehrpersonJson => patchState(this, setEntity(lehrpersonJson)),
        error: console.error,
      }),
    ),
  );

  loadLehrpersonenByKlasse = rxMethod<BackendEntityId>(
    pipe(
      switchMap((klasseId) => this.loader.tapLoading$(
        this.#lehrpersonenService.findAllAktivByKlasse$({klasseID: klasseId}),
        error => this.#appErrorHandlerService.handleError(error))),
      tapResponse({
        next: listJson => patchState(this, setAllEntities(listJson.lehrpersonList)),
        error: console.error,
      }),
    ),
  );

  createLehrperson = rxMethod<{
    klasseID: BackendEntityId
    lehrperson: LehrpersonSaveJson,
    onSuccess: (result: LehrpersonJson) => void
  }>(
    pipe(
      exhaustMap((params) => this.saveLoader.tapLoading$(
          this.#lehrpersonenService.createLehrperson$({
            klasseID: params.klasseID,
            lehrpersonSaveJson: params.lehrperson,
          }),
          error => this.#appErrorHandlerService.handleError(error),
        ).pipe(
          tapResponse({
            next: result => {
              patchState(this, addEntity(result));
              params.onSuccess(result);
            },
            error: console.error,
          }),
        ),
      )),
  );

  updateLehrperson = rxMethod<{
    klasseID: BackendEntityId
    lehrpersonToUpdate: LehrpersonSaveJson,
    onSuccess: (result: LehrpersonJson) => void,
  }>(
    pipe(
      exhaustMap((params) => this.saveLoader.tapLoading$(
        this.#lehrpersonenService.updateLehrperson$({
          klasseID: params.klasseID,
          lehrpersonID: requireValue(this.selectedEntity()?.id),
          lehrpersonSaveJson: params.lehrpersonToUpdate,
        }),
        error => this.#appErrorHandlerService.handleError(error))
        .pipe(
          tapResponse({
            next: result => {
              patchState(this, setEntity(result));
              params.onSuccess(result);
            },
            error: console.error,
          }),
        ),
      )),
  );

  deleteLehrperson = rxMethod<{
    klasseID: BackendEntityId
    lehrpersonID: BackendEntityId,
  }>(
    pipe(
      exhaustMap(params =>
        this.deleteLoader.tapLoading$(
          this.#lehrpersonenService.deleteLehrperson$({
            klasseID: params.klasseID,
            lehrpersonID: params.lehrpersonID,
          }),
          error => this.#appErrorHandlerService.handleError(error),
        )
          .pipe(
            tapResponse({
              next: () => patchState(this, removeEntity(params.lehrpersonID)),
              error: console.error,
            }),
          ),
      ),
    ),
  );

  addLehrpersonToKlasse = rxMethod<{
    klasseID: BackendEntityId
    lehrpersonID: BackendEntityId,
    onSuccess: () => void
  }>(
    pipe(
      exhaustMap(params =>
        this.deleteLoader.tapLoading$(
          this.#lehrpersonenService.addLehrpersonToKlasse$({
            klasseID: params.klasseID,
            lehrpersonID: params.lehrpersonID,
          }),
          error => this.#appErrorHandlerService.handleError(error),
        )
          .pipe(
            tapResponse({
              next: (result) => {
                patchState(this, addEntity(result));
                params.onSuccess();
              },
              error: console.error,
            }),
          ),
      ),
    ),
  );

  removeLehrpersonFromKlasse = rxMethod<{
    klasseID: BackendEntityId
    lehrpersonID: BackendEntityId,
    onSuccess: () => void
  }>(
    pipe(
      exhaustMap(params =>
        this.deleteLoader.tapLoading$(
          this.#lehrpersonenService.removeLehrpersonFromKlasse$({
            klasseID: params.klasseID,
            lehrpersonID: params.lehrpersonID,
          }),
          error => this.#appErrorHandlerService.handleError(error),
        )
          .pipe(
            tapResponse({
              next: () => {
                patchState(this, removeEntity(params.lehrpersonID));
                params.onSuccess();
              },
              error: console.error,
            }),
          ),
      ),
    ),
  );
}
