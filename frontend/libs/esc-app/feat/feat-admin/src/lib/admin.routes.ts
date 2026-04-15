import {inject} from '@angular/core';
import {Route} from '@angular/router';
import {Path, QuartiereOptionsStore, requireAcceptedAgb, requireRole, SchulenStore} from '@dvb/feat-shared';
import {
  hasArchivierungBerechtigung,
  hasBenutzerViewBerechtigung,
  hasBriefvorlagenViewBerechtigung,
  hasDuplikateBerechtigung,
  hasImpfstoffePageViewBerechtigung,
  hasLabelPageViewBerechtigung,
  hasListViewBerechtigung,
  hasOrganisationVerwaltungViewBerechtigung,
} from '@dvb/util-shared';
import {AdminPageComponent} from './admin/admin-page.component';

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
      requireAcceptedAgb(),
    ],
  },
  {
    path: Path.KLASSEN,
    loadChildren: () => import('./admin/klassen').then(c => c.klassenRoutes),
    canActivate: [
      requireRole(hasListViewBerechtigung()),
      requireAcceptedAgb(),
    ],
  },
  {
    path: Path.LABEL,
    loadChildren: () => import('./admin/label').then(c => c.labelsRoutes),
    canActivate: [
      requireRole(hasLabelPageViewBerechtigung()),
      requireAcceptedAgb(),
    ],
  },
  {
    path: Path.IMPFSTOFF,
    loadChildren: () => import('./admin/impfstoff').then(c => c.impfstoffRoutes),
    canActivate: [
      requireRole(hasImpfstoffePageViewBerechtigung()),
      requireAcceptedAgb(),
    ],
  },
  {
    path: Path.INFOMATERIAL,
    loadChildren: () => import('./admin/infomaterial').then(c => c.infomaterialRoutes),
    canActivate: [
      requireRole(hasListViewBerechtigung()),
      requireAcceptedAgb(),
    ],
  },
  {
    path: Path.BENUTZER,
    loadChildren: () => import('./admin/benutzer').then(c => c.benutzerRoutes),
    canActivate: [
      requireRole(hasBenutzerViewBerechtigung()),
      requireAcceptedAgb(),
    ],
  },
  {
    path: Path.BRIEFVORLAGEN,
    loadChildren: () => import('./admin/briefvorlagen').then(c => c.briefvorlageRoutes),
    canActivate: [
      requireRole(hasBriefvorlagenViewBerechtigung()),
      requireAcceptedAgb(),
    ],
  },
  {
    path: Path.DUPLIKATE,
    loadChildren: () => import('./admin/duplikat').then(c => c.duplikatRoutes),
    canActivate: [
      requireRole(hasDuplikateBerechtigung()),
      requireAcceptedAgb(),
    ],
  },
  {
    path: Path.AERZTE,
    loadChildren: () => import('./admin/aerzte').then(c => c.aerzteRoutes),
    canActivate: [
      requireRole(hasListViewBerechtigung()),
      requireAcceptedAgb(),
    ],
  },
  {
    path: Path.ARCHIVIERUNG,
    loadChildren: () => import('./admin/archivierung').then(c => c.archivierungRoutes),
    canActivate: [
      requireRole(hasArchivierungBerechtigung()),
      requireAcceptedAgb(),
    ],
  },
  {
    path: Path.ORGANISATION,
    loadChildren: () => import('./admin/organisation').then(c => c.organisationRoutes),
    canActivate: [
      requireRole(hasOrganisationVerwaltungViewBerechtigung()),
      requireAcceptedAgb(),
    ],
  },
];
