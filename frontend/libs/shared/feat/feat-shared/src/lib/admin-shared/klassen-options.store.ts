import {computed, inject, Injectable} from '@angular/core';
import {
  EntityAktivStatus,
  KlasseOptionJson,
  SchulenService,
  SchulenServiceFindSchuleKlassenOptionsRequestParams,
} from '@dvb/data-access-backend';
import {AppErrorHandlerService, Loader} from '@dvb/util-angular';
import {isNullish, Nullish} from '@dvb/util-shared';
import {tapResponse} from '@ngrx/operators';
import {patchState, signalStore, withComputed, withMethods} from '@ngrx/signals';
import {removeAllEntities, setAllEntities, withEntities} from '@ngrx/signals/entities';
import {rxMethod} from '@ngrx/signals/rxjs-interop';
import {pipe, switchMap, tap} from 'rxjs';

@Injectable({providedIn: 'root'})
export class KlassenOptionsStore extends signalStore(
  {protectedState: false},
  // withDevtools('klasseOptions'),
  withEntities<KlasseOptionJson>(),
  withComputed(({entities}) => ({
    entitiesAktiv: computed(() => entities()
      .filter((el: KlasseOptionJson) => el.aktivStatus === EntityAktivStatus.AKTIV)),
  })),
  withMethods((store) => ({
    contains(entities: KlasseOptionJson[], option: KlasseOptionJson): boolean {
      return entities.some(klasse => klasse.id === option.id)
    },
    entitiesIncluding(current: KlasseOptionJson | Nullish): KlasseOptionJson[] {
      if (isNullish(current) || this.contains(store.entities(), current)) {
        return store.entities();
      }
      return [...store.entities(), current];
    },
  })),
) {
  readonly #schulenService = inject(SchulenService);
  readonly #appErrorHandlerService = inject(AppErrorHandlerService);
  readonly loader = new Loader();

  resetKlasseOptions(): void {
    patchState(this, removeAllEntities());
  }

  loadKlasseOptions = rxMethod<SchulenServiceFindSchuleKlassenOptionsRequestParams>(
    pipe(
      tap(() => patchState(this, removeAllEntities())),
      switchMap((params) =>
        this.loader.tapLoading$(
          this.#schulenService.findSchuleKlassenOptions$({
            schuleId: params.schuleId,
            onlyActive: params.onlyActive,
          }),
          error => this.#appErrorHandlerService.handleError(error),
        ),
      ),
      tapResponse({
        next: result => {
          patchState(this, setAllEntities(result.klassenList))
        },
        error: console.error,
      }),
    ),
  )
}
