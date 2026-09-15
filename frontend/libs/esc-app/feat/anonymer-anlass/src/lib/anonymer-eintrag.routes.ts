/*
 * Copyright (C) 2026 DV Bern AG, Switzerland
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

import {ActivatedRouteSnapshot, Route, Router} from '@angular/router';
import {toObservable} from '@angular/core/rxjs-interop';
import {AnlassConfigOptionsStore, ImpfstoffeChargenStore, Path, requireAcceptedAgb, requireRole} from '@dvb/feat-shared';
import {
  AnlassKinderStore,
  AnlassStore,
  UntersuchungAbschlussPageComponent,
  UntersuchungAbschlussStore,
  UntersuchungArztComponent,
  UntersuchungArztStore,
  UntersuchungBewegungsapparatComponent,
  UntersuchungBewegungsapparatStore,
  UntersuchungGroessegewichtComponent,
  UntersuchungGroesseGewichtStore,
  UntersuchungHoerenAudiogrammStore,
  UntersuchungHoerenComponent,
  UntersuchungHoerenOtoskopieStore,
  UntersuchungImpfungenComponent,
  UntersuchungImpfungenStore,
  UntersuchungSchulrelevanteBeeintraechtigungComponent,
  UntersuchungSchulrelevanteBeeintraechtigungStore,
  UntersuchungSehenComponent,
  UntersuchungSehenStore,
  UntersuchungSignaturStore,
  UntersuchungSpracheComponent,
  UntersuchungSpracheStore,
  UntersuchungStore,
  UntersuchungVorabklaerungenComponent,
  UntersuchungVorabklaerungenStore,
} from '@dvb/feat-anlass';
import {DummyTabComponent} from './anonymer-anlass/anonyme-untersuchung/dummy-tab/dummy-tab.component';
import {requireValidBackendEntityId, toAnonymerEintragBackendEntityIdWithClass,} from "@dvb/data-access-types";
import {inject} from "@angular/core";
import {AnonymeUntersuchungComponent} from "./anonymer-anlass/anonyme-untersuchung/anonyme-untersuchung.component";
import {AnonymerEintragComponent} from "./anonymer-anlass/anonymer-eintrag.component";
import {AnonymerEintragStore} from "./anonymer-anlass/anonymer-eintrag.store";
import {AnonymerEintragBasisdatenComponent} from "./anonymer-anlass/basisdaten/anonymer-eintrag-basisdaten.component";
import {confirmOnDirty} from "@dvb/util-angular";
import {
  confirmAnonymeUntersuchungVerlassen, confirmOnDirtyBetweenUntersuchungsTabs
} from "./anonymer-anlass/anonyme-untersuchung/confirm-anonyme-untersuchung-verlassen.candeactivatefn";
import {
  hasAerztlicheUntersuchungEditBerechtigung,
  hasGroesseGewichtViewBerechtigung,
  hasHoerenEditBerechtigung,
  hasImpfungenEditBerechtigung,
  hasSehenEditBerechtigung,
  hasUntersuchungAbschlussViewBerechtigung,
  hasUntersuchungVorabklaerungenEditBerechtigung
} from "@dvb/util-shared";
import {catchError, filter, firstValueFrom, map, of, take, timeout} from 'rxjs';


const redirectToFirstVisibleAnonymeUntersuchungTab = async (route: ActivatedRouteSnapshot) => {
  const router = inject(Router);
  const anlassStore = inject(AnlassStore);
  const anlassId = requireValidBackendEntityId(route.parent?.parent?.params['anlassId']);

  if (anlassStore.selectedEntity()?.id !== anlassId) {
    anlassStore.setSelectedEntityId(anlassId);
    anlassStore.loadEntity(anlassId);
  }

  const config = await firstValueFrom(
    toObservable(anlassStore.selectedEntity).pipe(
      map(entity => {
        if (entity?.id !== anlassId) {
          return undefined;
        }

        return entity.anlassConfig;
      }),
      filter(configOrUndefined => !!configOrUndefined),
      take(1),
      timeout(5000),
      catchError(() => of(undefined)),
    ),
  );

  if (!config) {
    return true;
  }

  const tabs: { path: string; visible: boolean }[] = [
    {path: Path.ANLASS_UNTERSUCHUNG_EINLADUNG_VORABKLAERUNGEN, visible: config.showTabVorabklaerungen},
    {path: Path.ANLASS_UNTERSUCHUNG_EINLADUNG_IMPFUNGEN, visible: config.showTabImpfungen},
    {path: Path.ANLASS_UNTERSUCHUNG_EINLADUNG_SEHEN, visible: config.showTabSehen},
    {path: Path.ANLASS_UNTERSUCHUNG_EINLADUNG_HOEREN, visible: config.showTabHoeren},
    {path: Path.ANLASS_UNTERSUCHUNG_EINLADUNG_GROESSEGEWICHT, visible: config.showTabGroesseGewicht},
    {path: Path.ANLASS_UNTERSUCHUNG_EINLADUNG_ARZT, visible: config.showTabUntersuchung},
    {path: Path.ANLASS_UNTERSUCHUNG_EINLADUNG_SPRACHE, visible: config.showTabSprache},
    {
      path: Path.ANLASS_UNTERSUCHUNG_EINLADUNG_SCHULRELEVANTE_BEEINTRAECHTIGUNG,
      visible: config.showTabSchulrelevanteBeeintraechtigung
    },
    {path: Path.ANLASS_UNTERSUCHUNG_EINLADUNG_BEWEGUNGSAPPARAT, visible: config.showTabBewegungsapparat},
    {path: Path.ANLASS_UNTERSUCHUNG_EINLADUNG_ABSCHLUSS, visible: config.showTabAbschluss},
  ];

  const firstVisibleTab = tabs.find(tab => tab.visible);

  if (!firstVisibleTab) {
    return true;
  }

  const segments = route.pathFromRoot.flatMap(s => s.url.map(seg => seg.path));
  return router.createUrlTree([...segments, firstVisibleTab.path]);
};

export const anonymerEintragRoutes: Route[] = [
  {
    path: '',
    component: AnonymerEintragComponent,
    resolve: {
      _store: () => {
        inject(AnlassConfigOptionsStore).loadAnlassConfigOptions();
      }
    },
    providers: [
      AnonymerEintragStore,
      AnlassConfigOptionsStore,
      AnlassStore,
      UntersuchungStore,
      UntersuchungVorabklaerungenStore,
      UntersuchungHoerenAudiogrammStore,
      UntersuchungHoerenOtoskopieStore,
      UntersuchungGroesseGewichtStore,
      AnlassKinderStore,
      UntersuchungSpracheStore,
      UntersuchungSehenStore,
      UntersuchungSchulrelevanteBeeintraechtigungStore,
      UntersuchungArztStore,
      UntersuchungBewegungsapparatStore,
      UntersuchungAbschlussStore,
      UntersuchungSignaturStore,
      UntersuchungImpfungenStore,
    ],
    children: [
      {
        path: '',
        pathMatch: 'full',
        redirectTo: Path.ANONYM_BASISDATEN,
      },
      {
        path: Path.ANONYM_BASISDATEN,
        component: AnonymerEintragBasisdatenComponent,
        canDeactivate: [confirmOnDirty()],
      },
      {
        path: ':anlassId/' + Path.ANONYM_UNTERSUCHUNG,
        component: AnonymeUntersuchungComponent,
        resolve: {
          _store: (route: ActivatedRouteSnapshot) => {
            const anlassStore = inject(AnlassStore);
            const anlassId = requireValidBackendEntityId(route.params['anlassId']);
            anlassStore.setSelectedEntityId(anlassId);
            anlassStore.loadEntity(anlassId);
          },
        },
        canDeactivate: [confirmAnonymeUntersuchungVerlassen()],
        children: [
          {
            path: Path.ANLASS_UNTERSUCHUNG_EINLADUNG + '/:anonymerEintragId',
            resolve: {
              _store: (route: ActivatedRouteSnapshot) => {
                const einladungId = requireValidBackendEntityId(route.params['anonymerEintragId']);
                const anlassId = requireValidBackendEntityId(route.parent?.params['anlassId']);
                const untersuchungStore = inject(UntersuchungStore);
                untersuchungStore.setAnonymusTab(einladungId);
                untersuchungStore.loadTabsSaved({
                  einladungId: toAnonymerEintragBackendEntityIdWithClass(einladungId),
                  anlassId,
                })
              },
            },
            // TODO ESC-1301
            //  tab remove DummyTabComponent
            children: [
              {
                path: '',
                pathMatch: 'full',
                canActivate: [redirectToFirstVisibleAnonymeUntersuchungTab],
                component: DummyTabComponent,
              },
              {
                path: Path.ANLASS_UNTERSUCHUNG_EINLADUNG_VORABKLAERUNGEN,
                component: UntersuchungVorabklaerungenComponent,
                resolve: {
                  kindDate: (route: ActivatedRouteSnapshot) => {
                    const plainEinladungId = requireValidBackendEntityId(route.params['anonymerEintragId']);
                    const params = {
                      anlassId: requireValidBackendEntityId(route.parent?.parent?.paramMap.get('anlassId')),
                      einladungId: toAnonymerEintragBackendEntityIdWithClass(plainEinladungId),
                    };
                    inject(UntersuchungVorabklaerungenStore).loadUntersuchungVorabklaerungen(params);
                    inject(UntersuchungStore).setCurrentTabId(plainEinladungId);
                  },
                },
                canActivate: [
                  requireRole(hasUntersuchungVorabklaerungenEditBerechtigung()),
                  requireAcceptedAgb(),
                ],
                canDeactivate: [confirmOnDirtyBetweenUntersuchungsTabs()],
              },
              {
                path: Path.ANLASS_UNTERSUCHUNG_EINLADUNG_GROESSEGEWICHT,
                component: UntersuchungGroessegewichtComponent,
                resolve: {
                  _store: (route: ActivatedRouteSnapshot) => {
                    const params = {
                      einladungId: toAnonymerEintragBackendEntityIdWithClass(requireValidBackendEntityId(route.params['anonymerEintragId'])),
                      anlassId: requireValidBackendEntityId(route.parent?.parent?.paramMap.get('anlassId')),
                    };
                    inject(UntersuchungGroesseGewichtStore).loadGroesseGewicht(params);
                  }
                },
                canActivate: [
                  requireRole(hasGroesseGewichtViewBerechtigung()),
                  requireAcceptedAgb(),
                ],
                canDeactivate: [confirmOnDirtyBetweenUntersuchungsTabs()],
              },
              {
                path: Path.ANLASS_UNTERSUCHUNG_EINLADUNG_HOEREN,
                component: UntersuchungHoerenComponent,
                resolve: {
                  _store: (route: ActivatedRouteSnapshot) => {
                    const params = {
                      einladungId: toAnonymerEintragBackendEntityIdWithClass(requireValidBackendEntityId(route.params['anonymerEintragId'])),
                      anlassId: requireValidBackendEntityId(route.parent?.parent?.paramMap.get('anlassId')),
                    };
                    inject(UntersuchungHoerenAudiogrammStore).loadDefaultHoerenAudiogramm(params);
                    inject(UntersuchungHoerenOtoskopieStore).loadDefaultHoerenOtoskopie(params);
                    inject(UntersuchungHoerenOtoskopieStore).loadHoerenOtoskopie(params);
                    inject(UntersuchungHoerenAudiogrammStore).loadHoerenAudiogramm(params);
                  },
                },
                canActivate: [
                  requireRole(hasHoerenEditBerechtigung()),
                  requireAcceptedAgb(),
                ],
                canDeactivate: [confirmOnDirtyBetweenUntersuchungsTabs()],
              },
              {
                path: Path.ANLASS_UNTERSUCHUNG_EINLADUNG_SEHEN,
                component: UntersuchungSehenComponent,
                resolve: {
                  _store: (route: ActivatedRouteSnapshot) => {
                    const params = {
                      einladungId: toAnonymerEintragBackendEntityIdWithClass(requireValidBackendEntityId(route.params['anonymerEintragId'])),
                      anlassId: requireValidBackendEntityId(route.parent?.parent?.paramMap.get('anlassId')),
                    };
                    inject(UntersuchungSehenStore).loadDefaultSehenBeurteilung(params);
                    inject(UntersuchungSehenStore).loadSehenBeurteilung(params);
                    inject(UntersuchungSehenStore).loadSehenMassnahmen(params);
                    inject(UntersuchungSehenStore).loadSehenBemerkungFragebogen(params);
                  },
                },
                canActivate: [
                  requireRole(hasSehenEditBerechtigung()),
                  requireAcceptedAgb(),
                ],
                canDeactivate: [confirmOnDirtyBetweenUntersuchungsTabs()],
              },
              {
                path: Path.ANLASS_UNTERSUCHUNG_EINLADUNG_IMPFUNGEN,
                component: UntersuchungImpfungenComponent,
                resolve: {
                  _store: (route: ActivatedRouteSnapshot) => {
                    const currentEinladungId = toAnonymerEintragBackendEntityIdWithClass(requireValidBackendEntityId(route.params['anonymerEintragId']));
                    const currentAnlassId = requireValidBackendEntityId(
                      route.parent?.parent?.paramMap.get('anlassId'),
                    );
                    inject(ImpfstoffeChargenStore).loadImpfstoffeAndChargen();
                    const untersuchungImpfungenStore = inject(UntersuchungImpfungenStore);
                    untersuchungImpfungenStore.loadUntersuchungImpfungenData({
                      anlassId: currentAnlassId,
                      einladungId: currentEinladungId,
                    });
                  },
                },
                canActivate: [
                  requireRole(hasImpfungenEditBerechtigung()),
                  requireAcceptedAgb(),
                ],
                canDeactivate: [confirmOnDirtyBetweenUntersuchungsTabs()],
              },
              {
                path: Path.ANLASS_UNTERSUCHUNG_EINLADUNG_SPRACHE,
                component: UntersuchungSpracheComponent,
                resolve: {
                  _store: (route: ActivatedRouteSnapshot) => {
                    const params = {
                      einladungId: toAnonymerEintragBackendEntityIdWithClass(requireValidBackendEntityId(route.params['anonymerEintragId'])),
                      anlassId: requireValidBackendEntityId(route.parent?.parent?.paramMap.get('anlassId')),
                    };
                    inject(UntersuchungSpracheStore).loadUntersuchungSprache(params);
                    inject(UntersuchungSpracheStore).loadUntersuchungSpracheDefaults(params);
                  },
                },
                canActivate: [
                  requireRole(hasAerztlicheUntersuchungEditBerechtigung()),
                  requireAcceptedAgb(),
                ],
                canDeactivate: [confirmOnDirtyBetweenUntersuchungsTabs()],
              },
              {
                path: Path.ANLASS_UNTERSUCHUNG_EINLADUNG_BEWEGUNGSAPPARAT,
                component: UntersuchungBewegungsapparatComponent,
                resolve: {
                  _store: (route: ActivatedRouteSnapshot) => {
                    const params = {
                      einladungId: toAnonymerEintragBackendEntityIdWithClass(requireValidBackendEntityId(route.params['anonymerEintragId'])),
                      anlassId: requireValidBackendEntityId(route.parent?.parent?.paramMap.get('anlassId')),
                    };
                    inject(UntersuchungBewegungsapparatStore).loadBewegungsapparat(params);
                    inject(UntersuchungBewegungsapparatStore).loadBewegungsapparatDefaults(params);
                  },
                },
                canActivate: [
                  requireRole(hasAerztlicheUntersuchungEditBerechtigung()),
                  requireAcceptedAgb(),
                ],
                canDeactivate: [confirmOnDirtyBetweenUntersuchungsTabs()],
              },
              {
                path: Path.ANLASS_UNTERSUCHUNG_EINLADUNG_SCHULRELEVANTE_BEEINTRAECHTIGUNG,
                component: UntersuchungSchulrelevanteBeeintraechtigungComponent,
                resolve: {
                  _store: (route: ActivatedRouteSnapshot) => {
                    const params = {
                      einladungId: toAnonymerEintragBackendEntityIdWithClass(requireValidBackendEntityId(route.params['anonymerEintragId'])),
                      anlassId: requireValidBackendEntityId(route.parent?.parent?.paramMap.get('anlassId')),
                    };
                    inject(UntersuchungSchulrelevanteBeeintraechtigungStore).loadUntersuchungSchulrelevanteBeeintraechtigung(params);
                    inject(UntersuchungSchulrelevanteBeeintraechtigungStore).loadUntersuchungSchulrelevanteBeeintraechtigungDefaults(params);
                  },
                },
                canActivate: [
                  requireRole(hasAerztlicheUntersuchungEditBerechtigung()),
                  requireAcceptedAgb(),
                ],
                canDeactivate: [confirmOnDirtyBetweenUntersuchungsTabs()],
              },
              {
                path: Path.ANLASS_UNTERSUCHUNG_EINLADUNG_ARZT,
                component: UntersuchungArztComponent,
                resolve: {
                  _store: (route: ActivatedRouteSnapshot) => {
                    const params = {
                      einladungId: toAnonymerEintragBackendEntityIdWithClass(requireValidBackendEntityId(route.params['anonymerEintragId'])),
                      anlassId: requireValidBackendEntityId(route.parent?.parent?.paramMap.get('anlassId')),
                    };
                    inject(UntersuchungArztStore).loadDefaultUntersuchungArzt(params);
                    inject(UntersuchungArztStore).loadUntersuchungArzt(params);
                    inject(UntersuchungArztStore).loadUntersuchungArztBemerkung(params);
                  },
                },
                canActivate: [
                  requireRole(hasAerztlicheUntersuchungEditBerechtigung()),
                  requireAcceptedAgb(),
                ],
                canDeactivate: [confirmOnDirtyBetweenUntersuchungsTabs()],
              },
              {
                path: Path.ANLASS_UNTERSUCHUNG_EINLADUNG_ABSCHLUSS,
                component: UntersuchungAbschlussPageComponent,
                resolve: {
                  _store: (route: ActivatedRouteSnapshot) => {
                    const params = {
                      einladungId: toAnonymerEintragBackendEntityIdWithClass(requireValidBackendEntityId(route.params['anonymerEintragId'])),
                      anlassId: requireValidBackendEntityId(route.parent?.parent?.paramMap.get('anlassId')),
                    };
                    inject(UntersuchungAbschlussStore).loadUntersuchungAbschluss(params);
                    inject(UntersuchungSignaturStore).loadUntersuchungSignatur(params);
                  }
                },
                canActivate: [
                  requireRole(hasUntersuchungAbschlussViewBerechtigung()),
                  requireAcceptedAgb(),
                ],
                canDeactivate: [confirmOnDirtyBetweenUntersuchungsTabs()],
              },
            ]
          },
        ]
      },
    ],
  }
];
