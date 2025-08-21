import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { TranslocoPipe } from '@jsverse/transloco';
import { RequiredComponent } from '../../../controls/control-children/required/required.component';
import { ControlWithDetailModeDirective } from '../../../controls/control-with-detail-mode.directive';

@Component({
    selector: 'svui-single-checkbox-wrapper',
    templateUrl: './single-checkbox-wrapper.component.html',
    styleUrl: './single-checkbox-wrapper.component.scss',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [TranslocoPipe, RequiredComponent]
})
export class SingleCheckboxWrapperComponent extends ControlWithDetailModeDirective {
  readonly label = input.required<string>();
}
