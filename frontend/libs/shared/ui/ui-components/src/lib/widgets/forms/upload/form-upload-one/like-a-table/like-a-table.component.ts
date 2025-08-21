import { ChangeDetectionStrategy, Component } from '@angular/core';

@Component({
  selector: 'svui-like-a-table',
  standalone: true,
  templateUrl: './like-a-table.component.html',
  styleUrl: './like-a-table.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class LikeATableComponent {}
