import { ChangeDetectionStrategy, Component } from '@angular/core';
import { ErrorTextDirective } from '../../../../../notification/text';

@Component({
    selector: 'svui-form-error',
    templateUrl: './form-error.component.html',
    styleUrl: './form-error.component.scss',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [ErrorTextDirective]
})
export class FormErrorComponent {}
