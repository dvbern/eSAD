import { ChangeDetectionStrategy, Component } from '@angular/core';

@Component({
  selector: 'svui-input-button-combo',
  standalone: true,
  templateUrl: './input-button-combo.component.html',
  styleUrl: './input-button-combo.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class InputButtonComboComponent {}
