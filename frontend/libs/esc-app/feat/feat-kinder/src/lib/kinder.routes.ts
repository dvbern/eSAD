import {inject} from '@angular/core';
import {ActivatedRouteSnapshot, Route} from '@angular/router';
import {requireValidBackendEntityId} from '@dvb/data-access-types';
import {
  AerzteStore,
  AnlassConfigOptionsStore,
  AufgabenStore,
  BenutzerStore,
  BriefvorlagenStore,
  ImpfstoffeChargenStore,
  LabelsStore,
  Path,
  requireAcceptedAgb,
  requireRole,
  SchulenOptionsStore,
} from '@dvb/feat-shared';
import {confirmOnDirty, toDetailModeStrict} from '@dvb/util-angular';
import {
  hasAerztePageViewBerechtigung,
  hasAufgabenViewBerechtigungen,
  hasKindEditBerechtigung,
  hasKinderMedizinischeDatenViewBerechtigung,
  hasKindNotizViewBerechtigung,
  hasKindVorlagenViewBerechtigung,
  hasNotizEditBerechtigung,
} from '@dvb/util-shared';
import {DossierStore} from './kinder/dossier.store';
import {
  KinderAufgabeDetailsPageComponent,
} from './kinder/kinder-aufgaben/kinder-aufgabe-details/kinder-aufgabe-details.page.component';
import {
  KinderAufgabenListePageComponent,
} from './kinder/kinder-aufgaben/kinder-aufgaben-liste/kinder-aufgaben-liste.page.component';
import {KinderDetailPageComponent} from './kinder/kinder-detail/kinder-detail-page.component';
import {KontaktPersonKindOptionsStore} from './kinder/kinder-detail/kontaktpersonen/kontakt-person-kind-options.store';
import {KontaktPersonKindStore} from './kinder/kinder-detail/kontaktpersonen/kontakt-person-kind.store';
import {
  KontaktPersonKindComponent,
} from './kinder/kinder-detail/kontaktpersonen/kontakt-personen-kind-detail/kontakt-person-kind.component';
import {
  KontaktPersonenKindListComponent,
} from './kinder/kinder-detail/kontaktpersonen/kontakt-personen-kind-list/kontakt-personen-kind-list.component';
import {KindPersonendatenComponent} from './kinder/kinder-detail/personendaten/kind-personendaten.component';
import {KinderSchuleKlasseComponent} from './kinder/kinder-detail/schule-klasse/kinder-schule-klasse.component';
import {KindAnlaesseStore} from './kinder/kinder-dossier/dossier-anlaesse/kind-anlaesse-store.service';
import {KindDossierComponent} from './kinder/kinder-dossier/kind-dossier.component';
import {KinderListPageComponent} from './kinder/kinder-list/kinder-list.page.component';
import {KinderNotizPageComponent} from './kinder/kinder-notiz/kinder-notiz/kinder-notiz.page.component';
import {KinderNotizStore} from './kinder/kinder-notiz/kinder-notiz/kinder-notiz.store';
import {
  KinderNotizenListePageComponent,
} from './kinder/kinder-notiz/kinder-notizen-liste/kinder-notizen-liste.page.component';
import {KinderSummaryStore} from './kinder/kinder-summary.store';
import {
  KinderNotizenHochladenPageComponent
} from './kinder/kinder-notiz/kinder-notizen-hochladen/kinder-notizen-hochladen.page.component';
import {KinderMassenbriefStore} from "./kinder/kinder-list/kinder-massenbrief/kinder-massenbrief.store";

export const kinderRoutes: Route[] = [
  {
    path: '',
    providers: [
      DossierStore,
      KinderSummaryStore,
      KinderNotizStore,
      KontaktPersonKindStore,
      KindAnlaesseStore,
    ],
    resolve: {
      _store: () => {
        inject(DossierStore).reset();
        inject(AnlassConfigOptionsStore).loadAnlassConfigOptions();
        inject(ImpfstoffeChargenStore).loadImpfstoffeAndChargen();
        inject(KinderMassenbriefStore).resetState();
        if (inject(BenutzerStore).createHasRequiredRoleSig(hasKindVorlagenViewBerechtigung())()) {
          inject(BriefvorlagenStore).loadBriefvorlagen();
        }
        if (inject(BenutzerStore).createHasRequiredRoleSig(hasAerztePageViewBerechtigung())()) {
          inject(AerzteStore).loadAerzteList();
        }
      },
    },
    children: [
      {
        path: '',
        resolve: {
          _store: () => {
            inject(SchulenOptionsStore).loadSchulenOptions({onlyActive: false});
          },
          _labels: () => inject(LabelsStore).loadLabelsList(),

        },
        pathMatch: 'full',
        component: KinderListPageComponent,
      },
      {
        path: Path.CREATE,
        component: KinderDetailPageComponent,
        resolve: {
          _store: () => {
            inject(DossierStore).reset();
            inject(DossierStore).setNewMode(true);
          },
        },
        children: [
          {
            path: '',
            pathMatch: 'full',
            redirectTo: Path.PERSONENDATEN,
          },
          {
            path: Path.PERSONENDATEN,
            component: KindPersonendatenComponent,
            resolve: {
              _store: () => {
                inject(DossierStore).setDetailModeEdit();
              },
            },
            canDeactivate: [confirmOnDirty()],
          },
        ],
      },
      {
        path: ':id',
        resolve: {
          _store: (route: ActivatedRouteSnapshot) => {
            const param = requireValidBackendEntityId(route.params['id']);
            const benutzerStore = inject(BenutzerStore);
            inject(DossierStore).loadDossierForKind(param);
            if (benutzerStore.createHasRequiredRoleSig(hasKinderMedizinischeDatenViewBerechtigung())()) {
              inject(DossierStore).loadLastImpfung(param);
            }
            inject(DossierStore).setNewMode(false);
            if (benutzerStore.createHasRequiredRoleSig(hasKindNotizViewBerechtigung())()) {
              inject(KinderNotizStore).loadNotizenForKind(param);
            }
            if (benutzerStore.createHasRequiredRoleSig(hasAufgabenViewBerechtigungen())()) {
              inject(AufgabenStore).loadAufgabenForKind(param);
            }
            inject(KindAnlaesseStore).loadAnlaesseForKind(param);
          },
        },
        children: [
          // Kinderdossier
          {
            path: '',
            component: KindDossierComponent,
          },
          // Kindprofil
          {
            path: Path.PROFIL,
            component: KinderDetailPageComponent,
            resolve: {
              schulenOptions: () => inject(SchulenOptionsStore).loadSchulenOptions({onlyActive: true}),
            },
            children: [
              {
                path: '',
                pathMatch: 'full',
                redirectTo: Path.PERSONENDATEN,
              },
              {
                path: Path.PERSONENDATEN,
                children: [
                  {
                    path: '',
                    pathMatch: 'full',
                    redirectTo: 'view',
                  },
                  {
                    path: ':mode',
                    component: KindPersonendatenComponent,
                    resolve: {
                      _store: (route: ActivatedRouteSnapshot) => {
                        const mode = toDetailModeStrict(route.params['mode']);

                        inject(DossierStore).setDetailMode(mode);
                      },
                    },
                    canDeactivate: [confirmOnDirty()],
                  },
                ],
              },
              {
                path: Path.SCHULE_KLASSE,
                children: [
                  {
                    path: '',
                    pathMatch: 'full',
                    redirectTo: 'view',
                  },
                  {
                    path: ':mode',
                    component: KinderSchuleKlasseComponent,
                    resolve: {
                      _store: (route: ActivatedRouteSnapshot) => {
                        const mode = toDetailModeStrict(route.params['mode']);

                        inject(DossierStore).setDetailMode(mode);
                      },
                    },
                    canDeactivate: [confirmOnDirty()],
                  },
                ],
              },
              {
                path: Path.KONTAKTPERSONEN,
                providers: [KontaktPersonKindOptionsStore],
                children: [
                  {
                    path: '',
                    pathMatch: 'full',
                    redirectTo: 'all',
                  },
                  {
                    path: 'all',
                    component: KontaktPersonenKindListComponent,
                    resolve: {
                      _store: (route: ActivatedRouteSnapshot) => {
                        const kindId = requireValidBackendEntityId(route.parent?.parent?.params['id']);
                        inject(KontaktPersonKindStore).loadKontaktPersonenByKind({kindID: kindId});
                        inject(KontaktPersonKindStore).setDetailModeView();

                        if (inject(BenutzerStore).createHasRequiredRoleSig(hasKindEditBerechtigung())()) {
                          inject(KontaktPersonKindOptionsStore).loadKontaktPersonenByKindOptions({kindID: kindId});
                        }
                      },
                    },
                  },
                  {
                    path: Path.CREATE,
                    component: KontaktPersonKindComponent,
                    resolve: {
                      _store: () => {
                        inject(KontaktPersonKindStore).setSelectedEntityId(undefined);
                        inject(KontaktPersonKindStore).setDetailModeEdit();
                      },
                    },
                    canDeactivate: [confirmOnDirty()],
                  },
                  {
                    path: ':id',
                    resolve: {
                      _store: (route: ActivatedRouteSnapshot) => {
                        const kindId = requireValidBackendEntityId(route.parent?.parent?.params['id']);
                        const kontaktPersonKindId = requireValidBackendEntityId(route.params['id']);
                        inject(KontaktPersonKindStore).loadKontaktPersonKindById({
                          kindID: kindId,
                          kontaktPersonKindID: kontaktPersonKindId,
                        });
                        inject(KontaktPersonKindStore).setSelectedEntityId(kontaktPersonKindId);
                      },
                    },
                    children: [
                      {
                        path: '',
                        pathMatch: 'full',
                        redirectTo: 'view',
                      },
                      {
                        path: ':mode',
                        component: KontaktPersonKindComponent,
                        resolve: {
                          _store: (route: ActivatedRouteSnapshot) => {
                            const mode = toDetailModeStrict(route.params['mode']);
                            inject(KontaktPersonKindStore).setDetailMode(mode);
                          },
                        },
                        canDeactivate: [confirmOnDirty()],
                      },
                    ],
                  },
                ],
              },
            ],
          },
          // Notizen
          {
            path: Path.NOTIZEN,
            resolve: {
              _labels: () => inject(LabelsStore).loadLabelsList(),
            },
            children: [
              {
                path: '',
                pathMatch: 'full',
                redirectTo: Path.ALL,
              },
              {
                path: Path.ALL,
                component: KinderNotizenListePageComponent,
              },
              {
                path: Path.CREATE,
                component: KinderNotizPageComponent,
                resolve: {
                  _store: () => {
                    inject(KinderNotizStore).setSelectedEntityId(undefined);
                    inject(KinderNotizStore).setDetailMode('edit');
                  },
                },
                canDeactivate: [confirmOnDirty()],
                canActivate: [
                  requireRole(hasNotizEditBerechtigung()),
                  requireAcceptedAgb(),
                ],
              },
              {
                path: ':id',
                resolve: {
                  _store: (route: ActivatedRouteSnapshot) => {
                    const param = requireValidBackendEntityId(route.params['id']);
                    inject(KinderNotizStore).setSelectedEntityId(param);
                    inject(KinderNotizStore).setDetailMode('edit');
                  },
                },
                canActivate: [
                  requireRole(hasNotizEditBerechtigung()),
                  requireAcceptedAgb(),
                ],
                children: [
                  {
                    path: '',
                    component: KinderNotizPageComponent,
                    canDeactivate: [confirmOnDirty()],
                  },
                  {
                    path: Path.UPLOAD,
                    component: KinderNotizenHochladenPageComponent
                  }
                ]
              },
            ],
          },
          {
            path: Path.AUFGABEN,
            resolve: {
              _labels: () => inject(LabelsStore).loadLabelsList(),
            },
            children: [
              {
                path: '',
                pathMatch: 'full',
                component: KinderAufgabenListePageComponent,
              },
              {
                path: Path.CREATE,
                component: KinderAufgabeDetailsPageComponent,
                resolve: {
                  _store: () => inject(AufgabenStore).resetSelectedAufgabe(),
                },
                canDeactivate: [confirmOnDirty()],
              },
              {
                path: ':aufgabeId',
                component: KinderAufgabeDetailsPageComponent,
                resolve: {
                  _store: (route: ActivatedRouteSnapshot) => {
                    const param = requireValidBackendEntityId(route.params['aufgabeId']);
                    inject(AufgabenStore).setSelectedEntityId(param);
                    inject(AufgabenStore).loadAufgabe(param);
                  },
                },
                canDeactivate: [confirmOnDirty()],
              },
            ],
          },
        ],
      },
    ],
  },
];
