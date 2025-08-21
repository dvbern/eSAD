import { ChangeDetectionStrategy, Component, input } from '@angular/core';

export type SpinnerContext = 'inline' | 'table';

@Component({
  selector: 'svui-spinner',
  standalone: true,
  templateUrl: './spinner.component.html',
  styleUrl: './spinner.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SpinnerComponent {
  readonly working = input<boolean>(false);
  readonly context = input.required<SpinnerContext>();
}
