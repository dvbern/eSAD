import {CommonModule} from '@angular/common';
import {ChangeDetectionStrategy, Component, computed, inject} from '@angular/core';
import {RouterLink, RouterLinkActive, RouterOutlet} from '@angular/router';
import {SchulenStore} from '@dvb/feat-shared';
import {Path} from '@dvb/feat-shared';
import {PageComponent} from '@dvb/feat-shared';
import {CardComponent} from '@dvb/ui-components';
import {TranslocoPipe} from '@jsverse/transloco';
import {NgbNav, NgbNavItem} from '@ng-bootstrap/ng-bootstrap';

@Component({
    selector: 'feat-schulen-detail.page',
    imports: [
        CommonModule, RouterOutlet, RouterLink, RouterLinkActive, TranslocoPipe,
        NgbNav, NgbNavItem, CardComponent, PageComponent,
    ],
    templateUrl: './schulen-detail.page.component.html',
    styleUrl: './schulen-detail.page.component.scss',
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class SchulenDetailPageComponent {
  readonly store = inject(SchulenStore);

  protected readonly Path = Path;

  schulhausName = computed(() => this.store.selectedEntity()?.data.schulhaus);

  pageTitle = computed(() => {
    if (this.store.selectedIsNew()) {
      return 'schule.detail.page.title.create';
    } else {
      return this.schulhausName();
    }
  })

}
