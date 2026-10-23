import {inject, Injectable} from '@angular/core';
import {
  EntityAktivStatusSaveJson,
  ImpfstoffChargeJson,
  ImpfstoffeService,
  ImpfstoffJson,
  ImpfstoffSaveJson,
  Krankheit,
} from '@dvb/data-access-backend';
import {BackendEntityId} from '@dvb/data-access-types';
import {AppErrorHandlerService, Loader, withSelectedEntity} from '@dvb/util-angular';
import {ensureProps, hasValue, Nullish, requireValue} from '@dvb/util-shared';
import {TranslocoService} from '@jsverse/transloco';
import {tapResponse} from '@ngrx/operators';
import {patchState, signalStore, withMethods, withState} from '@ngrx/signals';
import {addEntity, removeEntity, setAllEntities, setEntity, withEntities} from '@ngrx/signals/entities';
import {rxMethod} from '@ngrx/signals/rxjs-interop';
import {exhaustMap, pipe, switchMap} from 'rxjs';
import {ImpfstoffChargeOption} from '../core/impfstoff-charge-option';

type ImpfstoffeChargenState = {
  chargenList: ImpfstoffChargeJson[];
};

const initialState: ImpfstoffeChargenState = {
  chargenList: [],
};

/**
 * Options fuer die Charge-Auswahl: alle aktiven Chargen des Impfstoffs sowie die aktuell gesetzte
 * Charge, auch wenn diese inaktiv oder ad hoc erfasst ist. Letztere wird als inaktiv markiert,
 * damit die Anzeige einen entsprechenden Suffix ergaenzen kann.
 */
export function getChargeOptions(
  activeChargen: ImpfstoffChargeJson[],
  currentChargeName: string | Nullish,
): ImpfstoffChargeOption[] {
  const options: ImpfstoffChargeOption[] = activeChargen.map(charge => ensureProps<ImpfstoffChargeOption>({
    label: charge.data.name,
    inaktiv: false,
  }));
  if (hasValue(currentChargeName) && !options.some(option => option.label === currentChargeName)) {
    options.push(ensureProps<ImpfstoffChargeOption>({
      label: currentChargeName,
      inaktiv: true,
    }));
  }

  return options;
}

@Injectable({providedIn: 'root'})
export class ImpfstoffeChargenStore extends signalStore(
  {protectedState: false},
  // withDevtools('impfstoffe-chargen'),
  withEntities<ImpfstoffJson>(),
  withSelectedEntity<ImpfstoffJson>(),
  withState(initialState),
  withMethods((store, translocoService = inject(TranslocoService)) => ({
    chargenForImpfstoff(impfstoffId: BackendEntityId | undefined): ImpfstoffChargeJson[] {
      return store.chargenList().filter((el: ImpfstoffChargeJson) => el.impfstoffId === impfstoffId);
    },
    activeChargenForImpfstoff(impfstoffId: BackendEntityId | undefined): ImpfstoffChargeJson[] {
      return store.chargenList().filter((el: ImpfstoffChargeJson) => el.data.aktiv && el.impfstoffId === impfstoffId);
    },
    chargeOptionsForImpfstoffIncluding(
      impfstoffId: BackendEntityId | undefined,
      currentChargeName: string | Nullish,
    ): ImpfstoffChargeOption[] {
      return getChargeOptions(this.activeChargenForImpfstoff(impfstoffId), currentChargeName);
    },
    getKrankheitenTranslatedList(krankheiten: Krankheit[]): string {
      return krankheiten.map(krankheit => translocoService.translate(`Krankheit.${krankheit}`)).join(', ');
    },
  })),
) {
  readonly #impfstoffeService = inject(ImpfstoffeService);
  readonly #appErrorHandlerService = inject(AppErrorHandlerService);
  readonly loader = new Loader();
  readonly saveLoader = new Loader();
  readonly deleteLoader = new Loader();
  readonly changeStatusLoader = new Loader();

  loadImpfstoffeAndChargen = rxMethod<void>(
    pipe(
      switchMap(() =>
        this.loader.tapLoading$(
          this.#impfstoffeService.findAllImpfstoffeAndChargen$(),
          error => this.#appErrorHandlerService.handleError(error))),
      tapResponse({
        next: result => {
          patchState(this, {
            chargenList: result.chargenList,
          });
          patchState(this, setAllEntities(result.impfstoffeList));
        },
        error: console.error,
      }),
    ),
  );

  createImpfstoff = rxMethod<{ impfstoff: ImpfstoffSaveJson, onSuccess: (result: ImpfstoffJson) => void; }>(
    pipe(
      exhaustMap(({impfstoff, onSuccess}) =>
        this.saveLoader.tapLoading$(
          this.#impfstoffeService.createImpfstoff$({impfstoffSaveJson: impfstoff}),
          error => this.#appErrorHandlerService.handleError(error),
        ).pipe(
          tapResponse({
            next: result => {
              patchState(this, addEntity(result));
              onSuccess(result);
            },
            error: console.error,
          }),
        ),
      ),
    ),
  );

  updateImpfstoff = rxMethod<{ impfstoff: ImpfstoffSaveJson, onSuccess: () => void }>(
    pipe(
      exhaustMap(
        ({impfstoff, onSuccess}) => this.saveLoader.tapLoading$(
          this.#impfstoffeService.updateImpfstoff$({
            impfstoffeId: requireValue(this.selectedEntity()?.id),
            impfstoffSaveJson: impfstoff,
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

  changeImpfstoffAktivStatus = rxMethod<{ impfstoffId: BackendEntityId, saveJson: EntityAktivStatusSaveJson }>(
    pipe(
      exhaustMap(({impfstoffId, saveJson}) =>
        this.changeStatusLoader
          .tapLoading$(this.#impfstoffeService.updateImpfstoffAktivStatus$({
              impfstoffId: impfstoffId,
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

  removeImpfstoff = rxMethod<BackendEntityId>(
    pipe(
      exhaustMap(idToDelete =>
        this.deleteLoader
          .tapLoading$(
            this.#impfstoffeService.removeImpfstoff$({
              impfstoffeId: requireValue(idToDelete),
            }),
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
}
