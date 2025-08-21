import {inject} from '@angular/core';
import {ActivatedRouteSnapshot, Route} from '@angular/router';
import {AerzteStore} from '@dvb/feat-shared';
import {Path} from '@dvb/feat-shared';
import {confirmOnDirty, toDetailModeStrict} from '@dvb/util-angular';
import {AerzteDetailPageComponent} from './aerzte-detail/aerzte-detail-page.component';
import {AerzteListPageComponent} from './aerzte-list/aerzte-list-page.component';

export const aerzteRoutes: Route[] = [
  {
    path: '',
    resolve: {
      _store: () => inject(AerzteStore).loadAerzteList(),
    },
    children: [
      {
        path: '',
        pathMatch: 'full',
        component: AerzteListPageComponent,
      },
      {
        path: Path.CREATE,
        pathMatch: 'full',
        component: AerzteDetailPageComponent,
        resolve: {
          _store: () => {
            inject(AerzteStore).setSelectedEntityId(undefined);
            inject(AerzteStore).setDetailModeEdit();
          },
        },
        canDeactivate: [confirmOnDirty()],
      },
      {
        // the following matches a uuid
        path: ':id',
        resolve: {
          _store: (route: ActivatedRouteSnapshot) => {
            inject(AerzteStore).setSelectedEntityId(route.params['id']);
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
            component: AerzteDetailPageComponent,
            resolve: {
              _store: (route: ActivatedRouteSnapshot) => {
                inject(AerzteStore).setDetailMode(toDetailModeStrict(route.params['mode']));
              },
            },
            canDeactivate: [confirmOnDirty()],
          },
        ],
      },
    ],
  },
];
