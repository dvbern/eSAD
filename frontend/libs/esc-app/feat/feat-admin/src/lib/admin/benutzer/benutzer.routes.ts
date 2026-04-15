import {ActivatedRouteSnapshot, Route} from '@angular/router';
import {BenutzerListPageComponent} from './benutzer-list/benutzer-list-page.component';
import {BenutzerListStore, BenutzerStore, OrganisationStore, Path} from "@dvb/feat-shared";
import {inject} from "@angular/core";
import {BenutzerDetailComponent} from "./benutzer-detail/benutzer-detail.component";
import {confirmOnDirty, toDetailModeStrict} from "@dvb/util-angular";
import {EntityId} from "@ngrx/signals/entities";
import {hasOrganisationVerwaltungViewBerechtigung} from "@dvb/util-shared";

export const benutzerRoutes: Route[] = [
  {
    path: '',
    providers: [],
    resolve: {
      _store: () => {
        inject(BenutzerListStore).loadBenutzerList();
        if (inject(BenutzerStore).createHasRequiredRoleSig(hasOrganisationVerwaltungViewBerechtigung())()) {
          inject(OrganisationStore).loadOrganisationen();
        }
      },
    },
    children: [
      {
        path: '',
        pathMatch: 'full',
        component: BenutzerListPageComponent,
      },
      {
        path: Path.CREATE,
        component: BenutzerDetailComponent,
        resolve: {
          _store: () => {
            inject(BenutzerListStore).setSelectedEntityId(undefined);
            inject(BenutzerListStore).setDetailModeEdit();
          }
        },
        canDeactivate: [confirmOnDirty()],
      },
      {
        // the following matches a uuid
        path: ':id',
        resolve: {
          _store: (route: ActivatedRouteSnapshot) => {
            const param: EntityId = route.params['id'];
            inject(BenutzerListStore).setSelectedEntityId(param);
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
            component: BenutzerDetailComponent,
            resolve: {
              _store: (route: ActivatedRouteSnapshot) => {
                const mode = toDetailModeStrict(route.params['mode']);
                inject(BenutzerListStore).setDetailMode(mode);
              },
            },
            canDeactivate: [confirmOnDirty()],
          },
        ],
      },
    ]
  },
];
