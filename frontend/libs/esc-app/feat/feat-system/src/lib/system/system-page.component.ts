
import {ChangeDetectionStrategy, Component} from '@angular/core';
import {RouterLink, RouterLinkActive, RouterOutlet} from '@angular/router';
import {Path} from '@dvb/feat-shared';
import {PageComponent} from '@dvb/feat-shared';
import {CardComponent} from '@dvb/ui-components';
import {NgbNav, NgbNavItem} from '@ng-bootstrap/ng-bootstrap';

@Component({
  selector: 'feat-system',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    PageComponent,
    CardComponent,
    NgbNav,
    NgbNavItem,
    RouterOutlet,
    RouterLink,
    RouterLinkActive
],
  templateUrl: './system-page.component.html',
  styles: ':host { display:block;}',
})
export class SystemPageComponent {
  protected readonly Path = Path;
}
