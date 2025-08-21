import {ChangeDetectionStrategy, Component, inject} from '@angular/core';
import {RouterLink, RouterLinkActive, RouterOutlet} from '@angular/router';
import {CollapsibleMenuComponent} from '@dvb/ui-components';
import {TranslocoPipe} from '@jsverse/transloco';
import {NgbDropdownItem, NgbNav, NgbNavItem, NgbNavLink} from '@ng-bootstrap/ng-bootstrap';
import {PlaygroundNavStore} from '../playground-nav.store';

@Component({
    selector: 'feat-playground-nav-kinder-tabs',
    templateUrl: './kinder-tabs.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [
        RouterLink,
        RouterLinkActive,
        NgbNav,
        NgbNavItem,
        RouterOutlet,
        NgbNavLink,
        TranslocoPipe,
        CollapsibleMenuComponent,
        NgbDropdownItem,
    ]
})
export class KinderTabsComponent {
  navStore = inject(PlaygroundNavStore);

}
