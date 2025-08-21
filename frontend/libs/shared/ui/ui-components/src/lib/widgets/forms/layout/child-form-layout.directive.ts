import { Directive, HostBinding } from '@angular/core';
import { classChildForm } from '../../styles-helper/styles-helper';
import { FormFullWidthDirective } from './form-full-width.directive';

@Directive({
  selector: '[svuiChildFormLayout]',
  standalone: true,
  hostDirectives: [FormFullWidthDirective],
})
export class ChildFormLayoutDirective {
  @HostBinding('class.' + classChildForm)
  private readonly childFormLayout = true;
}
