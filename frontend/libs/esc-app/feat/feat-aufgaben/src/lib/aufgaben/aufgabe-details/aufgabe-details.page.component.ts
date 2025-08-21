import {CommonModule} from '@angular/common';
import {ChangeDetectionStrategy, Component, computed, inject, ViewChild} from '@angular/core';
import {ReactiveFormsModule} from '@angular/forms';
import {Router} from '@angular/router';
import {AufgabenStore} from '@dvb/feat-shared';
import {Path} from '@dvb/feat-shared';
import {PageComponent} from '@dvb/feat-shared';
import {AufgabeDetailsComponent} from '@dvb/feat-shared';;
import {DirtyMarker} from '@dvb/util-angular';
import {TranslocoPipe, TranslocoService} from '@jsverse/transloco';

@Component({
  selector: 'feat-aufgabe-details',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    CommonModule,
    PageComponent,
    TranslocoPipe,
    ReactiveFormsModule,
    AufgabeDetailsComponent,
  ],
  templateUrl: './aufgabe-details.page.component.html',
  styles: `
    :host {
      display: block;
    }
  `,
})
export class AufgabeDetailsPageComponent implements DirtyMarker {
  @ViewChild(AufgabeDetailsComponent) aufgabenDetailComponent!: AufgabeDetailsComponent;

  readonly store = inject(AufgabenStore);
  readonly transloco = inject(TranslocoService);
  readonly #router = inject(Router);

  protected readonly Path = Path;

  isDirty(): boolean {
    return this.aufgabenDetailComponent.isDirty() || this.store.saveLoader.isLoading();
  }

  pageHeading = computed(() => {
    const selectedEntity = this.store.selectedEntity();
    return selectedEntity ? selectedEntity.data.titel : this.transloco.translate('aufgabe.details.titel.neu');
  });

  async navigateToList(): Promise<void> {
    await this.#router.navigate([Path.AUFGABEN]);
  }
}
