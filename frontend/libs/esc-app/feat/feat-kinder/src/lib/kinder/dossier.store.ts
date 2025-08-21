import {withDevtools} from '@angular-architects/ngrx-toolkit';
import {computed, inject, Injectable} from '@angular/core';
import {
  DossierJson,
  DossierSaveJson,
  DossierService,
  EsadIdJson,
  KindanlaesseService,
  KinderdossierPrintVariant,
  KinderService,
  KindJson,
  KindSaveJson,
  KindSaveModeDuplikat,
  LastImpfungJson,
  PrinterService,
} from '@dvb/data-access-backend';
import {BackendEntityId} from '@dvb/data-access-types';
import {EventBusStore, getOptionWithSuffix} from '@dvb/feat-shared';
import {
  AppErrorHandlerService,
  handleBlobResponseDownload,
  Loader,
  withDetailModeState,
  withIsNewModeFeature,
} from '@dvb/util-angular';
import {isNullish, isNullishOrEmpty, requireValue} from '@dvb/util-shared';
import {TranslocoService} from '@jsverse/transloco';
import {tapResponse} from '@ngrx/operators';
import {
  EmptyFeatureResult,
  patchState,
  signalStore,
  SignalStoreFeature,
  withComputed,
  withMethods,
  withState,
} from '@ngrx/signals';
import {rxMethod} from '@ngrx/signals/rxjs-interop';
import {exhaustMap, pipe, switchMap, tap} from 'rxjs';

type DossierState = {
  kindId: BackendEntityId | undefined;
  selectedDossier: DossierJson | undefined;
  lastImpfung: LastImpfungJson | undefined
};

const initialState: DossierState = {
  kindId: undefined,
  selectedDossier: undefined,
  lastImpfung: undefined
};

@Injectable()
export class DossierStore extends signalStore(
  {protectedState: false},
  withDevtools('dossier') as SignalStoreFeature<EmptyFeatureResult, EmptyFeatureResult>,
  withState(initialState),
  withDetailModeState(),
  withIsNewModeFeature(),
  withComputed(({selectedDossier}) => ({
    hasZusatzinformationen: computed(() =>
      !!selectedDossier()?.data?.kinderschutzfall ||
      !!selectedDossier()?.data?.keineAuskunftAnDrittpersonen ||
      !!selectedDossier()?.data?.sansPapier ||
      !!selectedDossier()?.data?.andereZusatzinformation),
    hasNachkontrolle: computed(() =>
      !!selectedDossier()?.data?.nachkontrolle?.data?.anlassFlags.nachkontrolleHoeren
      || !!selectedDossier()?.data?.nachkontrolle?.data?.anlassFlags.nachkontrolleSehen
      || !!selectedDossier()?.data?.nachkontrolle?.data?.anlassFlags.nachkontrolleGroesseGewicht
      || !!selectedDossier()?.data?.nachkontrolle?.data?.anlassFlags.nachkontrolleSprache
      || !!selectedDossier()?.data?.nachkontrolle?.data?.anlassFlags.nachkontrolleMotorik
      || !!selectedDossier()?.data?.nachkontrolle?.data?.anlassFlags.nachkontrolleAnderes,
    ),
    nameKind: computed(() => (selectedDossier()
      ? selectedDossier()?.kind.data.vorname + ' ' + selectedDossier()?.kind.data.name
      : '')),
  })),
  withMethods((store) => ({
    reset(): void {
      patchState(store, initialState);
    }
  })),
) {
  readonly #dossierService = inject(DossierService);
  readonly #kinderService = inject(KinderService);
  readonly #printerService = inject(PrinterService);
  readonly #kindAnlaesseService = inject(KindanlaesseService);
  readonly #appErrorHandlerService = inject(AppErrorHandlerService);
  readonly #transloco = inject(TranslocoService);
  readonly #eventBusStore = inject(EventBusStore);

  readonly loader = new Loader();
  readonly saveLoader = new Loader();
  readonly esadIdLoader = new Loader();

  selectedKind = computed(() =>
     kindMarkInactiveSchuleAndKlasse(this.#transloco.translate('gloabl.suffixInaktiv'), this.selectedDossier()?.kind)
  );

  loadLastImpfung = rxMethod<BackendEntityId>(
    pipe(
      tap(() => patchState(this, {lastImpfung: undefined})),
      switchMap((kindId) => {
          return this.loader
            .tapLoading$(this.#kindAnlaesseService.getLatestImpfung$({ kindId }),
              error => this.#appErrorHandlerService.handleError(error),
            );
        },
      ),
      tapResponse({
        next: result => patchState(this, {lastImpfung: result}),
        error: console.error,
      }),
    ),
  );

  loadDossierForKind = rxMethod<BackendEntityId>(
    pipe(
      tap(() => patchState(this, {selectedDossier: undefined})),
      switchMap((entityId) => {
          patchState(this, {kindId: entityId});
          return this.loader
            .tapLoading$(this.#dossierService
                .getOrCreateDossier$({kindId: entityId}),
              error => this.#appErrorHandlerService.handleError(error),
            );
        },
      ),
      tapResponse({
        next: result => {
          patchState(this, {selectedDossier: result});
        },
        error: console.error,
      }),
    ),
  );

  updateKind = rxMethod<{
    kind: KindSaveJson;
    kindSaveMode: KindSaveModeDuplikat;
    onSuccess: () => void
  }>(
    pipe(
      exhaustMap(({kind, kindSaveMode, onSuccess}) => { return this.saveLoader
            .tapLoading$(
              this.#kinderService.updateKind$({
                kindId: requireValue(this.selectedDossier()?.kind.id),
                kindSaveJson: kind,
                kindSaveMode: kindSaveMode,
              }), error => this.#appErrorHandlerService.handleError(error),
            )
            .pipe(
              tapResponse({
                next: result => {
                  if (result.kindJson && isNullishOrEmpty(result.idsOfPotentialDuplicates)) {
                    this.patchStateKind(result.kindJson);
                    onSuccess();
                  } else {
                    this.#eventBusStore.emit('KIND_DUPLIKAT_VORHANDEN',
                      {kindTriedToSave: kind, kindIdsOfDuplicates: requireValue(result.idsOfPotentialDuplicates)});
                  }
                },
                error: console.error,
              }),
            );
        },
      ),
    ),
  );

  generateEsadIdForKind = rxMethod<BackendEntityId>(
    pipe(
      exhaustMap((kindId) => this.esadIdLoader
        .tapLoading$(this.#kinderService.generateEsadIdForKind$({kindId: kindId}), error =>
          this.#appErrorHandlerService.handleError(error),
        )
        .pipe(
          tapResponse({
            next: result => {
              this.patchStateKind(result);
            },
            error: console.error,
          }),
        ),
      ),
    ),
  );

  setEsadIdForKind = rxMethod<{ kindId: BackendEntityId, esadId: EsadIdJson }>(
    pipe(
      exhaustMap(({kindId, esadId}) => this.esadIdLoader
        .tapLoading$(this.#kinderService
            .setEsadIdForKind$({kindId, esadIdJson: esadId}),
          error => this.#appErrorHandlerService.handleError(error))
        .pipe(
          tapResponse({
            next: result => {
              this.patchStateKind(result);
            },
            error: console.error,
          }),
        ),
      ),
    ),
  );

  updateDossier = rxMethod<{ dossier: DossierSaveJson, onSuccess: () => void }>(
    pipe(
      exhaustMap(({dossier, onSuccess}) => this.saveLoader
        .tapLoading$(
          this.#dossierService.updateDossier$({
            dossierId: requireValue(this.selectedDossier()?.id),
            dossierSaveJson: dossier,
          }), error => this.#appErrorHandlerService.handleError(error))
        .pipe(
          tapResponse({
            next: result => {
              patchState(this, {selectedDossier: result});
              onSuccess();
            },
            error: console.error,
          }),
        ),
      ),
    ),
  );

  downloadKinderdossierReport = rxMethod<{ kindId: BackendEntityId, variation: KinderdossierPrintVariant }>(
    pipe(
      switchMap(({ variation, kindId }) =>
        this.loader.tapLoading$(
          this.#printerService.downloadKinderdossier$({ variation, kindId }, 'response'),
          error => this.#appErrorHandlerService.handleError(error))),
      tapResponse({
        next: result => handleBlobResponseDownload(result, true),
        error: console.error,
      }),
    ),
  );

  private patchStateKind(result: KindJson): void {
    const selectedDossier: DossierJson | undefined = this.selectedDossier();
    if (selectedDossier) {
      patchState(this, {
        selectedDossier: {
          ...selectedDossier,
          kind: result,
        },
      });
    } else {
      throw Error('selectedDossier is undefined');
    }
  }
}

function kindMarkInactiveSchuleAndKlasse(translatedSuffix: string, kind?: KindJson, ) {
  if(isNullish(kind)){
    return
  }
  return {
    ...kind,
    data: {
      ...kind.data,
      schule: getOptionWithSuffix(translatedSuffix, kind.data.schule),
      klasse: getOptionWithSuffix(translatedSuffix, kind.data.klasse),
    }

  };
}