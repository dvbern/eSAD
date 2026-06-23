
import {ChangeDetectionStrategy, Component, computed, inject, viewChild} from '@angular/core';
import {ReactiveFormsModule} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {AufgabenStore} from '@dvb/feat-shared';
import {Path} from '@dvb/feat-shared';
import {PageComponent} from '@dvb/feat-shared';
import {AufgabeDetailsComponent} from '@dvb/feat-shared';
import {DirtyMarker} from '@dvb/util-angular';
import {TranslocoPipe, TranslocoService} from '@jsverse/transloco';

@Component({
  selector: 'feat-aufgabe-details',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    PageComponent,
    TranslocoPipe,
    ReactiveFormsModule,
    AufgabeDetailsComponent
],
  templateUrl: './aufgabe-details.page.component.html',
  styles: `
    :host {
      display: block;
    }
  `,
})
export class AufgabeDetailsPageComponent implements DirtyMarker {
  aufgabenDetailComponent = viewChild.required(AufgabeDetailsComponent);

  readonly store = inject(AufgabenStore);
  readonly transloco = inject(TranslocoService);
  readonly #router = inject(Router);
  readonly #activeRoute = inject(ActivatedRoute);

  protected readonly Path = Path;

  isDirty(): boolean {
    return this.aufgabenDetailComponent().isDirty() || this.store.saveLoader.isLoading();
  }

  pageHeading = computed(() => {
    const selectedEntity = this.store.selectedEntity();
    return selectedEntity ? selectedEntity.data.titel : this.transloco.translate('aufgabe.details.titel.neu');
  });

  async navigateToList(): Promise<void> {
    await this.#router.navigate([Path.AUFGABEN]);
  }

  navigateToEdit(): void {
    this.#router.navigate([Path.RELATIVE_ONE_UP, this.store.selectedEntityId()], {
      relativeTo: this.#activeRoute,
      replaceUrl: true,
    });
  }
}
