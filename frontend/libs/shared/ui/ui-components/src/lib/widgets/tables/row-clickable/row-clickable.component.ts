import {
  booleanAttribute,
  ChangeDetectionStrategy,
  Component,
  HostBinding,
  input,
  InputSignalWithTransform,
} from '@angular/core';

@Component({
  // eslint-disable-next-line @angular-eslint/component-selector
  selector: 'tr[svuiRowClickable]',
  standalone: true,
  templateUrl: './row-clickable.component.html',
  styleUrl: './row-clickable.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class RowClickableComponent {
  readonly clickable: InputSignalWithTransform<boolean, unknown> = input(true, {
    transform: booleanAttribute,
    alias: 'svuiRowClickable',
  });

  @HostBinding('class.svui_row-clickable') get clickableStyles(): boolean {
    return this.clickable();
  }
}
