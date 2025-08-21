import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { WithHeadingDirective } from '../../content';
import { CardComponent } from '../../content/card';
import { HeaderBarComponent } from '../../content/header-bar/header-bar.component';
import { HeadingDirective } from '../../headings/heading/heading.directive';
import { SpinnerComponent } from '../../icons/spinner';

@Component({
    selector: 'svui-list',
    templateUrl: './list.component.html',
    styleUrl: './list.component.scss',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [CardComponent, HeadingDirective, HeaderBarComponent, SpinnerComponent]
})
export class ListComponent extends WithHeadingDirective {
  readonly loading = input.required<boolean>();
}
