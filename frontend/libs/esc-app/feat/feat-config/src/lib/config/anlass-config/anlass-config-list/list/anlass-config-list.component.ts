import { DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject, input } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { AnlassConfigJson, AnlassConfigStatus, AnlassTyp } from '@dvb/data-access-backend';
import { BenutzerStore } from '@dvb/feat-shared';
import {
  CardComponent,
  CollapsibleMenuComponent,
  ColWidthComponent,
  EmptyTableMessageComponent,
  NgSelectDirective,
  SortableComponent,
  StatusTextComponent,
  TableComponent,
  TableFilterComponent,
  TableLinkComponent,
  TextInputDirective,
} from '@dvb/ui-components';
import { TableModel } from '@dvb/util-angular';
import { TranslocoPipe, TranslocoService } from '@jsverse/transloco';
import { NgOptionComponent, NgSelectComponent } from '@ng-select/ng-select';
import { AnlassConfigStore } from '../../anlass-config.store';
import { AnlassConfigListFilterForm } from '../anlass-config-list-filter.form';
import { NgbDropdownItem } from '@ng-bootstrap/ng-bootstrap';
import {AnlassConfigHelperService} from "../../anlass-config-helper.service";

@Component({
  selector: 'feat-anlass-config-list',
  imports: [
    CardComponent,
    TableComponent,
    ColWidthComponent,
    SortableComponent,
    TranslocoPipe,
    NgOptionComponent,
    NgSelectComponent,
    NgSelectDirective,
    TableFilterComponent,
    ReactiveFormsModule,
    TextInputDirective,
    EmptyTableMessageComponent,
    DatePipe,
    TableLinkComponent,
    CollapsibleMenuComponent,
    NgbDropdownItem,
    StatusTextComponent,
  ],
  templateUrl: './anlass-config-list.component.html',
  styleUrl: './anlass-config-list.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AnlassConfigListComponent {
  readonly store = inject(AnlassConfigStore);
  readonly benutzerStore = inject(BenutzerStore);
  readonly transloco = inject(TranslocoService);
  readonly anlassConfigHelperService = inject(AnlassConfigHelperService);

  readonly anlassTypOptions = Object.values(AnlassTyp);
  readonly anlassConfigStatusOptions = Object.values(AnlassConfigStatus);

  itemTable = input.required<TableModel<AnlassConfigJson>>();
  tableFilterForm = input.required<AnlassConfigListFilterForm>();
  loading = input.required<boolean>();

  async getSqlSkripts(item: AnlassConfigJson): Promise<void> {
    this.store.downloadSkripts({
      anlassConfigId: item.id,
    });
  }
}
