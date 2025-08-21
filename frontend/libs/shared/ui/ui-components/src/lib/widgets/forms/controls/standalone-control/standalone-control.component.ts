import { ChangeDetectionStrategy, Component } from '@angular/core';
import { ControlWithTypeDirective } from '../control-with-type.directive';
import { FormControlTopLabelComponent } from '../control-children/form-control-top-label/form-control-top-label.component';

@Component({
    selector: 'svui-standalone-control',
    templateUrl: './standalone-control.component.html',
    styleUrl: './standalone-control.component.scss',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [FormControlTopLabelComponent]
})
export class StandaloneControlComponent extends ControlWithTypeDirective {}
