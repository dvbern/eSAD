import {inject} from '@angular/core';
import {ActivatedRouteSnapshot, Route} from '@angular/router';
import {requireValidBackendEntityId} from '@dvb/data-access-types';
import {
  KlassenStore,
  KontaktPersonSchuleOptionsStore,
  KontaktPersonSchuleStore,
  SchulenStore,
} from '@dvb/feat-shared';
import {Path} from '@dvb/feat-shared';
import {confirmOnDirty, toDetailModeStrict} from '@dvb/util-angular';
import {
  SchulenDetailKlassenListComponent,
} from './schulen-detail/schulen-detail-klassen-list/schulen-detail-klassen-list.component';
import {
  SchulenDetailKontaktpersonenListComponent,
} from './schulen-detail/schulen-detail-kontaktpersonen-list/schulen-detail-kontaktpersonen-list.component';
import {
  SchulenDetailKontaktpersonenStammdatenComponent,
} from './schulen-detail/schulen-detail-kontaktpersonen-stammdaten/schulen-detail-kontaktpersonen-stammdaten.component';
import {
  SchulenDetailStammdatenComponent,
} from './schulen-detail/schulen-detail-stammdaten/schulen-detail-stammdaten.component';
import {SchulenDetailPageComponent} from './schulen-detail/schulen-detail.page.component';
import {SchulenListPageComponent} from './schulen-list/schulen-list.page.component';

export const schulenRoutes: Route[] = [
  {
    path: '',
    component: SchulenListPageComponent,
  },
  {
    path: Path.CREATE,
    component: SchulenDetailPageComponent,
    resolve: {
      _store: () => {
        inject(SchulenStore).setSelectedEntityId(undefined);
      },
    },
    children: [
      {
        path: '',
        pathMatch: 'full',
        redirectTo: Path.STAMMDATEN,
      },
      {
        path: Path.STAMMDATEN,
        component: SchulenDetailStammdatenComponent,
        resolve: {
          _store: () => {
            inject(SchulenStore).setDetailModeEdit();
          },
        },
        canDeactivate: [confirmOnDirty()],
      },
    ],
  },
  {
    path: ':id',
    component: SchulenDetailPageComponent,
    resolve: {
      _store: (route: ActivatedRouteSnapshot) => {
        const param = requireValidBackendEntityId(route.params['id']);
        inject(SchulenStore).setSelectedEntityId(param);
        inject(SchulenStore).loadEntity(param);
      },
    },
    children: [
      {
        path: '',
        pathMatch: 'full',
        redirectTo: Path.STAMMDATEN,
      },
      {
        path: Path.STAMMDATEN,
        children: [
          {
            path: '',
            pathMatch: 'full',
            redirectTo: 'view',
          },
          {
            path: ':mode',
            component: SchulenDetailStammdatenComponent,
            resolve: {
              _store: (route: ActivatedRouteSnapshot) => {
                const mode = toDetailModeStrict(route.params['mode']);
                inject(SchulenStore).setDetailMode(mode);
              },
            },
            canDeactivate: [confirmOnDirty()],
          },
        ],
      },
      {
        path: Path.KLASSEN,
        component: SchulenDetailKlassenListComponent,
        resolve: {
          _store: () => {
            const schuleId = requireValidBackendEntityId(inject(SchulenStore).selectedEntityId());
            inject(KlassenStore).loadKlassenBySchuleList({
              schuleId: schuleId,
            });
          },
        },
      },
      {
        path: Path.KONTAKTPERSONEN,
        providers: [KontaktPersonSchuleStore, KontaktPersonSchuleOptionsStore],
        children: [
          {
            path: '',
            pathMatch: 'full',
            redirectTo: 'all',
          },
          {
            path: 'all',
            component: SchulenDetailKontaktpersonenListComponent,
            resolve: {
              _store: () => {
                const schuleId = inject(SchulenStore).selectedEntityId();
                inject(KontaktPersonSchuleStore).loadKontaktPersonenBySchule({
                  schuleID: requireValidBackendEntityId(schuleId),
                });
                inject(KontaktPersonSchuleOptionsStore).loadKontaktPersonenExcludeSchule({
                  schuleID: requireValidBackendEntityId(schuleId),
                });
              },
            },
          },
          {
            path: Path.CREATE,
            component: SchulenDetailKontaktpersonenStammdatenComponent,
            resolve: {
              _store: () => {
                inject(KontaktPersonSchuleStore).setSelectedEntityId(undefined);
                inject(KontaktPersonSchuleStore).setDetailModeEdit();
              },
            },
            canDeactivate: [confirmOnDirty()],
          },
          {
            path: ':id',
            resolve: {
              _store: (route: ActivatedRouteSnapshot) => {
                const kontaktPersonSchuleId = requireValidBackendEntityId(route.params['id']);
                const schuleId = requireValidBackendEntityId(route.parent?.parent?.params['id']);
                inject(KontaktPersonSchuleStore).loadKontaktPersonSchuleById({
                  schuleId: schuleId,
                  kontaktPersonSchuleId: kontaktPersonSchuleId,
                });
                inject(KontaktPersonSchuleStore).setSelectedEntityId(kontaktPersonSchuleId);
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
                component: SchulenDetailKontaktpersonenStammdatenComponent,
                resolve: {
                  _store: (route: ActivatedRouteSnapshot) => {
                    const mode = toDetailModeStrict(route.params['mode']);
                    inject(KontaktPersonSchuleStore).setDetailMode(mode);
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
