import {ChangeDetectionStrategy, Component, model} from '@angular/core';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {
  BadgeComponent,
  ButtonComponent,
  CardComponent,
  CardHeaderComponent,
  CheckboxDirective,
  CollapsibleMenuComponent,
  DashboardComponent,
  IconOnlyButtonComponent,
  ListComponent,
  ListItemComponent,
  ListItemSecondaryDirective,
  ListItemTitleDirective,
  PageHeaderComponent,
  TableComponent,
} from '@dvb/ui-components';
import {NgbDropdownItem} from '@ng-bootstrap/ng-bootstrap';
import {TranslocoPipe} from "@jsverse/transloco";

@Component({
  selector: 'feat-playground-loading',
  templateUrl: './playground-loading.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    ButtonComponent,
    FormsModule,
    ReactiveFormsModule,
    CardComponent,
    TableComponent,
    CheckboxDirective,
    PageHeaderComponent,
    CollapsibleMenuComponent,
    NgbDropdownItem,
    BadgeComponent,
    IconOnlyButtonComponent,
    ListComponent,
    ListItemComponent,
    ListItemSecondaryDirective,
    ListItemTitleDirective,
    CardHeaderComponent,
    DashboardComponent,
    TranslocoPipe,
  ],
})
export class PlaygroundLoadingComponent {
  loading = model<boolean>(true);
  protected readonly alert = alert;

  listItem: string[] = ['Title', 'Text', 'Secondary Text', 'Tag'];
  list: string[][] = [this.listItem, this.listItem, this.listItem];
}
