import {ChangeDetectionStrategy, Component, inject} from '@angular/core';
import {ToastOutletComponent} from '@dvb/ui-components';
import {AppNotification, AppNotificationStore} from '@dvb/util-angular';

@Component({
  selector: 'feat-global-toasts',
  imports: [ToastOutletComponent],
  templateUrl: './global-toasts.component.html',
  styles: `
    :host {
      display: block;
    }
  `,
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class GlobalToastsComponent {
  readonly service = inject(AppNotificationStore);
  protected readonly Error = Error;

  toasts = this.service.notifications;

  removeToast($event: AppNotification): void {
    this.service.removeNotification($event);
  }
}
