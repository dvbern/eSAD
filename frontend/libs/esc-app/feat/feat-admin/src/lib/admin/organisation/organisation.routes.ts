import {inject} from "@angular/core";
import {OrganisationListPageComponent} from "./organisation-list/organisation-list-page.component";
import {ActivatedRouteSnapshot, Route} from "@angular/router";
import {requireValidBackendEntityId} from "@dvb/data-access-types";
import {MitgliederListPageComponent} from "./mitglieder-list/mitglieder-list-page.component";
import {BenutzerListStore, BenutzerStore, OrganisationStore, Path} from "@dvb/feat-shared";
import {confirmOnDirty, toDetailModeStrict} from "@dvb/util-angular";
import {MitgliederDetailsComponent} from "./mitglieder-details/mitglieder-details.component";
import {EntityId} from "@ngrx/signals/entities";
import {hasOrganisationVerwaltungViewBerechtigung, isNullish} from "@dvb/util-shared";

export const organisationRoutes: Route[] = [
  {
    path: '',
    resolve: {
      _store: () => {
        if (inject(BenutzerStore).createHasRequiredRoleSig(hasOrganisationVerwaltungViewBerechtigung())() && isNullish(inject(BenutzerStore).benutzer()?.organisationRollen)) {
          inject(OrganisationStore).loadOrganisationen();
        }
      },
    },
    children: [
      {
        path: '',
        pathMatch: 'full',
        component: OrganisationListPageComponent,
      },
      {
        path: Path.CREATE,
        component: MitgliederDetailsComponent,
        resolve: {
          _store: () => {
            inject(OrganisationStore).setSelectedEntityId(undefined);
            inject(OrganisationStore).setDetailModeEdit();
            inject(BenutzerListStore).setSelectedEntityId(undefined);
            inject(BenutzerListStore).setDetailModeEdit();
          }
        },
        canDeactivate: [confirmOnDirty()],
      },
      {
        path: ':organisationId',
        resolve: {
          _store: (route: ActivatedRouteSnapshot) => {
            const param = requireValidBackendEntityId(route.params['organisationId']);
            inject(OrganisationStore).setSelectedEntityId(param);
            inject(OrganisationStore).loadOrganisation(param);
            inject(BenutzerListStore).loadBenutzerListForOrganisation(param);
          },
        },
        children: [
          {
            path: '',
            component: MitgliederListPageComponent,
          },
          {
            path: Path.CREATE,
            component: MitgliederDetailsComponent,
            resolve: {
              _store: () => {
                inject(OrganisationStore).setDetailModeEdit();
                inject(BenutzerListStore).setSelectedEntityId(undefined);
                inject(BenutzerListStore).setDetailModeEdit();
              },
            },
          },
          {
            // the following matches a uuid
            path: ':mitgliedId',
            resolve: {
              _store: (route: ActivatedRouteSnapshot) => {
                const param: EntityId = route.params['mitgliedId'];
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
                component: MitgliederDetailsComponent,
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
    ]
  },
];
