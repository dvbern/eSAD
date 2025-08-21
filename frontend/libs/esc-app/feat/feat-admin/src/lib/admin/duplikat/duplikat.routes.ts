import {inject} from '@angular/core';
import {Route} from '@angular/router';
import {DuplikatListPageComponent} from './duplikat-list/duplikat-list.page.component';
import {DuplikatStore} from './duplikat.store';

export const duplikatRoutes: Route[] = [
  {
    path: '',
    providers: [DuplikatStore],
    resolve: {
      _store: () => inject(DuplikatStore).loadDuplikate(),
    },
    children: [
      {
        path: '',
        pathMatch: 'full',
        component: DuplikatListPageComponent,
      },
    ],
  },
];
