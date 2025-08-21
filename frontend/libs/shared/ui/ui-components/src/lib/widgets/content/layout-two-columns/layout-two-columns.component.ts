import { ChangeDetectionStrategy, Component } from '@angular/core';

@Component({
  selector: 'svui-layout-two-columns',
  standalone: true,
  templateUrl: './layout-two-columns.component.html',
  styleUrl: './layout-two-columns.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class LayoutTwoColumnsComponent {}
