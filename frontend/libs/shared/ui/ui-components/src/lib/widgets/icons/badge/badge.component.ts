import {ChangeDetectionStrategy, Component, HostBinding, input} from '@angular/core';

export type BadgeColor = 'purple' | 'blue' | 'orange' | 'lightgreen' | 'green' | 'red' | 'transparent';

@Component({
  selector: 'svui-badge',
  standalone: true,
  templateUrl: './badge.component.html',
  styleUrl: './badge.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class BadgeComponent {
  color = input<BadgeColor>('transparent');

  @HostBinding('class')
  get colorClass(): string {
    return 'bg-' + this.color();
  }
}
