import { ChangeDetectionStrategy, Component, input, OnInit } from '@angular/core';
import { ButtonColor } from '../bootstrap-button-color';
import { ButtonBase } from '../button-base.directive';
import { WorkingIconComponent } from '../../icons/working-icon/working-icon.component';

@Component({
    // eslint-disable-next-line @angular-eslint/component-selector
    selector: '[svuiLikeAButton]',
    templateUrl: './like-a-button.component.html',
    styleUrl: './like-a-button.component.scss',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [WorkingIconComponent]
})
export class LikeAButtonComponent extends ButtonBase implements OnInit {
  readonly buttonColor = input.required<ButtonColor>({
    alias: 'svuiLikeAButton',
  });

  ngOnInit(): void {
    this.buttonType = this.buttonColor();
  }
}
