import { ChangeDetectionStrategy, Component } from '@angular/core';

@Component({
  selector: 'svui-card-section',
  standalone: true,
  templateUrl: './card-section.component.html',
  styleUrl: './card-section.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CardSectionComponent {}
