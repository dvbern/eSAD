import {inject} from '@angular/core';
import {Route} from '@angular/router';
import {Path} from '@dvb/feat-shared';
import {DocumentQueueStore} from '@dvb/feat-shared';
import {PlaygroundAnlassElementsComponent} from './playground-anlass-elements/playground-anlass-elements.component';
import {
  PlaygroundBorderedFormInputComponent,
} from './playground-bordered-form-inputs/playground-bordered-form-input.component';
import {PlaygroundChartsComponent} from './playground-charts/playground-charts.component';
import {PlaygroundColumnsComponent} from './playground-columns/playground-columns.component';
import {PlaygroundDashboardComponent} from './playground-dashboard/playground-dashboard.component';
import {PlaygroundDialogsComponent} from './playground-dialogs/playground-dialogs.component';
import {PlaygroundDocumentQueueComponent} from './playground-document-queue/playground-document-queue.component';
import {PlaygroundDossierComponent} from './playground-dossier/playground-dossier.component';
import {PlaygroundFormInputComponent} from './playground-form-inputs/playground-form-input.component';
import {PlaygroundIconsComponent} from './playground-icons/playground-icons.component';
import {PlaygroundLongListsComponent} from './playground-lists/playground-long-lists.component';
import {PlaygroundLoadingComponent} from './playground-loading/playground-loading.component';
import {playgroundNavRoutes} from './playground-nav/playground-nav.routes';
import {PlaygroundNotificationsComponent} from './playground-notifications/playground-notifications.component';
import {PlaygroundTableComponent} from './playground-table/playground-table.component';
import {PlaygroundTypographyComponent} from './playground-typography/playground-typography.component';
import {PlaygroundComponent} from './playground.component';

export const playgroundRoutes: Route[] = [
  {
    path: '',
    pathMatch: 'full',
    redirectTo: Path.DASHBOARD,
  },
  {
    path: '',
    component: PlaygroundComponent,
    children: [
      {
        path: Path.TABLE,
        component: PlaygroundTableComponent,
      },
      {
        path: Path.FORM,
        component: PlaygroundFormInputComponent,
      },
      {
        path: 'bordered',
        component: PlaygroundBorderedFormInputComponent,
      },
      {
        path: Path.COLUMNS,
        component: PlaygroundColumnsComponent,
      },
      {
        path: Path.TYPORGRAPHY,
        component: PlaygroundTypographyComponent,
      },
      {
        path: Path.DASHBOARD,
        component: PlaygroundDashboardComponent,
      },
      {
        path: Path.DIALOGS,
        component: PlaygroundDialogsComponent,
      },
      {
        path: 'loading',
        component: PlaygroundLoadingComponent,
      },
      {
        path: 'errors',
        component: PlaygroundNotificationsComponent,
      },
      {
        path: 'nav',
        children: playgroundNavRoutes,
      },
      {
        path: 'icons',
        component: PlaygroundIconsComponent,
      },
      {
        path: Path.STAMMDATEN,
        loadChildren: () => import('@dvb/feat-stammdaten').then(c => c.StammdatenRoutes),
      },
      {
        path: 'dossier',
        component: PlaygroundDossierComponent,
      },
      {
        path: 'anlass',
        component: PlaygroundAnlassElementsComponent,
      },
      {
        path: 'charts',
        component: PlaygroundChartsComponent,
      },
      {
        path: 'lists',
        component: PlaygroundLongListsComponent,
      },
      {
        path: 'documentqueue',
        component: PlaygroundDocumentQueueComponent,
        providers: [DocumentQueueStore],
        resolve: {
          _store: () => {
            inject(DocumentQueueStore).loadDocumentQueueListForCurrentBenutzer();
          },
        },
      },
    ],
  },
];
