import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { Nullish } from '@dvb/util-shared';
import { IconWithTooltipComponent } from '../../../../icons/icon-with-tooltip/icon-with-tooltip.component';
import { SpinnerComponent } from '../../../../icons/spinner';

@Component({
    selector: 'svui-form-working-icon',
    templateUrl: './form-working-icon.component.html',
    styleUrl: './form-working-icon.component.scss',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [SpinnerComponent, IconWithTooltipComponent]
})
export class FormWorkingIconComponent {
  readonly working = input<boolean>(false);
  readonly tooltip = input<string | Nullish>(null);
}
