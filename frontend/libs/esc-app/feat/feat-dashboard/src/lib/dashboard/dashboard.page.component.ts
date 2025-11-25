import {ChangeDetectionStrategy, Component, inject} from '@angular/core';
import {BenutzerStore, KindSidebarComponent, PageComponent, Path} from '@dvb/feat-shared';
import {DashboardComponent} from '@dvb/ui-components';
import {hasAufgabenViewBerechtigungen} from '@dvb/util-shared';
import {AnlassTermineComponent} from '../anlass-termine/anlass-termine.component';
import {DashboardStore} from '../dashboard.store';
import {LetzteAnlaesseComponent} from '../letzte-anlaesse/letzte-anlaesse.component';
import {LetzteKinderComponent} from '../letzte-kinder/letzte-kinder.component';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'feat-dashboard',
  imports: [
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
  readonly benutzerStore = inject(BenutzerStore);

  public canViewAufgaben = this.benutzerStore.createHasRequiredRoleSig(hasAufgabenViewBerechtigungen());

  protected readonly Path = Path;
}
