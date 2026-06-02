import {ChangeDetectionStrategy, Component, computed, effect, HostBinding, inject, input, untracked} from '@angular/core';
import {TranslocoService} from '@jsverse/transloco';
import {WorkingIconComponent} from '../../icons/working-icon/working-icon.component';
import {IconButtonType} from '../bootstrap-button-color';
import {ButtonElement} from '../button-element.directive';
import {NgbTooltip} from "@ng-bootstrap/ng-bootstrap";

@Component({
  // eslint-disable-next-line @angular-eslint/component-selector
  selector: 'button[svuiIconOnlyButton]',
  templateUrl: './icon-only-button.component.html',
  styleUrl: './icon-only-button.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [WorkingIconComponent, NgbTooltip]
})
export class IconOnlyButtonComponent extends ButtonElement {
  readonly transloco = inject(TranslocoService);

  readonly badgeContent = input<number | null>(null);

  readonly buttonColor = input.required<IconButtonType>({
    alias: 'svuiIconOnlyButton',
  });

  readonly tooltip = input.required<string>();

  readonly computedTooltip = computed(() => {
    if (this.badgeContent() !== null) {
      return this.tooltip() + ': ' + this.badgeContent();
    } else {
      return this.tooltip();
    }
  });

  @HostBinding('class.svui_icon-button') readonly svuiStyles = true;

  @HostBinding('class')
  get buttonStyles(): string {
    switch (this.buttonColor()) {
      case 'inputAddOn':
        return 'svui_icon-button--input-add-on';
      case 'primary':
        return 'svui_icon-button--primary';
      case 'secondary':
        return 'svui_icon-button--secondary';
      case 'tableRowAction':
        return 'svui_icon-button--table-row-action';
      case 'success':
        return 'svui_icon-button--success';
    }
  }

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
