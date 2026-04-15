import {ChangeDetectionStrategy, Component, inject} from '@angular/core';
import {SystemAdminStore} from '@dvb/feat-shared';
import {ButtonComponent, CardComponent, HeadingDirective, IconWithTooltipComponent} from '@dvb/ui-components';
import {TranslocoPipe} from '@jsverse/transloco';

@Component({
  selector: 'feat-server-conf-request',
  imports: [TranslocoPipe, ButtonComponent, CardComponent, HeadingDirective, IconWithTooltipComponent],
  templateUrl: './server-configuration-request.component.html',
  styles: ':host { display:block;}',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ServerConfigurationRequestComponent {

  readonly systemAdminStore = inject(SystemAdminStore);

  triggerServerConfPrint() {
    this.systemAdminStore.triggerServerConfPrint();
  }
}
