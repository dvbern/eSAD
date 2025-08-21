import {withDevtools} from '@angular-architects/ngrx-toolkit';
import {computed, inject, Injectable} from '@angular/core';
import {
  EntityAktivStatus,
  SchulenService,
  SchulenServiceFindAllOptionsRequestParams,
  SchuleOptionJson,
} from '@dvb/data-access-backend';
import {AppErrorHandlerService, Loader} from '@dvb/util-angular';
import {isNullish, Nullish} from '@dvb/util-shared';
import {TranslocoService} from '@jsverse/transloco';
import {tapResponse} from '@ngrx/operators';
import {
  EmptyFeatureResult,
  patchState,
  signalStore,
  SignalStoreFeature,
  withComputed,
  withMethods,
} from '@ngrx/signals';
import {removeAllEntities, setAllEntities, withEntities} from '@ngrx/signals/entities';
import {rxMethod} from '@ngrx/signals/rxjs-interop';
import {pipe, switchMap, tap} from 'rxjs';
import {getOptionWithSuffix} from './inactive-options.util';

@Injectable({providedIn: 'root'})
export class SchulenOptionsStore extends signalStore(
  {protectedState: false},
  withDevtools('schule-options') as SignalStoreFeature<EmptyFeatureResult, EmptyFeatureResult>,
  withEntities<SchuleOptionJson>(),
  withComputed(({entities}) => ({
    entitiesAktiv: computed(() => entities()
      .filter((el: SchuleOptionJson) => el.aktivStatus === EntityAktivStatus.AKTIV)),
  })),
  withMethods((store) => ({
    contains(entities: SchuleOptionJson[], option: SchuleOptionJson): boolean {
      return entities.some(schule => schule.id === option.id)
    },
    entitiesIncluding(current: SchuleOptionJson | Nullish): SchuleOptionJson[] {
      if (isNullish(current) || this.contains(store.entities(), current)) {
        return store.entities();
      }
      return [...store.entities(), current];
    },
    entitiesActiveIncluding(current: SchuleOptionJson | Nullish): SchuleOptionJson[] {
      if (isNullish(current) || this.contains(store.entitiesAktiv(), current)) {
        return store.entitiesAktiv();
      }
      return [...store.entitiesAktiv(), current];
    },
  }))
) {
  readonly loader = new Loader();
  readonly #schulenService = inject(SchulenService);
  readonly #appErrorHandlerService = inject(AppErrorHandlerService);
  readonly #transloco = inject(TranslocoService);

  entitiesMarkedInactive = computed(() =>
    this.entities().map(entity => getOptionWithSuffix(this.#transloco.translate('gloabl.suffixInaktiv'), entity))
  );

  loadSchulenOptions = rxMethod<SchulenServiceFindAllOptionsRequestParams>(
    pipe(
      tap(() => patchState(this, removeAllEntities())),
      switchMap(params =>
        this.loader.tapLoading$(
          this.#schulenService.findAllOptions$({
            onlyActive: params.onlyActive,
          }),
          error => this.#appErrorHandlerService.handleError(error),
        ),
      ),
      tapResponse({
        next: result => patchState(this, setAllEntities(result.schulenList)),
        error: console.error,
      }),
    ),
  );
}
