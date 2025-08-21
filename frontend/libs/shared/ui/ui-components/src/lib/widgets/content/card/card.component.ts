import { ChangeDetectionStrategy, Component, input } from '@angular/core';

export type CardSpacing = 'allSides' | 'topBottom' | 'none';

@Component({
  selector: 'svui-card',
  standalone: true,
  templateUrl: './card.component.html',
  styleUrl: './card.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CardComponent {
  readonly innerSpacing = input<CardSpacing>('allSides');
}
