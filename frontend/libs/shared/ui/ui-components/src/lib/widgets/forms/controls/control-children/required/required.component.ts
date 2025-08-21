import { ChangeDetectionStrategy, Component } from '@angular/core';
import { TranslocoPipe } from '@jsverse/transloco';

@Component({
    selector: 'svui-required',
    templateUrl: './required.component.html',
    styleUrl: './required.component.scss',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [TranslocoPipe]
})
export class RequiredComponent {}
