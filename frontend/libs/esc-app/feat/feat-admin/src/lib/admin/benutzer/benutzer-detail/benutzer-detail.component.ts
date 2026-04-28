import {ChangeDetectionStrategy, Component, computed, inject, viewChild} from "@angular/core";
import {DirtyMarker} from "@dvb/util-angular";
import {BenutzerDetailsComponent, BenutzerListStore, BenutzerStore, PageComponent, Path} from "@dvb/feat-shared";
import {ReactiveFormsModule} from "@angular/forms";
import {ActivatedRoute, Router} from "@angular/router";
import {CardComponent} from "@dvb/ui-components";
import {TranslocoPipe, TranslocoService} from "@jsverse/transloco";
import {hasBenutzerverwaltungEditBerechtigungen} from "@dvb/util-shared";

@Component({
  selector: 'feat-benutzer-detail',
  imports: [
    CardComponent,
    PageComponent,
    TranslocoPipe,
    ReactiveFormsModule,
    BenutzerDetailsComponent
  ],
  templateUrl: './benutzer-detail.component.html',
  styleUrl: './benutzer-detail.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class BenutzerDetailComponent implements DirtyMarker {
  benutzerDetailComponent = viewChild.required(BenutzerDetailsComponent);

  readonly store = inject(BenutzerListStore);
  readonly benutzerStore = inject(BenutzerStore);
  readonly #router = inject(Router);
  readonly #activatedRoute = inject(ActivatedRoute);
  readonly #transloco = inject(TranslocoService);

  protected readonly Path = Path;

  canEdit = this.benutzerStore.createHasRequiredRoleSig(hasBenutzerverwaltungEditBerechtigungen());

  // TODO ESC-1197: Aktuell wird nur 1 Organisation pro Benutzer unterstützt
  organisation = computed(() =>
    this.store.selectedIsNew() || this.store.selectedEntity()?.data.organisationRollen.length === 0
      ? undefined
      : this.store.selectedEntity()?.data.organisationRollen[0].organisation);

  pageTitle = computed(() => {
    if (this.store.selectedIsNew()) {
      return this.#transloco.translate('benutzer.detail.title.create');
    } else {
      return this.#transloco.translate('benutzer.detail.title',
        {name: this.store.selectedEntity()?.data.fullName});
    }
  });

  isDirty(): boolean {
    return this.benutzerDetailComponent().isDirty() || this.store.loaderSave.isLoading();
  }

  navigateToList(): void {
    this.#router.navigate([Path.RELATIVE_ONE_UP], {relativeTo: this.#activatedRoute});
  }
}
