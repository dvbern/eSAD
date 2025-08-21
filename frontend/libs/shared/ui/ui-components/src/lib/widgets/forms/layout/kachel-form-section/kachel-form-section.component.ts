import { ChangeDetectionStrategy, Component } from '@angular/core';
import { WithAccessibleHeadingDirective } from '../../../headings/with-accessible-heading/with-accessible-heading.directive';
import { HeadingDirective } from '../../../headings/heading/heading.directive';

@Component({
    selector: 'svui-kachel-form-section',
    templateUrl: './kachel-form-section.component.html',
    styleUrl: './kachel-form-section.component.scss',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [HeadingDirective]
})
export class KachelFormSectionComponent extends WithAccessibleHeadingDirective {}
