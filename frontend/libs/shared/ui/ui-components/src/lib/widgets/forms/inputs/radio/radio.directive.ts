import { Directive, HostBinding, input } from '@angular/core';
import { ControlType, ValidatedInputWithIdDirective } from '../validated-input-with-id.directive';

@Directive({
  selector: 'input[svuiRadio]',
  standalone: true,
  providers: [
    {
      // See comment on ValidatedInputWithIdDirective for reasons
      provide: ValidatedInputWithIdDirective,
      useExisting: RadioDirective,
    },
  ],
})
export class RadioDirective extends ValidatedInputWithIdDirective {
  // type must be specified in caller explicitly, else Angular won't get it
  readonly type = input.required<'radio'>();
  readonly name = input.required<string>();

  @HostBinding('class.form-check-input') readonly bootstrapClass = true;

  get controlType(): ControlType {
    return 'radios';
  }

  get isLabelFloatable(): boolean {
    return false;
  }
}
