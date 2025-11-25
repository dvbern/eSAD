import { ChangeDetectionStrategy, Component, computed, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { PageComponent, Path, TenantSettingsStore } from '@dvb/feat-shared';
import { CardComponent } from '@dvb/ui-components';
import { isNotNullish } from '@dvb/util-shared';
import { TranslocoPipe, TranslocoService } from '@jsverse/transloco';
import { NgbNav, NgbNavItem } from '@ng-bootstrap/ng-bootstrap';
import { AnlassConfigStore } from '../anlass-config.store';

@Component({
  selector: 'feat-anlass-config',
  imports: [
    CardComponent,
    NgbNav,
    NgbNavItem,
    PageComponent,
    RouterOutlet,
    TranslocoPipe,
    RouterLink,
    RouterLinkActive,
  ],
  templateUrl: './anlass-config-detail.page.component.html',
  styleUrl: './anlass-config-detail.page.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AnlassConfigDetailPageComponent {
  readonly store = inject(AnlassConfigStore);
  readonly tenantSettingsStore = inject(TenantSettingsStore);
  readonly #translocoService = inject(TranslocoService);
  protected readonly Path = Path;

  pageTitle = computed(() => {
    const mandant = isNotNullish(this.tenantSettingsStore.settings())
      ? this.tenantSettingsStore.settings()?.tenant
      : '?';
    if (this.store.selectedIsNew()) {
      return this.#translocoService.translate('anlassconfig.detail.page.title.create', {
        mandant: mandant,
      });
    } else {
      const typ = this.#translocoService.translate('AnlassTyp.' + this.store.selectedEntity()?.data?.anlassTyp);
      return this.#translocoService.translate('anlassconfig.detail.page.title', {
        typ: typ,
        mandant: mandant,
      });
    }
  });
}
