import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { FormControlTopComponent } from '../control-children/form-control-top/form-control-top.component';
import { ViewValueComponent } from '../control-children/view-value/view-value.component';
import { FormControlBottomComponent } from '../control-children/form-control-bottom/form-control-bottom.component';
import { FormWorkingIconComponent } from '../control-children/form-working-icon/form-working-icon.component';
import { RequiredComponent } from '../control-children/required/required.component';
import { ControlInNormalFormDirective } from '../control-in-normal-form.directive';
import { Nullish } from '@dvb/util-shared';

export type InputDirection = 'horizontal' | 'vertical';

@Component({
    selector: 'svui-form-group',
    templateUrl: './form-group.component.html',
    styleUrl: './form-group.component.scss',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [
        ViewValueComponent,
        FormControlBottomComponent,
        FormWorkingIconComponent,
        FormControlTopComponent,
        RequiredComponent,
    ]
})
export class FormGroupComponent extends ControlInNormalFormDirective {
  readonly viewValue = input.required<string | Nullish>();
  readonly direction = input.required<InputDirection>();
}
