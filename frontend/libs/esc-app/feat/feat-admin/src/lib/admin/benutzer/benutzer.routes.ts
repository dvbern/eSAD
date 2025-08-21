import {Route} from '@angular/router';
import {BenutzerListPageComponent} from './benutzer-list/benutzer-list-page.component';

export const benutzerRoutes: Route[] = [
  {
    path: '',
    component: BenutzerListPageComponent,
  },
];
