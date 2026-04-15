import {ChangeDetectionStrategy, Component, input} from "@angular/core";
import {TableModel} from "@dvb/util-angular";
import {OrganisationJson} from "@dvb/data-access-backend";
import {
  CollapsibleMenuComponent,
  ColWidthComponent,
  EmptyTableMessageComponent,
  SortableComponent,
  TableComponent,
  TableFilterComponent,
  TableLinkComponent,
  TextInputDirective
} from "@dvb/ui-components";
import {ReactiveFormsModule} from "@angular/forms";
import {TranslocoPipe} from "@jsverse/transloco";
import {OrganisationListFilterForm} from "./organisation-list-filter.form";

@Component({
  selector: 'feat-organisation-list',
  templateUrl: 'organisation-list.component.html',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  styleUrl: './organisation-list.component.scss',
  imports: [
    EmptyTableMessageComponent,
    ReactiveFormsModule,
    SortableComponent,
    TableComponent,
    TableLinkComponent,
    TranslocoPipe,
    TableFilterComponent,
    TextInputDirective,
    CollapsibleMenuComponent,
    ColWidthComponent
  ],
})
export class OrganisationListComponent {
  itemTable = input.required<TableModel<OrganisationJson>>();
  tableFilterForm = input.required<OrganisationListFilterForm>();
  isLoading = input(false);
}
