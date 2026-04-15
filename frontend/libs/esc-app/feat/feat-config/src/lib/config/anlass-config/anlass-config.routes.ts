import {inject} from '@angular/core';
import {ActivatedRouteSnapshot, Route} from '@angular/router';
import {requireValidBackendEntityId} from '@dvb/data-access-types';
import {Path} from '@dvb/feat-shared';
import {confirmOnDirty, toDetailModeStrict} from '@dvb/util-angular';
import {AnlassConfigListPageComponent} from './anlass-config-list/anlass-config-list.page.component';
import {AnlassConfigStore} from './anlass-config.store';
import {
  AnlassConfigDetailAerztlicheUntersuchungComponent,
} from './anlass-config/anlass-config-aerztliche-untersuchung/anlass-config-detail-aerztliche-untersuchung.component';
import {
  AnlassConfigDetailBasisComponent,
} from './anlass-config/anlass-config-basis/anlass-config-detail-basis.component';
import {
  AnlassConfigDetailDatenComponent,
} from './anlass-config/anlass-config-daten/anlass-config-detail-daten.component';
import {AnlassConfigDetailPageComponent} from './anlass-config/anlass-config-detail.page.component';
import {
  AnlassConfigDetailGroesseGewichtComponent,
} from './anlass-config/anlass-config-groesse-gewicht/anlass-config-detail-groesse-gewicht.component';
import {
  AnlassConfigDetailHoerenAudiogrammComponent,
} from './anlass-config/anlass-config-hoeren-audiogramm/anlass-config-detail-hoeren-audiogramm.component';
import {
  AnlassConfigDetailHoerenOtoskopieComponent,
} from './anlass-config/anlass-config-hoeren-otoskopie/anlass-config-detail-hoeren-otoskopie.component';
import {
  AnlassConfigDetailImpfungenComponent,
} from './anlass-config/anlass-config-impfungen/anlass-config-detail-impfungen.component';
import {
  AnlassConfigDetailMassnahmenComponent,
} from './anlass-config/anlass-config-massnahmen/anlass-config-detail-massnahmen.component';
import {
  AnlassConfigDetailSehenComponent,
} from './anlass-config/anlass-config-sehen/anlass-config-detail-sehen.component';
import {
  AnlassConfigDetailTriageComponent,
} from './anlass-config/anlass-config-triage/anlass-config-detail-triage.component';
import {
  AnlassConfigDetailLaeusekontrolleComponent
} from "./anlass-config/anlass-config-laeusekontrolle/anlass-config-detail-laeusekontrolle.component";
import {
  AnlassConfigDetailSpracheComponent
} from "./anlass-config/anlass-config-detail-sprache/anlass-config-detail-sprache.component";
import {
  AnlassConfigDetailSchulrelevanteBeeintraechtigungComponent
} from "./anlass-config/anlass-config-detail-schulrelevante-beeintraechtigung/anlass-config-detail-schulrelevante-beeintraechtigung.component";
import {
  AnlassConfigDetailBewegungsapparatComponent
} from "./anlass-config/anlass-config-bewegungsapparat/anlass-config-detail-bewegungsapparat.component";
import {
  AnlassConfigDetailAbschlussComponent
} from "./anlass-config/anlass-config-abschluss/anlass-config-detail-abschluss.component";
import {
  AnlassConfigDetailVorabklaerungenComponent
} from "./anlass-config/anlass-config-vorabklaerungen/anlass-config-detail-vorabklaerungen.component";

export const anlassConfigRoutes: Route[] = [
  {
    path: '',
    component: AnlassConfigListPageComponent,
  },
  {
    path: Path.CREATE,
    component: AnlassConfigDetailPageComponent,
    resolve: {
      _store: () => {
        inject(AnlassConfigStore).setSelectedEntityId(undefined);
      },
    },
    children: [
      {
        path: '',
        pathMatch: 'full',
        redirectTo: Path.ANLASS_CONFIG_BASIS,
      },
      {
        path: Path.ANLASS_CONFIG_BASIS,
        component: AnlassConfigDetailBasisComponent,
        resolve: {
          _store: () => {
            inject(AnlassConfigStore).setDetailModeEdit();
          },
        },
        canDeactivate: [confirmOnDirty()],
      },
    ],
  },
  {
    path: ':id',
    component: AnlassConfigDetailPageComponent,
    resolve: {
      _store: (route: ActivatedRouteSnapshot) => {
        const param = requireValidBackendEntityId(route.params['id']);
        inject(AnlassConfigStore).setSelectedEntityId(param);
      },
    },
    children: [
      {
        path: '',
        pathMatch: 'full',
        redirectTo: Path.ANLASS_CONFIG_BASIS,
      },
      {
        path: Path.ANLASS_CONFIG_BASIS,
        children: [
          {
            path: '',
            pathMatch: 'full',
            redirectTo: 'view',
          },
          {
            path: ':mode',
            component: AnlassConfigDetailBasisComponent,
            resolve: {
              _store: (route: ActivatedRouteSnapshot) => {
                const mode = toDetailModeStrict(route.params['mode']);
                inject(AnlassConfigStore).setDetailMode(mode);
              },
            },
            canDeactivate: [confirmOnDirty()],
          },
        ],
      },
      {
        path: Path.ANLASS_CONFIG_DATEN,
        children: [
          {
            path: '',
            pathMatch: 'full',
            redirectTo: 'view',
          },
          {
            path: ':mode',
            component: AnlassConfigDetailDatenComponent,
            resolve: {
              _store: (route: ActivatedRouteSnapshot) => {
                const mode = toDetailModeStrict(route.params['mode']);
                inject(AnlassConfigStore).setDetailMode(mode);
              },
            },
            canDeactivate: [confirmOnDirty()],
          },
        ],
      },
      {
        path: Path.ANLASS_CONFIG_VORABKLAERUNGEN,
        children: [
          {
            path: '',
            pathMatch: 'full',
            redirectTo: 'view',
          },
          {
            path: ':mode',
            component: AnlassConfigDetailVorabklaerungenComponent,
            resolve: {
              _store: (route: ActivatedRouteSnapshot) => {
                const mode = toDetailModeStrict(route.params['mode']);
                inject(AnlassConfigStore).setDetailMode(mode);
              },
            },
            canDeactivate: [confirmOnDirty()],
          },
        ],
      },
      {
        path: Path.ANLASS_CONFIG_IMPFUNGEN,
        children: [
          {
            path: '',
            pathMatch: 'full',
            redirectTo: 'view',
          },
          {
            path: ':mode',
            component: AnlassConfigDetailImpfungenComponent,
            resolve: {
              _store: (route: ActivatedRouteSnapshot) => {
                const mode = toDetailModeStrict(route.params['mode']);
                inject(AnlassConfigStore).setDetailMode(mode);
              },
            },
            canDeactivate: [confirmOnDirty()],
          },
        ],
      },
      {
        path: Path.ANLASS_CONFIG_SEHEN,
        children: [
          {
            path: '',
            pathMatch: 'full',
            redirectTo: 'view',
          },
          {
            path: ':mode',
            component: AnlassConfigDetailSehenComponent,
            resolve: {
              _store: (route: ActivatedRouteSnapshot) => {
                const mode = toDetailModeStrict(route.params['mode']);
                inject(AnlassConfigStore).setDetailMode(mode);
              },
            },
            canDeactivate: [confirmOnDirty()],
          },
        ],
      },
      {
        path: Path.ANLASS_CONFIG_HOEREN_AUDIOGRAMM,
        children: [
          {
            path: '',
            pathMatch: 'full',
            redirectTo: 'view',
          },
          {
            path: ':mode',
            component: AnlassConfigDetailHoerenAudiogrammComponent,
            resolve: {
              _store: (route: ActivatedRouteSnapshot) => {
                const mode = toDetailModeStrict(route.params['mode']);
                inject(AnlassConfigStore).setDetailMode(mode);
              },
            },
            canDeactivate: [confirmOnDirty()],
          },
        ],
      },
      {
        path: Path.ANLASS_CONFIG_HOEREN_OTOSKOPIE,
        children: [
          {
            path: '',
            pathMatch: 'full',
            redirectTo: 'view',
          },
          {
            path: ':mode',
            component: AnlassConfigDetailHoerenOtoskopieComponent,
            resolve: {
              _store: (route: ActivatedRouteSnapshot) => {
                const mode = toDetailModeStrict(route.params['mode']);
                inject(AnlassConfigStore).setDetailMode(mode);
              },
            },
            canDeactivate: [confirmOnDirty()],
          },
        ],
      },
      {
        path: Path.ANLASS_CONFIG_GROEOSSE_GEWICHT,
        children: [
          {
            path: '',
            pathMatch: 'full',
            redirectTo: 'view',
          },
          {
            path: ':mode',
            component: AnlassConfigDetailGroesseGewichtComponent,
            resolve: {
              _store: (route: ActivatedRouteSnapshot) => {
                const mode = toDetailModeStrict(route.params['mode']);
                inject(AnlassConfigStore).setDetailMode(mode);
              },
            },
            canDeactivate: [confirmOnDirty()],
          },
        ],
      },
      {
        path: Path.ANLASS_CONFIG_TRIAGE,
        children: [
          {
            path: '',
            pathMatch: 'full',
            redirectTo: 'view',
          },
          {
            path: ':mode',
            component: AnlassConfigDetailTriageComponent,
            resolve: {
              _store: (route: ActivatedRouteSnapshot) => {
                const mode = toDetailModeStrict(route.params['mode']);
                inject(AnlassConfigStore).setDetailMode(mode);
              },
            },
            canDeactivate: [confirmOnDirty()],
          },
        ],
      },
      {
        path: Path.ANLASS_CONFIG_LAEUSEKONTROLLE,
        children: [
          {
            path: '',
            pathMatch: 'full',
            redirectTo: 'view',
          },
          {
            path: ':mode',
            component: AnlassConfigDetailLaeusekontrolleComponent,
            resolve: {
              _store: (route: ActivatedRouteSnapshot) => {
                const mode = toDetailModeStrict(route.params['mode']);
                inject(AnlassConfigStore).setDetailMode(mode);
              },
            },
            canDeactivate: [confirmOnDirty()],
          },
        ],
      },
      {
        path: Path.ANLASS_CONFIG_AERZTLICHE_UNTERSUCHUNG,
        children: [
          {
            path: '',
            pathMatch: 'full',
            redirectTo: 'view',
          },
          {
            path: ':mode',
            component: AnlassConfigDetailAerztlicheUntersuchungComponent,
            resolve: {
              _store: (route: ActivatedRouteSnapshot) => {
                const mode = toDetailModeStrict(route.params['mode']);
                inject(AnlassConfigStore).setDetailMode(mode);
              },
            },
            canDeactivate: [confirmOnDirty()],
          },
        ],
      },
      {
        path: Path.ANLASS_CONFIG_SPRACHE,
        children: [
          {
            path: '',
            pathMatch: 'full',
            redirectTo: 'view',
          },
          {
            path: ':mode',
            component: AnlassConfigDetailSpracheComponent,
            resolve: {
              _store: (route: ActivatedRouteSnapshot) => {
                const mode = toDetailModeStrict(route.params['mode']);
                inject(AnlassConfigStore).setDetailMode(mode);
              },
            },
            canDeactivate: [confirmOnDirty()],
          },
        ],
      },
      {
        path: Path.ANLASS_CONFIG_SCHULRELEVANTE_BEEINTRAECHTIGUNG,
        children: [
          {
            path: '',
            pathMatch: 'full',
            redirectTo: 'view',
          },
          {
            path: ':mode',
            component: AnlassConfigDetailSchulrelevanteBeeintraechtigungComponent,
            resolve: {
              _store: (route: ActivatedRouteSnapshot) => {
                const mode = toDetailModeStrict(route.params['mode']);
                inject(AnlassConfigStore).setDetailMode(mode);
              },
            },
            canDeactivate: [confirmOnDirty()],
          },
        ],
      },
      {
        path: Path.ANLASS_CONFIG_BEWEGUNGSPPARAT,
        children: [
          {
            path: '',
            pathMatch: 'full',
            redirectTo: 'view',
          },
          {
            path: ':mode',
            component: AnlassConfigDetailBewegungsapparatComponent,
            resolve: {
              _store: (route: ActivatedRouteSnapshot) => {
                const mode = toDetailModeStrict(route.params['mode']);
                inject(AnlassConfigStore).setDetailMode(mode);
              },
            },
            canDeactivate: [confirmOnDirty()],
          },
        ],
      },
      {
        path: Path.ANLASS_CONFIG_ABSCHLUSS,
        children: [
          {
            path: '',
            pathMatch: 'full',
            redirectTo: 'view',
          },
          {
            path: ':mode',
            component: AnlassConfigDetailAbschlussComponent,
            resolve: {
              _store: (route: ActivatedRouteSnapshot) => {
                const mode = toDetailModeStrict(route.params['mode']);
                inject(AnlassConfigStore).setDetailMode(mode);
              },
            },
            canDeactivate: [confirmOnDirty()],
          },
        ],
      },
      {
        path: Path.ANLASS_CONFIG_MASSNAHMEN,
        children: [
          {
            path: '',
            pathMatch: 'full',
            redirectTo: 'view',
          },
          {
            path: ':mode',
            component: AnlassConfigDetailMassnahmenComponent,
            resolve: {
              _store: (route: ActivatedRouteSnapshot) => {
                const mode = toDetailModeStrict(route.params['mode']);
                inject(AnlassConfigStore).setDetailMode(mode);
              },
            },
            canDeactivate: [confirmOnDirty()],
          },
        ],
      },
    ],
  },
];
