import { ChangeDetectionStrategy, Component, input } from '@angular/core';

@Component({
  selector: 'svui-textarea-counter',
  standalone: true,
  templateUrl: 'textarea-counter.component.html',
  styleUrl: 'textarea-counter.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CharacterCountComponent {
  readonly characterCount = input<number>();
  readonly maxCharacters = input<number>();
}
