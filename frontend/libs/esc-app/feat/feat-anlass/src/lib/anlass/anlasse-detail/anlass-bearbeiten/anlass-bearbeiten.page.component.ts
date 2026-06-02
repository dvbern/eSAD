import {ChangeDetectionStrategy, Component, computed, inject, ViewChild} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {PageComponent, Path} from '@dvb/feat-shared';
import {ButtonComponent, CardComponent, PageHeaderButtonsComponent} from '@dvb/ui-components';
import {DirtyMarker} from '@dvb/util-angular';
import {requireValue} from '@dvb/util-shared';
import {TranslocoPipe, TranslocoService} from '@jsverse/transloco';
import {AnlassStore} from '../../anlass.store';
import {anlassTitle} from '../../anlass.util';
import {AnlassDatenComponent} from './daten/anlass-daten.component';
import {KinderComponent} from './kinder/kinder.component';

@Component({
  selector: 'feat-anlass-detail-anlassdaten',
  templateUrl: './anlass-bearbeiten.page.component.html',
  styleUrl: './anlass-bearbeiten.page.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    CardComponent,
    AnlassDatenComponent,
    KinderComponent,
    PageComponent,
    TranslocoPipe,
    ButtonComponent,
    PageHeaderButtonsComponent,
  ],
})
export class AnlassBearbeitenPageComponent implements DirtyMarker {
  @ViewChild(AnlassDatenComponent) anlassDatenComponent!: AnlassDatenComponent;

  readonly store = inject(AnlassStore);
  readonly transloco = inject(TranslocoService);
  readonly #router = inject(Router);
  readonly #activatedRoute = inject(ActivatedRoute);

  protected readonly Path = Path;

  isDirty(): boolean {
    return this.anlassDatenComponent.form.isDirty() || this.store.saveLoader.isLoading();
  }

  pageTitle = computed(() => {
    if (this.store.selectedIsNew()) {
      return this.transloco.translate('anlass.detail.page.title.create');
    } else {

      const anlass = this.store.selectedEntity();

      if (!anlass) {
        return this.transloco.translate('anlass.detail.page.title.edit');
      }
      return requireValue(anlassTitle(anlass.data, this.transloco));
    }
  });

  backlink = computed(() => {
    if (this.store.selectedIsNew()) {
      return Path.RELATIVE_ONE_UP; // create mode back to list
    }
    return Path.RELATIVE_TWO_UP; // edit mode back to list instead of planung
  });

  gotoPlanung(): void {
    this.#router.navigate([Path.RELATIVE_ONE_UP, Path.ANLASS_PLANUNG], {relativeTo: this.#activatedRoute});
  }

}
