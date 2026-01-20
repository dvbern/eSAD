import { ChangeDetectionStrategy, Component, input } from '@angular/core';

@Component({
  selector: 'svui-card-in-card',
  standalone: true,
  templateUrl: './card-in-card.component.html',
  styles: ':host { display:block;}',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CardInCardComponent {
  readonly renderBorder = input<boolean>(true);
}
