import {ChangeDetectionStrategy, Component, computed, inject} from '@angular/core';
import {RouterLink, RouterLinkActive, RouterOutlet} from '@angular/router';
import {Path} from '@dvb/feat-shared';
import {PageComponent} from '@dvb/feat-shared';
import {CardComponent} from '@dvb/ui-components';
import {TranslocoPipe} from '@jsverse/transloco';
import {NgbNav, NgbNavItem} from '@ng-bootstrap/ng-bootstrap';
import {DossierStore} from '../dossier.store';
import {KontaktPersonKindStore} from './kontaktpersonen/kontakt-person-kind.store';

@Component({
  selector: 'feat-kinder-detail-page',
  templateUrl: './kinder-detail-page.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    PageComponent,
    TranslocoPipe,
    CardComponent,
    NgbNav,
    NgbNavItem,
    RouterOutlet,
    RouterLink,
    RouterLinkActive,
  ],
})
export class KinderDetailPageComponent {
  readonly store = inject(DossierStore);
  readonly kontaktPersonKindStore = inject(KontaktPersonKindStore);

  fullName = computed(() => this.store.nameKind());

  protected readonly Path = Path;
}
