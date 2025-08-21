import { ChangeDetectionStrategy, Component } from '@angular/core';

@Component({
  selector: 'svui-tile',
  standalone: true,
  templateUrl: './tile.component.html',
  styleUrl: './tile.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class TileComponent {}
