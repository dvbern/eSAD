import { Directive, HostBinding } from '@angular/core';
import { classFormLayoutWidthFull } from '../../styles-helper/styles-helper';

@Directive({
  selector: '[svuiFormFullWidth]',
  standalone: true,
})
export class FormFullWidthDirective {
  @HostBinding('class.' + classFormLayoutWidthFull)
  private readonly fullWidth = true;
}
