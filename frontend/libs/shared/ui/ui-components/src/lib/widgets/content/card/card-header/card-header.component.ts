import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { HeadingDirective } from '../../../headings/heading';
import { SpinnerComponent } from '../../../icons/spinner';

@Component({
    selector: 'svui-card-header',
    templateUrl: './card-header.component.html',
    styleUrl: './card-header.component.scss',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [HeadingDirective, SpinnerComponent]
})
export class CardHeaderComponent {
  heading = input<string>();
  readonly loading = input<boolean>(false);
}
