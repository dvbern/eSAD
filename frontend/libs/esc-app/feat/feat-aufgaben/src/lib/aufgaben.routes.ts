import {inject} from '@angular/core';
import {ActivatedRouteSnapshot, Route} from '@angular/router';
import {requireValidBackendEntityId} from '@dvb/data-access-types';
import {AufgabenStore, LabelsStore} from '@dvb/feat-shared';
import {Path} from '@dvb/feat-shared';
import {confirmOnDirty} from '@dvb/util-angular';
import {AufgabeDetailsPageComponent} from './aufgaben/aufgabe-details/aufgabe-details.page.component';
import {AufgabenListPageComponent} from './aufgaben/aufgaben-list/aufgaben-list.page.component';

export const aufgabenRoutes: Route[] = [
  {
    path: '',
    resolve: {
      aufgaben: () => inject(AufgabenStore).loadAufgaben(),
      _labels: () => inject(LabelsStore).loadLabelsList(),
    },
    children: [
      {
        path: '',
        pathMatch: 'full',
        component: AufgabenListPageComponent,
      },
      {
        path: Path.CREATE,
        component: AufgabeDetailsPageComponent,
        resolve: {
          _store: () => inject(AufgabenStore).resetSelectedAufgabe(),
        },
        canDeactivate: [confirmOnDirty()],
      },
      {
        path: ':aufgabeId',
        component: AufgabeDetailsPageComponent,
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
];
