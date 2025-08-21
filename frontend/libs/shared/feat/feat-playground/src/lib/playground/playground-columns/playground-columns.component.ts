import {ChangeDetectionStrategy, Component} from '@angular/core';
import {
  CardComponent,
  CardInCardComponent,
  HeadingDirective,
  IconComponent,
  InfoItemComponent,
  LayoutColumnsComponent,
} from '@dvb/ui-components';

@Component({
    selector: 'feat-playground-columns',
    templateUrl: './playground-columns.component.html',
    styleUrl: './playground-columns.component.scss',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [
        LayoutColumnsComponent,
        CardComponent,
        InfoItemComponent,
        IconComponent,
        HeadingDirective,
        CardInCardComponent
    ]
})
export class PlaygroundColumnsComponent {
}
