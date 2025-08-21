import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { ValidationErrors } from '@angular/forms';
import { Nullish } from '@dvb/util-shared';
import { NgbAlert } from '@ng-bootstrap/ng-bootstrap';
import { SharedUiControlMessagePipe } from '../../controls/control-children/form-control-error/control-message.pipe';

@Component({
    selector: 'svui-form-error-message',
    imports: [NgbAlert, SharedUiControlMessagePipe, SharedUiControlMessagePipe],
    templateUrl: './form-error-message.component.html',
    styleUrl: './form-error-message.component.scss',
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class FormErrorMessageComponent {
  errors = input.required<ValidationErrors | Nullish>();
}
