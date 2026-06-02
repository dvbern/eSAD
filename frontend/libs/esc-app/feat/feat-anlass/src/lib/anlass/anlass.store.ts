import {computed, inject, Injectable} from '@angular/core';
import {
  AnlassJson,
  AnlassSaveJson,
  AnlassService,
  AnlassStatus,
  BenutzerOptionJson,
  PrinterService,
  SchulInformationenJson,
} from '@dvb/data-access-backend';
import {BackendEntityId} from '@dvb/data-access-types';
import {AnlassJsonEnhanced, BenutzerStore, TenantSettingsStore} from '@dvb/feat-shared';
import {AppErrorHandlerService, handleBlobResponseDownload, Loader, withSelectedEntity} from '@dvb/util-angular';
import {
  hasAnlassCreateOrEditForNotAbgeschlossenBerechtigung,
  hasAnlassEditBerechtigungForAbgeschlossen,
  isNullish,
  requireValue,
} from '@dvb/util-shared';
import {TranslocoService} from '@jsverse/transloco';
import {tapResponse} from '@ngrx/operators';
import {patchState, signalStore, withComputed, withMethods, withState} from '@ngrx/signals';
import {addEntity, removeEntity, setAllEntities, setEntity, withEntities} from '@ngrx/signals/entities';
import {rxMethod} from '@ngrx/signals/rxjs-interop';
import {exhaustMap, pipe, switchMap} from 'rxjs';
import {enhanceAnlass} from './anlass.util';
import {withDevtools} from '@angular-architects/ngrx-toolkit';

type AnlassState = {
  schulInformationen: SchulInformationenJson[],
  prefilledAnlass: Partial<AnlassSaveJson> | undefined;
  prefilledKindId: BackendEntityId | undefined,
  totalRecords: number;
};

const initialState: AnlassState = {
  schulInformationen: [],
  prefilledAnlass: undefined,
  prefilledKindId: undefined,
  totalRecords: 0,
};

@Injectable()
export class AnlassStore extends signalStore(
  {protectedState: false},
  withDevtools('anlass'),
  withEntities<AnlassJsonEnhanced>(),
  withState(initialState),
  withSelectedEntity<AnlassJsonEnhanced>(),
  withComputed(({selectedEntity}) => {
    // Inject at store initialization
    const benutzerStore = inject(BenutzerStore);
    const tenantSettingsStore = inject(TenantSettingsStore);
    return {
      canUserEditAnlassInCurrentState: computed(() => {
        if (!tenantSettingsStore.zentraleOrganisationEnabled()) {
          if (!benutzerStore.isZustaendigForAnlass(selectedEntity())) {
            return false;
          }
        }
        const permissionCheck = selectedEntity()?.data?.anlassStatus === AnlassStatus.ABGESCHLOSSEN ?
          hasAnlassEditBerechtigungForAbgeschlossen() :
          hasAnlassCreateOrEditForNotAbgeschlossenBerechtigung();
        return benutzerStore.createHasRequiredRoleSig(permissionCheck)();
      }),
      isAbgeschlossen: computed(() => selectedEntity()?.data.anlassStatus === AnlassStatus.ABGESCHLOSSEN),
      arztOfCurrentAnlass: computed(() => {
        return selectedEntity()?.data.arzt;
      }),
      mpaOfCurrentAnlass: computed(() => {
        return selectedEntity()?.data.mpa;
      }),
      weiterePersonOfCurrentAnlass: computed(() => {
        return selectedEntity()?.data?.weiterePerson;
      }),
      groesseGewichtBmiPerzentileValues: computed(() => {
        return selectedEntity()?.anlassConfig.data.groesseGewichtBmiPerzentileValues ?? [];
      }),
      untersuchungEinbeinstandValues: computed(() => {
        return selectedEntity()?.anlassConfig.data.untersuchungEinbeinstandValues;
      }),
      untersuchungEinbeinhuepfenValues: computed(() => {
        return selectedEntity()?.anlassConfig.data.untersuchungEinbeinhuepfenValues;
      }),
      untersuchungMotorikValues: computed(() => {
        return selectedEntity()?.anlassConfig.data.untersuchungMotorikValues;
      }),
      untersuchungSprachfaehigkeitValues: computed(() => {
        return selectedEntity()?.anlassConfig.data.untersuchungSprachfaehigkeitValues;
      }),
      showFragebogen: computed(() => {
        return selectedEntity()?.anlassConfig.data.showFragebogen;
      }),
      showImpfausweis: computed(() => {
        return selectedEntity()?.anlassConfig.data.showImpfausweis;
      }),
      showAnmeldungBestaetigung: computed(() => {
        return selectedEntity()?.anlassConfig.data.showAnmeldungBestaetigung;
      }),
      showImpfeinverstaendnis: computed(() => {
        return selectedEntity()?.anlassConfig.data.showImpfeinverstaendnis;
      }),
      sehenBrilleLinsenValues: computed(() => {
        return selectedEntity()?.anlassConfig.data.sehenBrilleLinsenValues ?? [];
      }),
      isAnonymous: computed(() => {
        return selectedEntity()?.data.anlassConfigOption.anonym ?? false;
      }),
    };
  }),

  withMethods((store) => {
    const benutzerStore = inject(BenutzerStore);
    const tenantSettingsStore = inject(TenantSettingsStore);
    return {
      selectedEntityOrganisationId(): BackendEntityId | undefined {
        return store.selectedEntity()?.data.arzt?.organisationRollen?.organisation?.id;
      },
      showZustaendigerChangedWarning(newArzt: BenutzerOptionJson | undefined): boolean {
        if (tenantSettingsStore.zentraleOrganisationEnabled()) {
          // Bei zentraler Organisation nie warnen
          return false;
        }
        if (isNullish(this.selectedEntityOrganisationId())) {
          // Von null auf etwas -> nie warnen
          return false;
        }
        if (benutzerStore.hasFachrolleSupport()) {
          // Bei Support nie warnen
          return false;
        }
        return this.selectedEntityOrganisationId() !== newArzt?.organisationRollen?.organisation?.id;
      },
      prefillKind(kindIdOrUndefined: BackendEntityId | undefined): void {
        patchState(store, {
          prefilledKindId: kindIdOrUndefined,
        })
      },
      prefillAnlass(anlassPart: Partial<AnlassSaveJson> | undefined): void {
        patchState(store, {
          prefilledAnlass: anlassPart,
        });
      },
      resetPrefill(): void {
        patchState(store, {
          prefilledAnlass: undefined,
          prefilledKindId: undefined,
        });
      },
    };
  }),
) {
  readonly #anlassService = inject(AnlassService);
  readonly #printService = inject(PrinterService);
  readonly #appErrorHandlerService = inject(AppErrorHandlerService);
  readonly transloco = inject(TranslocoService);

  readonly loader = new Loader();
  readonly loadEntityLoader = new Loader();
  readonly klasseLehrpersonenLoader = new Loader();
  readonly saveLoader = new Loader();
  readonly deleteLoader = new Loader();

  loadAnlassList = rxMethod<void>(
    pipe(
      switchMap(() => this.loader.tapLoading$(this.#anlassService.findAllAnlaesseBerechtigt$(),
        error => this.#appErrorHandlerService.handleError(error))),
      tapResponse({
        next: listJson => patchState(this,
          setAllEntities(listJson.anlassList.map(each => enhanceAnlass(each, this.transloco)))),
        error: console.error,
      }),
    ),
  );

  loadEntity = rxMethod<BackendEntityId>(
    pipe(
      switchMap((entityId) => this.loadEntityLoader
        .tapLoading$(this.#anlassService
            .getAnlass$({anlassId: entityId}),
          error => this.#appErrorHandlerService.handleError(error),
        ),
      ),
      tapResponse({
        next: result => patchState(this, setEntity(enhanceAnlass(result, this.transloco))),
        error: console.error,
      }),
    ),
  );

  loadKlassenLehrpersonen = rxMethod<BackendEntityId>(
    pipe(
      switchMap((anlassId) => this.klasseLehrpersonenLoader
        .tapLoading$(this.#anlassService
            .getSchulInformationenForAnlass$({anlassId: anlassId}),
          error => this.#appErrorHandlerService.handleError(error),
        ),
      ),
      tapResponse({
        next: result => patchState(this, {schulInformationen: result.schulInformationenList}),
        error: console.error,
      }),
    ),
  );

  createAnlass = rxMethod<{ anlass: AnlassSaveJson, onSuccess: (result: AnlassJson) => void }>(
    pipe(
      exhaustMap(
        ({anlass, onSuccess}) => this.saveLoader.tapLoading$(
          this.#anlassService.createAnlass$({anlassSaveJson: anlass}),
          error => this.#appErrorHandlerService.handleError(error),
        ).pipe(
          tapResponse({
            next: result => {
              patchState(this, addEntity(enhanceAnlass(result, this.transloco)));
              onSuccess(result);
            },
            error: console.error,
          }),
        ),
      )),
  );

  changeAnlassStatus = rxMethod<{ anlassId: BackendEntityId, status: AnlassStatus, onSuccess: () => void }>(
    pipe(
      exhaustMap(({anlassId, status, onSuccess}) => this.saveLoader
        .tapLoading$(
          this.#anlassService.changeAnlassStatus$({
            anlassId: anlassId,
            status: status,
          }), error => this.#appErrorHandlerService.handleError(error))
        .pipe(
          tapResponse({
            next: result => {
              patchState(this, setEntity(enhanceAnlass(result, this.transloco)));
              onSuccess();
            },
            error: console.error,
          }),
        ),
      )),
  );

  downloadAnlassExportPrint = rxMethod<{ anlassId: BackendEntityId }>(
    pipe(
      exhaustMap(({anlassId}) =>
        this.loader.tapLoading$(
          this.#printService.downloadAnlassPrint$({anlassId}, 'response'),
          error => this.#appErrorHandlerService.handleError(error))),
      tapResponse({
        next: result => handleBlobResponseDownload(result, true),
        error: console.error,
      }),
    ),
  );

  updateAnlass = rxMethod<{ anlass: AnlassSaveJson, onSuccess: () => void }>(
    pipe(
      exhaustMap(({anlass, onSuccess}) => this.saveLoader
        .tapLoading$(
          this.#anlassService.updateAnlass$({
            anlassId: requireValue(this.selectedEntity()?.id),
            anlassSaveJson: anlass,
          }), error => this.#appErrorHandlerService.handleError(error))
        .pipe(
          tapResponse({
            next: result => {
              patchState(this, setEntity(enhanceAnlass(result, this.transloco)));
              onSuccess();
            },
            error: console.error,
          }),
        ),
      )),
  );

  removeAnlass = rxMethod<BackendEntityId>(
    pipe(
      exhaustMap(idToDelete =>
        this.deleteLoader.tapLoading$(
          this.#anlassService.removeAnlass$({anlassId: idToDelete}),
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
