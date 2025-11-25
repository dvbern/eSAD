
import {ChangeDetectionStrategy, Component, computed, inject} from '@angular/core';
import {RouterLink, RouterLinkActive, RouterOutlet} from '@angular/router';
import {KlassenStore} from '@dvb/feat-shared';
import {Path} from '@dvb/feat-shared';
import {PageComponent} from '@dvb/feat-shared';
import {CardComponent} from '@dvb/ui-components';
import {TranslocoPipe, TranslocoService} from '@jsverse/transloco';
import {NgbNav, NgbNavItem} from '@ng-bootstrap/ng-bootstrap';

@Component({
  selector: 'feat-klassen-detail.page',
  imports: [
    PageComponent,
    TranslocoPipe,
    CardComponent,
    NgbNav,
    NgbNavItem,
    RouterLink,
    RouterLinkActive,
    RouterOutlet
],
  templateUrl: './klassen-detail.page.component.html',
  styleUrl: './klassen-detail.page.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class KlassenDetailPageComponent {
  readonly store = inject(KlassenStore);
  readonly #transloco = inject(TranslocoService);

  protected readonly Path = Path;

  schulhausName = computed(() => this.store.selectedEntity()?.data.schule.bezeichnung);
  klassenbezeichnung = computed(() => this.store.selectedEntity()?.data.bezeichnung);

  getPageTitle(): string {
    if (this.store.selectedIsNew()) {
      return this.#transloco.translate('klasse.detail.page.title.create');
    } else {
      return this.schulhausName() + ' ' + this.klassenbezeichnung();
    }
  }
}
