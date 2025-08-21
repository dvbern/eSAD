import { Directive, input } from '@angular/core';

export type HeadingVisibility = 'hidden' | 'visible';

/**
 * Für Container, welche für die A11y immer ein Heading brauchen, welches aber versteckt werden kann.
 */
@Directive({ standalone: true })
export abstract class WithAccessibleHeadingDirective {
  readonly heading = input.required<string>();
  readonly headingVisibility = input<HeadingVisibility>('visible');
  readonly loading = input<boolean>(false);
}
