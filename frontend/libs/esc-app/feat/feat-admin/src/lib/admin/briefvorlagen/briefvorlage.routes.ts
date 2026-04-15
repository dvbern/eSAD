import {inject} from '@angular/core';
import {ActivatedRouteSnapshot, Route} from '@angular/router';
import {BriefvorlagenStore, Path, requireAcceptedAgb, requireRole, SystemAdminStore} from '@dvb/feat-shared';
import {confirmOnDirty} from '@dvb/util-angular';
import {hasBriefvorlagenViewBerechtigung} from '@dvb/util-shared';
import {EntityId} from '@ngrx/signals/entities';
import {BriefvorlagenDetailPageComponent} from './briefvorlagen-detail/briefvorlagen-detail.page.component';
import {BriefvorlagenListPageComponent} from './briefvorlagen-list/briefvorlagen-list-page.component';

export const briefvorlageRoutes: Route[] = [
  {
    path: '',
    providers: [SystemAdminStore],
    resolve: {
      _store: () => inject(BriefvorlagenStore).loadBriefvorlagen(),
    },
    children: [
      {
        path: '',
        pathMatch: 'full',
        component: BriefvorlagenListPageComponent,
      },
      {
        path: Path.CREATE,
        component: BriefvorlagenDetailPageComponent,
        resolve: {
          _store: () => {
            inject(BriefvorlagenStore).setSelectedEntityId(undefined);
            inject(BriefvorlagenStore).setDetailModeEdit();
          },
        },
        canDeactivate: [confirmOnDirty()],
      },
      {
        path: ':id',
        component: BriefvorlagenDetailPageComponent,
        resolve: {
          _store: (route: ActivatedRouteSnapshot) => {
            const param: EntityId = route.params['id'];
            inject(BriefvorlagenStore).setSelectedEntityId(param);
            inject(BriefvorlagenStore).setDetailMode('edit');
          },
        },
        canDeactivate: [confirmOnDirty()],
        canActivate: [
          requireRole(hasBriefvorlagenViewBerechtigung()),
          requireAcceptedAgb(),
        ],
      },
    ],
  },
];
