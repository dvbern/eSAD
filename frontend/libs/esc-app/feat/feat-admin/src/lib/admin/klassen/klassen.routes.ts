import {inject} from '@angular/core';
import {ActivatedRouteSnapshot, Route} from '@angular/router';
import {requireValidBackendEntityId} from '@dvb/data-access-types';
import {
  KlassenStore,
  KontaktPersonSchuleOptionsStore,
  SchulenOptionsStore,
  SchulenStore,
} from '@dvb/feat-shared';
import {Path} from '@dvb/feat-shared';
import {confirmOnDirty, toDetailModeStrict} from '@dvb/util-angular';
import {KlassenDatenComponent} from './klassen-detail/klassen-daten/klassen-daten.component';
import {KlassenDetailPageComponent} from './klassen-detail/klassen-detail.page.component';
import {LehrpersonComponent} from './klassen-detail/klassen-lehrpersonen/lehrpersonen-detail/lehrperson.component';
import {
  LehrpersonenListComponent,
} from './klassen-detail/klassen-lehrpersonen/lehrpersonen-list/lehrpersonen-list.component';
import {LehrpersonenOptionStore} from './klassen-detail/klassen-lehrpersonen/lehrpersonen-option.store';
import {LehrpersonenStore} from './klassen-detail/klassen-lehrpersonen/lehrpersonen.store';
import {KlassenListPageComponent} from './klassen-list/klassen-list.page.component';

export const klassenRoutes: Route[] = [
  {
    path: '',
    providers: [KontaktPersonSchuleOptionsStore],
    resolve: {
      _schulen: () => inject(SchulenStore).loadSchulenList(),
      _schuloptions: () => inject(SchulenOptionsStore).loadSchulenOptions({onlyActive: false}),
    },
    children: [
      {
        path: '',
        pathMatch: 'full',
        component: KlassenListPageComponent,
        resolve: {
          _store: () => inject(KlassenStore).loadKlassenList(),
        },
      },
      {
        path: Path.CREATE,
        component: KlassenDetailPageComponent,
        resolve: {
          _store: () => {
            inject(KlassenStore).setSelectedEntityId(undefined);
            inject(KlassenStore).setDetailModeEdit();
          },
        },
        children: [
          {
            path: '',
            pathMatch: 'full',
            redirectTo: Path.DATEN,
          },
          {
            path: Path.DATEN,
            component: KlassenDatenComponent,
            resolve: {
              _store: () => {
                inject(KlassenStore).setDetailModeEdit();
              },
            },
            canDeactivate: [confirmOnDirty()],
          },
        ],
      },
      {
        path: ':id',
        component: KlassenDetailPageComponent,
        resolve: {
          _store: (route: ActivatedRouteSnapshot) => {
            const param = requireValidBackendEntityId(route.params['id']);
            inject(KlassenStore).setSelectedEntityId(param);
            inject(KlassenStore).loadEntity(param);
          },
        },
        children: [
          {
            path: '',
            pathMatch: 'full',
            redirectTo: Path.DATEN,
          },
          {
            path: Path.DATEN,
            children: [
              {
                path: '',
                pathMatch: 'full',
                redirectTo: 'view',
              },
              {
                path: ':mode',
                component: KlassenDatenComponent,
                resolve: {
                  _store: (route: ActivatedRouteSnapshot) => {
                    const mode = toDetailModeStrict(route.params['mode']);
                    inject(KlassenStore).setDetailMode(mode);
                  },
                },
                canDeactivate: [confirmOnDirty()],
              },
            ],
          },
          {
            path: Path.LEHRPERSONEN,
            providers: [LehrpersonenStore, LehrpersonenOptionStore],
            children: [
              {
                path: '',
                pathMatch: 'full',
                redirectTo: 'all',
              },
              {
                path: 'all',
                component: LehrpersonenListComponent,
                resolve: {
                  _store: (route: ActivatedRouteSnapshot) => {
                    const param = requireValidBackendEntityId(route.parent?.parent?.params['id']);
                    inject(LehrpersonenStore).loadLehrpersonenByKlasse(param);
                    inject(LehrpersonenOptionStore).loadLehrpersonenOptions({klasseID: param});
                  },
                },
              },
              {
                path: Path.CREATE,
                component: LehrpersonComponent,
                resolve: {
                  _store: () => {
                    inject(LehrpersonenStore).setSelectedEntityId(undefined);
                    inject(LehrpersonenStore).setDetailModeEdit();
                  },
                },
                canDeactivate: [confirmOnDirty()],
              },
              {
                path: ':id',
                resolve: {
                  _store: (route: ActivatedRouteSnapshot) => {
                    const lehrpersonId = requireValidBackendEntityId(route.params['id']);
                    const klasseId = requireValidBackendEntityId(route.parent?.parent?.params['id']);
                    inject(LehrpersonenStore).loadLehrpersonById({klasseId: klasseId, lehrpersonId: lehrpersonId});
                    inject(LehrpersonenStore).setSelectedEntityId(lehrpersonId);
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
                    component: LehrpersonComponent,
                    resolve: {
                      _store: (route: ActivatedRouteSnapshot) => {
                        const mode = toDetailModeStrict(route.params['mode']);
                        inject(LehrpersonenStore).setDetailMode(mode);
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
    ],
  },
];
