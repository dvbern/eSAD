import { ChangeDetectionStrategy, Component } from '@angular/core';
import { WithAccessibleHeadingDirective } from '../../../headings/with-accessible-heading/with-accessible-heading.directive';
import { HeadingDirective } from '../../../headings/heading/heading.directive';
import {IconComponent} from "../../../icons";

@Component({
    selector: 'svui-kachel-form-section',
    templateUrl: './kachel-form-section.component.html',
    styleUrl: './kachel-form-section.component.scss',
    changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [HeadingDirective, IconComponent],
  host: {
    // if there is no heading render the kachel headless
    '[class.svui_kachel-form-section--headless]': '!heading()',
  },
})
export class KachelFormSectionComponent extends WithAccessibleHeadingDirective {}
