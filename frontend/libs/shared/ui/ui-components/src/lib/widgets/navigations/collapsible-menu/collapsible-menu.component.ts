import {ChangeDetectionStrategy, Component, computed, input} from '@angular/core';
import {NgbDropdownModule} from '@ng-bootstrap/ng-bootstrap';
import {IconButtonType, IconOnlyButtonComponent} from '../../buttons';
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
  ],
  host: {
    '[class.svui_collapsible-menu--transparent]': '!hasExplicitButtonColor()',
  },
})
export class CollapsibleMenuComponent {
  collapsibleId = 'collapsibleId';

  menuStyle = input<CollapsibleMenuStyle>('ICON');
  /**
   * Without an explicit color the toggle is rendered with a transparent background.
   */
  buttonColor = input<IconButtonType | undefined>(undefined);
  readonly hasExplicitButtonColor = computed(() => this.buttonColor() !== undefined);
  readonly effectiveButtonColor = computed<IconButtonType>(() => this.buttonColor() ?? 'secondary');
  label = input.required<string>();
  readonly loading = input.required<boolean>();
}
