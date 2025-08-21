import {inject, Injectable} from '@angular/core';
import {
  KontaktPersonSchuleJson,
  KontaktPersonSchuleSaveJson,
  KontaktpersonSchuleService,
} from '@dvb/data-access-backend';
import {BackendEntityId} from '@dvb/data-access-types';
import {AppErrorHandlerService, Loader, withSelectedEntity} from '@dvb/util-angular';
import {requireValue} from '@dvb/util-shared';
import {tapResponse} from '@ngrx/operators';
import {patchState, signalStore} from '@ngrx/signals';
import {addEntity, removeEntity, setAllEntities, setEntity, withEntities} from '@ngrx/signals/entities';
import {rxMethod} from '@ngrx/signals/rxjs-interop';
import {exhaustMap, pipe, switchMap} from 'rxjs';

@Injectable()
export class KontaktPersonSchuleStore extends signalStore(
  {protectedState: false},
  // withDevtools('kontaktPersonSchule'),
  withEntities<KontaktPersonSchuleJson>(),
  withSelectedEntity<KontaktPersonSchuleJson>(),
) {
  readonly loader = new Loader();
  readonly entityLoader = new Loader();
  readonly saveLoader = new Loader();
  readonly deleteLoader = new Loader();
  readonly #kontaktPersonSchuleService = inject(KontaktpersonSchuleService);
  readonly #appErrorHandlerService = inject(AppErrorHandlerService);

  loadKontaktPersonSchuleById = rxMethod<{ schuleId: BackendEntityId, kontaktPersonSchuleId: BackendEntityId }>(
    pipe(
      switchMap(({schuleId, kontaktPersonSchuleId}) => this.entityLoader.tapLoading$(
        this.#kontaktPersonSchuleService.getKontaktPersonSchule$({
          schuleID: schuleId,
          kontaktPersonSchuleID: kontaktPersonSchuleId,
        }),
        error => this.#appErrorHandlerService.handleError(error))),
      tapResponse({
        next: kontaktPersonSchuleJson => patchState(this, setEntity(kontaktPersonSchuleJson)),
        error: console.error,
      }),
    ),
  );

  loadKontaktPersonenBySchule = rxMethod<{ schuleID: BackendEntityId }>(
    pipe(
      switchMap((params) =>
        this.loader.tapLoading$(
          this.#kontaktPersonSchuleService.findAllAktivBySchule$(
            {
              schuleID: params.schuleID,
            },
          ),
          error => this.#appErrorHandlerService.handleError(error),
        ),
      ),
      tapResponse({
        next: result => patchState(this, setAllEntities(result.kontaktPersonenList)),
        error: console.error,
      }),
    ),
  );

  createKontaktPersonSchule = rxMethod<{
    schuleId: BackendEntityId,
    kontaktPerson: KontaktPersonSchuleSaveJson,
    onSuccess: (result: KontaktPersonSchuleJson) => void;
  }>(
    pipe(
      exhaustMap((params) => this.saveLoader.tapLoading$(
        this.#kontaktPersonSchuleService.createKontaktPersonSchule$({
          schuleID: params.schuleId,
          kontaktPersonSchuleSaveJson: params.kontaktPerson,
        }),
        error => this.#appErrorHandlerService.handleError(error))
        .pipe(
          tapResponse({
            next: result => {
              patchState(this, addEntity(result));
              params.onSuccess(result);
            },
            error: console.error,
          }),
        ),
      ),
    ),
  );

  updateKontaktPersonSchule = rxMethod<{
    schuleId: BackendEntityId,
    kontaktPersonToUpdate: KontaktPersonSchuleSaveJson,
    onSuccess: (result: KontaktPersonSchuleJson) => void;
  }>(
    pipe(
      exhaustMap((params) => this.saveLoader.tapLoading$(
        this.#kontaktPersonSchuleService.updateKontaktPersonSchule$({
          schuleID: params.schuleId,
          kontaktPersonSchuleID: requireValue(this.selectedEntity()?.id),
          kontaktPersonSchuleSaveJson: requireValue(params.kontaktPersonToUpdate),
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
        )),
    ),
  );

  deleteKontaktPerson = rxMethod<{
    schuleId: BackendEntityId,
    kontaktPersonId: BackendEntityId,
  }>(
    pipe(
      exhaustMap(params =>
        this.deleteLoader.tapLoading$(
          this.#kontaktPersonSchuleService.deleteKontaktPersonSchule$({
            kontaktPersonSchuleID: params.kontaktPersonId,
            schuleID: params.schuleId,
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

  addKontaktPersonToSchule = rxMethod<{
    schuleId: BackendEntityId,
    kontaktPersonId: BackendEntityId,
    onSuccess: () => void,
  }>(
    pipe(
      exhaustMap(params =>
        this.deleteLoader.tapLoading$(
          this.#kontaktPersonSchuleService.addKontaktPersonSchuleToSchule$({
            kontaktPersonSchuleID: params.kontaktPersonId,
            schuleID: params.schuleId,
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

  removeKontaktPersonFromSchule = rxMethod<{
    schuleId: BackendEntityId,
    kontaktPersonId: BackendEntityId,
    onSuccess: () => void,
  }>(
    pipe(
      exhaustMap(params =>
        this.deleteLoader.tapLoading$(
          this.#kontaktPersonSchuleService.removeKontaktPersonSchuleFromSchule$({
            kontaktPersonSchuleID: params.kontaktPersonId,
            schuleID: params.schuleId,
          }),
          error => this.#appErrorHandlerService.handleError(error),
        )
          .pipe(
            tapResponse({
              next: () => {
                patchState(this, removeEntity(params.kontaktPersonId));
                params.onSuccess()
              },
              error: console.error,
            }),
          ),
      ),
    ),
  );
}
