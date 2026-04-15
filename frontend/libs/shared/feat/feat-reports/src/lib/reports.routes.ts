import {inject} from '@angular/core';
import {Route} from '@angular/router';
import {DocumentQueueStore} from '@dvb/feat-shared';
import {ReportsComponent} from './reports/reports.component';

export const reportsRoutes: Route[] = [
  {
    path: '',
    component: ReportsComponent,
    providers: [DocumentQueueStore],
    resolve: {
      _store: () => {
        inject(DocumentQueueStore).loadDocumentQueueListForCurrentBenutzer();
      },
    },
  },
];
