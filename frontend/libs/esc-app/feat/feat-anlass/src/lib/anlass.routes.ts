import {inject} from '@angular/core';
import {ActivatedRouteSnapshot, Route} from '@angular/router';
import {AnlassSaveJson, NotizTyp} from '@dvb/data-access-backend';
import {requireValidBackendEntityId, requireValidBackendEntityIds} from '@dvb/data-access-types';
import {
  AnlassConfigOptionsStore,
  BenutzerStore,
  ImpfstoffeChargenStore,
  InfomaterialStore,
  LabelsStore,
  Path,
  requireAcceptedAgb,
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
  hasUntersuchungAbschlussViewBerechtigung,
  hasUntersuchungKindDatenViewBerechtigung,
  hasUntersuchungVorabklaerungenViewBerechtigung,
  isValidEnumValue,
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
import {
  EinladungNotizHochladenPageComponent,
} from './anlass/anlasse-detail/anlass-planung/notizen/notizen-hochladen/einladung-notiz-hochladen.page.component';
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
  UntersuchungLaeusekontrolleComponent,
} from './anlass/untersuchung/untersuchung-laeusekontrolle/untersuchung-laeusekontrolle.component';
import {
  UntersuchungLaeusekontrolleStore,
} from './anlass/untersuchung/untersuchung-laeusekontrolle/untersuchung-laeusekontrolle.store';
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
import {UntersuchungSpracheComponent} from "./anlass/untersuchung/untersuchung-sprache/untersuchung-sprache.component";
import {UntersuchungSpracheStore} from "./anlass/untersuchung/untersuchung-sprache/untersuchung-sprache.store";
import {
  UntersuchungBewegungsapparatStore
} from './anlass/untersuchung/untersuchung-bewegungsapparat/untersuchung-bewegungsapparat.store';
import {
  UntersuchungBewegungsapparatComponent
} from './anlass/untersuchung/untersuchung-bewegungsapparat/untersuchung-bewegungsapparat.component';
import {
  UntersuchungSchulrelevanteBeeintraechtigungComponent
} from "./anlass/untersuchung/untersuchung-schulrelevante-beeintraechtigung/untersuchung-schulrelevante-beeintraechtigung.component";
import {
  UntersuchungSchulrelevanteBeeintraechtigungStore
} from "./anlass/untersuchung/untersuchung-schulrelevante-beeintraechtigung/untersuchung-schulrelevante-beeintraechtigung.store";
import {
  UntersuchungAbschlussStore
} from "./anlass/untersuchung/untersuchung-abschluss-page/untersuchung-abschluss/untersuchung-abschluss.store";
import {
  UntersuchungVorabklaerungenStore
} from "./anlass/untersuchung/untersuchung-vorabklaerungen/untersuchung-vorabklaerungen.store";
import {
  UntersuchungVorabklaerungenComponent
} from "./anlass/untersuchung/untersuchung-vorabklaerungen/untersuchung-vorabklaerungen.component";
import {
  UntersuchungAbschlussPageComponent
} from "./anlass/untersuchung/untersuchung-abschluss-page/untersuchung-abschluss-page.component";
import {
  UntersuchungSignaturStore
} from "./anlass/untersuchung/untersuchung-abschluss-page/untersuchung-signatur/untersuchung-signatur.store";

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
            path: Path.ANLASS_UNTERSUCHUNG_EINLADUNG + '/:einladungId/' + Path.NOTIZEN + '/:notizTyp',
            component: EinladungNotizPageComponent,
            canActivate: [
              requireRole(hasAllNotizenEditBerechtigung()),
              requireAcceptedAgb(),
            ],
            canDeactivate: [confirmOnDirty()],
            resolve: {
              _labels: () => inject(LabelsStore).loadLabelsList(),
              _store: (route: ActivatedRouteSnapshot) => {
                const einladungId = requireValidBackendEntityId(route.params['einladungId']);
                inject(AnlassKinderStore).setSelectedEinladungId(einladungId);
                const notizTyp = requireValidNotizTyp(route.params['notizTyp']);
                inject(AnlassKinderStore).setCurrentNotizTyp(notizTyp);
              },
            },
          },
          {
            path: Path.ANLASS_UNTERSUCHUNG_EINLADUNG + '/:einladungId/' + Path.NOTIZEN + '/:notizTyp/' + Path.UPLOAD,
            component: EinladungNotizHochladenPageComponent,
            canActivate: [
              requireRole(hasAllNotizenEditBerechtigung()),
              requireAcceptedAgb(),
            ],
            resolve: {
              _labels: () => inject(LabelsStore).loadLabelsList(),
              _store: (route: ActivatedRouteSnapshot) => {
                const einladungId = requireValidBackendEntityId(route.params['einladungId']);
                inject(AnlassKinderStore).setSelectedEinladungId(einladungId);
                const notizTyp = requireValidNotizTyp(route.params['notizTyp']);
                inject(AnlassKinderStore).setCurrentNotizTyp(notizTyp);
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
                  UntersuchungVorabklaerungenStore,
                  UntersuchungImpfungenStore,
                  UntersuchungSehenStore,
                  UntersuchungHoerenAudiogrammStore,
                  UntersuchungHoerenOtoskopieStore,
                  UntersuchungGroesseGewichtStore,
                  UntersuchungTriageStore,
                  UntersuchungArztStore,
                  UntersuchungSpracheStore,
                  UntersuchungLaeusekontrolleStore,
                  UntersuchungSchulrelevanteBeeintraechtigungStore,
                  UntersuchungBewegungsapparatStore,
                  UntersuchungMassnahmenStore,
                  UntersuchungAbschlussStore,
                  UntersuchungSignaturStore,
                ],
                resolve: {
                  _store: (route: ActivatedRouteSnapshot) => {
                    const currentEinladungId = requireValidBackendEntityId(route.params['id']);
                    const anlassId = requireValidBackendEntityId(route.parent?.parent?.paramMap.get('anlassId'));
                    const params = ensureProps<AnlassEinladungenId>({
                      anlassId: anlassId,
                      einladungIds: [currentEinladungId],
                    });
                    inject(UntersuchungStore).setCurrentTabId(currentEinladungId);
                    inject(UntersuchungStore).loadEinladung(params);
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
                    canActivate: [
                      requireRole(hasUntersuchungKindDatenViewBerechtigung()),
                      requireAcceptedAgb(),
                    ],
                    canDeactivate: [confirmOnDirty()],
                  },
                  {
                    path: Path.ANLASS_UNTERSUCHUNG_EINLADUNG_VORABKLAERUNGEN,
                    component: UntersuchungVorabklaerungenComponent,
                    resolve: {
                      kindDate: (route: ActivatedRouteSnapshot) => {
                        const anlassId = requireValidBackendEntityId(
                          route.parent?.parent?.parent?.paramMap.get('anlassId'),
                        );
                        const einladungId = requireValidBackendEntityId(route.params['id']);
                        inject(UntersuchungVorabklaerungenStore).loadUntersuchungVorabklaerungen({
                          anlassId,
                          einladungId,
                        });
                        inject(UntersuchungStore).setCurrentTabId(einladungId);
                      },
                    },
                    canActivate: [
                      requireRole(hasUntersuchungVorabklaerungenViewBerechtigung()),
                      requireAcceptedAgb(),
                    ],
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
                        inject(ImpfstoffeChargenStore).loadImpfstoffeAndChargen();
                        const untersuchungImpfungenStore = inject(UntersuchungImpfungenStore);
                        untersuchungImpfungenStore.loadUntersuchungImpfungenData({
                          anlassId: currentAnlassId,
                          einladungId: currentEinladungId,
                        });
                        untersuchungImpfungenStore.initializeFolgeTermine({currentAnlassId});
                      },
                    },
                    canActivate: [
                      requireRole(hasImpfungenViewBerechtigung()),
                      requireAcceptedAgb(),
                    ],
                    canDeactivate: [confirmOnDirty()],
                  },
                  {
                    path: Path.ANLASS_UNTERSUCHUNG_EINLADUNG_SEHEN,
                    component: UntersuchungSehenComponent,
                    resolve: {
                      _store: (route: ActivatedRouteSnapshot) => {
                        const params = {
                          anlassId: requireValidBackendEntityId(route.parent?.parent?.parent?.paramMap.get('anlassId')),
                          einladungId: requireValidBackendEntityId(route.params['id']),
                        };
                        inject(UntersuchungSehenStore).loadDefaultSehenBeurteilung(params);
                        inject(UntersuchungSehenStore).loadSehenBeurteilung(params);
                        inject(UntersuchungSehenStore).loadSehenMassnahmen(params);
                        inject(UntersuchungSehenStore).loadSehenBemerkungFragebogen(params);
                      },
                    },
                    canActivate: [
                      requireRole(hasSehenViewBerechtigung()),
                      requireAcceptedAgb(),
                    ],
                    canDeactivate: [confirmOnDirty()],
                  },
                  {
                    path: Path.ANLASS_UNTERSUCHUNG_EINLADUNG_HOEREN,
                    component: UntersuchungHoerenComponent,
                    resolve: {
                      _store: (route: ActivatedRouteSnapshot) => {
                        const params = {
                          einladungId: requireValidBackendEntityId(route.params['id']),
                          anlassId: requireValidBackendEntityId(route.parent?.parent?.parent?.paramMap.get('anlassId')),
                        };
                        inject(UntersuchungHoerenAudiogrammStore).loadDefaultHoerenAudiogramm(params);
                        inject(UntersuchungHoerenOtoskopieStore).loadDefaultHoerenOtoskopie(params);
                        inject(UntersuchungHoerenAudiogrammStore).loadHoerenAudiogramm(params);
                        inject(UntersuchungHoerenOtoskopieStore).loadHoerenOtoskopie(params);
                      },
                    },
                    canActivate: [
                      requireRole(hasHoerenViewBerechtigung()),
                      requireAcceptedAgb(),
                    ],
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
                    canActivate: [
                      requireRole(hasGroesseGewichtViewBerechtigung()),
                      requireAcceptedAgb(),
                    ],
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
                    canActivate: [
                      requireRole(hasTriageViewBerechtigung()),
                      requireAcceptedAgb(),
                    ],
                    canDeactivate: [confirmOnDirty()],
                  },
                  {
                    path: Path.ANLASS_UNTERSUCHUNG_EINLADUNG_ARZT,
                    component: UntersuchungArztComponent,
                    resolve: {
                      _store: (route: ActivatedRouteSnapshot) => {
                        const params = {
                          einladungId: requireValidBackendEntityId(route.params['id']),
                          anlassId: requireValidBackendEntityId(route.parent?.parent?.parent?.paramMap.get('anlassId')),
                        };
                        inject(UntersuchungArztStore).loadDefaultUntersuchungArzt(params);
                        inject(UntersuchungArztStore).loadUntersuchungArzt(params);
                        inject(UntersuchungArztStore).loadUntersuchungArztBemerkung(params);
                      },
                    },
                    canActivate: [
                      requireRole(hasAerztlicheUntersuchungViewBerechtigung()),
                      requireAcceptedAgb(),
                    ],
                    canDeactivate: [confirmOnDirty()],
                  },
                  {
                    path: Path.ANLASS_UNTERSUCHUNG_EINLADUNG_SPRACHE,
                    component: UntersuchungSpracheComponent,
                    resolve: {
                      _store: (route: ActivatedRouteSnapshot) => {
                        const params = {
                          einladungId: requireValidBackendEntityId(route.params['id']),
                          anlassId: requireValidBackendEntityId(route.parent?.parent?.parent?.paramMap.get('anlassId')),
                        };
                        inject(UntersuchungSpracheStore).loadUntersuchungSprache(params);
                      },
                    },
                    canActivate: [
                      requireRole(hasAerztlicheUntersuchungViewBerechtigung()),
                      requireAcceptedAgb(),
                    ],
                    canDeactivate: [confirmOnDirty()],
                  },
                  {
                    path: Path.ANLASS_UNTERSUCHUNG_EINLADUNG_SCHULRELEVANTE_BEEINTRAECHTIGUNG,
                    component: UntersuchungSchulrelevanteBeeintraechtigungComponent,
                    resolve: {
                      _store: (route: ActivatedRouteSnapshot) => {
                        const params = {
                          einladungId: requireValidBackendEntityId(route.params['id']),
                          anlassId: requireValidBackendEntityId(route.parent?.parent?.parent?.paramMap.get('anlassId')),
                        };
                        inject(UntersuchungSchulrelevanteBeeintraechtigungStore)
                          .loadUntersuchungSchulrelevanteBeeintraechtigung(params);
                      },
                    },
                    canActivate: [
                      requireRole(hasAerztlicheUntersuchungViewBerechtigung()),
                      requireAcceptedAgb(),
                    ],
                    canDeactivate: [confirmOnDirty()],
                  },
                  {
                    path: Path.ANLASS_UNTERSUCHUNG_EINLADUNG_BEWEGUNGSAPPARAT,
                    component: UntersuchungBewegungsapparatComponent,
                    resolve: {
                      _store: (route: ActivatedRouteSnapshot) => {
                        const currentEinladungId = requireValidBackendEntityId(route.params['id']);
                        const currentAnlassId = requireValidBackendEntityId(
                          route.parent?.parent?.parent?.paramMap.get('anlassId'),
                        );
                        inject(UntersuchungBewegungsapparatStore).loadBewegungsapparat({
                          anlassId: currentAnlassId,
                          einladungId: currentEinladungId,
                        });
                      },
                    },
                    canActivate: [
                      requireRole(hasAerztlicheUntersuchungViewBerechtigung()),
                      requireAcceptedAgb(),
                    ],
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
                    canActivate: [
                      requireRole(hasLaeusekontrolleViewBerechtigung()),
                      requireAcceptedAgb(),
                    ],
                    canDeactivate: [confirmOnDirty()],
                  },
                  {
                    path: Path.ANLASS_UNTERSUCHUNG_EINLADUNG_ABSCHLUSS,
                    component: UntersuchungAbschlussPageComponent,
                    resolve: {
                      _store: (route: ActivatedRouteSnapshot) => {
                        const currentEinladungId = requireValidBackendEntityId(route.params['id']);
                        const currentAnlassId = requireValidBackendEntityId(
                          route.parent?.parent?.parent?.paramMap.get('anlassId'),
                        );
                        inject(UntersuchungAbschlussStore).loadUntersuchungAbschluss({
                          anlassId: currentAnlassId,
                          einladungId: currentEinladungId,
                        });
                        inject(UntersuchungSignaturStore).loadUntersuchungSignatur({
                          anlassId: currentAnlassId,
                          einladungId: currentEinladungId,
                        });
                      },
                    },
                    canActivate: [
                      requireRole(hasUntersuchungAbschlussViewBerechtigung()),
                      requireAcceptedAgb(),
                    ],
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
                        inject(UntersuchungVorabklaerungenStore).loadUntersuchungVorabklaerungen({
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
                        inject(UntersuchungSpracheStore).loadUntersuchungSprache({
                          anlassId: currentAnlassId,
                          einladungId: currentEinladungId,
                        });
                        inject(UntersuchungBewegungsapparatStore).loadBewegungsapparat({
                          anlassId: currentAnlassId,
                          einladungId: currentEinladungId,
                        });
                        inject(UntersuchungSchulrelevanteBeeintraechtigungStore)
                          .loadUntersuchungSchulrelevanteBeeintraechtigung({
                            anlassId: currentAnlassId,
                            einladungId: currentEinladungId,
                          });
                      },
                    },
                    canActivate: [
                      requireRole(hasMassnahmenViewBerechtigung()),
                      requireAcceptedAgb(),
                    ],
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

export function requireValidNotizTyp(value: unknown): NotizTyp {
  if (!isValidNotizTyp(value)) {
    throw new Error(`Invalid NotizTyp: ${value}`);
  }
  return value;
}

export function isValidNotizTyp(value: unknown): value is NotizTyp {
  if (typeof value !== 'string') {
    return false;
  }
  return isValidEnumValue(NotizTyp, value.toUpperCase())

}
