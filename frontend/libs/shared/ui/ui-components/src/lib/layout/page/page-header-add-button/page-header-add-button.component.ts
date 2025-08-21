import {ChangeDetectionStrategy, Component, input, output} from '@angular/core';
import {RouterLink} from '@angular/router';
import {TranslocoPipe} from '@jsverse/transloco';
import {IconOnlyButtonComponent} from '../../../widgets';

@Component({
  selector: 'svui-page-header-add-button',
  templateUrl: './page-header-add-button.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    IconOnlyButtonComponent,
    RouterLink,
    TranslocoPipe,
  ],
})
export class PageHeaderAddButtonComponent {
  addRouterLink = input<string>();
  add = output<void>();
}
