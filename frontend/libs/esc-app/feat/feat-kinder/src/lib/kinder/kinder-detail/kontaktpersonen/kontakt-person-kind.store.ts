import {computed, inject, Injectable} from '@angular/core';
import {KontaktPersonKindJson, KontaktPersonKindSaveJson, KontaktpersonKindService} from '@dvb/data-access-backend';
import {BackendEntityId} from '@dvb/data-access-types';
import {AppErrorHandlerService, Loader, withSelectedEntity} from '@dvb/util-angular';
import {requireValue} from '@dvb/util-shared';
import {tapResponse} from '@ngrx/operators';
import {patchState, signalStore, withComputed} from '@ngrx/signals';
import {addEntity, removeEntity, setAllEntities, setEntity, withEntities} from '@ngrx/signals/entities';
import {rxMethod} from '@ngrx/signals/rxjs-interop';
import {exhaustMap, pipe, switchMap} from 'rxjs';

@Injectable()
export class KontaktPersonKindStore extends signalStore(
  {protectedState: false},
  //withDevtools('KontaktPersonKind'),
  withEntities<KontaktPersonKindJson>(),
  withSelectedEntity<KontaktPersonKindJson>(),
  withComputed(({entities}) => ({
    countAll: computed(() => entities().length),
  })),
) {
  readonly loader = new Loader();
  readonly entityLoader = new Loader();
  readonly saveLoader = new Loader();
  readonly deleteLoader = new Loader();
  readonly #kontaktPersonService = inject(KontaktpersonKindService);
  readonly #appErrorHandlerService = inject(AppErrorHandlerService);

  loadKontaktPersonKindById = rxMethod<{ kindID: BackendEntityId, kontaktPersonKindID: BackendEntityId }>(
    pipe(
      switchMap(({kindID, kontaktPersonKindID}) => this.loader.tapLoading$(
        this.#kontaktPersonService.getKontaktPersonKind$({
          kindID: kindID,
          kontaktPersonKindID: kontaktPersonKindID,
        }),
        error => this.#appErrorHandlerService.handleError(error))),
      tapResponse({
        next: kontaktPersonKindJson => patchState(this, setEntity(kontaktPersonKindJson)),
        error: console.error,
      }),
    ),
  );

  loadKontaktPersonenByKind = rxMethod<{ kindID: BackendEntityId }>(
    pipe(
      switchMap((params) => this.loader.tapLoading$(
        this.#kontaktPersonService.findAktivByKind$({kindID: params.kindID}),
        error => this.#appErrorHandlerService.handleError(error))),
      tapResponse({
        next: listJson => patchState(this, setAllEntities(listJson.kontaktPersonenList)),
        error: console.error,
      }),
    ),
  );

  createKontaktPerson = rxMethod<{
    kindID: BackendEntityId,
    kontaktPerson: KontaktPersonKindSaveJson,
    onSuccess: (result: KontaktPersonKindJson) => void
  }>(
    pipe(
      exhaustMap((params) => this.saveLoader.tapLoading$(
          this.#kontaktPersonService.createKontaktPersonKind$({
            kindID: params.kindID,
            kontaktPersonKindSaveJson: params.kontaktPerson,
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

  updateKontaktPerson = rxMethod<{
    kindID: BackendEntityId,
    kontaktPersonToUpdate: KontaktPersonKindSaveJson,
    onSuccess: (result: KontaktPersonKindJson) => void,
  }>(
    pipe(
      exhaustMap((params) => this.saveLoader.tapLoading$(
        this.#kontaktPersonService.updateKontaktPersonKind$({
          kindID: params.kindID,
          kontaktPersonKindID: requireValue(this.selectedEntity()?.id),
          kontaktPersonKindSaveJson: params.kontaktPersonToUpdate,
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

  deleteKontaktPerson = rxMethod<{
    kindID: BackendEntityId,
    kontaktPersonId: BackendEntityId,
  }>(
    pipe(
      exhaustMap(params =>
        this.deleteLoader.tapLoading$(
          this.#kontaktPersonService.deleteKontaktPersonKind$({
            kindID: params.kindID,
            kontaktPersonKindID: params.kontaktPersonId,
          }),
          error => this.#appErrorHandlerService.handleError(error),
        )
          .pipe(
            tapResponse({
              next: () => patchState(this, removeEntity(params.kontaktPersonId)),
              error: console.error,
            }),
          ),
      ),
    ),
  );

  addKontaktPersonToKind = rxMethod<{
    kindID: BackendEntityId,
    kontaktPersonId: BackendEntityId,
    onSuccess: () => void
  }>(
    pipe(
      exhaustMap(params =>
        this.deleteLoader.tapLoading$(
          this.#kontaktPersonService.addKontaktPersonKindToKind$({
            kindID: params.kindID,
            kontaktPersonKindID: params.kontaktPersonId,
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

  removeKontaktPersonFromKind = rxMethod<{
    kindID: BackendEntityId,
    kontaktPersonId: BackendEntityId,
    onSuccess: () => void
  }>(
    pipe(
      exhaustMap(params =>
        this.deleteLoader.tapLoading$(
          this.#kontaktPersonService.removeKontaktPersonKindFromKind$({
            kindID: params.kindID,
            kontaktPersonKindID: params.kontaktPersonId,
          }),
          error => this.#appErrorHandlerService.handleError(error),
        )
          .pipe(
            tapResponse({
              next: () => {
                patchState(this, removeEntity(params.kontaktPersonId));
                params.onSuccess();
              },
              error: console.error,
            }),
          ),
      ),
    ),
  );
}
