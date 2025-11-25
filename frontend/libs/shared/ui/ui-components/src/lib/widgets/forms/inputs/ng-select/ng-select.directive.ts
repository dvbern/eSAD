import { Directive, HostBinding, inject, input } from '@angular/core';
import { NgControl } from '@angular/forms';
import { ControlType, ValidatedInputWithIdDirective } from '../validated-input-with-id.directive';

@Directive({
  selector: 'ng-select[svuiNgSelect]',
  standalone: true,
  providers: [
    {
      // See comment on ValidatedInputWithIdDirective for reasons
      provide: ValidatedInputWithIdDirective,
      useExisting: NgSelectDirective,
    },
  ],
})
export class NgSelectDirective extends ValidatedInputWithIdDirective {
  @HostBinding('class.form-control') private readonly bootstrapClass = true;

  @HostBinding('class.badges-nowrap')
  get badgesNoWrap(): boolean {
    return !this.allowBadgesToWrap();
  }

  // this is unfortunately needed, as ng-select produces errors if no label key is provided, see ESC-529
  // to enforce the usage of a label key, we use the required  input, the way to provide options
  // is using the ng-options directive in a @for loop
  readonly bindLabel = input.required<'dummy'>();

  // appendTo=body is needed e.g. inside a overflow:hidden, otherwise the dropdown gets cut off: https://ng-select.github.io/ng-select/#/append-to-element
  // appendTo must be specified in caller explicitly, else Angular won't get it
  readonly appendTo = input.required<'body'>();

  readonly allowBadgesToWrap = input<boolean>(false);

  constructor() {
    const ngCtrl = inject(NgControl, { optional: true, self: true });
    super(ngCtrl);
  }

  get controlType(): ControlType {
    return 'ngSelect';
  }

  get isLabelFloatable(): boolean {
    return true;
  }
}
