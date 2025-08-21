import {ChangeDetectionStrategy, Component} from '@angular/core';
import {BadgeComponent, CardComponent, HeadingDirective} from '@dvb/ui-components';

@Component({
    selector: 'feat-playground-typography',
    templateUrl: './playground-typography.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [
        CardComponent,
        HeadingDirective,
        BadgeComponent,
    ]
})
export class PlaygroundTypographyComponent {}
