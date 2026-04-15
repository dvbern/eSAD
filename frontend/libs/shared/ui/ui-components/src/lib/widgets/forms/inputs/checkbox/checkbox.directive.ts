import { Directive, HostBinding, input } from '@angular/core';
import { ControlType, ValidatedInputWithIdDirective } from '../validated-input-with-id.directive';

@Directive({
  selector: 'input[svuiCheckbox]',
  standalone: true,
  providers: [
    {
      // See comment on ValidatedInputWithIdDirective for reasons
      provide: ValidatedInputWithIdDirective,
      useExisting: CheckboxDirective,
    },
  ],
})
export class CheckboxDirective extends ValidatedInputWithIdDirective {
  // type must be specified in caller explicitly, else Angular won't get it
  readonly type = input.required<'checkbox'>();

  @HostBinding('class.form-check-input') readonly bootstrapClass = true;

  get controlType(): ControlType {
    return 'checkbox';
  }

  get isLabelFloatable(): boolean {
    return false;
  }
}
