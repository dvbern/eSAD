import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { RouterLink } from '@angular/router';
import {
  HeaderBarComponent,
  HeadingDirective,
  IconComponent,
  SpinnerComponent,
  WithHeadingDirective,
} from '../../../widgets';

@Component({
    selector: 'svui-page-header',
    templateUrl: './page-header.component.html',
    styleUrl: './page-header.component.scss',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [HeadingDirective, RouterLink, HeaderBarComponent, SpinnerComponent, IconComponent]
})
export class PageHeaderComponent extends WithHeadingDirective {
  readonly loading = input<boolean>(false);
}
