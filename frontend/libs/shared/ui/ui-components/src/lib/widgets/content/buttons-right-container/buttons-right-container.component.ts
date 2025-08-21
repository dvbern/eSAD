import { ChangeDetectionStrategy, Component } from '@angular/core';

@Component({
  selector: 'svui-buttons-right-container',
  standalone: true,
  templateUrl: './buttons-right-container.component.html',
  styleUrl: './buttons-right-container.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ButtonsRightContainerComponent {}
