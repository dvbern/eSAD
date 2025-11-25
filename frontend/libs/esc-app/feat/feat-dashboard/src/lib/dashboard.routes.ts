import {inject} from '@angular/core';
import {Route} from '@angular/router';
import {BackendEntityId} from '@dvb/data-access-types';
import {AufgabenStore, BenutzerStore} from '@dvb/feat-shared';
import {hasAufgabenViewBerechtigungen} from '@dvb/util-shared';
import {DashboardStore} from './dashboard.store';
import {DashboardPageComponent} from './dashboard/dashboard.page.component';

export const dashboardRoutes: Route[] = [
  {
    path: '',
    providers: [AufgabenStore, DashboardStore],
    resolve: {
      _store: () => {
        const userId = inject(BenutzerStore).requireBenutzer().id as BackendEntityId;
        inject(DashboardStore).loadAnlassListForUser(userId);
        const benutzerStore = inject(BenutzerStore);
        if (benutzerStore.createHasRequiredRoleSig(hasAufgabenViewBerechtigungen())()) {
          inject(DashboardStore).loadAllNichtAbgeschlosseneForCurrentUser();
        }
        inject(DashboardStore).loaddAktivitaetHistoryForUser();
      },
    },
    component: DashboardPageComponent,
  },
];
