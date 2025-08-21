import {ChangeDetectionStrategy, Component, computed, DestroyRef, inject, output, Signal} from '@angular/core';
import {NonNullableFormBuilder, ReactiveFormsModule, Validators} from '@angular/forms';
import {RouterLink} from '@angular/router';
import {Path} from '@dvb/feat-shared';
import {
  CardComponent,
  CheckboxDirective,
  CollapsibleMenuComponent, ColWidthComponent,
  EmptyTableMessageComponent,
  IconOnlyButtonComponent,
  NgSelectDirective,
  SortableComponent,
  TableComponent,
  TableControlComponent,
  TableFilterComponent, TableLinkComponent,
  TextInputDirective,
} from '@dvb/ui-components';

import {TableModel} from '@dvb/util-angular';
import {TranslocoPipe} from '@jsverse/transloco';
import {NgbDropdownItem} from '@ng-bootstrap/ng-bootstrap';
import {NgOptionComponent, NgSelectComponent} from '@ng-select/ng-select';
import {PlaygroundListeFilterForm} from './playground-liste-filter.form';

@Component({
    selector: 'feat-playground-table',
    templateUrl: './playground-table.component.html',
    styleUrl: './playground-table.component.scss',
    changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    TableComponent,
    IconOnlyButtonComponent,
    TableFilterComponent,
    TextInputDirective,
    TableControlComponent,
    CollapsibleMenuComponent,
    SortableComponent,
    CheckboxDirective,
    ReactiveFormsModule,
    NgbDropdownItem,
    RouterLink,
    NgOptionComponent,
    NgSelectComponent,
    NgSelectDirective,
    TranslocoPipe,
    CardComponent,
    EmptyTableMessageComponent,
    TableLinkComponent,
    ColWidthComponent,
  ],
})
export class PlaygroundTableComponent {
  form = inject(NonNullableFormBuilder).group({
    test: inject(NonNullableFormBuilder).control<number>(2, Validators.required),
  });

  dummies: Signal<DummyItem[]> = computed<DummyItem[]>(() => dummyList);

  itemTable = new TableModel<DummyItem>(
    {rawListSignal: this.dummies},
    {frontendOrBackend: 'frontend', pageOrScrollOrNothing: 'paginate', originalPageSize: 6});

  readonly tableFilterForm = new PlaygroundListeFilterForm({
    fb: inject(NonNullableFormBuilder),
    destroyRef: inject(DestroyRef),
    dummyItemTable: this.itemTable,
  });
  table: Schule[] = [item, item, item];

  readonly clickOutput = output<DummyItem>();

  doClick(item: DummyItem): void {
    this.clickOutput.emit(item);
    alert('Item anzeigen');
  }

  doClickFile(item: DummyItem): void {
    alert('File Hochgeladen');
    item.fileUploaded = true;
  }

  protected readonly alert = alert;
  protected readonly Path = Path;
}

interface Schule {
  schule?: string;
  stufe?: string;
}

const item: Schule = {schule: 'Primarschule eins', stufe: 'Primarstufe'};

export interface DummyItem {
  name?: string;
  nummer?: number;
  adresse?: {
    strasse?: string;
    ort?: string;
  };
  fileUploaded?: boolean;
}

// type DummyItemState = {
//   fullList: DummyItem[];
// };

// const dummyItemInitialState: DummyItemState = {
//   fullList: [],
// };

const dummyList: DummyItem[] = [
  {
    name: 'Axel',
    nummer: 23,
    adresse: {strasse: 'Meienriedweg 2', ort: 'Lengnau'},
    fileUploaded: false,
  },
  {
    name: 'Heinrich',
    nummer: 1,
    adresse: {strasse: 'Alpenstrasse 9', ort: 'Bern'},
    fileUploaded: true,
  },
  {
    name: 'Johanna',
    nummer: 4555,
    adresse: {strasse: 'Meienriedweg 2', ort: 'Lengnau'},
    fileUploaded: false,
  },
  {
    name: 'Mihail',
    adresse: {strasse: 'Moos', ort: 'Montreux'},
    fileUploaded: false,
  },
  {
    name: 'Maya',
    adresse: {strasse: 'Juragässchen 11', ort: 'Montreux'},
    fileUploaded: false,
  },
  {name: 'Joaquín', nummer: 890, fileUploaded: false},
  {name: 'Milena', nummer: 899, fileUploaded: false},
  {
    name: 'axell',
    nummer: 901,
    adresse: {strasse: 'Hanfrain 9', ort: 'Lengnau'},
    fileUploaded: false,
  },
  {
    nummer: 22,
    adresse: {strasse: 'Gugenhügel', ort: 'Lengnau'},
    fileUploaded: false,
  },
] as DummyItem[];
