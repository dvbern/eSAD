import { ChangeDetectionStrategy, Component } from '@angular/core';

@Component({
  selector: 'svui-layout-columns',
  standalone: true,
  templateUrl: './layout-columns.component.html',
  styleUrl: './layout-columns.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class LayoutColumnsComponent {}
