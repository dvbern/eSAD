import {CommonModule} from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  inject,
  input,
  OnInit,
  signal,
  TemplateRef,
  WritableSignal,
} from '@angular/core';
import {NavigationEnd, NavigationStart, Router, RouterLink, RouterLinkActive} from '@angular/router';
import {
  CollapsibleMenuComponent,
  IconComponent,
  IconOnlyButtonComponent,
  PageLayoutComponent,
  WithHeadingDirective,
} from '@dvb/ui-components';
import {AppLanguages} from '@dvb/data-access-backend';
import {appLanguageToLanguage, LanguageStore} from '@dvb/util-i18n';
import {
  hasAdminPageViewBerechtiung,
  hasAnlassViewBerechtigung,
  hasAufgabenViewBerechtigungen,
  hasConfigViewBerechtigung,
  hasKindNotizViewBerechtigung,
  hasKindPageViewBerechtigung,
  hasPlaygroundViewBerechtigung,
  hasReportViewBerechtigung,
  hasSystemPageViewBerechtiung,
  isNullish, withAnyBerechtigung,
} from '@dvb/util-shared';
import {TranslocoPipe} from '@jsverse/transloco';
import {NgbDropdownItem, NgbOffcanvas, OffcanvasDismissReasons} from '@ng-bootstrap/ng-bootstrap';
import {filter} from 'rxjs';
import {AufgabenStore} from '../../admin-shared/aufgaben.store';
import {BenutzerStore} from '../../benutzer/benutzer/benutzer.store';
import {Path} from '../../core/path';
import {DeploymentStore} from '../../deployment/deployment.store';
import {TenantSettingsStore} from '../../tenant/tenant-settings.store';

import {FachRollenListComponent} from './fach-rollen-list/fach-rollen-list.component';
import {GlobalSearchComponent} from './global-search/global-search.component';
import {TimetravelStatusComponent} from './timetravel-status/timetravel-status.component';
import {VersionDisplayComponent} from './version-display/version-display.component';
import {TenantFooterComponent} from "./tenant-footer/tenant-footer.component";

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
    IconComponent,
    TenantFooterComponent,
  ],
  templateUrl: './page.component.html',
  styleUrl: './page.component.scss',
})
export class PageComponent extends WithHeadingDirective implements OnInit {
  readonly benutzerStore = inject(BenutzerStore);
  readonly deploymentStore = inject(DeploymentStore);
  readonly aufgabenStore = inject(AufgabenStore);
  readonly languageStore = inject(LanguageStore);
  readonly tenantSettingsStore = inject(TenantSettingsStore);
  private readonly offcanvasService = inject(NgbOffcanvas);

  public canViewReport = this.benutzerStore.createHasRequiredRoleSig(hasReportViewBerechtigung());
  public canViewKind = this.benutzerStore.createHasRequiredRoleSig(hasKindPageViewBerechtigung());
  public canViewAnlass = this.benutzerStore.createHasRequiredRoleSig(hasAnlassViewBerechtigung());
  public canViewPlayground = this.benutzerStore.createHasRequiredRoleSig(hasPlaygroundViewBerechtigung());
  public canViewAdminPage = this.benutzerStore.createHasRequiredRoleSig(hasAdminPageViewBerechtiung());
  public canViewSystemPage = this.benutzerStore.createHasRequiredRoleSig(hasSystemPageViewBerechtiung());
  public canViewAufgaben = this.benutzerStore.createHasRequiredRoleSig(hasAufgabenViewBerechtigungen());
  public canViewKonfiguration = this.benutzerStore.createHasRequiredRoleSig(hasConfigViewBerechtigung());
  public canViewNotizen = this.benutzerStore.createHasRequiredRoleSig(hasKindNotizViewBerechtigung());
  public hasAnyBerechtigung = this.benutzerStore.createHasRequiredRoleSig(withAnyBerechtigung());

  Path = Path;
  router = inject(Router);
  closeResult: WritableSignal<string> = signal('');

  showNav = input<boolean>(true);
  showLogout = input<boolean>(true);
  showFooter = input<boolean>(true);

  public logout(): void {
    // If the logoutRedirectUrl is not set, we redirect to the application root. Note that a passed logoutRedirectUrl
    // must be whitelisted in the oauth2proxy config
    const logoutRedirectUrl = this.deploymentStore.config().clientAuthConfig.logoutRedirectUrl;
    if (isNullish(logoutRedirectUrl) || (logoutRedirectUrl.length === 0)) {
      window.location.href = `/oauth2/sign_out?rd=${window.location.origin}/`;
    } else {
      const encodedRedirectUri = encodeURIComponent(logoutRedirectUrl);
      window.location.href = `/oauth2/sign_out?rd=${encodedRedirectUri}`
    }
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

  public switchActiveLanguage(lang: AppLanguages): void {
    this.languageStore.setActiveLanguage(appLanguageToLanguage(lang));
  }

}
