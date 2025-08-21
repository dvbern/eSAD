import { withDevtools } from '@angular-architects/ngrx-toolkit';
import { computed, inject, Injectable } from '@angular/core';
import {
  AnlassFlagsJson,
  EinladungenService,
  EinladungJson,
  EinladungSaveJson,
  EinladungStatus,
  KinderService,
  KindJson,
  NaechsteStationJson,
  NotizenService,
  NotizJson,
  NotizSaveJson,
  PrinterService,
} from '@dvb/data-access-backend';
import { BackendEntityId } from '@dvb/data-access-types';
import {
  AppErrorHandlerService,
  AppNotificationStore,
  handleBlobResponseDownload,
  Loader,
  rxMethod,
} from '@dvb/util-angular';
import { ensurePropsFlat, isNotNullish, requireValue } from '@dvb/util-shared';
import { TranslocoService } from '@jsverse/transloco';
import { tapResponse } from '@ngrx/operators';
import {
  EmptyFeatureResult,
  patchState,
  signalStore,
  SignalStoreFeature,
  withComputed,
  withMethods,
  withState,
} from '@ngrx/signals';
import { catchError, concatMap, exhaustMap, filter, from, last, map, mergeMap, of, pipe, switchMap, tap } from 'rxjs';
import { AnlassStore } from '../anlass.store';
import { EinladungNotizTyp } from './anlass-planung/notizen/einladung-notiz.page.component';

export interface EinladungJsonEnhanced extends EinladungJson {
  nachkontrolleString: string;
}

type AnlassKinderState = {
  alleKinder: KindJson[],
  einladungen: EinladungJsonEnhanced[],
  selectedIdsForUntersuchung: BackendEntityId[],
  selectedEinladungId: BackendEntityId | undefined,
  kindWasAdded: boolean,
};

const initialState: AnlassKinderState = {
  alleKinder: [],
  einladungen: [],
  selectedIdsForUntersuchung: [],
  selectedEinladungId: undefined,
  kindWasAdded: false,
};

@Injectable()
export class AnlassKinderStore extends signalStore(
  { protectedState: false },
  withDevtools('anlassKinder') as SignalStoreFeature<EmptyFeatureResult, EmptyFeatureResult>,
  withState(initialState),
  withComputed(({ einladungen, alleKinder, selectedIdsForUntersuchung }) => ({
    selectedForUntersuchung: computed(() =>
      einladungen().filter((currentEinladung: EinladungJsonEnhanced) =>
        selectedIdsForUntersuchung().includes(currentEinladung.id),
      ),
    ),
    availableKinder: computed(() =>
      alleKinder().filter(
        kind =>
          !einladungen()
            .map(a => a.kind.id)
            .includes(kind.id),
      ),
    ),
    countEinladungen: computed(() => einladungen().length),
  })),
  withMethods((store, translateService = inject(TranslocoService)) => ({
    markEinladungForUntersuchung(id: BackendEntityId): void {
      if (!this.isEinladungMarkedForUntersuchung(id)) {
        patchState(store, { selectedIdsForUntersuchung: [...store.selectedIdsForUntersuchung(), id] });
      }
    },
    unmarkEinladungForUntersuchung(id: BackendEntityId): void {
      patchState(store, {
        selectedIdsForUntersuchung: [...store.selectedIdsForUntersuchung().filter(markedId => markedId !== id)],
      });
    },
    markAllEinladungenForUntersuchung(selectedIds: BackendEntityId[]): void {
      this.unmarkAllEinladungenForUntersuchung();
      patchState(store, {
        selectedIdsForUntersuchung: selectedIds,
      });
    },
    unmarkAllEinladungenForUntersuchung(): void {
      patchState(store, { selectedIdsForUntersuchung: [] });
    },
    isEinladungMarkedForUntersuchung(id: BackendEntityId): boolean {
      return store.selectedForUntersuchung().some((el: EinladungJsonEnhanced) => el.id === id);
    },
    einladungById(id: BackendEntityId): EinladungJsonEnhanced | undefined {
      return store.einladungen().find((el: EinladungJsonEnhanced) => el.id === id);
    },
    setSelectedEinladungId(einladungId: BackendEntityId | undefined): void {
      patchState(store, { selectedEinladungId: einladungId });
    },
    getSelectedEinladung(): EinladungJsonEnhanced | undefined {
      return store.einladungen().find((el: EinladungJsonEnhanced) => el.id === store.selectedEinladungId());
    },
    getSprachenMap(): Map<string, number> {
      const map = new Map();
      store
        .einladungen()
        .map(einladung => einladung.kind.data.erstsprache)
        .filter(sprache => sprache !== undefined && sprache !== null)
        .forEach(sprache => {
          if (map.has(sprache)) {
            const count = map.get(sprache) + 1;
            map.delete(sprache);
            map.set(sprache, count);
          } else {
            map.set(sprache, 1);
          }
        });
      return map;
    },
    getTeilnehmerMap(): Map<string, number> {
      const map = new Map();
      let overAllCount = 0;
      store
        .einladungen()
        .map(einladung => einladung.data.einladungStatus)
        .filter(status => status !== undefined && status !== null)
        .forEach(status => {
          const tranlatedStatus = translateService.translate('EinladungStatus.' + status);
          if (map.has(tranlatedStatus)) {
            const count = map.get(tranlatedStatus) + 1;
            map.delete(tranlatedStatus);
            map.set(tranlatedStatus, count);
          } else {
            map.set(tranlatedStatus, 1);
          }
          overAllCount += 1;
        });
      map.set(translateService.translate('anlass.summary.all'), overAllCount);
      return map;
    },
  })),
) {
  readonly #einladungenService = inject(EinladungenService);
  readonly #kinderService = inject(KinderService);
  readonly #printerService = inject(PrinterService);
  readonly #notizenService = inject(NotizenService);
  readonly #appErrorHandlerService = inject(AppErrorHandlerService);
  readonly #appNotficationService = inject(AppNotificationStore);
  readonly transloco = inject(TranslocoService);

  readonly anlassStore = inject(AnlassStore);

  readonly loader = new Loader();
  readonly saveLoader = new Loader();
  readonly deleteLoader = new Loader();

  resetKinderListe(): void {
    patchState(this, { alleKinder: [] });
  }

  resetEinladungen(): void {
    patchState(this, { einladungen: [], kindWasAdded: false });
  }

  loadEinladungen = rxMethod<BackendEntityId>(
    pipe(
      tap(() => patchState(this, { alleKinder: [], einladungen: [] })),
      switchMap(anlassId => {
          return this.loader.tapLoading$(this.#einladungenService.findEinladungenByAnlass$({anlassId}), error =>
            this.#appErrorHandlerService.handleError(error),
          );
        },
      ),
      tapResponse({
        next: result =>
          patchState(this, { einladungen: result.einladungList.map(einladung => this.enhanceEinladung(einladung)) }),
        error: console.error,
      }),
    ),
  );

  pollEinladungen = rxMethod<number>(
    pipe(
      map(() => this.anlassStore.selectedEntity()?.id),
      filter((anlassId): anlassId is BackendEntityId => !!anlassId),
      switchMap(anlassId => this.#einladungenService.findEinladungenByAnlass$({ anlassId })),
      catchError(error => {
        this.#appErrorHandlerService.handleError(error);
        return of();
      }),
      tapResponse({
        next: result =>
          patchState(this, {
            einladungen: result.einladungList.map(einladung => this.enhanceEinladung(einladung)),
          }),
        error: console.error,
      }),
    ),
  );

  loadKinderBySchuleOrKlasse = rxMethod<{ schuleId?: BackendEntityId; klasseId?: BackendEntityId }>(
    pipe(
      tap(() => patchState(this, { alleKinder: [] })),
      switchMap(params => {
        if (this.anlassStore.selectedIsNew()) {
          return of(undefined);
        } else {
          return this.loader.tapLoading$(
            this.#kinderService.findKinderBySchuleOrKlasse$({ schuleId: params.schuleId, klasseId: params.klasseId }),
            error => this.#appErrorHandlerService.handleError(error),
          );
        }
      }),
      tapResponse({
        next: result => patchState(this, { alleKinder: result?.kinderList ?? [] }),
        error: console.error,
      }),
    ),
  );

  addKinder = rxMethod<{ anlassId: BackendEntityId; kindIds: BackendEntityId[] }>(
    pipe(
      exhaustMap(params =>
        this.saveLoader.tapLoading$(
          this.#einladungenService.createEinladungenForAnlass$({
            anlassId: params.anlassId,
            kindIdListJson: { kinderIdList: params.kindIds },
          }),
          error => this.#appErrorHandlerService.handleError(error),
        ),
      ),
      tapResponse({
        next: result =>
          patchState(this, {
            einladungen: [
              ...this.einladungen(),
              ...result.einladungList.map(einladung => this.enhanceEinladung(einladung)),
            ],
            kindWasAdded: true,
          }),
        error: console.error,
      }),
    ),
  );

  removeEinladung = rxMethod<EinladungJson>(
    pipe(
      exhaustMap(enhancedEinladung => {
        const einladung = this.depriveEinladung(enhancedEinladung);
        return this.deleteLoader.tapLoading$(
          this.#einladungenService
            .removeEinladungenForAnlass$({
              anlassId: requireValue(this.anlassStore.selectedEntity()?.id),
              einladungListJson: { einladungList: [einladung] },
            })
            .pipe(map(() => einladung)),
          error => this.#appErrorHandlerService.handleError(error),
        );
      }),
      tapResponse({
        next: result => patchState(this, { einladungen: [...this.einladungen().filter(el => el.id !== result.id)] }),
        error: console.error,
      }),
    ),
  );

  einladungAbmelden = rxMethod<{ einladungId: BackendEntityId; einladungSaveJson: EinladungSaveJson }>(
    pipe(
      exhaustMap(params =>
        this.saveLoader.tapLoading$(
          this.#einladungenService.updateEinladung$({
            anlassId: requireValue(this.anlassStore.selectedEntity()?.id),
            einladungId: params.einladungId,
            einladungSaveJson: params.einladungSaveJson,
          }),
          error => this.#appErrorHandlerService.handleError(error),
        ),
      ),
      tapResponse({
        next: result => {
          patchState(this, {
            einladungen: [...this.einladungen().map(el => (el.id === result.id ? this.enhanceEinladung(result) : el))],
          });
          this.#appNotficationService.addNotification({
            alertType: 'success',
            id: crypto.randomUUID(),
            summary: this.transloco.translate('anlass.kinder.abmeldegrund.hinterlegen.success'),
            autohide: true,
          });
        },
        error: console.error,
      }),
    ),
  );

  downloadKinderdossierReport = rxMethod<{ kindId: BackendEntityId; anlassId: BackendEntityId }>(
    pipe(
      switchMap(({ anlassId, kindId }) =>
        this.loader.tapLoading$(
          this.#printerService.downloadKinderdossierForAnlass$({ anlassId, kindId }, 'response'),
          error => this.#appErrorHandlerService.handleError(error),
        ),
      ),
      tapResponse({
        next: result => handleBlobResponseDownload(result, true),
        error: console.error,
      }),
    ),
  );

  updateEinladungStatus = rxMethod<{ einladungId: BackendEntityId; status: EinladungStatus }>(
    pipe(
      exhaustMap(params => {
        const einladungById = this.einladungById(params.einladungId);
        const isStatusAbmeldung = params.status === EinladungStatus.TEILNAHME_ABGESAGT;
        return this.loader.tapLoading$(
          this.#einladungenService.updateEinladung$({
            anlassId: requireValue(this.anlassStore.selectedEntity()?.id),
            einladungId: params.einladungId,
            einladungSaveJson: {
              ...requireValue(einladungById).data,
              einladungStatus: params.status,
              grundAbmeldung: isStatusAbmeldung ? einladungById?.data.grundAbmeldung : undefined,
              artAbmeldung: isStatusAbmeldung ? einladungById?.data?.artAbmeldung : undefined,
              bemerkungAbmeldung: isStatusAbmeldung ? einladungById?.data?.bemerkungAbmeldung : undefined,
            },
          }),
          error => this.#appErrorHandlerService.handleError(error),
        );
      }),
      tapResponse({
        next: result => {
          patchState(this, {
            einladungen: [...this.einladungen().map(el => (el.id === result.id ? this.enhanceEinladung(result) : el))],
          });
        },
        error: console.error,
      }),
    ),
  );

  createNotizWithFiles = rxMethod<{
    notiz: NotizSaveJson;
    notizTyp: EinladungNotizTyp;
    fileUploads: File[];
    onSuccess: (notiz: NotizJson) => void;
  }>(
    pipe(
      exhaustMap(({notiz, notizTyp, onSuccess, fileUploads}) => {
        const [file, file2, file3, file4] = fileUploads;
        return this.saveLoader
            .tapLoading$(
              notizTyp === 'impfausweis'
                ? this.#einladungenService.createImpfausweisNotiz$({
                    anlassId: requireValue(this.anlassStore.selectedEntity()?.id),
                    einladungId: requireValue(this.selectedEinladungId()),
                    notizCreateJson: notiz,
                    file: file,
                    file2: file2,
                    file3: file3,
                    file4: file4,
                  })
                : this.#einladungenService.createFragebogenNotiz$({
                    anlassId: requireValue(this.anlassStore.selectedEntity()?.id),
                    einladungId: requireValue(this.selectedEinladungId()),
                    notizCreateJson: notiz,
                    file: file,
                    file2: file2,
                    file3: file3,
                    file4: file4,
                  }),
              error => this.#appErrorHandlerService.handleError(error),
            )
            .pipe(
              tapResponse({
                next: result => {
                  patchState(this, {
                    einladungen: this.replaceNotizOfEinladungInArray(result, notizTyp).map(einladung =>
                      this.enhanceEinladung(einladung),
                    ),
                  });
                  onSuccess(result);
                },
                error: console.error,
              }),
            );
        },
      ),
    ),
  );

  updateNotizWithFiles = rxMethod<{
    kindId: BackendEntityId,
    notizId: BackendEntityId,
    notiz: NotizSaveJson,
    notizTyp: EinladungNotizTyp,
    fileUploads: File[];
    onSuccess: (notiz: NotizJson) => void
  }>(
    pipe(
      exhaustMap(({ kindId, notizId, notiz, notizTyp, fileUploads, onSuccess }) => {
        const [file, file2, file3, file4] = fileUploads;
          return this.saveLoader
            .tapLoading$(
              this.#notizenService.updateNotiz$({
                kindId: kindId,
                notizId: notizId,
                file: file,
                file2: file2,
                file3: file3,
                file4: file4,
                notizSaveJson: notiz,
              }),
              error => this.#appErrorHandlerService.handleError(error),
            )
            .pipe(
              tapResponse({
                next: result => {
                  patchState(this, {
                    einladungen: this.replaceNotizOfEinladungInArray(result, notizTyp).map(einladung =>
                      this.enhanceEinladung(einladung),
                    ),
                  });
                  onSuccess(result);
                },
                error: console.error,
              }),
            );
        },
      ),
    ),
  );

  removeNotizFile = rxMethod<{
    kindId: BackendEntityId,
    notizId: BackendEntityId,
    fileId: BackendEntityId,
    notizTyp: EinladungNotizTyp,
    onSuccess?: (notiz: NotizJson) => void
  }>(
    pipe(
      exhaustMap(({ kindId, notizId, fileId, notizTyp, onSuccess }) =>
        this.saveLoader
          .tapLoading$(
            this.#notizenService.removeNotizFile$({
              kindId: kindId,
              notizId: notizId,
              fileId: fileId,
            }),
            error => this.#appErrorHandlerService.handleError(error),
          )
          .pipe(
            tapResponse({
              next: result => {
                patchState(this, {
                  einladungen: this.replaceNotizOfEinladungInArray(result, notizTyp).map(einladung =>
                    this.enhanceEinladung(einladung),
                  ),
                });
                if (isNotNullish(onSuccess)) {
                  onSuccess(result);
                }
              },
              error: console.error,
            }),
          ),
      ),
    ),
  );

  private replaceNotizOfEinladungInArray(result: NotizJson, notizTyp: EinladungNotizTyp): EinladungJson[] {
    const newEinadungArray = this.einladungen().map(einladung => {
      if (einladung.id === this.selectedEinladungId()) {
        if (notizTyp === 'impfausweis') {
          einladung.data.impfausweis = result;
        } else {
          einladung.data.fragebogen = result;
        }
        return einladung;
      }
      return einladung;
    });
    return newEinadungArray;
  }

  private enhanceEinladung(einladung: EinladungJson): EinladungJsonEnhanced {
    return {
      ...einladung,
      nachkontrolleString: this.getNachkontrolleString(einladung.nachkontrolle?.data?.anlassFlags),
    };
  }

  public depriveEinladung(einladung: EinladungJson): EinladungJson {
    return ensurePropsFlat<EinladungJson>({
      id: einladung.id,
      entityInfo: einladung.entityInfo,
      data: requireValue(einladung.data),
      kind: requireValue(einladung.kind),
      nachkontrolle: requireValue(einladung.nachkontrolle),
      untersuchungKindDaten: undefined,
      naechsteStation: ensurePropsFlat<NaechsteStationJson>({
        naechsteStation: einladung.naechsteStation?.naechsteStation,
        naechsteStationSeit: einladung.naechsteStation?.naechsteStationSeit,
      }),
    });
  }

  private getNachkontrolleString(nachkontrolle: AnlassFlagsJson | undefined): string {
    let nachkontrolleString = '';
    if (nachkontrolle?.nachkontrolleSehen) {
      nachkontrolleString += 'nachkontrolleSehen,';
    }
    if (nachkontrolle?.nachkontrolleHoeren) {
      nachkontrolleString += 'nachkontrolleHoeren,';
    }
    if (nachkontrolle?.nachkontrolleGroesseGewicht) {
      nachkontrolleString += 'nachkontrolleGroesseGewicht,';
    }
    if (nachkontrolle?.nachkontrolleSprache) {
      nachkontrolleString += 'nachkontrolleSprache,';
    }
    if (nachkontrolle?.nachkontrolleMotorik) {
      nachkontrolleString += 'nachkontrolleMotorik,';
    }
    if (nachkontrolle?.nachkontrolleAnderes) {
      nachkontrolleString += 'nachkontrolleAnderes,';
    }
    return nachkontrolleString;
  }
}
