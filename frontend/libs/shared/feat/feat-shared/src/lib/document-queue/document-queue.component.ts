import {CommonModule} from '@angular/common';
import {ChangeDetectionStrategy, Component, DestroyRef, inject} from '@angular/core';
import {NonNullableFormBuilder, ReactiveFormsModule} from '@angular/forms';
import {DocumentQueueDisplayJson} from '@dvb/data-access-backend';
import {
  CardComponent,
  ColWidthComponent,
  EmptyTableMessageComponent,
  IconOnlyButtonComponent,
  SortableComponent,
  TableComponent,
  TableFilterComponent,
  TextInputDirective,
} from '@dvb/ui-components';
import {TableModel} from '@dvb/util-angular';
import {TranslocoPipe} from '@jsverse/transloco';
import {DocumentQueueFilterForm} from './document-queue-filter.form';
import {DocumentQueueStore} from './document-queue.store';

@Component({
  selector: 'feat-document-queue',
  standalone: true,
  imports: [
    CommonModule, TableComponent, EmptyTableMessageComponent, TranslocoPipe, CardComponent, SortableComponent,
    TableFilterComponent, TextInputDirective, ReactiveFormsModule, IconOnlyButtonComponent,
    ColWidthComponent,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './document-queue.component.html',
  styles: `
    :host {
      display: block;
    }
  `,
})
export class DocumentQueueComponent {
  readonly documentQueueStore = inject(DocumentQueueStore);

  itemTable = new TableModel<DocumentQueueDisplayJson>(
    {rawListSignal: this.documentQueueStore.entities},
    {frontendOrBackend: 'frontend', pageOrScrollOrNothing: 'scroll'});
  readonly documentQueueFilterForm = new DocumentQueueFilterForm({
    fb: inject(NonNullableFormBuilder),
    destroyRef: inject(DestroyRef),
    paginatedTable: this.itemTable,
  });
}
