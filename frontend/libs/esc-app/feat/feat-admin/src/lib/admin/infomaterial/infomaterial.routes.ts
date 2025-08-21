import {inject} from '@angular/core';
import {ActivatedRouteSnapshot, Route} from '@angular/router';
import {InfomaterialStore, Path} from '@dvb/feat-shared';
import {confirmOnDirty, toDetailModeStrict} from '@dvb/util-angular';
import {EntityId} from '@ngrx/signals/entities';
import {InfomaterialDetailPageComponent} from './infomaterial-detail/infomaterial-detail.page.component';
import {InfomaterialListPageComponent} from './infomaterial-list/infomaterial-list.page.component';

export const infomaterialRoutes: Route[] = [
  {
    path: '',
    providers: [],
    resolve: {
      _store: () => inject(InfomaterialStore).loadInfomaterialList({onlyActive: false}),
    },
    children: [
      {
        path: '',
        pathMatch: 'full',
        component: InfomaterialListPageComponent,
      },
      {
        path: Path.CREATE,
        component: InfomaterialDetailPageComponent,
        resolve: {
          _store: () => {
            inject(InfomaterialStore).setSelectedEntityId(undefined);
            inject(InfomaterialStore).setDetailModeEdit();
          },
        },
        canDeactivate: [confirmOnDirty()],
      },
      {
        // the following matches a uuid
        path: ':id',
        resolve: {
          _store: (route: ActivatedRouteSnapshot) => {
            const param: EntityId = route.params['id'];
            inject(InfomaterialStore).setSelectedEntityId(param);
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
            component: InfomaterialDetailPageComponent,
            resolve: {
              _store: (route: ActivatedRouteSnapshot) => {
                const mode = toDetailModeStrict(route.params['mode']);
                inject(InfomaterialStore).setDetailMode(mode);
              },
            },
            canDeactivate: [confirmOnDirty()],
          },
        ],
      },
    ],
  },
];
