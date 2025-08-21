import { ChangeDetectionStrategy, Component } from '@angular/core';

@Component({
  selector: 'svui-tile-grid',
  standalone: true,
  templateUrl: './tile-grid.component.html',
  styleUrl: './tile-grid.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class TileGridComponent {}
