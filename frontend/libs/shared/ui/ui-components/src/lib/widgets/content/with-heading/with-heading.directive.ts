import { Directive, input } from '@angular/core';

export interface InternalLink {
  label: string;
  routerLink: string;
}

@Directive({
  selector: '[svuiWithHeading]',
  standalone: true,
})
export class WithHeadingDirective {
  readonly heading = input.required<string>();
  readonly backLink = input<InternalLink>();
}
