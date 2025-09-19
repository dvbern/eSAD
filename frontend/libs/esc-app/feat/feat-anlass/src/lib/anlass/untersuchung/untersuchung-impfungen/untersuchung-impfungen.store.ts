import {withDevtools} from '@angular-architects/ngrx-toolkit';
import {inject, Injectable} from '@angular/core';
import {
  BenutzerOptionJson,
  UntersuchungImpfstatusListSaveJson,
  UntersuchungImpfungenChecklisteSaveJson,
  UntersuchungImpfungenDoppelkontrolleSaveJson,
  UntersuchungImpfungenImpfausweisSaveJson,
  UntersuchungImpfungenJson,
  UntersuchungImpfungenMassnahmenJson,
  UntersuchungImpfungenSaveJson,
  UntersuchungImpfungenService,
  UntersuchungImpfungenServiceSaveUntersuchungMassnahmenRequestParams,
  UntersuchungImpfungInternJson,
  UntersuchungImpfungInternSaveJson,
} from '@dvb/data-access-backend';
import {BackendEntityId} from '@dvb/data-access-types';
import {AppErrorHandlerService, AppNotificationStore, Loader} from '@dvb/util-angular';
import {ensurePropsFlat, requireValue} from '@dvb/util-shared';
import {TranslocoService} from '@jsverse/transloco';
import {tapResponse} from '@ngrx/operators';
import {EmptyFeatureResult, patchState, signalStore, SignalStoreFeature, withMethods, withState} from '@ngrx/signals';
import {rxMethod} from '@ngrx/signals/rxjs-interop';
import {exhaustMap, map, pipe, switchMap, tap} from 'rxjs';
import {UntersuchungStore} from '../untersuchung.store';

type UntersuchungState = {
  impfausweis: UntersuchungImpfungenImpfausweisSaveJson | undefined,
  checklist: UntersuchungImpfungenChecklisteSaveJson | undefined,
  doppelkontrolle: UntersuchungImpfungenDoppelkontrolleSaveJson | undefined,
  versionUntersuchungImpfungen: number | undefined,
  massnahmen: UntersuchungImpfungenMassnahmenJson | undefined,
  impfstatusList: UntersuchungImpfstatusListSaveJson | undefined,
  impfungenInternList: UntersuchungImpfungInternJson[],
};

const initialState: UntersuchungState = {
  impfausweis: undefined,
  checklist: undefined,
  doppelkontrolle: undefined,
  versionUntersuchungImpfungen: undefined,
  massnahmen: undefined,
  impfstatusList: undefined,
  impfungenInternList: [],
};

@Injectable()
export class UntersuchungImpfungenStore extends signalStore(
  { protectedState: false },
  withDevtools('untersuchungImpfung') as SignalStoreFeature<EmptyFeatureResult, EmptyFeatureResult>,
  withState(initialState),
  withMethods((store) => ({
    prepareUntersuchungSaveJson(): UntersuchungImpfungenSaveJson {
      return ensurePropsFlat<UntersuchungImpfungenSaveJson>({
        impfausweis: requireValue(store.impfausweis()),
        checkliste: requireValue(store.checklist()),
        doppelkontrolle: requireValue(store.doppelkontrolle()),
        version: requireValue(store.versionUntersuchungImpfungen()),
      })
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
  readonly #appErrorHandlerService = inject(AppErrorHandlerService);
  readonly #untersuchungStore = inject(UntersuchungStore);
  readonly #appNotificationStore = inject(AppNotificationStore);
  readonly #transloco = inject(TranslocoService);

  readonly loader = new Loader();
  readonly saveImpfungenLoader = new Loader();
  readonly saveMassnahmenLoader = new Loader();

  loadUntersuchungImpfungenData = rxMethod<{ anlassId: BackendEntityId, einladungId: BackendEntityId }>(
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
    anlassId: BackendEntityId,
    einladungId: BackendEntityId,
    impfausweisUnvollstaendig: boolean,
  }>(
    pipe(
      exhaustMap(payload =>
        this.saveImpfungenLoader.tapLoading$(
          this.#impfungenService.saveUntersuchungImpfungen$({
            anlassId: payload.anlassId,
            einladungId: payload.einladungId,
            untersuchungImpfungenSaveJson: {
              ... this.prepareUntersuchungSaveJson(),
              impfausweis: {
                ...requireValue(this.impfausweis()),
                impfausweisUnvollstaendig: payload.impfausweisUnvollstaendig,
              },
            },
          }),
          error => this.#appErrorHandlerService.handleError(error),
        ),
      ),
      tapResponse({
        next: result => {
          this.patchImpfausweis(result);
          this.#appNotificationStore.addSuccessNotification(this.#transloco.translate(
            'untersuchung.impfungen.impfausweisUnvollstaendig.changed'));
        },
        error: console.error,
      }),
    ),
  );

  changeImpfausweisNichtVorhanden = rxMethod<{
    anlassId: BackendEntityId,
    einladungId: BackendEntityId,
    impfausweisNichtVorhanden: boolean,
  }>(
    pipe(
      exhaustMap(payload =>
        this.saveImpfungenLoader.tapLoading$(
          this.#impfungenService.saveUntersuchungImpfungen$({
            anlassId: payload.anlassId,
            einladungId: payload.einladungId,
            untersuchungImpfungenSaveJson: {
              ... this.prepareUntersuchungSaveJson(),
              impfausweis: {
                ...requireValue(this.impfausweis()),
                impfausweisNichtVorhanden: payload.impfausweisNichtVorhanden,
              },
            },
          }),
          error => this.#appErrorHandlerService.handleError(error),
        ),
      ),
      tapResponse({
        next: result => {
          this.patchImpfausweis(result);
          this.#appNotificationStore.addSuccessNotification(this.#transloco.translate(
            'untersuchung.impfungen.impfausweisNichtVorhanden.changed'));
        },
        error: console.error,
      }),
    ),
  );

  changeImpfausweisZweiPlusEinsSchema = rxMethod<{
    anlassId: BackendEntityId,
    einladungId: BackendEntityId,
    impfausweisZweiPlusEinsSchema: boolean,
  }>(
    pipe(
      exhaustMap(payload =>
        this.saveImpfungenLoader.tapLoading$(
          this.#impfungenService.saveUntersuchungImpfungen$({
            anlassId: payload.anlassId,
            einladungId: payload.einladungId,
            untersuchungImpfungenSaveJson: {
              ... this.prepareUntersuchungSaveJson(),
              impfausweis: {
                ...requireValue(this.impfausweis()),
                impfausweisZweiPlusEinsSchema: payload.impfausweisZweiPlusEinsSchema,
              },
            },
          }),
          error => this.#appErrorHandlerService.handleError(error),
        ),
      ),
      tapResponse({
        next: result => {
          this.patchImpfausweis(result);
          this.#appNotificationStore.addSuccessNotification(this.#transloco.translate(
            'untersuchung.impfungen.impfausweisZweiPlusEinsSchema.changed'));
        },
        error: console.error,
      }),
    ),
  );

  visierenDoppelkontrolleVisum1 = rxMethod<{ anlassId: BackendEntityId, einladungId: BackendEntityId }>(
    pipe(
      exhaustMap(payload =>
        this.saveImpfungenLoader.tapLoading$(
          this.#impfungenService.visierenDoppelkontrolleVisum1$({
            anlassId: payload.anlassId,
            einladungId: payload.einladungId,
            untersuchungImpfungenSaveJson: {
              ... this.prepareUntersuchungSaveJson(),
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
    anlassId: BackendEntityId,
    einladungId: BackendEntityId,
    user: BenutzerOptionJson,
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
                visumPerson2:  payload.user,
              },
            }
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

  visierenDoppelkontrolleVisum2 = rxMethod<{ anlassId: BackendEntityId, einladungId: BackendEntityId }>(
    pipe(
      exhaustMap(payload =>
        this.saveImpfungenLoader.tapLoading$(
          this.#impfungenService.visierenDoppelkontrolleVisum2$({
            anlassId: payload.anlassId,
            einladungId: payload.einladungId,
            untersuchungImpfungenSaveJson: {
              ... this.prepareUntersuchungSaveJson(),
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

  resetDoppelkontrolle = rxMethod<{ anlassId: BackendEntityId, einladungId: BackendEntityId }>(
    pipe(
      exhaustMap(payload =>
        this.saveImpfungenLoader.tapLoading$(
          this.#impfungenService.resetDoppelkontrolle$({
            anlassId: payload.anlassId,
            einladungId: payload.einladungId,
            untersuchungImpfungenSaveJson: {
              ... this.prepareUntersuchungSaveJson(),
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

  saveMassnahmen = rxMethod<UntersuchungImpfungenServiceSaveUntersuchungMassnahmenRequestParams>(
    pipe(
      exhaustMap(params =>
        this.saveMassnahmenLoader.tapLoading$(this.#impfungenService.saveUntersuchungMassnahmen$(params), error =>
          this.#appErrorHandlerService.handleError(error),
        ),
      ),
      tapResponse({
        next: massnahmen => {
          patchState(this, { massnahmen: massnahmen });
          this.#appNotificationStore.addSuccessNotification(this.#transloco.translate('global.data.saved'));
        },
        error: console.error,
      }),
    ),
  );

  einzelTerminErstellen = rxMethod<{
    anlassId: BackendEntityId,
    einladungId: BackendEntityId,
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
    anlassId: BackendEntityId,
    einladungId: BackendEntityId,
    impfstatusList: UntersuchungImpfstatusListSaveJson,
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
    anlassId: BackendEntityId,
    einladungId: BackendEntityId,
    impfung: UntersuchungImpfungInternSaveJson,
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
              },
              error: console.error,
            }),
          ),
      ),
    ),
  );

  updateImpfungIntern = rxMethod<{
    anlassId: BackendEntityId,
    einladungId: BackendEntityId,
    impfungId: BackendEntityId,
    impfung: UntersuchungImpfungInternSaveJson,
  }>(
    pipe(
      exhaustMap(payload =>
        this.saveImpfungenLoader
          .tapLoading$(
            this.#impfungenService.updateUntersuchungImpfungenIntern$({
              anlassId: payload.anlassId,
              einladungId: payload.einladungId,
              impfungInternId: requireValue(payload.impfungId),
              untersuchungImpfungInternSaveJson: payload.impfung,
            }),
            error => this.#appErrorHandlerService.handleError(error),
          )
          .pipe(
            tapResponse({
              next: result => {
                patchState(this, {
                  impfungenInternList: this.impfungenInternList().map(impfung =>
                    impfung.id === result.id ? result : impfung,
                  ),
                });
              },
              error: console.error,
            }),
          ),
      ),
    ),
  );

  removeImpfungIntern = rxMethod<{
    anlassId: BackendEntityId,
    einladungId: BackendEntityId,
    impfungId: BackendEntityId,
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
    anlassId: BackendEntityId,
    einladungId: BackendEntityId,
    checklist: UntersuchungImpfungenChecklisteSaveJson,
  }>(
    pipe(
      exhaustMap(({ anlassId, einladungId, checklist }) =>
        this.saveImpfungenLoader.tapLoading$(
          this.#impfungenService.saveUntersuchungImpfungen$({
            anlassId: anlassId,
            einladungId: einladungId,
            untersuchungImpfungenSaveJson: {
              ... this.prepareUntersuchungSaveJson(),
              checkliste: checklist,
            },
          }),
          error => this.#appErrorHandlerService.handleError(error),
        ),
      ),
      tapResponse({
        next: result => {
          this.patchChecklist(result);
          this.#appNotificationStore.addSuccessNotification(this.#transloco.translate(
            'untersuchung.impfungen.checklist.changed'));
        },
        error: console.error,
      }),
    ),
  );
}
