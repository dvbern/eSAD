import {inject, Injectable} from '@angular/core';
import {AufgabeJson, AufgabeSaveJson, AufgabeService} from '@dvb/data-access-backend';
import {BackendEntityId} from '@dvb/data-access-types';
import {AppErrorHandlerService, handleBlobResponseDownload, Loader, withSelectedEntity} from '@dvb/util-angular';
import {isNotNullish, requireValue} from '@dvb/util-shared';
import {tapResponse} from '@ngrx/operators';
import {patchState, signalStore, withMethods, withState} from '@ngrx/signals';
import {
  addEntity,
  removeAllEntities,
  removeEntity,
  setAllEntities,
  setEntity,
  withEntities,
} from '@ngrx/signals/entities';
import {rxMethod} from '@ngrx/signals/rxjs-interop';
import {concatMap, exhaustMap, from, last, map, mergeMap, pipe, switchMap, tap} from 'rxjs';

export interface AufgabeJsonEnhanced extends AufgabeJson {
  kindFullName?: string;
  labelIds: Array<BackendEntityId>;
}

type AufgabeState = {
  numOfOffeneFaelligeAufgaben: number;
};

const initialState: AufgabeState = {
  numOfOffeneFaelligeAufgaben: 0,
};

@Injectable({ providedIn: 'root' })
export class AufgabenStore extends signalStore(
  { protectedState: false },
  // withDevtools('aufgaben'),
  withEntities<AufgabeJsonEnhanced>(),
  withSelectedEntity<AufgabeJsonEnhanced>(),
  withState(initialState),
  withMethods(store => ({
    resetSelectedAufgabe(): void {
      store.setSelectedEntityId(undefined);
    },
  })),
) {
  readonly #aufgabenService = inject(AufgabeService);
  readonly #appErrorHandlerService = inject(AppErrorHandlerService);

  readonly loader = new Loader();
  readonly saveLoader = new Loader();
  readonly deleteLoader = new Loader();

  getNumOfFaelligeAufgabenForCurrentUser = rxMethod<void>(
    pipe(
      switchMap(() => this.loader.tapLoading$(
        this.#aufgabenService.getNumOfFaelligeAufgabenForCurrentUser$(),
        error => this.#appErrorHandlerService.handleError(error))),
      tapResponse({
        next: result => patchState(this, { numOfOffeneFaelligeAufgaben: result.numOfOffeneFaelligeAufgaben }),
        error: console.error,
      }),
    ),
  );

  loadAufgaben = rxMethod<void>(
    pipe(
      switchMap(() =>
        this.loader.tapLoading$(
          this.#aufgabenService.findAllAufgaben$(),
          error => this.#appErrorHandlerService.handleError(error))),
      tapResponse({
        next: result => {
          patchState(this, setAllEntities(result.aufgaben.map(aufgabe => enhanceAufgabe(aufgabe))));
          patchState(this,
            {
              numOfOffeneFaelligeAufgaben: result.numOfOffeneFaelligeAufgaben,
            });
        },
        error: console.error,
      }),
    ),
  );

  loadAufgabenForKind = rxMethod<BackendEntityId>(
    pipe(
      tap(() => patchState(this, removeAllEntities())),
      switchMap(kindId => this.loader.tapLoading$(
        this.#aufgabenService.findAllForKind$({ kindId }),
        error => this.#appErrorHandlerService.handleError(error)),
      ),
      tapResponse(({
        next: result => {
          patchState(this, setAllEntities(result.aufgaben.map(aufgabe => enhanceAufgabe(aufgabe))));
          patchState(this,
            {
              numOfOffeneFaelligeAufgaben: result.numOfOffeneFaelligeAufgaben,
            });
        },
        error: console.error,
      })),
    ),
  );

  loadAufgabe = rxMethod<BackendEntityId>(
    pipe(
      switchMap(aufgabeId =>
        this.loader.tapLoading$(
          this.#aufgabenService.getAufgabe$({ aufgabeId }),
          error => this.#appErrorHandlerService.handleError(error))),
      tapResponse({
        next: result => {
          this.setSelectedEntityId(result.id);
          patchState(this, setEntity(enhanceAufgabe(result)));
        },
        error: console.error,
      }),
    ),
  );

  createAufgabe = rxMethod<{ aufgabeSaveJson: AufgabeSaveJson, onSuccess: (aufgabe: AufgabeJson) => void }>(
    pipe(
      exhaustMap(
        ({ aufgabeSaveJson, onSuccess }) => this.saveLoader.tapLoading$(
          this.#aufgabenService.createAufgabe$({ aufgabeSaveJson }),
          error => this.#appErrorHandlerService.handleError(error),
        ).pipe(
          tapResponse({
            next: result => {
              patchState(this, addEntity(enhanceAufgabe(result)));
              onSuccess(result);
            },
            error: console.error,
          }),
        ),
      ),
    ),
  );

  updateAufgabe = rxMethod<{ aufgabeSaveJson: AufgabeSaveJson, onSuccess: (aufgabe: AufgabeJson) => void }>(
    pipe(
      exhaustMap(
        ({ aufgabeSaveJson, onSuccess }) => this.saveLoader.tapLoading$(
          this.#aufgabenService.updateAufgabe$({
            aufgabeId: requireValue(this.selectedEntity()?.id),
            aufgabeSaveJson,
          }),
          error => this.#appErrorHandlerService.handleError(error),
        ).pipe(
          tapResponse({
            next: result => {
              patchState(this, setEntity(enhanceAufgabe(result)));
              onSuccess(result);
            },
            error: console.error,
          }),
        ),
      ),
    ),
  );

  removeAufgabe = rxMethod<BackendEntityId>(
    pipe(
      exhaustMap(idToDelete =>
        this.deleteLoader.tapLoading$(
          this.#aufgabenService.removeAufgabe$({ aufgabeId: idToDelete }),
          error => this.#appErrorHandlerService.handleError(error),
        ).pipe(map(() => (idToDelete))),
      ),
      tapResponse({
        next: result => patchState(this, removeEntity(result)),
        error: console.error,
      }),
    ),
  );

  uploadAufgabeFiles = rxMethod<{ aufgabeId: BackendEntityId, fileUploads: File[], onSuccess: () => void }>(
    pipe(
      mergeMap(({ aufgabeId, fileUploads, onSuccess }) => {
        return from(fileUploads).pipe(
          concatMap(fileUpload =>
            this.saveLoader.tapLoading$(
              this.#aufgabenService.addAufgabeFile$({
                aufgabeId: aufgabeId,
                file: fileUpload,
              }),
              error => this.#appErrorHandlerService.handleError(error),
            ),
          ),
          last(),
          tapResponse({
            next: result => {
              patchState(this, setEntity(enhanceAufgabe(result)));
              onSuccess();
            },
            error: (error) => {
              console.error('File upload error:', error);
            },
          }),
        );
      }),
    ),
  );

  downloadAufgabeFile = rxMethod<{
    aufgabeId: BackendEntityId,
    fileId: BackendEntityId,
    downloadDirectly?: boolean,
    onSuccess?: (fileblob: Blob) => void
  }>(
    pipe(
      exhaustMap(({ aufgabeId, fileId, downloadDirectly, onSuccess }) => this.loader.tapLoading$(
          this.#aufgabenService.downloadAufgabeFileSync$({ aufgabeId, fileId }, 'response'),
          error => this.#appErrorHandlerService.handleError(error),
        ).pipe(tapResponse({
            next: result => {
              handleBlobResponseDownload(result, downloadDirectly ?? false);
              if (isNotNullish(onSuccess)) {
                onSuccess(requireValue(result.body));
              }
            },
            error: console.error,
          }),
        ),
      ),
    ),
  );

  removeAufgabeFile = rxMethod<{
    aufgabeId: BackendEntityId,
    fileId: BackendEntityId,
    onSuccess?: (aufgabe: AufgabeJson) => void
  }>(
    pipe(
      exhaustMap(({ aufgabeId, fileId, onSuccess }) => this.deleteLoader.tapLoading$(
          this.#aufgabenService.removeAufgabeFile$({ aufgabeId, fileId }),
          error => this.#appErrorHandlerService.handleError(error),
        ).pipe(
          tapResponse({
            next: result => {
              patchState(this, setEntity(enhanceAufgabe(result)));
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
}

export function enhanceAufgabe(aufgabe: AufgabeJson): AufgabeJsonEnhanced {
  return {
    ...aufgabe,
    kindFullName: isNotNullish(aufgabe.data.kind)
      ? aufgabe.data.kind?.vorname + ' ' + aufgabe.data.kind?.name
      : '',
    labelIds: aufgabe.data.labels.map(each => each.id),
  };
}
