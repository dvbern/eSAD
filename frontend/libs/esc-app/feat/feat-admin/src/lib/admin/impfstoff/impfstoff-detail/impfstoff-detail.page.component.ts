import {CommonModule} from '@angular/common';
import {ChangeDetectionStrategy, Component, computed, inject} from '@angular/core';
import {ReactiveFormsModule} from '@angular/forms';
import {RouterLink, RouterLinkActive, RouterOutlet} from '@angular/router';
import {ImpfstoffeChargenStore} from '@dvb/feat-shared';
import {BenutzerStore} from '@dvb/feat-shared';
import {Path} from '@dvb/feat-shared';
import {PageComponent} from '@dvb/feat-shared';
import {CardComponent} from '@dvb/ui-components';
import {DetailMode} from '@dvb/util-angular';
import {TranslocoPipe, TranslocoService} from '@jsverse/transloco';
import {NgbNav, NgbNavItem} from '@ng-bootstrap/ng-bootstrap';

@Component({
  selector: 'feat-label-detail.page',
  imports: [
    CommonModule,
    PageComponent,
    TranslocoPipe,
    ReactiveFormsModule,
    CardComponent,
    NgbNav,
    NgbNavItem,
    RouterLink,
    RouterLinkActive,
    RouterOutlet,
  ],
  templateUrl: './impfstoff-detail.page.component.html',
  styles: ':host { display:block;}',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ImpfstoffDetailPageComponent {

  readonly store = inject(ImpfstoffeChargenStore);
  readonly benutzerStore = inject(BenutzerStore);
  protected readonly DetailMode = DetailMode;
  readonly #transloco = inject(TranslocoService);

  protected readonly Path = Path;

  impfstoffeName = computed(() => this.store.selectedEntity()?.data.name);

  pageTitle = computed(() => {
    if (this.store.selectedIsNew()) {
      return 'impfstoff.detail.page.title.create';
    } else {
      return this.#transloco.translate('impfstoff.detail.page.title.edit') + ' ' + this.impfstoffeName();
    }
  });
}
