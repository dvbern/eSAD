import {ChangeDetectionStrategy, Component, DestroyRef, inject, OnInit} from "@angular/core";
import {BenutzerStore, OrganisationStore, PageComponent, Path} from '@dvb/feat-shared';
import {FilterSessionStorage, TableModel} from "@dvb/util-angular";
import {OrganisationJson} from "@dvb/data-access-backend";
import {TranslocoPipe} from "@jsverse/transloco";
import {CardComponent, PageHeaderAddButtonComponent} from "@dvb/ui-components";
import {OrganisationListComponent} from "./organisation-list.component";
import {OrganisationFilterForm, OrganisationListFilterForm} from "./organisation-list-filter.form";
import {NonNullableFormBuilder} from "@angular/forms";
import {ensureProps, hasOrganisationVerwaltungEditBerechtigung,} from "@dvb/util-shared";

@Component({
  selector: 'feat-organisation-list.page',
  templateUrl: 'organisation-list-page.component.html',
  standalone: true,
  styleUrl: './organisation-list-page.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    PageComponent,
    TranslocoPipe,
    CardComponent,
    OrganisationListComponent,
    PageHeaderAddButtonComponent
  ],
})
export class OrganisationListPageComponent extends FilterSessionStorage<OrganisationFilterForm> implements OnInit {
  protected readonly Path = Path;
  readonly organisationStore = inject(OrganisationStore);
  readonly benutzerStore = inject(BenutzerStore);

  readonly SESSIONSTORAGE_KEY = 'organisationFilter';

  itemTable = new TableModel<OrganisationJson>(
    {
      rawListSignal: this.organisationStore.entities,
    },
    { frontendOrBackend: 'frontend', pageOrScrollOrNothing: 'scroll' },
  );

  readonly tableFilterForm = new OrganisationListFilterForm({
    fb: inject(NonNullableFormBuilder),
    destroyRef: inject(DestroyRef),
    paginatedTable: this.itemTable,
  });

  canCreateOrganisation = this.benutzerStore.createHasRequiredRoleSig(hasOrganisationVerwaltungEditBerechtigung());

  ngOnInit() {
    const filter = this.getSessionStorageFilter();
    if (filter) {
      this.tableFilterForm.reset(filter);
    }
  }

  mapSessionStorageStringToFilter(sessionStorageFilter: string): OrganisationFilterForm | undefined {
    try {
      const parsed = JSON.parse(sessionStorageFilter) as OrganisationFilterForm;

      return ensureProps<OrganisationFilterForm>({
        verantwortlicherArzt: parsed.verantwortlicherArzt ?? '',
      });
    } catch (error) {
      console.warn('Failed to parse session storage filter:', error);
      return undefined;
    }
  }
}
