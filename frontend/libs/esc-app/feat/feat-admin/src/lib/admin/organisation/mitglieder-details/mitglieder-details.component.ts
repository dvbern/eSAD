import {ChangeDetectionStrategy, Component, computed, inject, viewChild, ViewChild} from "@angular/core";
import {CardComponent} from "@dvb/ui-components";
import {
  BenutzerDetailsComponent,
  BenutzerListStore,
  BenutzerStore,
  OrganisationStore,
  PageComponent,
  Path
} from "@dvb/feat-shared";
import {TranslocoPipe, TranslocoService} from "@jsverse/transloco";
import {ReactiveFormsModule} from "@angular/forms";
import {DirtyMarker} from "@dvb/util-angular";
import {ActivatedRoute, Router} from "@angular/router";
import {hasOrganisationVerwaltungEditBerechtigung, isNotNullish} from "@dvb/util-shared";

@Component({
  selector: 'feat-organisation-mitglieder-details',
  imports: [
    CardComponent,
    PageComponent,
    TranslocoPipe,
    ReactiveFormsModule,
    BenutzerDetailsComponent
  ],
  templateUrl: './mitglieder-details.component.html',
  styleUrl: './mitglieder-details.component.scss',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class MitgliederDetailsComponent implements DirtyMarker {
  benutzerDetailComponent = viewChild.required(BenutzerDetailsComponent);

  readonly organisationStore = inject(OrganisationStore);
  readonly store = inject(BenutzerListStore);
  readonly benutzerStore = inject(BenutzerStore);
  readonly #router = inject(Router);
  readonly #activatedRoute = inject(ActivatedRoute);
  readonly #transloco = inject(TranslocoService);

  canEditMitglieder = this.benutzerStore.createHasRequiredRoleSig(hasOrganisationVerwaltungEditBerechtigung());
  organisation = computed(() => this.organisationStore.selectedIsNew() ?
    undefined :
    this.organisationStore.selectedEntity());

  protected readonly Path = Path;

  pageTitle = computed(() => {
    if (this.organisationStore.selectedIsNew()) {
      return this.#transloco.translate('organisation.title.create');
    } else {
      if (this.store.selectedIsNew()) {
        return this.#transloco.translate('organisation.mitglied.title.create');
      }
      return this.#transloco.translate('organisation.mitglied.edit');
    }
  });

  isDirty(): boolean {
    return this.benutzerDetailComponent().isDirty() || this.store.loaderSave.isLoading();
  }

  navigateToList(): void {
    const selectedOrganisation = this.organisationStore.selectedEntity();
    if (isNotNullish(selectedOrganisation)) {
      this.organisationStore.loadOrganisation(selectedOrganisation.id)
    } else {
      this.organisationStore.loadOrganisationen();
    }
    this.#router.navigate([Path.RELATIVE_ONE_UP], {relativeTo: this.#activatedRoute});
  }

  getBackRouterLink(): string {
    const selectedOrganisation = this.organisationStore.selectedEntity();
    if (isNotNullish(selectedOrganisation)) {
      return '/' + Path.ADMIN + '/' + Path.ORGANISATION + '/' + selectedOrganisation.id;
    } else {
      return '/' + Path.ADMIN + '/' + Path.ORGANISATION
    }
  }
}
