import {ChangeDetectionStrategy, Component, effect, input} from '@angular/core';
import {NgbCollapse} from '@ng-bootstrap/ng-bootstrap';
import {IconComponent} from '../../icons/icon/icon.component';
import {CardComponent, CardHeaderComponent, CardSpacing} from '../card';

export type CollapsibleState = 'open' | 'collapsed';

@Component({
    selector: 'svui-card-collapsible',
    templateUrl: './card-collapsible.component.html',
    styleUrl: './card-collapsible.component.scss',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [CardHeaderComponent, IconComponent, NgbCollapse, CardComponent]
})
export class CardCollapsibleComponent {
  readonly heading = input<string>('');
  readonly initialState = input.required<CollapsibleState>();
  readonly innerSpacing = input<CardSpacing>('allSides');

  isCollapsed = false;

  constructor() {
    effect(() => {
      const initialCollapsed = this.initialState();
      if (initialCollapsed !== undefined) {
        this.isCollapsed = initialCollapsed === 'collapsed';
      }
    });
  }
}
