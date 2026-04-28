import {DatePipe} from '@angular/common';
import {ChangeDetectionStrategy, Component, inject} from '@angular/core';
import {RouterLink} from '@angular/router';
import {BenutzerStore, PageComponent, Path, TenantSettingsStore} from '@dvb/feat-shared';
import {IconComponent} from '@dvb/ui-components';
import {TranslocoPipe} from '@jsverse/transloco';

@Component({
  selector: 'feat-app-mode-selection-page',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    PageComponent,
    RouterLink,
    TranslocoPipe,
    DatePipe,
    IconComponent,
  ],
  templateUrl: './app-mode-selection.page.component.html',
  styleUrl: './app-mode-selection.page.component.scss',
})
export class AppModeSelectionPageComponent {
  readonly benutzerStore = inject(BenutzerStore);
  readonly tenantSettingsStore = inject(TenantSettingsStore);

  readonly today = new Date();

  protected readonly Path = Path;
}
