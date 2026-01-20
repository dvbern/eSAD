import {inject} from '@angular/core';
import {ActivatedRouteSnapshot, Route} from '@angular/router';
import {requireValidBackendEntityId} from '@dvb/data-access-types';
import {DossierStore} from '@dvb/feat-kinder';
import {LabelsStore, Path} from '@dvb/feat-shared';
import {confirmOnDirty} from '@dvb/util-angular';
import {NotizDetailPageComponent} from './notiz-details/notiz-detail.page.component';
import {NotizenHochladenPageComponent} from './notizen-hochladen/notizen-hochladen.page.component';
import {NotizenListStore} from './notizen-list.store';
import {NotizenListePageComponent} from './notizen-list/notizen-liste.page.component';

export const notizenListRoutes: Route[] = [
  {
    path: '',
    providers: [DossierStore, NotizenListStore],
    resolve: {
      notizen: () => inject(NotizenListStore).loadNotizen(),
      _labels: () => inject(LabelsStore).loadLabelsList(),
    },
    children: [
      {
        path: '',
        pathMatch: 'full',
        component: NotizenListePageComponent,
      },
      {
        path: ':notizId',
        resolve: {
          _store: (route: ActivatedRouteSnapshot) => {
            const param = requireValidBackendEntityId(route.params['notizId']);
            inject(NotizenListStore).setSelectedEntityId(param);
          },
        },
        children: [
          {
            path: '',
            component: NotizDetailPageComponent,
            canDeactivate: [confirmOnDirty()],
          },
          {
            path: Path.UPLOAD,
            component: NotizenHochladenPageComponent,
          },
        ],
      },
    ],
  },
];
