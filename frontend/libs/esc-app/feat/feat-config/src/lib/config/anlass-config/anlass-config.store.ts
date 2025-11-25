import { inject, Injectable } from '@angular/core';
import { AnlassConfigJson, AnlassConfigSaveJson, AnlassConfigService, AnlassTyp } from '@dvb/data-access-backend';
import {
  AppErrorHandlerService,
  AppNotificationStore,
  handleBlobResponseDownload,
  Loader,
  rxMethod,
  withSelectedEntity,
} from '@dvb/util-angular';
import {
  addDaysToISODateString,
  fromISODateString,
  isNotNullish,
  isNullish,
  requireValue,
  toISODateString,
  tomorrow,
} from '@dvb/util-shared';
import { TranslocoService } from '@jsverse/transloco';
import { tapResponse } from '@ngrx/operators';
import { patchState, signalStore, withMethods, withState } from '@ngrx/signals';
import { setAllEntities, withEntities } from '@ngrx/signals/entities';
import { exhaustMap, pipe, switchMap } from 'rxjs';
import { BackendEntityId, ISODateString } from '@dvb/data-access-types';
import { isAfter } from 'date-fns';
import { withDevtools } from '@angular-architects/ngrx-toolkit';

type AnlassConfigState = {
  anlassConfigList: AnlassConfigJson[];
  totalRecords: number;
  minDatumVon: ISODateString;
};

const initialState: AnlassConfigState = {
  anlassConfigList: [],
  totalRecords: 0,
  minDatumVon: toISODateString(tomorrow()),
};

@Injectable({ providedIn: 'root' })
export class AnlassConfigStore extends signalStore(
  { protectedState: false },
  withDevtools('anlass-config'),
  withEntities<AnlassConfigJson>(),
  withState(initialState),
  withSelectedEntity<AnlassConfigJson>(),
  withMethods(store => ({
    calculateMinDatumVon(typ: AnlassTyp | undefined): void {
      if (isNullish(typ)) {
        return undefined;
      }

      const allOfType = store.entities().filter((el: AnlassConfigJson) => el.data.anlassTyp === typ);
      allOfType.sort((a, b) => {
        const aDatumBis = fromISODateString(a.data.datumBis);
        const bDatumBis = fromISODateString(b.data.datumBis);
        return aDatumBis.getTime() - bDatumBis.getTime();
      });
      const latestAnlassConfigForSameTyp = allOfType.pop();
      if (isNotNullish(latestAnlassConfigForSameTyp)) {
        // Datum muss in Zukunft sein UND nach dem Start des letzten Abschnittes
        if (isAfter(latestAnlassConfigForSameTyp.data.datumVon, tomorrow())) {
          const minStartDate = addDaysToISODateString(latestAnlassConfigForSameTyp.data.datumVon, 1);
          patchState(store, { minDatumVon: minStartDate });
        }
      }
    },
  })),
) {
  readonly #anlassConfigService = inject(AnlassConfigService);
  readonly #appErrorHandlerService = inject(AppErrorHandlerService);
  readonly #appNotificationStore = inject(AppNotificationStore);
  readonly #transloco = inject(TranslocoService);

  readonly loader = new Loader();
  readonly saveLoader = new Loader();

  loadAnlassConfigList = rxMethod<void>(
    pipe(
      switchMap(() =>
        this.loader.tapLoading$(this.#anlassConfigService.findAllAnlassConfigs$(), error =>
          this.#appErrorHandlerService.handleError(error),
        ),
      ),
      tapResponse({
        next: listJson => patchState(this, setAllEntities(listJson.anlassConfigList)),
        error: console.error,
      }),
    ),
  );

  createAnlassConfig = rxMethod<{
    anlassConfig: AnlassConfigSaveJson;
    onSuccess: () => void;
  }>(
    pipe(
      exhaustMap(({ anlassConfig, onSuccess }) =>
        this.saveLoader
          .tapLoading$(
            this.#anlassConfigService.createAnlassConfig$({
              anlassConfigSaveJson: anlassConfig,
            }),
            error => this.#appErrorHandlerService.handleError(error),
          )
          .pipe(
            tapResponse({
              next: result => {
                patchState(this, setAllEntities(result.anlassConfigList));
                this.#appNotificationStore.addSuccessNotification(this.#transloco.translate('global.data.saved'));
                onSuccess();
              },
              error: console.error,
            }),
          ),
      ),
    ),
  );

  updateAnlassConfig = rxMethod<{
    anlassConfig: AnlassConfigSaveJson;
    onSuccess: () => void;
  }>(
    pipe(
      exhaustMap(({ anlassConfig, onSuccess }) =>
        this.saveLoader
          .tapLoading$(
            this.#anlassConfigService.updateAnlassConfig$({
              anlassConfigId: requireValue(this.selectedEntity()?.id),
              anlassConfigSaveJson: anlassConfig,
            }),
            error => this.#appErrorHandlerService.handleError(error),
          )
          .pipe(
            tapResponse({
              next: result => {
                patchState(this, setAllEntities(result.anlassConfigList));
                this.#appNotificationStore.addSuccessNotification(this.#transloco.translate('global.data.saved'));
                onSuccess();
              },
              error: console.error,
            }),
          ),
      ),
    ),
  );

  downloadSkripts = rxMethod<{
    anlassConfigId: BackendEntityId;
  }>(
    pipe(
      exhaustMap(({ anlassConfigId }) =>
        this.saveLoader
          .tapLoading$(
            this.#anlassConfigService.downloadSqlSkripts$(
              {
                anlassConfigId: anlassConfigId,
              },
              'response',
            ),
            error => this.#appErrorHandlerService.handleError(error),
          )
          .pipe(
            tapResponse({
              next: result => {
                handleBlobResponseDownload(result, true);
              },
              error: console.error,
            }),
          ),
      ),
    ),
  );
}
