import { Directive, HostBinding, input } from '@angular/core';
import { classFormLayoutWidthFull } from '../../styles-helper/styles-helper';

export type HeadingContext = 'page' | 'card' | 'list' | 'formSection' | 'hidden';

@Directive({
  selector: 'h1[svuiHeading],h2[svuiHeading],h3[svuiHeading],h4[svuiHeading],h5[svuiHeading],h6[svuiHeading],',
  standalone: true,
})
export class HeadingDirective {
  readonly context = input.required<HeadingContext>({ alias: 'svuiHeading' });

  @HostBinding('class.svui_heading') private readonly svuiHeading = true;

  @HostBinding('class.' + classFormLayoutWidthFull)
  get formSectionStyles(): boolean {
    return this.context() === 'formSection';
  }

  @HostBinding('class')
  get styles(): string {
    switch (this.context()) {
      case 'page':
        return 'svui_heading--page';
      case 'card':
        return 'svui_heading--card';
      case 'list':
        return 'svui_heading--list';
      case 'formSection':
        return 'svui_heading--form';
      case 'hidden':
        return 'visually-hidden';
    }
  }
}
