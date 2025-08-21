import {ChangeDetectionStrategy, Component} from '@angular/core';
import {
  BadgeComponent,
  CardComponent,
  CardHeaderComponent,
  CollapsibleMenuComponent,
  DashboardComponent,
  HeadingDirective,
  IconOnlyButtonComponent,
  ListComponent,
  ListItemComponent,
  ListItemSecondaryDirective,
  ListItemTitleDirective,
  NgSelectDirective,
} from '@dvb/ui-components';
import {TranslocoPipe} from '@jsverse/transloco';
import {NgOptionComponent, NgSelectComponent} from '@ng-select/ng-select';

@Component({
  selector: 'feat-playground-dashboard',
  templateUrl: './playground-dashboard.component.html',
  styleUrl: './playground-dashboard.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    DashboardComponent,
    ListComponent,
    ListItemComponent,
    HeadingDirective,
    IconOnlyButtonComponent,
    ListItemTitleDirective,
    ListItemSecondaryDirective,
    BadgeComponent,
    CardComponent,
    NgSelectComponent,
    NgOptionComponent,
    NgSelectDirective,
    TranslocoPipe,
    CardHeaderComponent,
    CollapsibleMenuComponent,
  ],
})
export class PlaygroundDashboardComponent {
  listItem: string[] = ['Title', 'Text', 'Secondary Text', 'Tag'];
  list: string[][] = [this.listItem, this.listItem, this.listItem];
}
