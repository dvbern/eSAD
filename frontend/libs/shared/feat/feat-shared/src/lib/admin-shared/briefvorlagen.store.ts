import {inject, Injectable} from '@angular/core';
import {BriefvorlageJson, BriefvorlageSaveJson, BriefvorlageService} from '@dvb/data-access-backend';
import {BackendEntityId} from '@dvb/data-access-types';
import {AppErrorHandlerService, handleBlobResponseDownload, Loader, withSelectedEntity} from '@dvb/util-angular';
import {requireValue} from '@dvb/util-shared';
import {tapResponse} from '@ngrx/operators';
import {patchState, signalStore} from '@ngrx/signals';
import {
  addEntity,
  removeAllEntities,
  removeEntity,
  setAllEntities,
  setEntity,
  withEntities,
} from '@ngrx/signals/entities';
import {rxMethod} from '@ngrx/signals/rxjs-interop';
import {exhaustMap, map, pipe, switchMap, tap} from 'rxjs';

@Injectable({providedIn: 'root'})
export class BriefvorlagenStore extends signalStore(
  {protectedState: false},
  // withDevtools('briefvorlagen'),
  withEntities<BriefvorlageJson>(),
  withSelectedEntity<BriefvorlageJson>(),
) {

  readonly #briefvorlageService = inject(BriefvorlageService);
  readonly #appErrorHandlerService = inject(AppErrorHandlerService);

  readonly loader = new Loader();
  readonly saveLoader = new Loader();
  readonly deleteLoader = new Loader();

  loadBriefvorlagen = rxMethod<void>(
    pipe(
      tap(() => patchState(this, removeAllEntities())),
      switchMap(() => this.loader.tapLoading$(this.#briefvorlageService.findAllBriefvorlagen$(),
        error => this.#appErrorHandlerService.handleError(error))),
      tapResponse({
        next: briefvorlagenList => patchState(this, setAllEntities(briefvorlagenList.briefvorlageList)),
        error: console.error,
      }),
    ),
  );

  createBriefvorlagen = rxMethod<{
    fileData: File,
    briefvorlageSaveJson: BriefvorlageSaveJson,
    onSuccess: () => void
  }>(
    pipe(
      exhaustMap(
        ({ fileData, briefvorlageSaveJson, onSuccess }) => this.saveLoader.tapLoading$(
          this.#briefvorlageService.createBriefvorlage$({
            briefvorlageSaveJson: briefvorlageSaveJson,
            file: fileData,
          }),
          error => this.#appErrorHandlerService.handleError(error),
        ).pipe(
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

  updateBriefvorlagen = rxMethod<{
    fileData?: File
    briefvorlageSaveJson: BriefvorlageSaveJson,
    onSuccess: (briefvorlageJson: BriefvorlageJson) => void
  }>(
    pipe(
      exhaustMap(
        ({ fileData, briefvorlageSaveJson, onSuccess }) => this.saveLoader.tapLoading$(
          this.#briefvorlageService.updateBriefvorlage$({
            file: fileData,
            briefvorlageId: requireValue(this.selectedEntity()?.id),
            briefvorlageSaveJson: briefvorlageSaveJson,
          }),
          error => this.#appErrorHandlerService.handleError(error),
        ).pipe(
          tapResponse({
            next: result => {
              patchState(this, setEntity(result));
              onSuccess(result);
            },
            error: console.error,
          }),
        ),
      ),
    ),
  );

  removeBriefvorlagen = rxMethod<BackendEntityId>(
    pipe(
      exhaustMap(idToDelete =>
        this.deleteLoader.tapLoading$(
          this.#briefvorlageService.removeBriefvorlage$({ briefvorlageId: idToDelete }),
          error => this.#appErrorHandlerService.handleError(error),
        ).pipe(map(() => (idToDelete))),
      ),
      tapResponse({
        next: result => patchState(this, removeEntity(result)),
        error: console.error,
      }),
    ),
  );

  downloadBrief = rxMethod<{
    briefvorlageId: BackendEntityId,
    kindId: BackendEntityId,
    einladungId?: BackendEntityId,
    arztId?: BackendEntityId,
    onSuccess: () => void
  }>(
    pipe(
      exhaustMap(
        ({briefvorlageId, kindId, einladungId, arztId, onSuccess}) => this.saveLoader.tapLoading$(
          this.#briefvorlageService.downloadBrief$({
            briefvorlageId: briefvorlageId,
            kindId: kindId,
            einladungId: einladungId,
            arztId
          }, 'response'),
          error => this.#appErrorHandlerService.handleError(error),
        ).pipe(
          tapResponse({
            next: (result) => {
              handleBlobResponseDownload(result, true);
              onSuccess();
            },
            error: console.error,
          }),
        ),
      ),
    ),
  );

  downloadBriefvorlageFile = rxMethod<{
    briefvorlageId: BackendEntityId,
  }>(
    pipe(
      exhaustMap(
        ({briefvorlageId}) => this.saveLoader.tapLoading$(
          this.#briefvorlageService.downloadBriefvorlageFile$({
            briefvorlageId: briefvorlageId
          }, 'response'),
          error => this.#appErrorHandlerService.handleError(error),
        ).pipe(
          tapResponse({
            next: (result) => {
              handleBlobResponseDownload(result, true);
            },
            error: console.error,
          }),
        ),
      ),
    ),
  );

  downloadTestBrief = rxMethod<{
    briefvorlageId: BackendEntityId,
    onSuccess: () => void
  }>(
    pipe(
      exhaustMap(
        ({briefvorlageId, onSuccess}) => this.saveLoader.tapLoading$(
          this.#briefvorlageService.downloadTestBrief$({
            briefvorlageId: briefvorlageId,
          }, 'response'),
          error => this.#appErrorHandlerService.handleError(error),
        ).pipe(
          tapResponse({
            next: (result) => {
              handleBlobResponseDownload(result, true);
              onSuccess();
            },
            error: console.error,
          }),
        ),
      ),
    ),
  );
}
