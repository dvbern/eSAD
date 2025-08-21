import {inject} from '@angular/core';
import {ActivatedRouteSnapshot, Route, Router} from '@angular/router';
import {Path} from '@dvb/feat-shared';
import {EntityId} from '@ngrx/signals/entities';
import {KindUntersuchungComponent} from './kind-untersuchung/kind-untersuchung.component';
import {PlaygroundNavUntersuchungStore} from './kind-untersuchung/playground-nav-untersuchung.store';
import {PlaygroundNavDatenComponent} from './kind-untersuchung/untersuchung-daten/untersuchung-daten.component';
import {PlaygroundNavGroesseComponent} from './kind-untersuchung/untersuchung-groesse/untersuchung-groesse.component';
import {PlaygroundNavHoerenComponent} from './kind-untersuchung/untersuchung-hoeren/untersuchung-hoeren.component';
import {
  PlaygroundNavImpfungenComponent,
} from './kind-untersuchung/untersuchung-impfungen/untersuchung-impfungen.component';
import {PlaygroundNavSehenComponent} from './kind-untersuchung/untersuchung-sehen/untersuchung-sehen.component';
import {KinderTabsComponent} from './kinder-tabs/kinder-tabs.component';
import {PlaygroundNavNoChildSelectedComponent} from './no-child-selected.component';
import {PlaygroundNavStore} from './playground-nav.store';

export const playgroundNavRoutes: Route[] = [
  {
    path: '',
    component: KinderTabsComponent,
    providers: [PlaygroundNavUntersuchungStore, PlaygroundNavStore],
    resolve: {
      _store: () => inject(PlaygroundNavUntersuchungStore).init(),
    },
    // TODO team: ESC-136 use path constants
    children: [
      {
        path: '',
        pathMatch: 'full',
        redirectTo: () => {
          const navStore = inject(PlaygroundNavStore);
          const lastSelectedId = navStore.selectedEntityId()?.toString();
          const openTabs = navStore.visibleTabs();
          if (lastSelectedId && navStore.isTabVisible(lastSelectedId)) {
            // go to previously selected tab
            return lastSelectedId.toString();
          }
          const firstOpenTab = openTabs.length ? openTabs[0] : undefined;
          if (firstOpenTab) {
            // go to the first open tap
            return firstOpenTab.id;
          }
          return 'empty';
        },
      },
      {
        // no tab open -> placeholder
        path: 'empty',
        component: PlaygroundNavNoChildSelectedComponent,
      },
      {
        path: ':id',
        component: KindUntersuchungComponent,
        canActivate: [
          (route: ActivatedRouteSnapshot) => {
            const param: EntityId = route.params['id'];
            if (inject(PlaygroundNavStore).isTabVisible(param.toString())) {
              return true;
            } else {
              return inject(Router).navigate([Path.PLAYGROUND, 'nav']);
            }
          },
        ],
        resolve: {
          _store: (route: ActivatedRouteSnapshot) => {
            const param: EntityId = route.params['id'];
            inject(PlaygroundNavStore).setSelectedEntityId(param);
          },
        },
        children: [
          {
            path: '',
            pathMatch: 'full',
            redirectTo: () => {
              const untersuchungStore = inject(PlaygroundNavUntersuchungStore);
              const lastSelectedId = untersuchungStore.selectedEntityId()?.toString();
              if (lastSelectedId) {
                // go to previously selected menu item
                return lastSelectedId.toString();
              }
              // go to first child
              return 'daten';
            },
          },
          {
            path: 'daten',
            component: PlaygroundNavDatenComponent,
            resolve: {
              _store: () => inject(PlaygroundNavUntersuchungStore).setSelectedEntityId('daten'),
            },
          },
          {
            path: 'impfungen',
            component: PlaygroundNavImpfungenComponent,
            resolve: {
              _store: () => inject(PlaygroundNavUntersuchungStore).setSelectedEntityId('impfungen'),
            },
          },
          {
            path: 'sehen',
            component: PlaygroundNavSehenComponent,
            resolve: {
              _store: () => inject(PlaygroundNavUntersuchungStore).setSelectedEntityId('sehen'),
            },
          },
          {
            path: 'hoeren',
            component: PlaygroundNavHoerenComponent,
            resolve: {
              _store: () => inject(PlaygroundNavUntersuchungStore).setSelectedEntityId('hoeren'),
            },
          },
          {
            path: 'groesse',
            component: PlaygroundNavGroesseComponent,
            resolve: {
              _store: () => inject(PlaygroundNavUntersuchungStore).setSelectedEntityId('groesse'),
            },
          },
        ],
      },

    ],
  },
]


