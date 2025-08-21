import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { RequiredComponent } from '../required/required.component';
import { HeadingVisibility } from '../../../../headings/with-accessible-heading/with-accessible-heading.directive';

@Component({
    selector: 'svui-form-control-top-label',
    templateUrl: './form-control-top-label.component.html',
    styleUrl: './form-control-top-label.component.scss',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [RequiredComponent]
})
export class FormControlTopLabelComponent {
  readonly required = input.required<boolean>();
  readonly controlId = input.required<string>();
  readonly visibility = input<HeadingVisibility>('visible');
}
