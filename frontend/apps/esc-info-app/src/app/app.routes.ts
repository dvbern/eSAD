import { Route } from '@angular/router';

export const appRoutes: Route[] = [
  {
    path: '',
    loadChildren: () => import('@dvb/feat-publicinfo').then((m) => m.publicinfoRoutes),
  },
];
