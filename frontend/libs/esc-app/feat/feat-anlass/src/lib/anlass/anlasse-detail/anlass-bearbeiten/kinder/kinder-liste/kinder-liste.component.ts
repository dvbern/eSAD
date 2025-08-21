import {DatePipe} from '@angular/common';
import {ChangeDetectionStrategy, Component, computed, DestroyRef, inject} from '@angular/core';
import {FormsModule, NonNullableFormBuilder, ReactiveFormsModule} from '@angular/forms';
import {Geschlecht, KindJson, Klassenstufe, KlasseOptionJson, SchuleOptionJson} from '@dvb/data-access-backend';
import {KlassenOptionsStore, SchulenOptionsStore} from '@dvb/feat-shared';
import {Path} from '@dvb/feat-shared';
import {
  CardCollapsibleComponent,
  CardComponent,
  ColWidthComponent,
  EmptyTableMessageComponent,
  FormControlComponent,
  FormLayoutComponent,
  IconComponent,
  IconOnlyButtonComponent,
  NgSelectDirective,
  SortableComponent,
  TableComponent,
  TableFilterComponent,
  TableLinkComponent,
  TextInputDirective,
} from '@dvb/ui-components';
import {TableModel} from '@dvb/util-angular';
import {compareFn, requireValue} from '@dvb/util-shared';
import {TranslocoPipe} from '@jsverse/transloco';
import {NgOptionComponent, NgSelectComponent} from '@ng-select/ng-select';
import {AnlassStore} from '../../../../anlass.store';
import {AnlassKinderStore} from '../../../anlass-kinder.store';
import {getUseCaseGechlecht} from '../kinder.util';
import {KinderFilterForm} from './kinder-filter.form';

@Component({
  selector: 'feat-anlass-detail-anlassdaten-kinder-liste',
  templateUrl: './kinder-liste.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    FormsModule,
    SortableComponent,
    TableComponent,
    TranslocoPipe,
    DatePipe,
    IconComponent,
    TableFilterComponent,
    TextInputDirective,
    NgOptionComponent,
    NgSelectComponent,
    NgSelectDirective,
    ReactiveFormsModule,
    IconOnlyButtonComponent,
    CardCollapsibleComponent,
    FormControlComponent,
    FormLayoutComponent,
    CardComponent,
    EmptyTableMessageComponent,
    TableLinkComponent,
    ColWidthComponent,
  ],
})
export class KinderLsteComponent {
  readonly store = inject(AnlassStore);
  readonly anlassKinderStore = inject(AnlassKinderStore);
  readonly schulenOptionsStore = inject(SchulenOptionsStore);
  readonly klassenOptionsStore = inject(KlassenOptionsStore);

  itemTable = new TableModel<KindJson>(
    {rawListSignal: this.anlassKinderStore.availableKinder},
    {frontendOrBackend: 'frontend', pageOrScrollOrNothing: 'scroll'});

  schuleOptions = computed(() => this.schulenOptionsStore.entitiesAktiv());
  klassenOptions = computed(() => this.klassenOptionsStore.entities());

  protected readonly compareFnSchule = compareFn<SchuleOptionJson>;
  protected readonly compareFnKlasse = compareFn<KlasseOptionJson>;

  readonly tableFilterForm = new KinderFilterForm({
    fb: inject(NonNullableFormBuilder),
    destroyRef: inject(DestroyRef),
    paginatedTable: this.itemTable,
    klassenOptionsStore: inject(KlassenOptionsStore),
    anlassKinderStore: this.anlassKinderStore,
  });

  readonly klassenstufen = Object.values(Klassenstufe);
  readonly geschlechter = Object.values(Geschlecht);

  addAllKinder(): void {
    if (this.itemTable.filteredListSize() > 0) {
      const anlassId = requireValue(this.store.selectedEntity()?.id);

      this.anlassKinderStore.addKinder(
        {
          anlassId,
          kindIds: this.itemTable.filteredSortedList()
            .map(each => each.id),
        });
    }
  }

  addKind(kind: KindJson): void {
    const anlassId = requireValue(this.store.selectedEntity()?.id);
    this.anlassKinderStore.addKinder({anlassId, kindIds: [kind.id]});
  }

  protected readonly getUseCaseGechlecht = getUseCaseGechlecht;
  protected readonly Path = Path;
}
