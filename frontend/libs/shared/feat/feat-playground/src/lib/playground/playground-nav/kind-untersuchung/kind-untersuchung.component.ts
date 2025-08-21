import {ChangeDetectionStrategy, Component, inject} from '@angular/core';
import {RouterLink, RouterLinkActive, RouterOutlet} from '@angular/router';
import {CardComponent} from '@dvb/ui-components';
import {NgbNav, NgbNavItem} from '@ng-bootstrap/ng-bootstrap';
import {PlaygroundNavStore} from '../playground-nav.store';
import {PlaygroundNavUntersuchungStore} from './playground-nav-untersuchung.store';

@Component({
    selector: 'feat-playground-nav-kind-untersuchung',
    templateUrl: './kind-untersuchung.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [
        CardComponent,
        RouterLink,
        RouterLinkActive,
        NgbNav,
        NgbNavItem,
        RouterOutlet,
    ]
})
export class KindUntersuchungComponent {

  navStore = inject(PlaygroundNavStore);
  untersuchungStore = inject(PlaygroundNavUntersuchungStore);
}
