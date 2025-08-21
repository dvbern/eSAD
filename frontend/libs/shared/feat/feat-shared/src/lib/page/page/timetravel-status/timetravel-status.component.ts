import {CommonModule} from '@angular/common';
import {ChangeDetectionStrategy, Component, inject} from '@angular/core';
import {IconComponent} from '@dvb/ui-components';
import {hasSystemPageViewBerechtiung} from '@dvb/util-shared';
import {BenutzerStore} from '../../../benutzer/benutzer/benutzer.store';
import {TimetravelStore} from '../../../core/timetravel-store';
import {DeploymentStore} from '../../../deployment/deployment.store';

@Component({
  selector: 'feat-page-timetravel-status',
  standalone: true,
  imports: [
    CommonModule,
    IconComponent,
  ],
  templateUrl: './timetravel-status.component.html',
  styleUrl: './timetravel-status.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class TimetravelStatusComponent {

  timetravelStore = inject(TimetravelStore);
  benutzerStore = inject(BenutzerStore);

  deploymentStore = inject(DeploymentStore);
  isTimetravelEnabled = this.deploymentStore.isTimetravelEnabled;
  hasTimetravelRights = this.benutzerStore.createHasRequiredRoleSig(hasSystemPageViewBerechtiung());

  now = this.timetravelStore.now;
  isTimetravelling = this.timetravelStore.timetravelling;

}
