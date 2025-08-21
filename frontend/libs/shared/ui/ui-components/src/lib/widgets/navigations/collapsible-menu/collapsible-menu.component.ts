import {ChangeDetectionStrategy, Component, input} from '@angular/core';
import {NgbDropdownModule} from '@ng-bootstrap/ng-bootstrap';
import {IconOnlyButtonComponent} from '../../buttons';
import {SpinnerComponent} from '../../icons/spinner';

export type CollapsibleMenuStyle = 'ICON' | 'LINK';

@Component({
  selector: 'svui-collapsible-menu',
  templateUrl: './collapsible-menu.component.html',
  styleUrl: './collapsible-menu.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    NgbDropdownModule,
    IconOnlyButtonComponent,
    SpinnerComponent,
  ]
})
export class CollapsibleMenuComponent {
  collapsibleId = 'collapsibleId';

  menuStyle = input<CollapsibleMenuStyle>('ICON');
  label = input.required<string>();
  readonly loading = input.required<boolean>();
}
