import {Route} from "@angular/router";
import {inject} from "@angular/core";
import {ArchivierungStore} from "./archivierung.store";
import {ArchivierungListPageComponent} from "./archivierung-list/archivierung-list.page.component";
import {ArchivierungListComponent} from "./archivierung-list/archivierung-list.component";
import {Path} from "@dvb/feat-shared";

export const archivierungRoutes: Route[] = [
  {
    path: '',
    providers: [ArchivierungStore],
    // resolve: {
    //   _store: () => inject(ArchivierungStore).loadArchivDokumente(),
    // },
    component: ArchivierungListPageComponent,
    children: [
      {
        path: '',
        pathMatch: 'full',
        redirectTo: Path.ARCHIVIERUNG_OFFEN,
      },
      {
        path: Path.ARCHIVIERUNG_OFFEN,
        component: ArchivierungListComponent,
        resolve: {
          _store: () => {
            inject(ArchivierungStore).loadArchivDokumente(true);
            inject(ArchivierungStore).setArchivModus("OFFEN");
          },
        }
      },
      {
        path: Path.ARCHIVIERUNG_ABGESCHLOSSEN,
        component: ArchivierungListComponent,
        resolve: {
          _store: () => {
            inject(ArchivierungStore).loadArchivDokumente(false);
            inject(ArchivierungStore).setArchivModus("ABGESCHLOSSEN");
          },
        }
      }
    ],
  }
];
