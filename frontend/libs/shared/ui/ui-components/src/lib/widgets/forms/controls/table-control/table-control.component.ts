import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { ControlWithIdWrapperDirective } from '../control-with-id-wrapper.directive';
import { SharedUiControlMessagePipe } from '../control-children/form-control-error/control-message.pipe';
import { RequiredComponent } from '../control-children/required/required.component';

@Component({
    selector: 'svui-table-control',
    templateUrl: './table-control.component.html',
    styleUrl: './table-control.component.scss',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [RequiredComponent, SharedUiControlMessagePipe]
})
export class TableControlComponent extends ControlWithIdWrapperDirective {
  readonly rowHeader = input.required<string>();
  readonly columnHeader = input.required<string>();
}
