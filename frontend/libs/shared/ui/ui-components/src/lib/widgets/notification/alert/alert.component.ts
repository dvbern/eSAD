import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { AlertType } from '@dvb/util-angular';
import { NgbAlert } from '@ng-bootstrap/ng-bootstrap';

@Component({
    selector: 'svui-alert',
    templateUrl: './alert.component.html',
    styleUrl: './alert.component.scss',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [NgbAlert]
})
export class AlertComponent {
  readonly type = input.required<AlertType>();
  readonly dismissible = input<boolean>(false);
}
