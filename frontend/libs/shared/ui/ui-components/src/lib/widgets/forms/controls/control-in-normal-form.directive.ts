import {booleanAttribute, Directive, HostBinding, input} from '@angular/core';
import {IconUseCase} from '../../icons/icon/icon.component';
import {
  classFormLayoutWidthDouble,
  classFormLayoutWidthFull,
  classFormLayoutWidthHalf,
  classFormLayoutWidthNormal,
  classFormLayoutWidthOneAndHalf,
} from '../../styles-helper/styles-helper';
import {ControlWithTypeDirective} from './control-with-type.directive';

export type FormInputSize = 'normal' | 'half' | 'oneAndHalf' | 'double' | 'full';

@Directive({
  standalone: true,
})
export class ControlInNormalFormDirective extends ControlWithTypeDirective {
  readonly size = input<FormInputSize>('normal');
  readonly iconTopRight = input<{ iconTooltip: string, icon: IconUseCase }>();
  readonly bulkEdit = input<boolean, unknown>(false, {transform: booleanAttribute});

  @HostBinding('class')
  get widthStyles(): string {
    switch (this.size()) {
      case 'normal':
        return classFormLayoutWidthNormal;
      case 'half':
        return classFormLayoutWidthHalf;
      case 'full':
        return classFormLayoutWidthFull;
      case 'double':
        return classFormLayoutWidthDouble;
      case 'oneAndHalf':
        return classFormLayoutWidthOneAndHalf;
    }
  }
}
