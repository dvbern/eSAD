import { ChangeDetectionStrategy, Component, computed, input, output } from '@angular/core';
import { Alert } from '@dvb/util-angular';
import { isNullish, Nullish } from '@dvb/util-shared';
import { NgbAlertModule } from '@ng-bootstrap/ng-bootstrap';

@Component({
    selector: 'svui-alerts',
    templateUrl: './alerts.component.html',
    styleUrl: './alerts.component.scss',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [NgbAlertModule]
})
export class AlertsComponent {
  readonly alerts = input<Alert | Alert[] | Nullish>([]);
  readonly removeAlert = output<Alert>();

  protected alertsAsArray = computed<Alert[]>(() => {
    const a = this.alerts();
    if (isNullish(a)) {
      return [];
    }

    return Array.isArray(a) ? a : [a];
  });
}
