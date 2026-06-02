import {withDevtools} from '@angular-architects/ngrx-toolkit';
import {computed, inject, Injectable} from '@angular/core';
import {EntityAktivStatus, OrganisationJson, SchulenService, SchuleOptionJson,} from '@dvb/data-access-backend';
import {AppErrorHandlerService, Loader} from '@dvb/util-angular';
import {isNotNullish, isNullish, Nullish} from '@dvb/util-shared';
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
import {BenutzerStore} from "../benutzer/benutzer/benutzer.store";
import {TenantSettingsStore} from "../tenant/tenant-settings.store";

@Injectable({providedIn: 'root'})
export class SchulenOptionsStore extends signalStore(
  {protectedState: false},
  withDevtools('schule-options') as SignalStoreFeature<EmptyFeatureResult, EmptyFeatureResult>,
  withEntities<SchuleOptionJson>(),
  withComputed(({entities}) => ({
    entitiesAktiv: computed(() => entities()
      .filter((el: SchuleOptionJson) => el.aktivStatus === EntityAktivStatus.AKTIV)),
  })),
  withMethods((store, benutzerStore = inject(BenutzerStore), tenantStore = inject(TenantSettingsStore), transloco = inject(TranslocoService)) => ({
    schulenFiltered(
      currentlySelectedOption: SchuleOptionJson | Nullish,
      onlyActive: boolean,
      onlyFromMyOrganisation: boolean
    ): SchuleOptionJson[] {
      return getFilteredSchulenOptions(
        store.entities(),
        currentlySelectedOption,
        onlyActive,
        onlyFromMyOrganisation,
        tenantStore.zentraleOrganisationEnabled(),
        benutzerStore.currentOrganisation(),
        benutzerStore.hasFachrolleSupport(),
        transloco.translate('gloabl.suffixInaktiv'),
      );
    },
  }))
) {
  readonly loader = new Loader();
  readonly #schulenService = inject(SchulenService);
  readonly #appErrorHandlerService = inject(AppErrorHandlerService);

  loadSchulenOptions = rxMethod<void>(
    pipe(
      tap(() => patchState(this, removeAllEntities())),
      switchMap(_ =>
        this.loader.tapLoading$(
          this.#schulenService.findAllOptions$(),
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

export function getFilteredSchulenOptions(
  entities: SchuleOptionJson[],
  currentlySelectedOption: SchuleOptionJson | Nullish,
  onlyActive: boolean,
  onlyFromMyOrganisation: boolean,
  zentraleOrganisationEnabled: boolean | undefined,
  currentOrganisation: OrganisationJson | Nullish,
  hasFachrolleSupport: boolean,
  inaktivSuffix: string,
): SchuleOptionJson[] {
  let result = entities;
  // Wenn gewuenscht, nur aktive Elemente
  if (onlyActive) {
    result = result.filter((el: SchuleOptionJson) => el.aktivStatus === EntityAktivStatus.AKTIV);
  } else {
    result = result.map(entity => getOptionWithSuffix(inaktivSuffix, entity))
  }
  // Wenn gewuenscht, nur die Elemente der eigenen Organisation zurueckgeben
  if (!zentraleOrganisationEnabled && onlyFromMyOrganisation) {
    // Sonderfall: weder Organisation gesetzt noch SUPPORT -> keine Berechtigung
    if (isNullish(currentOrganisation) && !hasFachrolleSupport) {
      return [];
    }
    // having no organisation implies having a system rolle -> unfiltered for support
    if (isNotNullish(currentOrganisation)) {
      result = result.filter(entity => entity.organisationId === currentOrganisation.id);
    }
  }
  // Sicherstellen, dass nach allen Filtern das currentlySelectedOption noch in der Liste ist
  if (isNullish(currentlySelectedOption) || contains(result, currentlySelectedOption)) {
    return result;
  }
  return [...result, currentlySelectedOption];
}

function contains(entities: SchuleOptionJson[], option: SchuleOptionJson): boolean {
  return entities.some(schule => schule.id === option.id);
}
