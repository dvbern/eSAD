import { Directive, HostBinding } from '@angular/core';
import { classTextError } from '../../styles-helper/styles-helper';

@Directive({
  selector: '[svuiErrorText]',
  standalone: true,
})
export class ErrorTextDirective {
  @HostBinding('class.' + classTextError)
  private readonly classTextError = true;
}
