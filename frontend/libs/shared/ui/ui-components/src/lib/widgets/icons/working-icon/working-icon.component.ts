import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { IconComponent, IconUseCase } from '../icon';
import { SpinnerComponent } from '../spinner';

@Component({
    selector: 'svui-working-icon',
    templateUrl: './working-icon.component.html',
    styleUrl: './working-icon.component.scss',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [IconComponent, SpinnerComponent]
})
export class WorkingIconComponent {
  readonly icon = input<IconUseCase | undefined>(undefined, {
    alias: 'iconUseCase',
  });
  readonly working = input<boolean>(false);
}
