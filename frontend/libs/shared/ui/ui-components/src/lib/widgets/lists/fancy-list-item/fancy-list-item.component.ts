import { DatePipe } from '@angular/common';
import {ChangeDetectionStrategy, Component, HostBinding, input} from '@angular/core';
import {Params} from '@angular/router';
import {TableLinkComponent} from '../../tables';

@Component({
    selector: 'svui-fancy-list-item',
    templateUrl: './fancy-list-item.component.html',
    styleUrl: './fancy-list-item.component.scss',
    changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    DatePipe,
    TableLinkComponent
],
})
export class FancyListItemComponent {
  mainText = input.required<string>();
  subText = input<string>();
  date = input.required<Date>();

  routerlinkDestination = input.required<string>();
  linkQueryParams = input<Params>();
  linkText = input.required<string>();

  gray = input<boolean>(false);

  @HostBinding('class')
  get disabledStyle(): string {
    return this.gray() ? 'disabled' : '';
  }
}
