import {ActivatedRouteSnapshot, Route} from '@angular/router';
import {inject} from "@angular/core";
import {LabelsStore} from "@dvb/feat-shared";
import {NotizenListStore} from "./notizen-list.store";
import {confirmOnDirty} from "@dvb/util-angular";
import {requireValidBackendEntityId} from "@dvb/data-access-types";
import {NotizenListePageComponent} from "./notizen-list/notizen-liste.page.component";
import {DossierStore} from "@dvb/feat-kinder";
import {NotizDetailPageComponent} from "./notiz-details/notiz-detail.page.component";

export const notizenListRoutes: Route[] = [
  {
    path: '',
    providers: [
      DossierStore,
      NotizenListStore
    ],
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
        component: NotizDetailPageComponent,
        resolve: {
          _store: (route: ActivatedRouteSnapshot) => {
            const param = requireValidBackendEntityId(route.params['notizId']);
            inject(NotizenListStore).setSelectedEntityId(param);
          },
        },
        canDeactivate: [confirmOnDirty()],
      },
    ],
  }
];
