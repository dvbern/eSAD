import {ChangeDetectionStrategy, Component, computed, inject} from '@angular/core';
import {RouterLink} from '@angular/router';
import {BenutzerStore, Path} from '@dvb/feat-shared';
import {ButtonComponent, ListComponent, PageHeaderButtonsComponent} from '@dvb/ui-components';
import {
  formatDateStr,
  hasKinderMedizinischeDatenEditBerechtigung,
  hasKinderMedizinischeDatenViewBerechtigung,
} from '@dvb/util-shared';
import {TranslocoPipe, TranslocoService} from '@jsverse/transloco';
import {DossierStore} from '../../dossier.store';

@Component({
  selector: 'feat-kind-dossier-impfungen',
  templateUrl: './kind-dossier-impfungen.component.html',
  styles: `
    :host {
      display: block;
    }
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    ListComponent,
    TranslocoPipe,
    PageHeaderButtonsComponent,
    ButtonComponent,
    RouterLink,
  ],
})
export class KindDossierImpfungenComponent {
  readonly transloco = inject(TranslocoService);
  readonly dossierStore = inject(DossierStore);
  readonly benutzerStore = inject(BenutzerStore);
  protected readonly formatDateStr = formatDateStr;
  protected readonly Path = Path;

  canEditMedizinischeDaten = this.benutzerStore.createHasRequiredRoleSig(hasKinderMedizinischeDatenEditBerechtigung());

  lastImpfung = computed(() => this.dossierStore.lastImpfung());
}
