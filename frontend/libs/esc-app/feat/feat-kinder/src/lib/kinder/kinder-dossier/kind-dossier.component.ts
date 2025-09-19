import {ChangeDetectionStrategy, Component, inject} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {AufgabenStore, KindSidebarComponent, PageComponent, Path} from '@dvb/feat-shared';
import {LayoutColumnsComponent} from '@dvb/ui-components';
import {KindAnlaesseComponent} from './dossier-anlaesse/kind-anlaesse.component';
import {KindDossierImpfungenComponent} from './dossier-impfungen/kind-dossier-impfungen.component';
import {KindDossierMainComponent} from './dossier-main/kind-dossier-main.component';
import {KindDossierNotizenComponent} from './dossier-notizen/kind-dossier-notizen.component';
import {DossierStore} from '../dossier.store';

@Component({
  selector: 'feat-kinder-detail-dossier',
  templateUrl: './kind-dossier.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    LayoutColumnsComponent,
    PageComponent,
    FormsModule,
    KindAnlaesseComponent,
    KindDossierNotizenComponent,
    KindDossierMainComponent,
    KindSidebarComponent,
    KindDossierImpfungenComponent,
  ],
})
export class KindDossierComponent {
  readonly dossierStore = inject(DossierStore);
  readonly aufgabenStore = inject(AufgabenStore);
  protected readonly Path = Path;
}
