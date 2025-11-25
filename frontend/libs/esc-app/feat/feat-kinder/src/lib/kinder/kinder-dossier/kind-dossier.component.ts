import {ChangeDetectionStrategy, Component, inject} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {AufgabenStore, BenutzerStore, KindSidebarComponent, PageComponent, Path} from '@dvb/feat-shared';
import {LayoutColumnsComponent} from '@dvb/ui-components';
import {
  hasAufgabenViewBerechtigungen,
  hasKinderMedizinischeDatenViewBerechtigung,
  hasKindNotizViewBerechtigung,
} from '@dvb/util-shared';
import {DossierStore} from '../dossier.store';
import {KindAnlaesseComponent} from './dossier-anlaesse/kind-anlaesse.component';
import {KindDossierImpfungenComponent} from './dossier-impfungen/kind-dossier-impfungen.component';
import {KindDossierMainComponent} from './dossier-main/kind-dossier-main.component';
import {KindDossierNotizenComponent} from './dossier-notizen/kind-dossier-notizen.component';

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
  readonly benutzerStore = inject(BenutzerStore);

  canViewAufgaben = this.benutzerStore.createHasRequiredRoleSig(hasAufgabenViewBerechtigungen());
  canViewNotizen = this.benutzerStore.createHasRequiredRoleSig(hasKindNotizViewBerechtigung());
  canViewMedizinischeDaten = this.benutzerStore.createHasRequiredRoleSig(hasKinderMedizinischeDatenViewBerechtigung());

  protected readonly Path = Path;
}
