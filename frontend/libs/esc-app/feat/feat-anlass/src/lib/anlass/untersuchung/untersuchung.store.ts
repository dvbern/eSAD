import {computed, inject, Injectable} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {
  DossierJson,
  DossierService,
  EinladungenService,
  EinladungenServiceGetUntersuchungTabsSavedByEinladungIdRequestParams,
  EinladungJson,
  UntersuchungTabInfoJson,
  UntersuchungTabsSavedJson,
} from '@dvb/data-access-backend';
import {BackendEntityId} from '@dvb/data-access-types';
import {Path} from '@dvb/feat-shared';
import {AppErrorHandlerService, Loader} from '@dvb/util-angular';
import {requireValue} from '@dvb/util-shared';
import {tapResponse} from '@ngrx/operators';
import {patchState, signalStore, withComputed, withMethods, withState} from '@ngrx/signals';
import {rxMethod} from '@ngrx/signals/rxjs-interop';
import {exhaustMap, map, pipe, switchMap} from 'rxjs';
import {AnlassEinladungenId} from './anlass-einladungen-id';

type UntersuchungState = {
  tabList: UntersuchungTabInfoJson[];
  hiddenTabs: UntersuchungTabInfoJson[];
  currentTabId: BackendEntityId | undefined;
  currentEinladung: EinladungJson | undefined;
  currentDossier: DossierJson | undefined;
  tabsSaved: UntersuchungTabsSavedJson | undefined;
};

const initialState: UntersuchungState = {
  tabList: [],
  hiddenTabs: [],
  currentTabId: undefined,
  currentEinladung: undefined,
  currentDossier: undefined,
  tabsSaved: undefined,
};

@Injectable()
export class UntersuchungStore extends signalStore(
  {protectedState: false},
  // withDevtools('untersuchung'),
  withState(initialState),
  withComputed(({tabList, currentTabId}) => ({
    currentTab: computed(() => {
      return tabList().find((tab) => tab.einladungID === currentTabId());
    }),
  })),
  withMethods((store) => ({
    setCurrentTabId(query: BackendEntityId | undefined): void {
      patchState(store, (_state) => ({currentTabId: query}));
    },
    reset(): void {
      patchState(store, initialState);
    },
  })),
) {
  readonly #einladungenService = inject(EinladungenService);
  readonly #dossierService = inject(DossierService);
  readonly #appErrorHandlerService = inject(AppErrorHandlerService);
  readonly router = inject(Router);

  readonly loader = new Loader();
  readonly tabsSavedLoader = new Loader();
  readonly hiddenTabsLoader = new Loader();
  readonly loadEinladungLoader = new Loader();

  async addTab(id: BackendEntityId, alreadyOpenEinladungenIds: string, activatedRoute: ActivatedRoute): Promise<boolean> {
    // find the TabInfo object to move from the hidden tabs to the open tabs, then try to navigate there
    const tabToOpen = this.hiddenTabs().find(tab => tab.einladungID === id);
    if (tabToOpen) {
      // wait and check if the navigation was really performed (could be canceled due to dirty check)
      const navigationSuccessful = await this.reloadUntersuchung(id, alreadyOpenEinladungenIds, activatedRoute);

      // now we know if we really did navigate away, so apply needed store changes
      if (navigationSuccessful) {
        const remainingHiddenTabs = this.hiddenTabs().filter(tab => tab.einladungID !== tabToOpen.einladungID);

        patchState(this,
          {
            currentTabId: id,
            tabList: [...this.tabList(), tabToOpen], // add the new tab to the list
            hiddenTabs: remainingHiddenTabs, // update list of hidden tabs, it no longer contains the added child
          },
        );
      }
      return navigationSuccessful;
    }
    return false;
  }

  async closeTab(id: BackendEntityId, alreadyOpenEinladungenIds: string, activatedRoute: ActivatedRoute): Promise<boolean> {
    const tabToClose = this.tabList().find(tab => tab.einladungID === id);
    if (tabToClose) {
      const remainingTabList = this.tabList().filter(tab => tab.einladungID !== tabToClose.einladungID);
      const wasLastOpenTab = remainingTabList.length === 0;

      let navigationSuccessful = false;
      let nextTabId;
      if (wasLastOpenTab) {
        navigationSuccessful = await this.goToAnlassplanung(activatedRoute);
        nextTabId = undefined; // Kein zu oeffnendes Tab mehr vorhanden
      } else {
        nextTabId = this.currentTabId();
        if (id === this.currentTabId()) {
          nextTabId = remainingTabList[0].einladungID;
        }
        navigationSuccessful = await this.reloadUntersuchung(requireValue(nextTabId), alreadyOpenEinladungenIds, activatedRoute);
      }

      // now we know if we really did navigate away, so apply needed store changes
      if (navigationSuccessful) {
        patchState(this,
          {
            tabList: remainingTabList,
            hiddenTabs: [...this.hiddenTabs(), tabToClose],
          },
        );

        this.setCurrentTabId(nextTabId);
      }
      return navigationSuccessful;
    }
    return false;
  }

  loadTabList = rxMethod<AnlassEinladungenId>(
    pipe(
      switchMap((payload) =>
        this.loader.tapLoading$(
          this.#einladungenService.getUntersuchungTabInfos$({
            anlassId: payload.anlassId,
            einladungIds: payload.einladungIds,
          }),
          error => this.#appErrorHandlerService.handleError(error))),
      tapResponse({
        next: result => {
          const serverSideList = result.tabInfoList.sort((a, b) => a.label.localeCompare(b.label));
          patchState(this, {tabList: [...serverSideList]});
        },
        error: console.error,
      }),
    ),
  );

  loadHiddenTabs = rxMethod<AnlassEinladungenId>(
    pipe(
      switchMap((payload) =>
        this.hiddenTabsLoader.tapLoading$(
          this.#einladungenService.getHiddenUntersuchungTabInfos$({
            anlassId: payload.anlassId,
            einladungIds: payload.einladungIds,
          }),
          error => this.#appErrorHandlerService.handleError(error))),
      tapResponse({
        next: result => {
          const serverSideList = result.tabInfoList.sort((a, b) => a.label.localeCompare(b.label));
          patchState(this, {hiddenTabs: [...serverSideList]});
        },
        error: console.error,
      }),
    ),
  );

  loadEinladung = rxMethod<AnlassEinladungenId>(
    pipe(
      switchMap((params) => this.loadEinladungLoader
        .tapLoading$(this.#einladungenService.getEinladungById$({
            anlassId: params.anlassId,
            einladungId: params.einladungIds[0],
          }).pipe(
            map(result => ({result, params})),
          ),
          error => this.#appErrorHandlerService.handleError(error),
        ),
      ),
      tapResponse({
        next: ({result, params}) => {
          patchState(this, {currentEinladung: result});
          this.loadTabsSaved({
            anlassId: params.anlassId,
            einladungId: params.einladungIds[0],
          });
          this.loadDossierForCurrentKind(result.kind.id);
        },
        error: console.error,
      }),
    ),
  );

  loadDossierForCurrentKind = rxMethod<BackendEntityId>(
    pipe(
      switchMap((entityId) =>
        this.loader.tapLoading$(this.#dossierService.getOrCreateDossier$({ kindId: entityId }),
          error => this.#appErrorHandlerService.handleError(error)),
      ),
      tapResponse({
        next: result => patchState(this, {currentDossier: result}),
        error: console.error,
      }),
    ),
  );

  loadTabsSaved = rxMethod<EinladungenServiceGetUntersuchungTabsSavedByEinladungIdRequestParams>(
    pipe(
      switchMap((params) =>
        this.tabsSavedLoader.tapLoading$(
          this.#einladungenService.getUntersuchungTabsSavedByEinladungId$({
            anlassId: params.anlassId,
            einladungId: params.einladungId,
          }),
          error => this.#appErrorHandlerService.handleError(error),
        ),
      ),
      tapResponse({
        next: result => patchState(this, {tabsSaved: result}),
        error: console.error,
      }),
    ),
  );

  untersuchungAbschliessen = rxMethod<{
    anlassId: BackendEntityId,
    einladungId: BackendEntityId,
    onSuccess: () => void
  }>(
    pipe(
      exhaustMap(
        (params) => this.loader.tapLoading$(
          this.#einladungenService.untersuchungAbschliessen$({
            anlassId: params.anlassId,
            einladungId: params.einladungId,
          }),
          error => this.#appErrorHandlerService.handleError(error),
        ).pipe(
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

  private reloadUntersuchung(id: BackendEntityId, einladungIds: string, activatedRoute: ActivatedRoute): Promise<boolean> {
    return this.router.navigate([
        Path.RELATIVE_ONE_UP,
        Path.ANLASS_UNTERSUCHUNG,
        Path.ANLASS_UNTERSUCHUNG_EINLADUNG,
        id,
      ],
      {relativeTo: activatedRoute, queryParams: {einladungIds: einladungIds}});
  }

  private goToAnlassplanung(activatedRoute: ActivatedRoute): Promise<boolean> {
    return this.router.navigate([
        Path.RELATIVE_ONE_UP,
        Path.ANLASS_PLANUNG,
      ],
      {relativeTo: activatedRoute});
  }
}
