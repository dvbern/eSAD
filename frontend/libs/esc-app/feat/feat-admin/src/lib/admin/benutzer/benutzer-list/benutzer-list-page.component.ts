
import {ChangeDetectionStrategy, Component, computed, DestroyRef, inject} from '@angular/core';
import {NonNullableFormBuilder, ReactiveFormsModule} from '@angular/forms';
import {BenutzerJson, FachRolle} from '@dvb/data-access-backend';
import {BenutzerStore, PageComponent, Path, TranslatedStringPipe} from '@dvb/feat-shared';
import {
  CardComponent,
  CollapsibleMenuComponent,
  ColWidthComponent,
  EmptyTableMessageComponent,
  NgSelectDirective,
  PageHeaderButtonsComponent,
  PageHeaderListSummaryComponent,
  SortableComponent,
  SummaryListItem,
  TableComponent,
  TableFilterComponent,
  TextInputDirective,
} from '@dvb/ui-components';
import {TableModel} from '@dvb/util-angular';
import {hasListEditBerechtigung} from '@dvb/util-shared';
import {TranslocoPipe, TranslocoService} from '@jsverse/transloco';
import {NgbDropdownItem} from '@ng-bootstrap/ng-bootstrap';
import {NgOptionComponent, NgSelectComponent} from '@ng-select/ng-select';

import {BenutzerListFilterForm} from './benutzer-list-filter.form';
import {BenutzerListStore} from './benutzer-list.store';

@Component({
  selector: 'feat-benutzer-list.page',
  providers: [TranslatedStringPipe],
  imports: [
    PageComponent,
    TableFilterComponent,
    TextInputDirective,
    ReactiveFormsModule,
    TranslocoPipe,
    SortableComponent,
    TableComponent,
    PageHeaderButtonsComponent,
    NgSelectDirective,
    NgSelectComponent,
    NgOptionComponent,
    PageHeaderListSummaryComponent,
    NgbDropdownItem,
    CollapsibleMenuComponent,
    CardComponent,
    EmptyTableMessageComponent,
    ColWidthComponent
],
  templateUrl: './benutzer-list-page.component.html',
  styles: ':host { display:block;}',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class BenutzerListPageComponent {

  readonly benutzerListStore = inject(BenutzerListStore);
  readonly benutzerStore = inject(BenutzerStore);
  readonly transloco = inject(TranslocoService);

  readonly Path = Path;
  readonly rollen = Object.values(FachRolle);

  benutzerTranslated = this.transloco.translate('benutzer.list.summary.benutzer');

  itemTable = new TableModel<BenutzerJson>(
    {rawListSignal: this.benutzerListStore.entities},
    {frontendOrBackend: 'frontend', pageOrScrollOrNothing: 'scroll'});
  readonly tableFilterForm = new BenutzerListFilterForm({
    fb: inject(NonNullableFormBuilder),
    destroyRef: inject(DestroyRef),
    paginatedTable: this.itemTable,
  });

  public printFachRollen(fachRollen: Array<FachRolle>): string {
    return fachRollen
      .map(each => this.transloco.translate('benutzer.list.fachRolle.' + each))
      .join(', ');
  }

  summaryItems = computed(() => {
    const items: SummaryListItem[] = [
      {
        name: this.benutzerTranslated,
        totalCount: this.itemTable.rawListSize(),
        filteredCount: this.itemTable.filteredListSize(),
      },
    ];
    return items;
  });

  canEditActive = this.benutzerStore.createHasRequiredRoleSig(hasListEditBerechtigung());

  public isUserActive(benutzerJson: BenutzerJson): boolean {
    return benutzerJson.data.aktiv;
  }

  public toggleActive(item: BenutzerJson): void {
    this.benutzerListStore.toggleActiveOnBenutzer(item)
  }
}
