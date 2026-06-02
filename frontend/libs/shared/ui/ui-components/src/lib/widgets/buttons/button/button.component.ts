import { ChangeDetectionStrategy, Component, effect, input, untracked } from '@angular/core';
import { ButtonElement } from '../button-element.directive';
import { ButtonColor } from '../bootstrap-button-color';
import { WorkingIconComponent } from '../../icons/working-icon/working-icon.component';

@Component({
    // eslint-disable-next-line @angular-eslint/component-selector
    selector: 'button[svuiButton]',
    templateUrl: './button.component.html',
    styleUrl: './button.component.scss',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [WorkingIconComponent]
})
export class ButtonComponent extends ButtonElement {
  readonly buttonColor = input.required<ButtonColor>({
    alias: 'svuiButton',
  });

  constructor() {
    super();
    effect(() => {
      const color = this.buttonColor();
      untracked(() => {
        this.buttonType = color;
      });
    });
  }
}
