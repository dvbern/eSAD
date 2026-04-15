import {ChangeDetectionStrategy, Component, inject, input} from "@angular/core";
import {
  CollapsibleMenuComponent,
  EmptyTableMessageComponent, NgSelectDirective,
  SortableComponent,
  TableComponent,
  TableFilterComponent,
  TableLinkComponent, TextInputDirective
} from "@dvb/ui-components";
import {ReactiveFormsModule} from "@angular/forms";
import {TranslocoPipe, TranslocoService} from "@jsverse/transloco";
import {TableModel} from "@dvb/util-angular";
import {FachRolle} from "@dvb/data-access-backend";
import {OrganisationMitgliederListFilterForm} from "./mitglieder-list-filter.form";
import {NgOptionComponent, NgSelectComponent} from "@ng-select/ng-select";
import {BenutzerEnhancedJson, Path} from "@dvb/feat-shared";

@Component({
  selector: 'feat-organisation-mitglieder-list',
  templateUrl: 'mitglieder-list.component.html',
  standalone: true,
  styleUrl: './mitglieder-list.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    EmptyTableMessageComponent,
    ReactiveFormsModule,
    SortableComponent,
    TableComponent,
    TableLinkComponent,
    TranslocoPipe,
    TableFilterComponent,
    TextInputDirective,
    NgOptionComponent,
    NgSelectComponent,
    NgSelectDirective,
    CollapsibleMenuComponent,
  ],
})
export class MitgliederListComponent {
  itemTable = input.required<TableModel<BenutzerEnhancedJson>>();
  tableFilterForm = input.required<OrganisationMitgliederListFilterForm>();
  isLoading = input(false);
  readonly transloco = inject(TranslocoService);

  readonly fachrollen = Object.values(FachRolle);
  protected readonly Path = Path;

  public printFachRollen(fachRollen: Array<FachRolle>): string {
    return fachRollen
      .map(each => this.transloco.translate('FachRolle.' + each))
      .join(', ');
  }

}
