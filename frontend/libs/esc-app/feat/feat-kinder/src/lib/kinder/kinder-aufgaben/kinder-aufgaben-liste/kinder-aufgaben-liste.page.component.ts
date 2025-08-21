import {ChangeDetectionStrategy, Component, inject} from '@angular/core';
import {AufgabenStore} from '@dvb/feat-shared';
import {BenutzerStore} from '@dvb/feat-shared';
import {Path} from '@dvb/feat-shared';
import {PageComponent} from '@dvb/feat-shared';
import {AufgabenListComponent} from '@dvb/feat-shared';;
import {PageHeaderAddButtonComponent} from '@dvb/ui-components';
import {hasAufgabenEditBerechtigungen} from '@dvb/util-shared';
import {TranslocoPipe} from '@jsverse/transloco';
import {DossierStore} from '../../dossier.store';

@Component({
  selector: 'feat-kinder-aufgaben.page',
  templateUrl: './kinder-aufgaben-liste.page.component.html',
  styles: ':host { display:block;}',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    AufgabenListComponent,
    PageComponent,
    TranslocoPipe,
    PageHeaderAddButtonComponent,
  ],
})
export class KinderAufgabenListePageComponent {
  readonly store = inject(AufgabenStore);
  readonly dossierStore = inject(DossierStore);

  protected readonly Path = Path;

  readonly benutzerStore = inject(BenutzerStore);
  canEditAufgabe = this.benutzerStore.createHasRequiredRoleSig(hasAufgabenEditBerechtigungen());

}
