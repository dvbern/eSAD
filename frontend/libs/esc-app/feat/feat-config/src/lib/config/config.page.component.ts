import {ChangeDetectionStrategy, Component, inject} from '@angular/core';
import {RouterLink} from '@angular/router';
import {BenutzerStore, PageComponent, Path} from '@dvb/feat-shared';
import {ListComponent, ListItemComponent, ListItemSecondaryDirective, ListItemTitleDirective} from '@dvb/ui-components';
import {hasConfigViewBerechtigung} from '@dvb/util-shared';
import {TranslocoPipe} from '@jsverse/transloco';

@Component({
  selector: 'feat-config',
  imports: [
    PageComponent,
    ListComponent,
    ListItemComponent,
    ListItemSecondaryDirective,
    ListItemTitleDirective,
    RouterLink,
    TranslocoPipe,
  ],
  templateUrl: './config.page.component.html',
  styleUrl: './config.page.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ConfigPageComponent {
  protected readonly Path = Path;

  benutzerStore = inject(BenutzerStore);

  canViewConfig = this.benutzerStore.createHasRequiredRoleSig(hasConfigViewBerechtigung());
}
