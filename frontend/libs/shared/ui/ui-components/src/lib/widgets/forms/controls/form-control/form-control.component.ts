import {NgTemplateOutlet} from '@angular/common';
import {ChangeDetectionStrategy, Component, input} from '@angular/core';
import {Nullish} from '@dvb/util-shared';
import {SingleCheckboxWrapperComponent} from '../../inputs';
import {FormControlBottomComponent} from '../control-children/form-control-bottom/form-control-bottom.component';
import {
  FormControlTopLabelComponent,
} from '../control-children/form-control-top-label/form-control-top-label.component';
import {FormControlTopComponent} from '../control-children/form-control-top/form-control-top.component';
import {FormWorkingIconComponent} from '../control-children/form-working-icon/form-working-icon.component';
import {ViewValueComponent} from '../control-children/view-value/view-value.component';
import {ControlInNormalFormDirective} from '../control-in-normal-form.directive';

@Component({
  selector: 'svui-form-control',
  templateUrl: './form-control.component.html',
  styleUrl: './form-control.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    NgTemplateOutlet,
    SingleCheckboxWrapperComponent,
    ViewValueComponent,
    FormControlBottomComponent,
    FormWorkingIconComponent,
    FormControlTopLabelComponent,
    FormControlTopComponent,
    SingleCheckboxWrapperComponent,

  ],
})
export class FormControlComponent extends ControlInNormalFormDirective {
  readonly viewValue = input.required<string | Nullish>();
}
