import {inject, Injectable} from '@angular/core';
import {BenutzerOptionJson, BenutzerService, FachRolle} from '@dvb/data-access-backend';
import {AppErrorHandlerService, Loader, withSelectedEntity} from '@dvb/util-angular';
import {isNotNullish, isNullish, Nullish} from '@dvb/util-shared';
import {patchState, signalStore, withComputed, withMethods} from '@ngrx/signals';
import {setAllEntities, withEntities} from '@ngrx/signals/entities';
import {rxMethod} from '@ngrx/signals/rxjs-interop';
import {pipe, switchMap, tap} from 'rxjs';
import {BenutzerStore} from "../benutzer/benutzer/benutzer.store";

export type BenutzerOptionType = 'FACHROLLEN' | 'ARZT' | 'HILFSPERSON' | 'VERWALTUNG';

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
  withComputed(() => ({
  })),
  withMethods((store, benutzerStore = inject(BenutzerStore)) => ({
    getOptionForBenutzer(id: string | undefined): BenutzerOptionJson | undefined {
      if (isNullish(id)) {
        return undefined;
      }
      return store.entities().find((el: BenutzerOptionJson) => el.id === id);
    },
    contains(entities: BenutzerOptionJson[], option: BenutzerOptionJson): boolean {
      return entities.filter(arzt => arzt.id === option.id).length > 0
    },
    benutzerFiltered(
      currentlySelectedOption: BenutzerOptionJson | Nullish,
      benutzerOptionType: BenutzerOptionType,
      onlyFromMyOrganisation: boolean
    ): BenutzerOptionJson[] {
      let result = store.entities();
      // Wenn gewuenscht, nur spezifische Rollen
      if (benutzerOptionType == 'ARZT') {
        result = result.filter(option => option.organisationRollen?.fachRollen?.includes(FachRolle.SCHULAERZTIN));
      } else if (benutzerOptionType == 'HILFSPERSON') {
        result = result.filter(option => option.organisationRollen?.fachRollen?.some(
          organisationsRolle => [FachRolle.MPA, FachRolle.PFP, FachRolle.SCHOOL_NURSE].includes(organisationsRolle)
        ));
      } else if (benutzerOptionType == 'VERWALTUNG') {
        result = result.filter(option =>
          option.organisationRollen?.fachRollen?.includes(FachRolle.ADMIN) ||
          option.systemRollen?.fachRollen?.some(
            systemRolle => [FachRolle.SUPPORT, FachRolle.SYSTEM_ADMIN].includes(systemRolle)
          )
        );
      } else if (benutzerOptionType == 'FACHROLLEN') {
        result = result.filter(option => !option.systemRollen?.fachRollen?.some(
          organisationsRolle => [FachRolle.SUPPORT, FachRolle.SYSTEM_ADMIN].includes(organisationsRolle)
        ));
      }
      // Wenn gewuenscht, nur die Elemente der eigenen Organisation zurueckgeben
      if (onlyFromMyOrganisation) {
        const myOrganisation = benutzerStore.currentOrganisation();
        // Sonderfall: weder Organisation gesetzt noch SUPPORT -> keine Berechtigung
        if (isNullish(myOrganisation) && !benutzerStore.hasFachrolleSupport()) {
          return [];
        }
        if (isNotNullish(myOrganisation)) {
          result = result.filter(entity => entity.organisationRollen?.organisation?.id === myOrganisation.id);
        }
      }
      // Sicherstellen, dass nach allen Filtern das currentlySelectedOption noch in der Liste ist
      if (isNullish(currentlySelectedOption) || this.contains(result, currentlySelectedOption)) {
        return result;
      }
      return [...result, currentlySelectedOption];
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
