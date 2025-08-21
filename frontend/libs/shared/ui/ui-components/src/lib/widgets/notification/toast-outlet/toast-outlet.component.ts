import {ChangeDetectionStrategy, Component, input, output} from '@angular/core';
import {AppNotification} from '@dvb/util-angular';
import {NgbAlertModule} from '@ng-bootstrap/ng-bootstrap';

@Component({
    selector: 'svui-toast-outlet',
    templateUrl: './toast-outlet.component.html',
    styleUrl: './toast-outlet.component.scss',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [NgbAlertModule]
})
export class ToastOutletComponent {
  readonly toasts = input<AppNotification[]>([]);
  readonly removeToast = output<AppNotification>();

}
