import {withDevtools} from '@angular-architects/ngrx-toolkit';
import {computed, inject, Injectable} from '@angular/core';
import {
  BenutzerOptionJson,
  DossierJson,
  DossierMedizinischSaveJson,
  DossierService,
  EntityAktivStatus,
  EntityAktivStatusSaveJson,
  EsadIdJson,
  KindanlaesseService,
  KinderdossierPrintVariant,
  KinderService,
  KindJson,
  KindSaveJson,
  KindSaveModeDuplikat,
  LastImpfungJson,
  PapierakteStatus,
  PrinterService,
} from '@dvb/data-access-backend';
import {BackendEntityId} from '@dvb/data-access-types';
import {EventBusStore, getOptionWithSuffix} from '@dvb/feat-shared';
import {
  AppErrorHandlerService,
  AppNotificationStore,
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
      !!selectedDossier()?.dataMedizinisch?.kinderschutzfall ||
      !!selectedDossier()?.dataMedizinisch?.keineAuskunftAnDrittpersonen ||
      !!selectedDossier()?.dataMedizinisch?.sansPapier ||
      !!selectedDossier()?.dataMedizinisch?.andereZusatzinformation),
    hasNachkontrolle: computed(() =>
      !!selectedDossier()?.dataMedizinisch?.nachkontrolle?.data?.anlassFlags.nachkontrolleHoeren
      || !!selectedDossier()?.dataMedizinisch?.nachkontrolle?.data?.anlassFlags.nachkontrolleSehen
      || !!selectedDossier()?.dataMedizinisch?.nachkontrolle?.data?.anlassFlags.nachkontrolleGroesseGewicht
      || !!selectedDossier()?.dataMedizinisch?.nachkontrolle?.data?.anlassFlags.nachkontrolleSprache
      || !!selectedDossier()?.dataMedizinisch?.nachkontrolle?.data?.anlassFlags.nachkontrolleMotorik
      || !!selectedDossier()?.dataMedizinisch?.nachkontrolle?.data?.anlassFlags.nachkontrolleAnderes,
    ),
    nameKind: computed(() => (selectedDossier()
      ? selectedDossier()?.kind.data.vorname + ' ' + selectedDossier()?.kind.data.name
      : '')),
    isKindAktiv: computed(() => (
      selectedDossier()?.kind?.data?.kindAktivStatus === EntityAktivStatus.AKTIV
    )),
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
  readonly #appNotificationStore = inject(AppNotificationStore);
  readonly #transloco = inject(TranslocoService);
  readonly #eventBusStore = inject(EventBusStore);

  readonly loader = new Loader();
  readonly saveLoader = new Loader();
  readonly esadIdLoader = new Loader();
  readonly changeStatusLoader = new Loader();

  selectedKind = computed(() =>
    kindMarkInactiveSchuleAndKlasse(this.#transloco.translate('gloabl.suffixInaktiv'), this.selectedDossier()?.kind)
  );

  loadLastImpfung = rxMethod<BackendEntityId>(
    pipe(
      tap(() => patchState(this, {lastImpfung: undefined})),
      switchMap((kindId) => {
          return this.loader
            .tapLoading$(this.#kindAnlaesseService.getLatestImpfung$({kindId}),
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

  activateKind = rxMethod<{ kindId: BackendEntityId, saveJson: EntityAktivStatusSaveJson, onSuccess: () => void }>(
    pipe(
      exhaustMap(({kindId, saveJson, onSuccess}) =>
        this.changeStatusLoader
          .tapLoading$(this.#kinderService.updateKindAktivStatus$({
              kindId: kindId,
              entityAktivStatusSaveJson: saveJson,
            }),
            error =>
              this.#appErrorHandlerService.handleError(error),
          )
          .pipe(
            tapResponse({
              next: (kind: KindJson ) => {
                  this.patchStateKind(kind);
                  onSuccess();
              },
              error: console.error,
            }),
          ),
      ),
    ),
  );

  updateKind = rxMethod<{
    kind: KindSaveJson;
    kindSaveMode: KindSaveModeDuplikat;
    onSuccess: () => void
  }>(
    pipe(
      exhaustMap(({kind, kindSaveMode, onSuccess}) => {
          return this.saveLoader
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

  updateDossierMedizinisch = rxMethod<{ dossier: DossierMedizinischSaveJson, onSuccess: () => void }>(
    pipe(
      exhaustMap(({dossier, onSuccess}) => this.saveLoader
        .tapLoading$(
          this.#dossierService.updateDossierMedizinisch$({
            dossierId: requireValue(this.selectedDossier()?.id),
            dossierMedizinischSaveJson: dossier,
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

  updateDossierVerantwortlichArzt = rxMethod<{ dossier: DossierJson, arztOption: BenutzerOptionJson }>(
    pipe(
      exhaustMap(({dossier, arztOption}) => this.saveLoader
        .tapLoading$(
          this.#dossierService.updateDossierVerantwortlichArzt$({
            dossierId: dossier.id,
            benutzerOptionJson: arztOption,
          }), error => this.#appErrorHandlerService.handleError(error))
        .pipe(
          tapResponse({
            next: result => {
              patchState(this, {selectedDossier: result});
              this.#appNotificationStore.addSuccessNotification(this.#transloco.translate('global.data.saved'));
            },
            error: console.error,
          }),
        ),
      ),
    ),
  );

  updateDossierVerantwortlichMpa = rxMethod<{ dossier: DossierJson, mpaOption: BenutzerOptionJson }>(
    pipe(
      exhaustMap(({dossier, mpaOption}) => this.saveLoader
        .tapLoading$(
          this.#dossierService.updateDossierVerantwortlichMpa$({
            dossierId: dossier.id,
            benutzerOptionJson: mpaOption,
          }), error => this.#appErrorHandlerService.handleError(error))
        .pipe(
          tapResponse({
            next: result => {
              patchState(this, {selectedDossier: result});
              this.#appNotificationStore.addSuccessNotification(this.#transloco.translate('global.data.saved'));
            },
            error: console.error,
          }),
        ),
      ),
    ),
  );

  updateDossierPapierakteStatus = rxMethod<{ dossier: DossierJson, status: PapierakteStatus }>(
    pipe(
      exhaustMap(({dossier, status}) => this.saveLoader
        .tapLoading$(
          this.#dossierService.updateDossierPapierakteStatus$({
            dossierId: dossier.id,
            status: status,
          }), error => this.#appErrorHandlerService.handleError(error))
        .pipe(
          tapResponse({
            next: result => {
              patchState(this, {selectedDossier: result});
              this.#appNotificationStore.addSuccessNotification(this.#transloco.translate('global.data.saved'));
            },
            error: console.error,
          }),
        ),
      ),
    ),
  );

  downloadKinderdossierReport = rxMethod<{ kindId: BackendEntityId, variation: KinderdossierPrintVariant }>(
    pipe(
      switchMap(({variation, kindId}) =>
        this.loader.tapLoading$(
          this.#printerService.downloadKinderdossier$({variation, kindId}, 'response'),
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
      throw new Error('selectedDossier is undefined');
    }
  }
}

function kindMarkInactiveSchuleAndKlasse(translatedSuffix: string, kind?: KindJson,) {
  if (isNullish(kind)) {
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
