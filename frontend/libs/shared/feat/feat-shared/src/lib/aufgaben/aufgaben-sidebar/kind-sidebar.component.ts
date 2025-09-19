import {ChangeDetectionStrategy, Component, computed, inject, input} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {RouterLink} from '@angular/router';
import {AufgabeStatus} from '@dvb/data-access-backend';
import {
  BadgeComponent,
  EmptyListMessageComponent,
  FancyListItemComponent,
  ListComponent,
  PageHeaderAddButtonComponent,
} from '@dvb/ui-components';
import {fromISODateString, hasAufgabenEditBerechtigungen} from '@dvb/util-shared';
import {TranslocoPipe} from '@jsverse/transloco';
import {AufgabeJsonEnhanced} from '../../admin-shared/aufgaben.store';
import {BenutzerStore} from '../../benutzer/benutzer/benutzer.store';
import {Path} from '../../core/path';
import {TranslatedStringPipe} from '../../translated-strings/view/translated-string.pipe';

@Component({
  selector: 'feat-aufgaben-sidebar',
  templateUrl: './kind-sidebar.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    TranslocoPipe,
    FormsModule,
    RouterLink,
    PageHeaderAddButtonComponent,
    ListComponent,
    BadgeComponent,
    FancyListItemComponent,
    TranslatedStringPipe,
    EmptyListMessageComponent,
  ],
})
export class KindSidebarComponent {

  readonly benutzerStore = inject(BenutzerStore);
  showCreateAufgabeButton = input.required<boolean>();
  routingPrefix = input.required<string>();
  aufgabenList = input<AufgabeJsonEnhanced[]>([]);
  loading = input(false);

  aufgabenNichtAbgeschlossen = computed(() => this.aufgabenList()
    .filter(a => [AufgabeStatus.OFFEN, AufgabeStatus.IN_BEARBEITUNG].includes(a.data.status))
    .slice(0, 5));

  canEditAufgaben = computed(() =>
    this.benutzerStore.createHasRequiredRoleSig(hasAufgabenEditBerechtigungen()) &&
    this.showCreateAufgabeButton()
  );

  protected readonly Path = Path;
  protected readonly fromISODateString = fromISODateString;
}
