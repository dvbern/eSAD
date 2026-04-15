import {ChangeDetectionStrategy, Component} from '@angular/core';
import {PageComponent, Path} from "@dvb/feat-shared";
import {TranslocoPipe} from "@jsverse/transloco";
import {CardComponent} from "@dvb/ui-components";
import {NgbNav, NgbNavItem} from "@ng-bootstrap/ng-bootstrap";
import {RouterLink, RouterLinkActive, RouterOutlet} from "@angular/router";

@Component({
  selector: 'feat-archivierung-list-page',
  standalone: true,
  imports: [
    PageComponent,
    TranslocoPipe,
    CardComponent,
    NgbNav,
    NgbNavItem,
    RouterOutlet,
    RouterLink,
    RouterLinkActive
  ],
  templateUrl: './archivierung-list.page.component.html',
  styleUrl: './archivierung-list.page.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ArchivierungListPageComponent {

  protected readonly Path = Path;


}
