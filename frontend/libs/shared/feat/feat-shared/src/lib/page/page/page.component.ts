import {CommonModule} from '@angular/common';
import {ChangeDetectionStrategy, Component, inject, OnInit, signal, TemplateRef, WritableSignal} from '@angular/core';
import {NavigationEnd, NavigationStart, Router, RouterLink, RouterLinkActive} from '@angular/router';
import {AufgabenStore} from '../../admin-shared/aufgaben.store';
import {BenutzerStore} from '../../benutzer/benutzer/benutzer.store';
import {Path} from '../../core/path';
import {DeploymentStore} from '../../deployment/deployment.store';
import {
  CollapsibleMenuComponent,
  IconOnlyButtonComponent,
  PageLayoutComponent,
  WithHeadingDirective,
} from '@dvb/ui-components';
import {
  hasAdminPageViewBerechtiung,
  hasAnlassViewBerechtigung,
  hasKindPageViewBerechtigung,
  hasPlaygroundViewBerechtigung,
  hasReportViewBerechtigung,
  hasSystemPageViewBerechtiung,
} from '@dvb/util-shared';
import {TranslocoPipe} from '@jsverse/transloco';
import {NgbDropdownItem, NgbOffcanvas, OffcanvasDismissReasons} from '@ng-bootstrap/ng-bootstrap';
import {filter} from 'rxjs';

import {FachRollenListComponent} from './fach-rollen-list/fach-rollen-list.component';
import {GlobalSearchComponent} from './global-search/global-search.component';
import {TimetravelStatusComponent} from './timetravel-status/timetravel-status.component';
import {VersionDisplayComponent} from './version-display/version-display.component';

@Component({
  selector: 'feat-page',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    CommonModule,
    PageLayoutComponent,
    RouterLink,
    RouterLinkActive,
    NgbDropdownItem,
    CollapsibleMenuComponent,
    IconOnlyButtonComponent,
    TranslocoPipe,
    GlobalSearchComponent,
    TimetravelStatusComponent,
    FachRollenListComponent,
    VersionDisplayComponent,
  ],
  templateUrl: './page.component.html',
  styleUrl: './page.component.scss',
})
export class PageComponent extends WithHeadingDirective implements OnInit {
  readonly benutzerStore = inject(BenutzerStore);
  readonly deploymentStore = inject(DeploymentStore);
  readonly aufgabenStore = inject(AufgabenStore);
  private readonly offcanvasService = inject(NgbOffcanvas);

  public canViewReport = this.benutzerStore.createHasRequiredRoleSig(hasReportViewBerechtigung());
  public canViewKind = this.benutzerStore.createHasRequiredRoleSig(hasKindPageViewBerechtigung());
  public canViewAnlass = this.benutzerStore.createHasRequiredRoleSig(hasAnlassViewBerechtigung());
  public canViewPlayground = this.benutzerStore.createHasRequiredRoleSig(hasPlaygroundViewBerechtigung());
  public canViewAdminPage = this.benutzerStore.createHasRequiredRoleSig(hasAdminPageViewBerechtiung());
  public canViewSystemPage = this.benutzerStore.createHasRequiredRoleSig(hasSystemPageViewBerechtiung());
  Path = Path;
  router = inject(Router);
  closeResult: WritableSignal<string> = signal('');

  public logout(): void {
    const encodedRedirectUri = encodeURIComponent(this.deploymentStore.config().clientAuthConfig.logoutUrl);
    window.location.href = `/oauth2/sign_out?rd=${encodedRedirectUri}`;
  }

  public ngOnInit(): void {
    // close offcanvas menu on navigation
    this.router.events
      .pipe(filter(event => event instanceof NavigationStart || event instanceof NavigationEnd))
      .subscribe(() => {
        this.offcanvasService.dismiss('navigated');
      });
  }

  open(content: TemplateRef<any>) {
    this.offcanvasService.open(content, {
      position: 'end',
      ariaLabelledBy: 'Navigation',
    }).result.then(
      (result) => {
        this.closeResult.set(`Closed with: ${result}`);
      },
      (reason) => {
        this.closeResult.set(`Dismissed ${this.getDismissReason(reason)}`);
      },
    );
  }

  private getDismissReason(reason: any): string {
    switch (reason) {
      case OffcanvasDismissReasons.ESC:
        return 'by pressing ESC';
      case OffcanvasDismissReasons.BACKDROP_CLICK:
        return 'by clicking on the backdrop';
      default:
        return `with: ${reason}`;
    }
  }

}
