import {ChangeDetectionStrategy, Component, HostBinding, input} from '@angular/core';

export type ColWidth =
  'date'
  | 'checkbox'
  | 'timeVonBis'
  | 'text-small'
  | 'text-medium'
  | 'text-large'
  | 'select-medium'
  | 'select-small'
  | 'options';

@Component({
  // eslint-disable-next-line @angular-eslint/component-selector
  selector: 'col[svuiColWidth]',
  standalone: true,
  templateUrl: './col-width.component.html',
  styleUrl: './col-width.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ColWidthComponent {

  colWidth = input<ColWidth>('text-medium');

  @HostBinding('class')
  get widthStyles(): string {
    return 'width-' + (this.colWidth());
  }

}
