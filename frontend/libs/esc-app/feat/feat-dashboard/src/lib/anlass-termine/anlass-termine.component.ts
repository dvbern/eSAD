import {CommonModule} from '@angular/common';
import {ChangeDetectionStrategy, Component, computed, inject} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {RouterLink} from '@angular/router';
import {BackendEntityId} from '@dvb/data-access-types';
import {BenutzerOptionsStore} from '@dvb/feat-shared';
import {BenutzerStore} from '@dvb/feat-shared';
import {Path} from '@dvb/feat-shared';
import {
  EmptyTableMessageComponent,
  ListComponent,
  NgSelectDirective,
  PageHeaderAddButtonComponent,
  TableComponent,
  TableLinkComponent,
} from '@dvb/ui-components';
import {hasAnlassCreateBerechtigung} from '@dvb/util-shared';
import {TranslocoPipe} from '@jsverse/transloco';
import {NgOptionComponent, NgSelectComponent} from '@ng-select/ng-select';
import {DashboardStore} from '../dashboard.store';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'feat-anlass-termine',
  imports: [
    CommonModule,
    ListComponent,
    TranslocoPipe,
    RouterLink,
    NgSelectComponent,
    FormsModule,
    TableComponent,
    TableLinkComponent,
    EmptyTableMessageComponent,
    NgSelectDirective,
    NgOptionComponent,
    PageHeaderAddButtonComponent,
  ],
  templateUrl: './anlass-termine.component.html',
})
export class AnlassTermineComponent {
  readonly #benutzerOptionsStore = inject(BenutzerOptionsStore);
  readonly dashboardStore = inject(DashboardStore);
  readonly #benutzerStore = inject(BenutzerStore);

  readonly Path = Path;

  anlassList = computed(() => this.dashboardStore.anlassList());
  allUsers = computed(() => this.#benutzerOptionsStore.entities());

  canCreateAnlass = this.#benutzerStore.createHasRequiredRoleSig(hasAnlassCreateBerechtigung());

  selectedAccount: BackendEntityId = this.#benutzerStore.requireBenutzer().id as BackendEntityId;

  onUserChange(benutzerId: BackendEntityId): void {
    this.dashboardStore.loadAnlassListForUser(benutzerId);
  }
}
