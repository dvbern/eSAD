import { ChangeDetectionStrategy, Component, input, OnInit } from '@angular/core';
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
export class ButtonComponent extends ButtonElement implements OnInit {
  readonly buttonColor = input.required<ButtonColor>({
    alias: 'svuiButton',
  });

  ngOnInit(): void {
    this.buttonType = this.buttonColor();
  }
}
