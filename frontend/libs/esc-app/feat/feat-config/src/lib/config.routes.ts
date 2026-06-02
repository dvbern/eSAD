import {inject} from '@angular/core';
import {Route} from '@angular/router';
import {Path, requireAcceptedAgb, requireRole} from '@dvb/feat-shared';
import {hasConfigViewBerechtigung} from '@dvb/util-shared';
import {AnlassConfigStore} from './config/anlass-config';
import {ConfigPageComponent} from './config/config.page.component';
import {TenantSettingsPageComponent} from './config/tenant-settings/tenant-settings-page.component';

export const configRoutes: Route[] = [
  {
    path: '',
    pathMatch: 'full',
    component: ConfigPageComponent,
  },
  {
    path: Path.ANLASS_CONFIG,
    loadChildren: () => import('./config/anlass-config').then(c => c.anlassConfigRoutes),
    resolve: {
      _store: () => {
        inject(AnlassConfigStore).loadAnlassConfigList();
        inject(AnlassConfigStore).setSelectedEntityId(undefined);
      },
    },
    canActivate: [
      requireRole(hasConfigViewBerechtigung()),
      requireAcceptedAgb(),
    ],
  },
  {
    path: Path.TENANT_CONFIG,
    component: TenantSettingsPageComponent
  }
];
