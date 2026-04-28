import {ChangeDetectionStrategy, Component, DestroyRef, inject, OnInit} from "@angular/core";
import {
  BenutzerEnhancedJson,
  BenutzerListStore,
  BenutzerStore,
  OrganisationStore,
  PageComponent,
  Path
} from "@dvb/feat-shared";
import {TranslocoPipe} from "@jsverse/transloco";
import {CardComponent, PageHeaderAddButtonComponent, StatusTextComponent} from "@dvb/ui-components";
import {FilterSessionStorage, TableModel} from "@dvb/util-angular";
import {FachRolle} from "@dvb/data-access-backend";
import {MitgliederListComponent} from "./mitglieder-list.component";
import {NonNullableFormBuilder} from "@angular/forms";
import {OrganisationMitgliederFilter, OrganisationMitgliederListFilterForm} from "./mitglieder-list-filter.form";
import {ensureProps, hasBenutzerverwaltungEditBerechtigungen, isNotNullish, toValidEnumList} from "@dvb/util-shared";

@Component({
  selector: 'feat-organisation-mitglieder-list.page',
  templateUrl: 'mitglieder-list-page.component.html',
  standalone: true,
  styleUrl: './mitglieder-list-page.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    PageComponent,
    TranslocoPipe,
    CardComponent,
    MitgliederListComponent,
    PageHeaderAddButtonComponent,
    StatusTextComponent
  ],
})
export class MitgliederListPageComponent extends FilterSessionStorage<OrganisationMitgliederFilter> implements OnInit {
  protected readonly Path = Path;
  readonly organisationStore = inject(OrganisationStore);
  readonly benutzerListStore = inject(BenutzerListStore);
  readonly benutzerStore = inject(BenutzerStore);

  readonly SESSIONSTORAGE_KEY = 'organisationMitgliederFilter';

  canCreateMitglieder = this.benutzerStore.createHasRequiredRoleSig(hasBenutzerverwaltungEditBerechtigungen());

  itemTable = new TableModel<BenutzerEnhancedJson>(
    {
      rawListSignal: this.benutzerListStore.entities,
    },
    { frontendOrBackend: 'frontend', pageOrScrollOrNothing: 'scroll' },
  );

  readonly tableFilterForm = new OrganisationMitgliederListFilterForm({
    fb: inject(NonNullableFormBuilder),
    destroyRef: inject(DestroyRef),
    paginatedTable: this.itemTable,
  });

  ngOnInit() {
    const filter = this.getSessionStorageFilter();
    if (filter) {
      this.tableFilterForm.reset(filter);
    }
  }

  mapSessionStorageStringToFilter(sessionStorageFilter: string): OrganisationMitgliederFilter | undefined {
    try {
      const parsed = JSON.parse(sessionStorageFilter) as OrganisationMitgliederFilter;

      return ensureProps<OrganisationMitgliederFilter>({
        nachname: parsed.nachname ?? '',
        vorname: parsed.vorname ?? '',
        email: parsed.email ?? '',
        fachRollen: toValidEnumList(FachRolle, parsed.fachRollen),
      });
    } catch (error) {
      console.warn('Failed to parse session storage filter:', error);
      return undefined;
    }
  }

  getRouterBackLink(): string {
    if (isNotNullish(this.benutzerStore.benutzer()?.currentOrganisationRollen)) {
      return Path.RELATIVE_TWO_UP;
    }
    return Path.RELATIVE_ONE_UP;
  }
}
