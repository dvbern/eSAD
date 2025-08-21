import { ChangeDetectionStrategy, Component } from '@angular/core';

@Component({
  selector: 'svui-header-bar',
  standalone: true,
  templateUrl: './header-bar.component.html',
  styleUrl: './header-bar.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class HeaderBarComponent {}
