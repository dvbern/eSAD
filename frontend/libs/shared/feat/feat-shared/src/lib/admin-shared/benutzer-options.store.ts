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
  withMethods(() => ({
    organisationsRollen(current: BenutzerOptionJson): FachRolle[] | Nullish {
      return current.organisationRollen?.fachRollen;
    },
    systemRollen(current: BenutzerOptionJson): FachRolle[] | Nullish {
      return current.systemRollen?.fachRollen;
    }
  })),
  withComputed(({entities, organisationsRollen, systemRollen}) => ({
    arztOptions: computed((): BenutzerOptionJson[] => {
      return entities().filter(benutzerOption => organisationsRollen(benutzerOption)?.includes(FachRolle.SCHULAERZTIN))
    }),
    hilfspersonOptions: computed((): BenutzerOptionJson[] => {
      return entities()
        .filter(benutzerOption => organisationsRollen(benutzerOption)?.some(
          organisationsRolle => [FachRolle.MPA, FachRolle.PFP, FachRolle.SCHOOL_NURSE].includes(organisationsRolle)
        ))
    }),
    supportOptions: computed((): BenutzerOptionJson[] => {
      return entities()
        .filter(benutzerOption => systemRollen(benutzerOption)?.includes(FachRolle.SUPPORT));
    }),
    supperuserOptions: computed((): BenutzerOptionJson[] => {
      return entities().filter(benutzerOption => organisationsRollen(benutzerOption)?.includes(FachRolle.SUPERUSER));
    }),
    adminuserOptions: computed((): BenutzerOptionJson[] => {
      return entities().filter(benutzerOption => organisationsRollen(benutzerOption)?.includes(FachRolle.ADMIN));
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
    hilfspersonOptionsIncluding(current: BenutzerOptionJson | Nullish): BenutzerOptionJson[] {
      if (isNullish(current) || this.contains(store.hilfspersonOptions(), current)) {
        return store.hilfspersonOptions();
      }
      return [...store.hilfspersonOptions(), current];
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
