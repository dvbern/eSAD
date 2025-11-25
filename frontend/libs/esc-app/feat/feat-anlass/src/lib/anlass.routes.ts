import {inject} from '@angular/core';
import {ActivatedRouteSnapshot, Route} from '@angular/router';
import {AnlassSaveJson} from '@dvb/data-access-backend';
import {requireValidBackendEntityId, requireValidBackendEntityIds} from '@dvb/data-access-types';
import {
  BenutzerStore,
  AnlassConfigOptionsStore,
  ImpfstoffeChargenStore,
  InfomaterialStore,
  LabelsStore,
  Path,
  requireRole,
  SchulenOptionsStore,
} from '@dvb/feat-shared';
import {confirmOnDirty, DetailMode} from '@dvb/util-angular';
import {
  ensureProps,
  hasAerztlicheUntersuchungViewBerechtigung,
  hasAllNotizenEditBerechtigung,
  hasGroesseGewichtViewBerechtigung,
  hasHoerenViewBerechtigung,
  hasImpfungenViewBerechtigung,
  hasLaeusekontrolleViewBerechtigung,
  hasListViewBerechtigung,
  hasMassnahmenViewBerechtigung,
  hasSehenViewBerechtigung,
  hasTriageViewBerechtigung,
  hasUntersuchungKindDatenViewBerechtigung,
} from '@dvb/util-shared';
import {provideTranslocoScope} from '@jsverse/transloco';
import {AnlassListPageComponent} from './anlass/anlass-list/anlass-list.page.component';
import {AnlassStore} from './anlass/anlass.store';
import {
  AnlassBearbeitenPageComponent,
} from './anlass/anlasse-detail/anlass-bearbeiten/anlass-bearbeiten.page.component';
import {AnlassDetailPageComponent} from './anlass/anlasse-detail/anlass-detail-page.component';
import {AnlassKinderStore} from './anlass/anlasse-detail/anlass-kinder.store';
import {AnlassPlanungPageComponent} from './anlass/anlasse-detail/anlass-planung/anlass-planung.page.component';
import {
  EinladungNotizPageComponent,
} from './anlass/anlasse-detail/anlass-planung/notizen/einladung-notiz.page.component';
import {AnlassEinladungenId} from './anlass/untersuchung/anlass-einladungen-id';
import {UntersuchungArztComponent} from './anlass/untersuchung/untersuchung-arzt/untersuchung-arzt.component';
import {UntersuchungArztStore} from './anlass/untersuchung/untersuchung-arzt/untersuchung-arzt.store';
import {UntersuchungDatenComponent} from './anlass/untersuchung/untersuchung-daten/untersuchung-daten.component';
import {UntersuchungDatenStore} from './anlass/untersuchung/untersuchung-daten/untersuchung-daten.store';
import {
  UntersuchungGroessegewichtComponent,
} from './anlass/untersuchung/untersuchung-groessegewicht/untersuchung-groessegewicht.component';
import {
  UntersuchungGroesseGewichtStore,
} from './anlass/untersuchung/untersuchung-groessegewicht/untersuchung-groessegewicht.store';
import {
  UntersuchungHoerenAudiogrammStore,
} from './anlass/untersuchung/untersuchung-hoeren/untersuchung-hoeren-audiogramm/untersuchung-hoeren-audiogramm.store';
import {
  UntersuchungHoerenOtoskopieStore,
} from './anlass/untersuchung/untersuchung-hoeren/untersuchung-hoeren-otoskopie/untersuchung-hoeren-otoskopie.store';
import {UntersuchungHoerenComponent} from './anlass/untersuchung/untersuchung-hoeren/untersuchung-hoeren.component';
import {
  UntersuchungImpfungenComponent,
} from './anlass/untersuchung/untersuchung-impfungen/untersuchung-impfungen.component';
import {UntersuchungImpfungenStore} from './anlass/untersuchung/untersuchung-impfungen/untersuchung-impfungen.store';
import {
  UntersuchungMassnahmenComponent,
} from './anlass/untersuchung/untersuchung-massnahmen/untersuchung-massnahmen.component';
import {UntersuchungMassnahmenStore} from './anlass/untersuchung/untersuchung-massnahmen/untersuchung-massnahmen.store';
import {UntersuchungPageComponent} from './anlass/untersuchung/untersuchung-page.component';
import {UntersuchungSehenComponent} from './anlass/untersuchung/untersuchung-sehen/untersuchung-sehen.component';
import {UntersuchungSehenStore} from './anlass/untersuchung/untersuchung-sehen/untersuchung-sehen.store';
import {UntersuchungTriageComponent} from './anlass/untersuchung/untersuchung-triage/untersuchung-triage.component';
import {UntersuchungTriageStore} from './anlass/untersuchung/untersuchung-triage/untersuchung-triage.store';
import {UntersuchungStore} from './anlass/untersuchung/untersuchung.store';
import {
  UntersuchungLaeusekontrolleComponent
} from "./anlass/untersuchung/untersuchung-laeusekontrolle/untersuchung-laeusekontrolle.component";
import {
  UntersuchungLaeusekontrolleStore
} from "./anlass/untersuchung/untersuchung-laeusekontrolle/untersuchung-laeusekontrolle.store";

export const anlassRoutes: Route[] = [
  {
    path: '',
    providers: [AnlassStore, AnlassKinderStore, UntersuchungStore, InfomaterialStore, provideTranslocoScope('Anlass')],
    resolve: {
      _store: () => {
        inject(AnlassStore).loadAnlassList();
        inject(AnlassStore).setSelectedEntityId(undefined);
        const benutzerStore = inject(BenutzerStore);
        if (benutzerStore.createHasRequiredRoleSig(hasListViewBerechtigung())()) {
          inject(InfomaterialStore).loadInfomaterialList({onlyActive: true});
          inject(SchulenOptionsStore).loadSchulenOptions({onlyActive: true});
        }
        inject(AnlassConfigOptionsStore).loadAnlassConfigOptions();
      },
    },
    children: [
      {
        path: '',
        pathMatch: 'full',
        component: AnlassListPageComponent,
      },
      {
        path: Path.CREATE,
        component: AnlassDetailPageComponent,
        resolve: {
          _store: () => inject(AnlassStore).setSelectedEntityId(undefined),
          _einladungen: () => inject(AnlassKinderStore).resetEinladungen(),
          _prefill: (route: ActivatedRouteSnapshot) => {
            // Prefilled Kind
            const kindIdParam = route.queryParams['kindId'];
            if (kindIdParam && requireValidBackendEntityId(kindIdParam)) {
              inject(AnlassStore).prefillKind(kindIdParam);
            }
            // Prefilled Anlass Data
            const anlassPartialParam = route.queryParams['anlassPartial'];
            if (anlassPartialParam) {
              const anlassPartial: Partial<AnlassSaveJson> = JSON.parse(anlassPartialParam);
              inject(AnlassStore).prefillAnlass(anlassPartial);
            }
          },
        },
        children: [
          {
            path: '',
            component: AnlassBearbeitenPageComponent,
            resolve: {
              _store: () => inject(AnlassStore).setDetailModeEdit(),
            },
            canDeactivate: [confirmOnDirty()],
          },
        ],
      },
      {
        path: ':anlassId',
        component: AnlassDetailPageComponent,
        resolve: {
          _store: (route: ActivatedRouteSnapshot) => {
            const param = requireValidBackendEntityId(route.params['anlassId']);
            inject(AnlassStore).setSelectedEntityId(param);
            inject(AnlassStore).loadEntity(param);
            inject(AnlassKinderStore).loadEinladungen(param);
          },
        },
        children: [
          {
            path: '',
            pathMatch: 'full',
            redirectTo: Path.ANLASS_BEARBEITUNG,
          },
          {
            path: Path.ANLASS_BEARBEITUNG,
            component: AnlassBearbeitenPageComponent,
            resolve: {
              _store: () => {
                inject(AnlassStore).setDetailMode(DetailMode.EDIT);
              },
            },
            canDeactivate: [confirmOnDirty()],
          },
          {
            path: Path.ANLASS_PLANUNG,
            component: AnlassPlanungPageComponent,
            resolve: {
              _store: (route: ActivatedRouteSnapshot) => {
                inject(AnlassStore).setDetailMode(DetailMode.VIEW);
                inject(AnlassKinderStore).unmarkAllEinladungenForUntersuchung();
                const anlassId = requireValidBackendEntityId(route.parent?.paramMap.get('anlassId'));
                inject(AnlassStore).loadKlassenLehrpersonen(anlassId);
              },
            },
          },
          {
            path: Path.ANLASS_UNTERSUCHUNG_EINLADUNG + '/:einladungId/' + Path.NOTIZEN,
            component: EinladungNotizPageComponent,
            canActivate: [requireRole(hasAllNotizenEditBerechtigung())],
            canDeactivate: [confirmOnDirty()],
            resolve: {
              _labels: () => inject(LabelsStore).loadLabelsList(),
              _store: (route: ActivatedRouteSnapshot) => {
                const einladungId = requireValidBackendEntityId(route.params['einladungId']);
                inject(AnlassKinderStore).setSelectedEinladungId(einladungId);
              },
            },
          },
          {
            path: Path.ANLASS_UNTERSUCHUNG,
            component: UntersuchungPageComponent,
            providers: [UntersuchungDatenStore],
            resolve: {
              _store: (route: ActivatedRouteSnapshot) => {
                const einladungIdsAsString = route.queryParamMap.get('einladungIds');
                if (einladungIdsAsString) {
                  // param is comma separated list of relevant ids
                  const currentEinladungIds = requireValidBackendEntityIds(einladungIdsAsString.split(','));
                  const currentAnlassId = requireValidBackendEntityId(route.parent?.paramMap.get('anlassId'));
                  const anlassKinderStore = inject(AnlassKinderStore);
                  const untersuchungStore = inject(UntersuchungStore);
                  for (const einladungId of currentEinladungIds) {
                    anlassKinderStore.markEinladungForUntersuchung(einladungId);
                  }
                  const params = ensureProps<AnlassEinladungenId>({
                    anlassId: currentAnlassId,
                    einladungIds: currentEinladungIds,
                  });
                  // The same params because hiddenTabs are calculated based on the openTabs
                  untersuchungStore.loadTabList(params);
                  untersuchungStore.loadHiddenTabs(params);
                }
              },
            },
            children: [
              {
                path: Path.ANLASS_UNTERSUCHUNG_EINLADUNG + '/:id',
                providers: [
                  UntersuchungImpfungenStore,
                  UntersuchungSehenStore,
                  UntersuchungHoerenAudiogrammStore,
                  UntersuchungHoerenOtoskopieStore,
                  UntersuchungGroesseGewichtStore,
                  UntersuchungTriageStore,
                  UntersuchungArztStore,
                  UntersuchungLaeusekontrolleStore,
                  UntersuchungMassnahmenStore,
                ],
                resolve: {
                  _store: (route: ActivatedRouteSnapshot) => {
                    const currentEinladungId = requireValidBackendEntityId(route.params['id']);
                    const anlassId = requireValidBackendEntityId(route.parent?.parent?.paramMap.get('anlassId'));
                    const params = ensureProps<AnlassEinladungenId>({
                      anlassId: anlassId,
                      einladungIds: [currentEinladungId],
                    });
                    inject(ImpfstoffeChargenStore).loadImpfstoffeAndChargen();
                    inject(UntersuchungStore).setCurrentTabId(currentEinladungId);
                    inject(UntersuchungStore).loadEinladung(params);
                    inject(UntersuchungSehenStore).loadDefaultSehenBeurteilung(params);
                    inject(UntersuchungHoerenAudiogrammStore).loadDefaultHoerenAudiogramm(params);
                    inject(UntersuchungHoerenOtoskopieStore).loadDefaultHoerenOtoskopie(params);
                    inject(UntersuchungArztStore).loadDefaultUntersuchungArzt(params);
                  },
                },
                children: [
                  {
                    path: '',
                    pathMatch: 'full',
                    redirectTo: Path.ANLASS_UNTERSUCHUNG_EINLADUNG_DATEN,
                  },
                  {
                    path: Path.ANLASS_UNTERSUCHUNG_EINLADUNG_DATEN,
                    component: UntersuchungDatenComponent,
                    resolve: {
                      kindDate: (route: ActivatedRouteSnapshot) => {
                        const anlassId = requireValidBackendEntityId(
                          route.parent?.parent?.parent?.paramMap.get('anlassId'),
                        );
                        const einladungId = requireValidBackendEntityId(route.params['id']);
                        inject(UntersuchungDatenStore).loadDurchfuehrungDaten({
                          anlassId,
                          einladungId,
                        });
                        inject(UntersuchungStore).setCurrentTabId(einladungId);
                      },
                    },
                    canActivate: [requireRole(hasUntersuchungKindDatenViewBerechtigung())],
                    canDeactivate: [confirmOnDirty()],
                  },
                  {
                    path: Path.ANLASS_UNTERSUCHUNG_EINLADUNG_IMPFUNGEN,
                    component: UntersuchungImpfungenComponent,
                    resolve: {
                      _store: (route: ActivatedRouteSnapshot) => {
                        const currentEinladungId = requireValidBackendEntityId(route.params['id']);
                        const currentAnlassId = requireValidBackendEntityId(
                          route.parent?.parent?.parent?.paramMap.get('anlassId'),
                        );
                        inject(UntersuchungImpfungenStore).loadUntersuchungImpfungenData({
                          anlassId: currentAnlassId,
                          einladungId: currentEinladungId,
                        });
                      },
                    },
                    canActivate: [requireRole(hasImpfungenViewBerechtigung())],
                    canDeactivate: [confirmOnDirty()],
                  },
                  {
                    path: Path.ANLASS_UNTERSUCHUNG_EINLADUNG_SEHEN,
                    component: UntersuchungSehenComponent,
                    resolve: {
                      _store: (route: ActivatedRouteSnapshot) => {
                        const currentEinladungId = requireValidBackendEntityId(route.params['id']);
                        const currentAnlassId = requireValidBackendEntityId(
                          route.parent?.parent?.parent?.paramMap.get('anlassId'),
                        );
                        inject(UntersuchungSehenStore).loadSehenBeurteilung({
                          anlassId: currentAnlassId,
                          einladungId: currentEinladungId,
                        });
                        inject(UntersuchungSehenStore).loadSehenMassnahmen({
                          anlassId: currentAnlassId,
                          einladungId: currentEinladungId,
                        });
                        inject(UntersuchungSehenStore).loadSehenBemerkungFragebogen({
                          anlassId: currentAnlassId,
                          einladungId: currentEinladungId,
                        });
                      },
                    },
                    canActivate: [requireRole(hasSehenViewBerechtigung())],
                    canDeactivate: [confirmOnDirty()],
                  },
                  {
                    path: Path.ANLASS_UNTERSUCHUNG_EINLADUNG_HOEREN,
                    component: UntersuchungHoerenComponent,
                    resolve: {
                      _store: (route: ActivatedRouteSnapshot) => {
                        const currentEinladungId = requireValidBackendEntityId(route.params['id']);
                        const currentAnlassId = requireValidBackendEntityId(
                          route.parent?.parent?.parent?.paramMap.get('anlassId'),
                        );
                        inject(UntersuchungHoerenAudiogrammStore).loadHoerenAudiogramm({
                          anlassId: currentAnlassId,
                          einladungId: currentEinladungId,
                        });
                        inject(UntersuchungHoerenOtoskopieStore).loadHoerenOtoskopie({
                          anlassId: currentAnlassId,
                          einladungId: currentEinladungId,
                        });
                      },
                    },
                    canActivate: [requireRole(hasHoerenViewBerechtigung())],
                    canDeactivate: [confirmOnDirty()],
                  },
                  {
                    path: Path.ANLASS_UNTERSUCHUNG_EINLADUNG_GROESSEGEWICHT,
                    component: UntersuchungGroessegewichtComponent,
                    resolve: {
                      _store: (route: ActivatedRouteSnapshot) => {
                        const currentEinladungId = requireValidBackendEntityId(route.params['id']);
                        const currentAnlassId = requireValidBackendEntityId(
                          route.parent?.parent?.parent?.paramMap.get('anlassId'),
                        );
                        inject(UntersuchungGroesseGewichtStore).loadGroesseGewicht({
                          anlassId: currentAnlassId,
                          einladungId: currentEinladungId,
                        });
                      },
                    },
                    canActivate: [requireRole(hasGroesseGewichtViewBerechtigung())],
                    canDeactivate: [confirmOnDirty()],
                  },
                  {
                    path: Path.ANLASS_UNTERSUCHUNG_EINLADUNG_TRIAGE,
                    component: UntersuchungTriageComponent,
                    resolve: {
                      _store: (route: ActivatedRouteSnapshot) => {
                        const currentEinladungId = requireValidBackendEntityId(route.params['id']);
                        const currentAnlassId = requireValidBackendEntityId(
                          route.parent?.parent?.parent?.paramMap.get('anlassId'),
                        );
                        inject(UntersuchungTriageStore).loadTriage({
                          anlassId: currentAnlassId,
                          einladungId: currentEinladungId,
                        });
                      },
                    },
                    canActivate: [requireRole(hasTriageViewBerechtigung())],
                    canDeactivate: [confirmOnDirty()],
                  },
                  {
                    path: Path.ANLASS_UNTERSUCHUNG_EINLADUNG_ARZT,
                    component: UntersuchungArztComponent,
                    resolve: {
                      _store: (route: ActivatedRouteSnapshot) => {
                        const currentEinladungId = requireValidBackendEntityId(route.params['id']);
                        const currentAnlassId = requireValidBackendEntityId(
                          route.parent?.parent?.parent?.paramMap.get('anlassId'),
                        );
                        inject(UntersuchungArztStore).loadUntersuchungArzt({
                          anlassId: currentAnlassId,
                          einladungId: currentEinladungId,
                        });
                        inject(UntersuchungArztStore).loadUntersuchungArztBemerkung({
                          anlassId: currentAnlassId,
                          einladungId: currentEinladungId,
                        });
                      },
                    },
                    canActivate: [requireRole(hasAerztlicheUntersuchungViewBerechtigung())],
                    canDeactivate: [confirmOnDirty()],
                  },
                  {
                    path: Path.ANLASS_UNTERSUCHUNG_EINLADUNG_LAEUSEKONTROLLE,
                    component: UntersuchungLaeusekontrolleComponent,
                    resolve: {
                      _store: (route: ActivatedRouteSnapshot) => {
                        const currentEinladungId = requireValidBackendEntityId(route.params['id']);
                        const currentAnlassId = requireValidBackendEntityId(
                          route.parent?.parent?.parent?.paramMap.get('anlassId'),
                        );
                        inject(UntersuchungLaeusekontrolleStore).loadLaeusekontrolle({
                          anlassId: currentAnlassId,
                          einladungId: currentEinladungId,
                        });
                      },
                    },
                    canActivate: [requireRole(hasLaeusekontrolleViewBerechtigung())],
                    canDeactivate: [confirmOnDirty()],
                  },
                  {
                    path: Path.ANLASS_UNTERSUCHUNG_EINLADUNG_MASSNAHMEN,
                    component: UntersuchungMassnahmenComponent,
                    resolve: {
                      _store: (route: ActivatedRouteSnapshot) => {
                        const currentEinladungId = requireValidBackendEntityId(route.params['id']);
                        const currentAnlassId = requireValidBackendEntityId(
                          route.parent?.parent?.parent?.paramMap.get('anlassId'),
                        );
                        inject(UntersuchungMassnahmenStore).loadMassnahmen({
                          anlassId: currentAnlassId,
                          einladungId: currentEinladungId,
                        });
                        inject(UntersuchungImpfungenStore).loadUntersuchungImpfungenData({
                          anlassId: currentAnlassId,
                          einladungId: currentEinladungId,
                        });
                        inject(UntersuchungSehenStore).loadSehenBeurteilung({
                          anlassId: currentAnlassId,
                          einladungId: currentEinladungId,
                        });
                        inject(UntersuchungSehenStore).loadSehenMassnahmen({
                          anlassId: currentAnlassId,
                          einladungId: currentEinladungId,
                        });
                        inject(UntersuchungSehenStore).loadSehenBemerkungFragebogen({
                          anlassId: currentAnlassId,
                          einladungId: currentEinladungId,
                        });
                        inject(UntersuchungHoerenAudiogrammStore).loadHoerenAudiogramm({
                          anlassId: currentAnlassId,
                          einladungId: currentEinladungId,
                        });
                        inject(UntersuchungHoerenOtoskopieStore).loadHoerenOtoskopie({
                          anlassId: currentAnlassId,
                          einladungId: currentEinladungId,
                        });
                        inject(UntersuchungGroesseGewichtStore).loadGroesseGewicht({
                          anlassId: currentAnlassId,
                          einladungId: currentEinladungId,
                        });
                        inject(UntersuchungArztStore).loadUntersuchungArzt({
                          anlassId: currentAnlassId,
                          einladungId: currentEinladungId,
                        });
                        inject(UntersuchungArztStore).loadUntersuchungArztBemerkung({
                          anlassId: currentAnlassId,
                          einladungId: currentEinladungId,
                        });
                      },
                    },
                    canActivate: [requireRole(hasMassnahmenViewBerechtigung())],
                    canDeactivate: [confirmOnDirty()],
                  },
                ],
              },
            ],
          },
        ],
      },
    ],
  },
];
