import {inject, Injectable} from '@angular/core';
import {ImpfstoffChargeJson, ImpfstoffChargenService, ImpfstoffChargeSaveJson} from '@dvb/data-access-backend';
import {BackendEntityId} from '@dvb/data-access-types';
import {AppErrorHandlerService, Loader, withSelectedEntity} from '@dvb/util-angular';
import {requireValue} from '@dvb/util-shared';
import {tapResponse} from '@ngrx/operators';
import {patchState, signalStore} from '@ngrx/signals';
import {addEntity, removeEntity, setAllEntities, setEntity, withEntities} from '@ngrx/signals/entities';
import {rxMethod} from '@ngrx/signals/rxjs-interop';
import {exhaustMap, pipe} from 'rxjs';

@Injectable()
export class ChargenStore extends signalStore(
  { protectedState: false },
  // withDevtools('Chargen'),
  withEntities<ImpfstoffChargeJson>(),
  withSelectedEntity<ImpfstoffChargeJson>(),
) {
  readonly #impfstoffChargenService = inject(ImpfstoffChargenService);
  readonly #appErrorHandlerService = inject(AppErrorHandlerService);

  readonly loader = new Loader();
  readonly saveLoader = new Loader();
  readonly deleteLoader = new Loader();

  loadChargenByImpfstoff = rxMethod<BackendEntityId>(
    pipe(
      exhaustMap(impfstoffId =>
        this.loader.tapLoading$(
          this.#impfstoffChargenService.findAllByImpfstoff$({ impfstoffId }),
          error => this.#appErrorHandlerService.handleError(error),
        ).pipe(
          tapResponse({
            next: listJson => {
              patchState(this, setAllEntities(listJson.impfstoffChargeList));
            },
            error: console.error,
          }),
        ),
      ),
    ),
  );

  createImpfstoffCharge = rxMethod<{
    impfstoffID: BackendEntityId,
    impfstoffChargeSaveJson: ImpfstoffChargeSaveJson,
    onSuccess: () => void
  }>(
    pipe(
      exhaustMap(({ impfstoffID, impfstoffChargeSaveJson, onSuccess }) =>
        this.saveLoader.tapLoading$(
          this.#impfstoffChargenService.createImpfstoffCharge$({ impfstoffId: impfstoffID, impfstoffChargeSaveJson }),
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

  updateImpfstoffCharge = rxMethod<{
    impfstoffID: BackendEntityId,
    chargeToUpdate: ImpfstoffChargeSaveJson,
    onSuccess: () => void
  }>(
    pipe(
      exhaustMap(
        ({ impfstoffID, chargeToUpdate, onSuccess }) => this.saveLoader.tapLoading$(
          this.#impfstoffChargenService.updateImpfstoffCharge$({
            impfstoffId: impfstoffID,
            impfstoffChargeId: requireValue(this.selectedEntity()?.id),
            impfstoffChargeSaveJson: chargeToUpdate,
          }),
          error => this.#appErrorHandlerService.handleError(error),
        ).pipe(
          tapResponse({
            next: result => {
              patchState(this, setEntity(result));
              onSuccess();
            },
            error: console.error,
          }),
        ),
      ),
    ),
  );

  removeImpfstoffCharge = rxMethod<{
    impfstoffID: BackendEntityId,
    chargeID: BackendEntityId
  }>(
    pipe(
      exhaustMap(({ impfstoffID, chargeID }) =>
        this.deleteLoader
          .tapLoading$(
            this.#impfstoffChargenService.removeImpfstoffCharge$({
              impfstoffId: impfstoffID,
              impfstoffChargeId: requireValue(chargeID),
            }),
            error => this.#appErrorHandlerService.handleError(error),
          )
          .pipe(
            tapResponse({
              next: () => {
                patchState(this, removeEntity(chargeID));
              },
              error: console.error,
            }),
          ),
      ),
    ),
  );

  toggleActiveOnCharge = rxMethod<ImpfstoffChargeJson>(
    pipe(
      exhaustMap((impfstoffChargeJson) => this.saveLoader
        .tapLoading$(
          this.#impfstoffChargenService.updateImpfstoffCharge$({
            impfstoffChargeId: impfstoffChargeJson.id,
            impfstoffId: impfstoffChargeJson.impfstoffId,
            impfstoffChargeSaveJson: { ...impfstoffChargeJson.data, aktiv: !impfstoffChargeJson.data.aktiv },
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
