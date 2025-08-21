import {Location} from '@angular/common';
import {ChangeDetectionStrategy, Component, computed, inject, ViewChild} from '@angular/core';
import {Router} from '@angular/router';
import {AufgabenStore} from '@dvb/feat-shared';
import {Path} from '@dvb/feat-shared';

import {PageComponent} from '@dvb/feat-shared';
import {AufgabeDetailsComponent} from '@dvb/feat-shared';;
import {DirtyMarker} from '@dvb/util-angular';
import {TranslocoPipe, TranslocoService} from '@jsverse/transloco';
import {DossierStore} from '../../dossier.store';

@Component({
  selector: 'feat-kinder-aufgabe-details.page',
  templateUrl: './kinder-aufgabe-details.page.component.html',
  styles: ':host { display:block;}',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    PageComponent,
    AufgabeDetailsComponent,
    TranslocoPipe,
  ],
})
export class KinderAufgabeDetailsPageComponent implements DirtyMarker {
  @ViewChild(AufgabeDetailsComponent) aufgabeDetailsComponent!: AufgabeDetailsComponent;

  readonly store = inject(AufgabenStore);
  readonly dossierStore = inject(DossierStore);

  readonly transloco = inject(TranslocoService);
  readonly router = inject(Router);
  readonly location = inject(Location);

  protected readonly Path = Path;

  pageHeading = computed(() => {
    const selectedEntity = this.store.selectedEntity();
    return selectedEntity
      ? selectedEntity.data.titel
      : this.transloco.translate('aufgabe.details.titel.neu.kind', { nameKind: this.dossierStore.nameKind() });
  });

  isDirty(): boolean {
    return this.aufgabeDetailsComponent.isDirty() || this.store.saveLoader.isLoading();
  }

  navigateBack(): void {
    this.location.back();
  }
}
