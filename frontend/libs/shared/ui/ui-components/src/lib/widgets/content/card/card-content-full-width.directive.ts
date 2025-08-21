import { Directive, HostBinding } from '@angular/core';
import { classCardContentFullWidth } from '../../styles-helper/styles-helper';

@Directive({
  selector: '[svuiCardContentFullWidth]',
  standalone: true,
})
export class CardContentFullWidthDirective {
  @HostBinding('class.' + classCardContentFullWidth)
  private readonly classCardContentFullWidth = true;
}
