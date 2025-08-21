import {inject} from '@angular/core';
import {ActivatedRouteSnapshot, Route} from '@angular/router';
import {requireValidBackendEntityId} from '@dvb/data-access-types';
import {ImpfstoffeChargenStore, Path} from '@dvb/feat-shared';
import {confirmOnDirty, toDetailModeStrict} from '@dvb/util-angular';
import {EntityId} from '@ngrx/signals/entities';
import {ChargeDetailComponent} from './impfstoff-detail/impfstoff-chargen/charge-detail/charge-detail.component';
import {ChargenListComponent} from './impfstoff-detail/impfstoff-chargen/chargen-list/chargen-list.component';
import {ChargenStore} from './impfstoff-detail/impfstoff-chargen/chargen.store';
import {ImpfstoffDatenComponent} from './impfstoff-detail/impfstoff-daten/impfstoff-daten.component';
import {ImpfstoffDetailPageComponent} from './impfstoff-detail/impfstoff-detail.page.component';
import {ImpfstoffListPageComponent} from './impfstoff-list/impfstoff-list-page.component';

export const impfstoffRoutes: Route[] = [
  {
    path: '',
    providers: [],
    resolve: {
      _store: () => inject(ImpfstoffeChargenStore).loadImpfstoffeAndChargen(),
    },
    children: [
      {
        path: '',
        pathMatch: 'full',
        component: ImpfstoffListPageComponent,
      },
      {
        path: Path.CREATE,
        component: ImpfstoffDetailPageComponent,
        resolve: {
          _store: () => {
            inject(ImpfstoffeChargenStore).setSelectedEntityId(undefined);
            inject(ImpfstoffeChargenStore).setDetailModeEdit();
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
            component: ImpfstoffDatenComponent,
            resolve: {
              _store: () => {
                inject(ImpfstoffeChargenStore).setDetailModeEdit();
              },
            },
            canDeactivate:[confirmOnDirty()]
          },
        ],
      },
      {
        path: ':id',
        component: ImpfstoffDetailPageComponent,
        resolve: {
          _store: (route: ActivatedRouteSnapshot) => {
            const param: EntityId = route.params['id'];
            inject(ImpfstoffeChargenStore).setSelectedEntityId(param);
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
                component: ImpfstoffDatenComponent,
                resolve: {
                  _store: (route: ActivatedRouteSnapshot) => {
                    const mode = toDetailModeStrict(route.params['mode']);
                    inject(ImpfstoffeChargenStore).setDetailMode(mode);
                  },
                },
                canDeactivate:[confirmOnDirty()]
              },
            ],
          },
          {
            path: Path.CHARGE,
            providers: [ChargenStore],
            resolve: {
              _store: (route: ActivatedRouteSnapshot) => {
                const param = requireValidBackendEntityId(route.parent?.params['id']);
                inject(ChargenStore).loadChargenByImpfstoff(param);
              },
            },
            children: [
              {
                path: '',
                pathMatch: 'full',
                redirectTo: 'all',
              },
              {
                path: 'all',
                component: ChargenListComponent,
              },
              {
                path: Path.CREATE,
                component: ChargeDetailComponent,
                resolve: {
                  _store: () => {
                    inject(ChargenStore).setSelectedEntityId(undefined);
                    inject(ChargenStore).setDetailModeEdit();
                  },
                },
                canDeactivate:[confirmOnDirty()]
              },
            ],
          },
        ],
      },
    ],
  },
];
