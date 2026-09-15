import {withDevtools} from '@angular-architects/ngrx-toolkit';
import {inject, Injectable} from '@angular/core';
import {
  AnlassService,
  AnlassTyp,
  AnlassWithDateTimeJson,
  AnlassWithOpenSlotsListJson,
  BenutzerOptionJson,
  EinladungenService,
  UntersuchungImpfstatusListSaveJson,
  UntersuchungImpfungenChecklisteSaveJson,
  UntersuchungImpfungenDataMinimalJson,
  UntersuchungImpfungenDataMinimalSaveJson,
  UntersuchungImpfungenDoppelkontrolleSaveJson,
  UntersuchungImpfungenImpfausweisSaveJson,
  UntersuchungImpfungenJson,
  UntersuchungImpfungenMassnahmenJson,
  UntersuchungImpfungenMassnahmenSaveJson,
  UntersuchungImpfungenNurUnvollstaendigeJson,
  UntersuchungImpfungenNurUnvollstaendigeSaveJson,
  UntersuchungImpfungenSaveJson,
  UntersuchungImpfungenService,
  UntersuchungImpfungInternJson,
  UntersuchungImpfungInternSaveJson,
} from '@dvb/data-access-backend';
import {BackendEntityId, BackendEntityIdWithClass, requireValidBackendEntityId} from '@dvb/data-access-types';
import {AppErrorHandlerService, AppNotificationStore, Loader} from '@dvb/util-angular';
import {ensurePropsFlat, requireValue} from '@dvb/util-shared';
import {TranslocoService} from '@jsverse/transloco';
import {tapResponse} from '@ngrx/operators';
import {EmptyFeatureResult, patchState, signalStore, SignalStoreFeature, withMethods, withState} from '@ngrx/signals';
import {rxMethod} from '@ngrx/signals/rxjs-interop';
import {exhaustMap, filter, map, pipe, switchMap, take, tap} from 'rxjs';
import {UntersuchungStore} from '../untersuchung.store';
import {toObservable} from '@angular/core/rxjs-interop';
import {EventBusStore} from "@dvb/feat-shared";

type UntersuchungState = {
  dataMinimal: UntersuchungImpfungenDataMinimalJson | undefined;
  nurUnvollstaendige: UntersuchungImpfungenNurUnvollstaendigeJson | undefined;
  impfausweis: UntersuchungImpfungenImpfausweisSaveJson | undefined;
  checklist: UntersuchungImpfungenChecklisteSaveJson | undefined;
  doppelkontrolle: UntersuchungImpfungenDoppelkontrolleSaveJson | undefined;
  versionUntersuchungImpfungen: number | undefined;
  massnahmen: UntersuchungImpfungenMassnahmenJson | undefined;
  impfstatusList: UntersuchungImpfstatusListSaveJson | undefined;
  impfungenInternList: UntersuchungImpfungInternJson[];
  imfanlaesseScheduled: AnlassWithDateTimeJson[];
  impffolgetermine: AnlassWithOpenSlotsListJson;
};

const initialState: UntersuchungState = {
  dataMinimal: undefined,
  nurUnvollstaendige: undefined,
  impfausweis: undefined,
  checklist: undefined,
  doppelkontrolle: undefined,
  versionUntersuchungImpfungen: undefined,
  massnahmen: undefined,
  impfstatusList: undefined,
  impfungenInternList: [],
  imfanlaesseScheduled: [],
  impffolgetermine: {
    maximalAnzahlSlots: 0,
    anlaesseWithOpenEinladungenSlots: [],
  },
};

@Injectable()
export class UntersuchungImpfungenStore extends signalStore(
  { protectedState: false },
  withDevtools('untersuchungImpfung') as SignalStoreFeature<EmptyFeatureResult, EmptyFeatureResult>,
  withState(initialState),
  withMethods(store => ({
    prepareUntersuchungSaveJson(): UntersuchungImpfungenSaveJson {
      return ensurePropsFlat<UntersuchungImpfungenSaveJson>({
        impfausweis: requireValue(store.impfausweis()),
        checkliste: requireValue(store.checklist()),
        doppelkontrolle: requireValue(store.doppelkontrolle()),
        version: requireValue(store.versionUntersuchungImpfungen()),
      });
    },
    patchImpfausweis(data: UntersuchungImpfungenJson): void {
      patchState(store, {
        impfausweis: data.data.impfausweis,
        versionUntersuchungImpfungen: data.data.version,
      });
    },
    patchChecklist(data: UntersuchungImpfungenJson): void {
      patchState(store, {
        checklist: data.data.checkliste,
        versionUntersuchungImpfungen: data.data.version,
      });
    },
    patchDoppelkontrolle(data: UntersuchungImpfungenJson): void {
      patchState(store, {
        doppelkontrolle: data.data.doppelkontrolle,
        versionUntersuchungImpfungen: data.data.version,
      });
    },
  })),
) {
  readonly #impfungenService = inject(UntersuchungImpfungenService);
  readonly #anlassService = inject(AnlassService);
  readonly #einladungenService = inject(EinladungenService);
  readonly #appErrorHandlerService = inject(AppErrorHandlerService);
  readonly #untersuchungStore = inject(UntersuchungStore);
  readonly #eventBusStore = inject(EventBusStore);

  readonly #appNotificationStore = inject(AppNotificationStore);
  readonly #transloco = inject(TranslocoService);

  readonly loader = new Loader();
  readonly saveImpfungenLoader = new Loader();
  readonly saveMassnahmenLoader = new Loader();
  readonly saveDataMinimalLoader = new Loader();
  readonly saveNurUnvollstaendigeLoader = new Loader();
  readonly impffolgetermineLoader = new Loader();

  loadUntersuchungImpfungenData = rxMethod<{ anlassId: BackendEntityId; einladungId: BackendEntityIdWithClass }>(
    pipe(
      tap(() => patchState(this, initialState)),
      switchMap(payload =>
        this.loader.tapLoading$(
          this.#impfungenService.getUntersuchungImpfungenData$({
            anlassId: payload.anlassId,
            einladungId: payload.einladungId,
          }),
          error => this.#appErrorHandlerService.handleError(error),
        ),
      ),
      tapResponse({
        next: result => {
          patchState(this, {
            dataMinimal: result.dataMinimal,
            nurUnvollstaendige: result.nurUnvollstaendige,
            impfausweis: result.untersuchungImpfungen.data.impfausweis,
            checklist: result.untersuchungImpfungen.data.checkliste,
            doppelkontrolle: result.untersuchungImpfungen.data.doppelkontrolle,
            versionUntersuchungImpfungen: result.untersuchungImpfungen.data.version,
            massnahmen: result.massnahmen,
            impfstatusList: result.impfstatusList,
            impfungenInternList: result.impfungInternList.impfungenInternList,
          });
        },
        error: console.error,
      }),
    ),
  );

  changeImpfausweisUnvollstaendig = rxMethod<{
    anlassId: BackendEntityId;
    einladungId: BackendEntityIdWithClass;
    impfausweisUnvollstaendig: boolean;
  }>(
    pipe(
      exhaustMap(payload =>
        this.saveImpfungenLoader.tapLoading$(
          this.#impfungenService
            .saveUntersuchungImpfungen$({
              anlassId: payload.anlassId,
              einladungId: payload.einladungId,
              untersuchungImpfungenSaveJson: {
                ...this.prepareUntersuchungSaveJson(),
                impfausweis: {
                  ...requireValue(this.impfausweis()),
                  impfausweisUnvollstaendig: payload.impfausweisUnvollstaendig,
                  impfausweisStatusMutiert: true,
                },
              },
            })
            .pipe(map(impfungen => ({ result: impfungen, params: payload }))),
          error => this.#appErrorHandlerService.handleError(error),
        ),
      ),
      tapResponse({
        next: ({ params, result }) => {
          this.patchImpfausweis(result);
          // we have to reload checkmark state
          this.#untersuchungStore.loadTabsSaved({
            anlassId: params.anlassId,
            einladungId: params.einladungId,
          });
          this.#appNotificationStore.addSuccessNotification(
            this.#transloco.translate('untersuchung.impfungen.impfausweisUnvollstaendig.changed'),
          );
        },
        error: console.error,
      }),
    ),
  );

  changeImpfausweisNichtVorhanden = rxMethod<{
    anlassId: BackendEntityId;
    einladungId: BackendEntityIdWithClass;
    impfausweisNichtVorhanden: boolean;
  }>(
    pipe(
      exhaustMap(payload =>
        this.saveImpfungenLoader.tapLoading$(
          this.#impfungenService
            .saveUntersuchungImpfungen$({
              anlassId: payload.anlassId,
              einladungId: payload.einladungId,
              untersuchungImpfungenSaveJson: {
                ...this.prepareUntersuchungSaveJson(),
                impfausweis: {
                  ...requireValue(this.impfausweis()),
                  impfausweisNichtVorhanden: payload.impfausweisNichtVorhanden,
                  impfausweisStatusMutiert: true,
                },
              },
            })
            .pipe(map(impfungen => ({ result: impfungen, params: payload }))),
          error => this.#appErrorHandlerService.handleError(error),
        ),
      ),
      tapResponse({
        next: ({ params, result }) => {
          this.patchImpfausweis(result);
          // we have to reload checkmark state
          this.#untersuchungStore.loadTabsSaved({
            anlassId: params.anlassId,
            einladungId: params.einladungId,
          });
          this.#appNotificationStore.addSuccessNotification(
            this.#transloco.translate('untersuchung.impfungen.impfausweisNichtVorhanden.changed'),
          );
        },
        error: console.error,
      }),
    ),
  );

  changeImpfausweisZweiPlusEinsSchema = rxMethod<{
    anlassId: BackendEntityId;
    einladungId: BackendEntityIdWithClass;
    impfausweisZweiPlusEinsSchema: boolean;
  }>(
    pipe(
      exhaustMap(payload =>
        this.saveImpfungenLoader.tapLoading$(
          this.#impfungenService
            .saveUntersuchungImpfungen$({
              anlassId: payload.anlassId,
              einladungId: payload.einladungId,
              untersuchungImpfungenSaveJson: {
                ...this.prepareUntersuchungSaveJson(),
                impfausweis: {
                  ...requireValue(this.impfausweis()),
                  impfausweisZweiPlusEinsSchema: payload.impfausweisZweiPlusEinsSchema,
                  impfausweisStatusMutiert: true,
                },
              },
            })
            .pipe(map(impfungen => ({ result: impfungen, params: payload }))),
          error => this.#appErrorHandlerService.handleError(error),
        ),
      ),
      tapResponse({
        next: ({ params, result }) => {
          this.patchImpfausweis(result);
          // we have to reload checkmark state
          this.#untersuchungStore.loadTabsSaved({
            anlassId: params.anlassId,
            einladungId: params.einladungId,
          });
          this.#appNotificationStore.addSuccessNotification(
            this.#transloco.translate('untersuchung.impfungen.impfausweisZweiPlusEinsSchema.changed'),
          );
        },
        error: console.error,
      }),
    ),
  );

  saveDataMinimal = rxMethod<{
    anlassId: BackendEntityId;
    einladungId: BackendEntityIdWithClass;
    untersuchungImpfungenDataMinimalSaveJson: UntersuchungImpfungenDataMinimalSaveJson;
  }>(
    pipe(
      exhaustMap(params =>
        this.saveDataMinimalLoader.tapLoading$(
          this.#impfungenService
            .saveUntersuchungImpfungenDataMinimal$({
              anlassId: params.anlassId,
              einladungId: params.einladungId,
              untersuchungImpfungenDataMinimalSaveJson: params.untersuchungImpfungenDataMinimalSaveJson,
            })
            .pipe(map(dataMinimal => ({ dataMinimal, params }))),
          error => this.#appErrorHandlerService.handleError(error),
        ),
      ),
      tapResponse({
        next: ({ dataMinimal, params }) => {
          patchState(this, { dataMinimal: dataMinimal });
          // have to reload checkmark state and whole impfungData for version number
          this.#untersuchungStore.loadTabsSaved({
            anlassId: params.anlassId,
            einladungId: params.einladungId,
          });
          this.loadUntersuchungImpfungenData({ anlassId: params.anlassId, einladungId: params.einladungId });
          this.#appNotificationStore.addSuccessNotification(this.#transloco.translate('global.data.saved'));
        },
        error: console.error,
      }),
    ),
  );

  saveNurUnvollstaendige = rxMethod<{
    anlassId: BackendEntityId;
    einladungId: BackendEntityIdWithClass;
    untersuchungImpfungenNurUnvollstaendigeSaveJson: UntersuchungImpfungenNurUnvollstaendigeSaveJson;
  }>(
    pipe(
      exhaustMap(params =>
        this.saveNurUnvollstaendigeLoader.tapLoading$(
          this.#impfungenService
            .saveUntersuchungImpfungenNurUnvollstaendige$({
              anlassId: params.anlassId,
              einladungId: params.einladungId,
              untersuchungImpfungenNurUnvollstaendigeSaveJson: params.untersuchungImpfungenNurUnvollstaendigeSaveJson,
            })
            .pipe(map(nurUnvollstaendige => ({ nurUnvollstaendige, params }))),
          error => this.#appErrorHandlerService.handleError(error),
        ),
      ),
      tapResponse({
        next: ({ nurUnvollstaendige, params }) => {
          patchState(this, { nurUnvollstaendige: nurUnvollstaendige });
          // have to reload checkmark state and whole impfungData for version number
          this.#untersuchungStore.loadTabsSaved({
            anlassId: params.anlassId,
            einladungId: params.einladungId,
          });
          this.loadUntersuchungImpfungenData({ anlassId: params.anlassId, einladungId: params.einladungId });
          this.#appNotificationStore.addSuccessNotification(this.#transloco.translate('global.data.saved'));
        },
        error: console.error,
      }),
    ),
  );

  visierenDoppelkontrolleVisum1 = rxMethod<{ anlassId: BackendEntityId; einladungId: BackendEntityId }>(
    pipe(
      exhaustMap(payload =>
        this.saveImpfungenLoader.tapLoading$(
          this.#impfungenService.visierenDoppelkontrolleVisum1$({
            anlassId: payload.anlassId,
            einladungId: payload.einladungId,
            untersuchungImpfungenSaveJson: {
              ...this.prepareUntersuchungSaveJson(),
            },
          }),
          error => this.#appErrorHandlerService.handleError(error),
        ),
      ),
      tapResponse({
        next: result => {
          this.patchDoppelkontrolle(result);
        },
        error: console.error,
      }),
    ),
  );

  anfordernDoppelkontrolleVisum2 = rxMethod<{
    anlassId: BackendEntityId;
    einladungId: BackendEntityId;
    user: BenutzerOptionJson;
  }>(
    pipe(
      exhaustMap(payload =>
        this.saveImpfungenLoader.tapLoading$(
          this.#impfungenService.anfordernDoppelkontrolleVisum2$({
            anlassId: payload.anlassId,
            einladungId: payload.einladungId,
            untersuchungImpfungenSaveJson: {
              ...this.prepareUntersuchungSaveJson(),
              doppelkontrolle: {
                ...requireValue(this.doppelkontrolle()),
                visumPerson2: payload.user,
              },
            },
          }),
          error => this.#appErrorHandlerService.handleError(error),
        ),
      ),
      tapResponse({
        next: result => {
          this.patchDoppelkontrolle(result);
        },
        error: console.error,
      }),
    ),
  );

  visierenDoppelkontrolleVisum2 = rxMethod<{ anlassId: BackendEntityId; einladungId: BackendEntityId }>(
    pipe(
      exhaustMap(payload =>
        this.saveImpfungenLoader.tapLoading$(
          this.#impfungenService.visierenDoppelkontrolleVisum2$({
            anlassId: payload.anlassId,
            einladungId: payload.einladungId,
            untersuchungImpfungenSaveJson: {
              ...this.prepareUntersuchungSaveJson(),
            },
          }),
          error => this.#appErrorHandlerService.handleError(error),
        ),
      ),
      tapResponse({
        next: result => {
          this.patchDoppelkontrolle(result);
        },
        error: console.error,
      }),
    ),
  );

  resetDoppelkontrolle = rxMethod<{ anlassId: BackendEntityId; einladungId: BackendEntityId }>(
    pipe(
      exhaustMap(payload =>
        this.saveImpfungenLoader.tapLoading$(
          this.#impfungenService.resetDoppelkontrolle$({
            anlassId: payload.anlassId,
            einladungId: payload.einladungId,
            untersuchungImpfungenSaveJson: {
              ...this.prepareUntersuchungSaveJson(),
            },
          }),
          error => this.#appErrorHandlerService.handleError(error),
        ),
      ),
      tapResponse({
        next: result => {
          this.patchDoppelkontrolle(result);
        },
        error: console.error,
      }),
    ),
  );

  saveMassnahmen = rxMethod<{
    anlassId: BackendEntityId;
    einladungId: BackendEntityIdWithClass;
    untersuchungImpfungenMassnahmenSaveJson: UntersuchungImpfungenMassnahmenSaveJson;
  }>(
    pipe(
      exhaustMap(params =>
        this.saveMassnahmenLoader.tapLoading$(
          this.#impfungenService.saveUntersuchungMassnahmen$(params).pipe(map(massnahmen => ({ massnahmen, params }))),
          error => this.#appErrorHandlerService.handleError(error),
        ),
      ),
      tapResponse({
        next: ({ massnahmen, params }) => {
          patchState(this, { massnahmen: massnahmen });
          this.#untersuchungStore.loadTabsSaved({
            anlassId: params.anlassId,
            einladungId: params.einladungId,
          });
          this.loadUntersuchungImpfungenData({ anlassId: params.anlassId, einladungId: params.einladungId });
          this.#appNotificationStore.addSuccessNotification(this.#transloco.translate('global.data.saved'));
        },
        error: console.error,
      }),
    ),
  );

  einzelTerminErstellen = rxMethod<{
    anlassId: BackendEntityId;
    einladungId: BackendEntityId;
    onSuccess: () => void;
  }>(
    pipe(
      exhaustMap(params =>
        this.saveMassnahmenLoader
          .tapLoading$(
            this.#impfungenService.einzelTerminErstellen$({
              anlassId: params.anlassId,
              einladungId: params.einladungId,
            }),
            error => this.#appErrorHandlerService.handleError(error),
          )
          .pipe(
            tapResponse({
              next: _ => {
                params.onSuccess();
              },
              error: console.error,
            }),
          ),
      ),
    ),
  );

  saveUntersuchungImpfstatusList = rxMethod<{
    anlassId: BackendEntityId;
    einladungId: BackendEntityIdWithClass;
    impfstatusList: UntersuchungImpfstatusListSaveJson;
  }>(
    pipe(
      exhaustMap(payload =>
        this.saveImpfungenLoader.tapLoading$(
          this.#impfungenService
            .saveUntersuchungImpfungenImpfstatus$({
              anlassId: payload.anlassId,
              einladungId: payload.einladungId,
              untersuchungImpfstatusListSaveJson: payload.impfstatusList,
            })
            .pipe(map(result => ({ result, payload }))),
          error => this.#appErrorHandlerService.handleError(error),
        ),
      ),
      tapResponse({
        next: ({ result, payload }) => {
          patchState(this, { impfstatusList: result });
          this.#untersuchungStore.loadTabsSaved({
            anlassId: payload.anlassId,
            einladungId: payload.einladungId,
          });
          this.#appNotificationStore.addSuccessNotification(
            this.#transloco.translate('untersuchung.impfungen.imfstatusList.saved'),
          );
        },
        error: console.error,
      }),
    ),
  );

  createImpfungIntern = rxMethod<{
    anlassId: BackendEntityId;
    einladungId: BackendEntityIdWithClass;
    impfung: UntersuchungImpfungInternSaveJson;
  }>(
    pipe(
      exhaustMap(payload =>
        this.saveImpfungenLoader
          .tapLoading$(
            this.#impfungenService.createUntersuchungImpfungIntern$({
              anlassId: payload.anlassId,
              einladungId: payload.einladungId,
              untersuchungImpfungInternSaveJson: payload.impfung,
            }),
            error => this.#appErrorHandlerService.handleError(error),
          )
          .pipe(
            tapResponse({
              next: result => {
                patchState(this, {
                  impfungenInternList: [...this.impfungenInternList(), result],
                });
                // Vorsichtshalber die Impfstoffchargen neu laden
                this.#eventBusStore.emit('IMPFSTOFF_CHARGEN_POTENTIALLY_CHANGED',
                  {impfstoffId: result.data.impfstoff.id});
              },
              error: console.error,
            }),
          ),
      ),
    ),
  );

  updateImpfungIntern = rxMethod<{
    anlassId: BackendEntityId;
    einladungId: BackendEntityIdWithClass;
    impfungId: BackendEntityId;
    impfung: UntersuchungImpfungInternSaveJson;
  }>(
    pipe(
      exhaustMap(payload =>
        this.saveImpfungenLoader
          .tapLoading$(
            this.#impfungenService
              .updateUntersuchungImpfungenIntern$({
                anlassId: payload.anlassId,
                einladungId: payload.einladungId,
                impfungInternId: requireValue(payload.impfungId),
                untersuchungImpfungInternSaveJson: payload.impfung,
              })
              .pipe(map(result => ({ params: payload, result }))),
            error => this.#appErrorHandlerService.handleError(error),
          )
          .pipe(
            tapResponse({
              next: ({ params, result }) => {
                patchState(this, {
                  impfungenInternList: this.impfungenInternList().map(impfung =>
                    impfung.id === result.id ? result : impfung,
                  ),
                });
                // we have to reload checkmark state
                this.#untersuchungStore.loadTabsSaved({
                  anlassId: params.anlassId,
                  einladungId: params.einladungId,
                });
                // Vorsichtshalber die Impfstoffchargen neu laden
                this.#eventBusStore.emit('IMPFSTOFF_CHARGEN_POTENTIALLY_CHANGED',
                  {impfstoffId: result.data.impfstoff.id});
              },
              error: console.error,
            }),
          ),
      ),
    ),
  );

  removeImpfungIntern = rxMethod<{
    anlassId: BackendEntityId;
    einladungId: BackendEntityIdWithClass;
    impfungId: BackendEntityId;
  }>(
    pipe(
      exhaustMap(payload =>
        this.saveImpfungenLoader
          .tapLoading$(
            this.#impfungenService.removeUntersuchungImpfungenIntern$({
              anlassId: payload.anlassId,
              einladungId: payload.einladungId,
              impfungInternId: payload.impfungId,
            }),
            error => this.#appErrorHandlerService.handleError(error),
          )
          .pipe(
            tapResponse({
              next: () => {
                patchState(this, {
                  impfungenInternList: [...this.impfungenInternList().filter(value => value.id !== payload.impfungId)],
                });
              },
              error: console.error,
            }),
          ),
      ),
    ),
  );

  changeUntersuchungImpfungenChecklist = rxMethod<{
    anlassId: BackendEntityId;
    einladungId: BackendEntityIdWithClass;
    checklist: UntersuchungImpfungenChecklisteSaveJson;
  }>(
    pipe(
      exhaustMap(({ anlassId, einladungId, checklist }) =>
        this.saveImpfungenLoader.tapLoading$(
          this.#impfungenService.saveUntersuchungImpfungen$({
            anlassId: anlassId,
            einladungId: einladungId,
            untersuchungImpfungenSaveJson: {
              ...this.prepareUntersuchungSaveJson(),
              checkliste: checklist,
            },
          }),
          error => this.#appErrorHandlerService.handleError(error),
        ),
      ),
      tapResponse({
        next: result => {
          this.patchChecklist(result);
          this.#appNotificationStore.addSuccessNotification(
            this.#transloco.translate('untersuchung.impfungen.checklist.changed'),
          );
        },
        error: console.error,
      }),
    ),
  );

  addKindToFolgetermin = rxMethod<{
    folgeTerminAnlassId: BackendEntityId;
    kindId: BackendEntityId;
    currentAnlassId: BackendEntityId;
  }>(
    pipe(
      exhaustMap(({ folgeTerminAnlassId, kindId, currentAnlassId }) =>
        this.impffolgetermineLoader
          .tapLoading$(
            this.#einladungenService.createEinladungenForAnlass$({
              anlassId: folgeTerminAnlassId,
              kindIdListJson: { kinderIdList: [kindId] },
            }),
            error => this.#appErrorHandlerService.handleError(error),
          )
          .pipe(
            tapResponse({
              next: () => {
                this.#appNotificationStore.addSuccessNotification(
                  this.#transloco.translate('untersuchung.tab.impfungen.massnahmen.folgetermin.success'),
                );
                this.#loadImpffolgetermin({ kindId: requireValidBackendEntityId(kindId) });
                this.#loadScheduledImpftermine({ kindId, excludedAnlassId: currentAnlassId });
              },
              error: console.error,
            }),
          ),
      ),
    ),
  );

  initializeFolgeTermine = rxMethod<{ currentAnlassId: BackendEntityId }>(
    pipe(
      switchMap(({ currentAnlassId }) =>
        toObservable(this.#untersuchungStore.currentDossier).pipe(
          map(dossier => dossier?.kind.id),
          filter((id): id is BackendEntityId => !!id),
          take(1),
          map(kindId => ({ currentAnlassId, kindId })),
        ),
      ),
      tapResponse({
        next: ({ currentAnlassId, kindId }) => {
          this.#loadImpffolgetermin({ kindId: requireValidBackendEntityId(kindId) });
          this.#loadScheduledImpftermine({ kindId, excludedAnlassId: currentAnlassId });
        },
        error: console.error,
      }),
    ),
  );

  #loadImpffolgetermin = rxMethod<{ kindId: BackendEntityId }>(
    pipe(
      switchMap(({ kindId }) =>
        this.impffolgetermineLoader.tapLoading$(
          this.#anlassService.searchAnlassTermineWithAvailableSlots$({
            kindId,
            anlassTyp: [AnlassTyp.IMPFAKTION, AnlassTyp.IMPFTERMIN, AnlassTyp.IMPFSPRECHSTUNDE],
          }),
          error => this.#appErrorHandlerService.handleError(error),
        ),
      ),
      tapResponse({
        next: result => {
          patchState(this, {
            impffolgetermine: result,
          });
        },
        error: console.error,
      }),
    ),
  );

  #loadScheduledImpftermine = rxMethod<{ kindId: BackendEntityId; excludedAnlassId: BackendEntityId }>(
    pipe(
      switchMap(({ kindId, excludedAnlassId }) =>
        this.impffolgetermineLoader.tapLoading$(
          this.#anlassService.findScheduledAnlaesseForKind$({
            kindId,
            excludeAnlassIds: [excludedAnlassId],
            anlassTyp: [AnlassTyp.IMPFAKTION, AnlassTyp.IMPFTERMIN, AnlassTyp.IMPFSPRECHSTUNDE],
          }),
          error => this.#appErrorHandlerService.handleError(error),
        ),
      ),
      tapResponse({
        next: result => {
          patchState(this, {
            imfanlaesseScheduled: result.anlaesseWithDateTime,
          });
        },
        error: console.error,
      }),
    ),
  );
}
