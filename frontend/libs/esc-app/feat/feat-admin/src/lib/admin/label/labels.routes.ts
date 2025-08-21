import {inject} from '@angular/core';
import {ActivatedRouteSnapshot, Route} from '@angular/router';
import {LabelsStore} from '@dvb/feat-shared';
import {Path} from '@dvb/feat-shared';
import {confirmOnDirty, toDetailModeStrict} from '@dvb/util-angular';
import {EntityId} from '@ngrx/signals/entities';
import {LabelDetailPageComponent} from './label-detail/label-detail.page.component';
import {LabelListPageComponent} from './label-list/label-list.page.component';

export const labelsRoutes: Route[] = [
  {
    path: '',
    providers: [],
    resolve: {
      _store: () => inject(LabelsStore).loadLabelsList(),
    },
    children: [
      {
        path: '',
        pathMatch: 'full',
        component: LabelListPageComponent,
      },

      {
        path: Path.CREATE,
        component: LabelDetailPageComponent,
        resolve: {
          _store: () => {
            inject(LabelsStore).setSelectedEntityId(undefined);
            inject(LabelsStore).setDetailModeEdit();
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
            inject(LabelsStore).setSelectedEntityId(param);
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
            component: LabelDetailPageComponent,
            resolve: {
              _store: (route: ActivatedRouteSnapshot) => {
                const mode = toDetailModeStrict(route.params['mode']);
                inject(LabelsStore).setDetailMode(mode);
              },
            },
            canDeactivate: [confirmOnDirty()],
          },
        ],
      },

    ],
  },
];
