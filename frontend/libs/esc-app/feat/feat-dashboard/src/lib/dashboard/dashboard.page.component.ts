import {CommonModule} from '@angular/common';
import {ChangeDetectionStrategy, Component, inject} from '@angular/core';
import {Path} from '@dvb/feat-shared';
import {PageComponent} from '@dvb/feat-shared';
import {KindSidebarComponent} from '@dvb/feat-shared';;
import {DashboardComponent} from '@dvb/ui-components';
import {AnlassTermineComponent} from '../anlass-termine/anlass-termine.component';
import {DashboardStore} from '../dashboard.store';
import {LetzteAnlaesseComponent} from '../letzte-anlaesse/letzte-anlaesse.component';
import {LetzteKinderComponent} from '../letzte-kinder/letzte-kinder.component';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'feat-dashboard',
  imports: [
    CommonModule,
    DashboardComponent,
    PageComponent,
    AnlassTermineComponent,
    KindSidebarComponent,
    LetzteAnlaesseComponent,
    LetzteKinderComponent,
  ],
  templateUrl: './dashboard.page.component.html',
})
export class DashboardPageComponent {
  readonly dashboardStore = inject(DashboardStore);

  protected readonly Path = Path;
}
