
import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { DeploymentEnvironment } from '@dvb/data-access-backend';
import { DeploymentStore } from './deployment.store';

@Component({
    selector: 'feat-shared-deployment-indicator',
    imports: [],
    templateUrl: './deployment-indicator.component.html',
    styleUrls: ['./deployment-indicator.component.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class DeploymentIndicatorComponent {
  store = inject(DeploymentStore);
  DeploymentEnvironment = DeploymentEnvironment;
}
