import { ChangeDetectionStrategy, Component } from '@angular/core';

@Component({
  selector: 'svui-info-items-container',
  standalone: true,
  templateUrl: './info-items-container.component.html',
  styleUrl: './info-items-container.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class InfoItemsContainerComponent {}
