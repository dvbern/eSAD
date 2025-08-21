import { booleanAttribute, ChangeDetectionStrategy, Component, input } from '@angular/core';
import { FormFullWidthDirective } from '../../layout';
import { LikeATableComponent } from './like-a-table/like-a-table.component';
import { SpinnerComponent } from '../../../icons';
import { FormControlTopComponent } from '../../controls/control-children/form-control-top/form-control-top.component';
import { RequiredComponent } from '../../controls/control-children/required/required.component';
import { FormControlBottomComponent } from '../../controls/control-children/form-control-bottom/form-control-bottom.component';

@Component({
    selector: 'svui-form-upload-one',
    templateUrl: './form-upload-one.component.html',
    styleUrl: './form-upload-one.component.scss',
    changeDetection: ChangeDetectionStrategy.OnPush,
    hostDirectives: [FormFullWidthDirective],
    imports: [
        LikeATableComponent,
        SpinnerComponent,
        FormControlTopComponent,
        RequiredComponent,
        FormControlBottomComponent,
    ]
})
export class FormUploadOneComponent {
  // form-control stuff
  readonly label = input.required<string>();
  readonly required = input(false, { transform: booleanAttribute });
  readonly showRequiredErrorMessage = input(false);
  readonly uploadWorking = input<boolean>(false, { alias: 'working' });

  // uploader stuff
  readonly fileName = input.required<string | undefined>();
}
