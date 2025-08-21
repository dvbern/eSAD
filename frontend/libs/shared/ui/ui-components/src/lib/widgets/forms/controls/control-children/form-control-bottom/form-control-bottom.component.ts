import { booleanAttribute, ChangeDetectionStrategy, Component, input, InputSignalWithTransform } from '@angular/core';
import { ValidationErrors } from '@angular/forms';
import { FormControlErrorComponent } from '../form-control-error/form-control-error.component';
import { Nullish } from '@dvb/util-shared';

@Component({
    selector: 'svui-form-control-bottom',
    templateUrl: './form-control-bottom.component.html',
    styleUrl: './form-control-bottom.component.scss',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [FormControlErrorComponent]
})
export class FormControlBottomComponent {
  readonly minHeight = input.required<boolean>();
  readonly errors = input.required<ValidationErrors | Nullish>();
  readonly doValidation: InputSignalWithTransform<boolean, unknown> = input.required({
    transform: booleanAttribute,
    alias: 'validateWhen',
  });
}
