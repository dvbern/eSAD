import {Route} from '@angular/router';
import {SystemAdminStore} from '@dvb/feat-shared';
import {Path} from '@dvb/feat-shared';
import {SystemAdminActionsComponent} from './system/general/system-admin-actions.component';
import {JobsComponent} from './system/jobs/jobs.component';
import {SystemPageComponent} from './system/system-page.component';

export const systemRoutes: Route[] = [
  {
    path: '',
    providers: [SystemAdminStore],
    component: SystemPageComponent,
    children: [
      {
        path: '',
        pathMatch: 'full',
        redirectTo: Path.SYSTEM_GENERAL,
      },
      {
        path: Path.SYSTEM_GENERAL,
        children: [
          {
            path: '',
            pathMatch: 'full',
            component: SystemAdminActionsComponent,
          },
        ],
      },
      {
        path: Path.SYSTEM_JOB,
        children: [
          {
            path: '',
            pathMatch: 'full',
            component: JobsComponent,
          },
        ],
      },
    ],
  },
];

