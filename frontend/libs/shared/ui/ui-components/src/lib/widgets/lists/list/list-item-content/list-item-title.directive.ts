import { Directive, HostBinding } from '@angular/core';
import { classListItemTitle } from '../../../styles-helper/styles-helper';

@Directive({
  selector: '[svuiListItemTitle]',
  standalone: true,
})
export class ListItemTitleDirective {
  @HostBinding('class.' + classListItemTitle)
  private readonly classListItemTitle = true;
}
