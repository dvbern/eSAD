import {inject} from '@angular/core';
import {Route} from '@angular/router';
import {QuartiereOptionsStore, SchulenStore} from '@dvb/feat-shared';
import {requireRole} from '@dvb/feat-shared';
import {Path} from '@dvb/feat-shared';
import {
  hasBenutzerViewBerechtigung,
  hasBriefvorlagenViewBerechtigung,
  hasDuplikateBerechtigung,
  hasImpfstoffePageViewBerechtigung,
  hasLabelPageViewBerechtigung,
  hasListViewBerechtigung,
} from '@dvb/util-shared';
import {AdminPageComponent} from './admin/admin-page.component';
import {BenutzerListStore} from './admin/benutzer';

export const adminRoutes: Route[] = [
  {
    path: '',
    component: AdminPageComponent,
  },
  {
    path: Path.SCHULEN,
    loadChildren: () => import('./admin/schulen').then(c => c.schulenRoutes),
    resolve: {
      _store: () => {
        inject(SchulenStore).loadSchulenList();
        inject(SchulenStore).setSelectedEntityId(undefined);
        inject(QuartiereOptionsStore).loadQuartiereList();
      },
    },
    canActivate: [
      requireRole(hasListViewBerechtigung()),
    ],
  },
  {
    path: Path.KLASSEN,
    loadChildren: () => import('./admin/klassen').then(c => c.klassenRoutes),
    canActivate: [
      requireRole(hasListViewBerechtigung()),
    ],
  },
  {
    path: Path.LABEL,
    loadChildren: () => import('./admin/label').then(c => c.labelsRoutes),
    canActivate: [
      requireRole(hasLabelPageViewBerechtigung()),
    ],
  },
  {
    path: Path.IMPFSTOFF,
    loadChildren: () => import('./admin/impfstoff').then(c => c.impfstoffRoutes),
    canActivate: [
      requireRole(hasImpfstoffePageViewBerechtigung()),
    ],
  },
  {
    path: Path.INFOMATERIAL,
    loadChildren: () => import('./admin/infomaterial').then(c => c.infomaterialRoutes),
    canActivate: [
      requireRole(hasListViewBerechtigung()),
    ],
  },
  {
    path: Path.BENUTZER,
    providers: [BenutzerListStore],
    loadChildren: () => import('./admin/benutzer').then(c => c.benutzerRoutes),
    resolve: {
      _store: () => inject(BenutzerListStore).loadBenutzerList(),
    },
    canActivate: [
      requireRole(hasBenutzerViewBerechtigung()),
    ],
  },
  {
    path: Path.BRIEFVORLAGEN,
    loadChildren: () => import('./admin/briefvorlagen').then(c => c.briefvorlageRoutes),
    canActivate: [
      requireRole(hasBriefvorlagenViewBerechtigung()),
    ],
  },
  {
    path: Path.DUPLIKATE,
    loadChildren: () => import('./admin/duplikat').then(c => c.duplikatRoutes),
    canActivate: [
      requireRole(hasDuplikateBerechtigung()),
    ],
  },
  {
    path: Path.AERZTE,
    loadChildren: () => import('./admin/aerzte').then(c => c.aerzteRoutes),
    canActivate: [
      requireRole(hasListViewBerechtigung())
    ],
  },
];
