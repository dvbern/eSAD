import { inject } from '@angular/core';
import { ActivatedRouteSnapshot, Routes } from '@angular/router';
import { confirmOnDirty } from '@dvb/util-angular';
import { StammdatenDispatchComponent } from './stammdaten-dispatch/stammdaten-dispatch.component';
import { StammdatenComponent } from './stammdaten/stammdaten.component';
import { StammdatenStore } from './stammdaten/stammdaten.store';

export const StammdatenRoutes: Routes = [
  {
    path: '',
    // pathMatch: 'full',
    component: StammdatenDispatchComponent,
    children: [
      {
        path: ':id',
        component: StammdatenComponent,
        providers: [StammdatenStore],
        resolve: {
          _store: (route: ActivatedRouteSnapshot) => inject(StammdatenStore).initFromRoute(route.params['id']),
        },
        canDeactivate: [confirmOnDirty()],
      },
    ],
  },
];
