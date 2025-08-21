import { Location, NgTemplateOutlet } from '@angular/common';
import { ChangeDetectionStrategy, Component, HostBinding, inject, input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { TranslocoPipe } from '@jsverse/transloco';
import { IconComponent } from '../../icons/icon';
import { SpinnerComponent } from '../../icons/spinner';
import { classBootstrapVisuallyHidden, classFormLayoutWidthFull } from '../../styles-helper/styles-helper';
import { HeadingVisibility } from '../with-accessible-heading/with-accessible-heading.directive';

export type TitleContext = 'page' | 'card' | 'section' | 'formSection' | 'paragraph';

@Component({
    selector: 'svui-title',
    templateUrl: './title.component.html',
    styleUrl: './title.component.scss',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [NgTemplateOutlet, SpinnerComponent, IconComponent, TranslocoPipe, RouterLink]
})
export class TitleComponent {
  readonly context = input.required<TitleContext>();
  readonly working = input<boolean>(false);
  readonly showBackLink = input<boolean>(false);
  readonly visibility = input<HeadingVisibility>('visible');
  readonly location = inject(Location);

  @HostBinding('class.' + classFormLayoutWidthFull)
  get formSectionStyles(): boolean {
    return this.context() === 'formSection';
  }

  @HostBinding('class.' + classBootstrapVisuallyHidden)
  get hiddenTitle(): boolean {
    return this.visibility() === 'hidden';
  }
}
