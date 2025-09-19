import {inject} from '@angular/core';
import {Route} from '@angular/router';
import {AufgabenStore, NoRolesPageComponent, Path, requireRole} from '@dvb/feat-shared';
import {
  hasAdminPageViewBerechtiung,
  hasAnlassViewBerechtigung,
  hasAufgabenViewBerechtigungen,
  hasKindPageViewBerechtigung,
  hasPlaygroundViewBerechtigung,
  hasReportsViewBerechtigung,
  hasSystemPageViewBerechtiung,
} from '@dvb/util-shared';

export const appRoutes: Route[] = [
  {
    path: '',
    resolve: {
      aufgaben: () => {
        inject(AufgabenStore).getNumOfFaelligeAufgabenForCurrentUser();
      },
    },
    children: [
      {
        path: '',
        pathMatch: 'full',
        redirectTo: Path.DASHBOARD,
      },
      {
        path: Path.NO_ROLES,
        pathMatch: 'full',
        component: NoRolesPageComponent,
      },
      {
        path: Path.DASHBOARD,
        canActivate: [
          requireRole(hasAnlassViewBerechtigung()),
        ],
        loadChildren: () => import('@dvb/feat-dashboard').then(c => c.dashboardRoutes),
      },
      {
        path: Path.ANLASS,
        canActivate: [
          requireRole(hasAnlassViewBerechtigung()),
        ],
        loadChildren: () => import('@dvb/feat-anlass').then(c => c.anlassRoutes),
      },
      {
        path: Path.KINDER,
        canActivate: [
          requireRole(hasKindPageViewBerechtigung()),
        ],
        loadChildren: () => import('@dvb/feat-kinder').then(c => c.kinderRoutes),
      },
      {
        path: Path.REPORT,
        canActivate: [
          requireRole(hasReportsViewBerechtigung()),
        ],
        loadChildren: () => import('@dvb/feat-reports').then(c => c.reportsRoutes),
      },
      {
        path: Path.ADMIN,
        canActivate: [
          requireRole(hasAdminPageViewBerechtiung()),
        ],
        loadChildren: () => import('@dvb/feat-admin').then(c => c.adminRoutes),
      },
      {
        path: Path.SYSTEM,
        canActivate: [
          requireRole(hasSystemPageViewBerechtiung()),
        ],
        loadChildren: () => import('@dvb/feat-system').then(c => c.systemRoutes),
      },
      {
        path: Path.AUFGABEN,
        canActivate: [
          requireRole(hasAufgabenViewBerechtigungen()),
        ],
        loadChildren: () => import('@dvb/feat-aufgaben').then(c => c.aufgabenRoutes),
      },
      {
        path: Path.PLAYGROUND,
        canActivate: [
          requireRole(hasPlaygroundViewBerechtigung()),
        ],
        loadChildren: () => import('@dvb/feat-playground').then(c => c.playgroundRoutes),
      },
      { path: '**', redirectTo: 'dummy' },
    ],
  },
];
