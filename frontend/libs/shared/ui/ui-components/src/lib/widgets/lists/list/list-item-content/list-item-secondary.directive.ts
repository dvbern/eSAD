import { Directive, HostBinding } from '@angular/core';
import { classListItemSecondary } from '../../../styles-helper/styles-helper';

@Directive({
  selector: '[svuiListItemSecondary]',
  standalone: true,
})
export class ListItemSecondaryDirective {
  @HostBinding('class.' + classListItemSecondary)
  private readonly classListItemSecondary = true;
}
