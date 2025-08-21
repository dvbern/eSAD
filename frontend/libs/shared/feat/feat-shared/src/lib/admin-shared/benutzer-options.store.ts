import {computed, inject, Injectable} from '@angular/core';
import {BenutzerOptionJson, BenutzerService, FachRolle} from '@dvb/data-access-backend';
import {AppErrorHandlerService, Loader, withSelectedEntity} from '@dvb/util-angular';
import {isNullish, Nullish} from '@dvb/util-shared';
import {patchState, signalStore, withComputed, withMethods} from '@ngrx/signals';
import {setAllEntities, withEntities} from '@ngrx/signals/entities';
import {rxMethod} from '@ngrx/signals/rxjs-interop';
import {pipe, switchMap, tap} from 'rxjs';

@Injectable({providedIn: 'root'})
export class BenutzerOptionsStore extends signalStore(
  {protectedState: false},
  withEntities<BenutzerOptionJson>(),
  withSelectedEntity<BenutzerOptionJson>(),
  withComputed(({entities}) => ({
    arztOptions: computed((): BenutzerOptionJson[] => {
      return entities().filter(benutzerOption => benutzerOption.fachrollen.includes(FachRolle.SCHULAERZTIN))
    }),
    mpaOptions: computed((): BenutzerOptionJson[] => {
      return entities().filter(benutzerOption => benutzerOption.fachrollen.includes(FachRolle.MPA))
    }),
    supportOptions: computed((): BenutzerOptionJson[] => {
      return entities().filter(benutzerOption => benutzerOption.fachrollen.includes(FachRolle.SUPPORT));
    }),
    supperuserOptions: computed((): BenutzerOptionJson[] => {
      return entities().filter(benutzerOption => benutzerOption.fachrollen.includes(FachRolle.SUPERUSER));
    }),
    adminuserOptions: computed((): BenutzerOptionJson[] => {
      return entities().filter(benutzerOption => benutzerOption.fachrollen.includes(FachRolle.ADMIN));
    }),
  })),
  withMethods((store) => ({
    getOptionForBenutzer(id: string | undefined): BenutzerOptionJson | undefined {
      if (isNullish(id)) {
        return undefined;
      }
      return store.entities().find((el: BenutzerOptionJson) => el.id === id);
    },
    contains(entities: BenutzerOptionJson[], option: BenutzerOptionJson): boolean {
      return entities.filter(arzt => arzt.id === option.id).length > 0
    },
    arztOptionsIncluding(current: BenutzerOptionJson | Nullish): BenutzerOptionJson[] {
      if (isNullish(current) || this.contains(store.arztOptions(), current)) {
        return store.arztOptions();
      }
      return [...store.arztOptions(), current];
    },
    mpaOptionsIncluding(current: BenutzerOptionJson | Nullish): BenutzerOptionJson[] {
      if (isNullish(current) || this.contains(store.mpaOptions(), current)) {
        return store.mpaOptions();
      }
      return [...store.mpaOptions(), current];
    },
    entitiesIncluding(current: BenutzerOptionJson | Nullish): BenutzerOptionJson[] {
      if (isNullish(current) || this.contains(store.entities(), current)) {
        return store.entities();
      }
      return [...store.entities(), current];
    },
  })),
) {
  readonly loader = new Loader();

  readonly #benutzerService = inject(BenutzerService);
  readonly #appErrorHandlerService = inject(AppErrorHandlerService);
  loadAllBenutzerOptions = rxMethod<void>(
    pipe(
      switchMap(() => this.loader.tapLoading$(this.#benutzerService.findAllActiveBenutzerOptions$(),
        error => this.#appErrorHandlerService.handleError(error))),
      tap({
        next: benutzerOptions => patchState(this, setAllEntities(benutzerOptions.benutzerList)),
        error: console.error,
      }),
    ),
  );
}
