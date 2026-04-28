import {inject} from '@angular/core';
import {Route} from '@angular/router';
import {
  AgbPageComponent,
  AufgabenStore,
  BenutzerStore,
  GenericMessagePageComponent,
  Path,
  requireAcceptedAgb,
  requireNotAcceptedAgb,
  requireRole,
  requireStatistikEnabled,
  TenantSettingsStore,
} from '@dvb/feat-shared';
import {
  hasAdminPageViewBerechtiung,
  hasAllNotizenEditBerechtigung,
  hasAnlassViewBerechtigung,
  hasAufgabenViewBerechtigungen,
  hasConfigViewBerechtigung, hasDashboardBerechtigungen,
  hasKindPageViewBerechtigung,
  hasPlaygroundViewBerechtigung,
  hasReportViewBerechtigung,
  hasSystemPageViewBerechtiung,
  withAnyBerechtigung,
} from '@dvb/util-shared';

export const appRoutes: Route[] = [
  {
    path: '',
    resolve: {
      aufgaben: () => {
        const benutzerStore = inject(BenutzerStore);
        if (benutzerStore.createHasRequiredRoleSig(hasAufgabenViewBerechtigungen())()) {
          inject(AufgabenStore).getNumOfFaelligeAufgabenForCurrentUser();
        }
      },
    },
    children: [
      {
        path: '',
        pathMatch: 'full',
        redirectTo: () => inject(TenantSettingsStore).defaultLandingPage(), // get tenant's default page
      },
      {
        path: Path.INFO,
        pathMatch: 'full',
        component: GenericMessagePageComponent,
      },
      {
        path: Path.AGB,
        pathMatch: 'full',
        canActivate: [
          requireRole(withAnyBerechtigung()),
          requireNotAcceptedAgb()
        ],
        component: AgbPageComponent,
      },
      {
        path: Path.APP_MODE_SELECTION,
        canActivate: [
          requireRole(withAnyBerechtigung()),
          requireAcceptedAgb(),
        ],
        loadChildren: () => import('@dvb/feat-app-mode-selection').then(c => c.appModeSelectionRoutes),
      },
      {
        path: Path.STATISTIK,
        canActivate: [
          requireRole(withAnyBerechtigung()),
          requireAcceptedAgb(),
          requireStatistikEnabled,
        ],
        loadChildren: () => import('@dvb/feat-statistik').then(c => c.statistikRoutes),
      },
      {
        path: Path.DASHBOARD,
        canActivate: [
          requireRole(hasDashboardBerechtigungen()),
          requireAcceptedAgb(),
        ],
        loadChildren: () => import('@dvb/feat-dashboard').then(c => c.dashboardRoutes),
      },
      {
        path: Path.ANLASS,
        canActivate: [
          requireRole(hasAnlassViewBerechtigung()),
          requireAcceptedAgb(),
        ],
        loadChildren: () => import('@dvb/feat-anlass').then(c => c.anlassRoutes),
      },
      {
        path: Path.KINDER,
        canActivate: [
          requireRole(hasKindPageViewBerechtigung()),
          requireAcceptedAgb(),
        ],
        loadChildren: () => import('@dvb/feat-kinder').then(c => c.kinderRoutes),
      },
      {
        path: Path.REPORT,
        canActivate: [
          requireRole(hasReportViewBerechtigung()),
          requireAcceptedAgb(),
        ],
        loadChildren: () => import('@dvb/feat-reports').then(c => c.reportsRoutes),
      },
      {
        path: Path.ADMIN,
        canActivate: [
          requireRole(hasAdminPageViewBerechtiung()),
          requireAcceptedAgb(),
        ],
        loadChildren: () => import('@dvb/feat-admin').then(c => c.adminRoutes),
      },
      {
        path: Path.SYSTEM,
        canActivate: [
          requireRole(hasSystemPageViewBerechtiung()),
          requireAcceptedAgb(),
        ],
        loadChildren: () => import('@dvb/feat-system').then(c => c.systemRoutes),
      },
      {
        path: Path.NOTIZEN,
        canActivate: [
          requireRole(hasAllNotizenEditBerechtigung()),
          requireAcceptedAgb(),
        ],
        loadChildren: () => import('@dvb/feat-notizen').then(c => c.notizenListRoutes),
      },
      {
        path: Path.AUFGABEN,
        canActivate: [
          requireRole(hasAufgabenViewBerechtigungen()),
          requireAcceptedAgb(),
        ],
        loadChildren: () => import('@dvb/feat-aufgaben').then(c => c.aufgabenRoutes),
      },
      {
        path: Path.CONFIG,
        canActivate: [
          requireRole(hasConfigViewBerechtigung()),
          requireAcceptedAgb(),
        ],
        loadChildren: () => import('@dvb/feat-config').then(c => c.configRoutes),
      },
      {
        path: Path.PLAYGROUND,
        canActivate: [
          requireRole(hasPlaygroundViewBerechtigung()),
          requireAcceptedAgb(),
        ],
        loadChildren: () => import('@dvb/feat-playground').then(c => c.playgroundRoutes),
      },
      {path: '**', redirectTo: 'dummy'},
    ],
  },
];
