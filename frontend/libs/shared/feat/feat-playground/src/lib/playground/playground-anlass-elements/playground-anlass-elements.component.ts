import {ChangeDetectionStrategy, Component} from '@angular/core';
import {AnlassSummaryLayoutComponent, CardComponent, HeadingDirective} from '@dvb/ui-components';
import {fromISODateString} from '@dvb/util-shared';

@Component({
    selector: 'feat-playground-anlass-elements',
    templateUrl: './playground-anlass-elements.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [
        CardComponent,
        HeadingDirective,
        AnlassSummaryLayoutComponent,
    ]
})
export class PlaygroundAnlassElementsComponent {
  protected readonly fromISODateString = fromISODateString;
}
