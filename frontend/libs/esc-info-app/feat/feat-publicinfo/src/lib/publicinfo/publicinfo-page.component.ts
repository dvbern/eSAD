import { ChangeDetectionStrategy, Component, OnInit, computed, inject, signal } from '@angular/core';
import {
  DeploymentStore,
  OAuth2ProxyService,
  PageComponent,
  TenantSettingsStore,
  TranslatedEnumViewValuePipe,
} from '@dvb/feat-shared';
import { LoginMethod, LoginOptionJson } from '@dvb/data-access-backend';
import { ButtonComponent } from '@dvb/ui-components';
import { TranslocoPipe } from '@jsverse/transloco';
import {NgOptimizedImage} from "@angular/common";

/**
 * Fallback `rd` for visitors who supplied no valid redirect target. It deliberately stays
 * *relative*: the oauth2-proxy only accepts absolute `rd` values for whitelisted hosts, so every
 * `rd` this page hands to `/oauth2/start` has to be a path.
 *
 * It is NOT the target of the "already authenticated" forward - that one needs an absolute URL,
 * see {@link PublicinfoPageComponent.mainAppUrl}.
 */
export const MAIN_APP_ROOT = '/';

/**
 * Logo of an IdP-branded login button. Width/height are the intrinsic sizes of the asset and are
 * rendered as attributes so the button does not reflow while the image loads.
 */
export interface LoginOptionLogo {
  /** Relative to the app's baseHref - the shared asset folder is published as `img/`. */
  readonly src: string;
  readonly width: number;
  readonly height: number;
}

/**
 * Logo per login method, deliberately kept in the frontend: how an IdP button looks is a pure
 * presentation concern, so it needs neither a field on `LoginOptionJson` nor a DB migration.
 * The colours belong to the same decision and live in `publicinfo-page.component.scss`, keyed by
 * `data-login-method`. Both mirror the Keycloak login theme (hin-styles.css / agov-styles.css) so
 * that the landing page and the Keycloak login page show the same buttons.
 */
const LOGIN_OPTION_LOGOS: Partial<Record<LoginMethod, LoginOptionLogo>> = {
  HIN: { src: 'img/idp-hin.png', width: 60, height: 25 },
  AGOV_ZH: { src: 'img/idp-agov.svg', width: 32, height: 32 },
  BE_LOGIN: { src: 'img/idp-agov.svg', width: 32, height: 32 },
  DVBERN_HEMED_ENTRA: { src: 'img/idp-azure.svg', width: 16, height: 16 },
};

/**
 * Public info page with per-IdP login buttons ("Option 1 hybrid"):
 * - the load-bearing method selection for deep links and session expiry happens on the
 *   Keycloak login page of the tenant realm (server-side, no SPA involvement);
 * - this page is the branded entry point for people who visit it deliberately: it explains
 *   the IdPs and offers one button per configured method, sending `kc_idp_hint` through
 *   `/oauth2/start` so the Keycloak selection page is skipped for that click;
 * - an already-authenticated visitor is forwarded straight into the main app.
 * There is deliberately NO auto-start and NO persisted "last used method" here: expiry and
 * deep links are handled by Keycloak, so this page needs no memory and no guards.
 */
@Component({
  selector: 'feat-publicinfo',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [PageComponent, TranslocoPipe, ButtonComponent, TranslatedEnumViewValuePipe, NgOptimizedImage],
  templateUrl: './publicinfo-page.component.html',
  styleUrl: './publicinfo-page.component.scss',
})
export class PublicinfoPageComponent implements OnInit {
  private readonly tenantSettingsStore = inject(TenantSettingsStore);
  private readonly oauth2ProxyService = inject(OAuth2ProxyService);
  private readonly deploymentStore = inject(DeploymentStore);

  readonly loginOptions = this.tenantSettingsStore.loginOptions;

  /**
   * The identity providers, i.e. everything except {@link LoginMethod.PASSWORD}: those are the
   * branded, equally weighted "one click and you are at your IdP" buttons.
   */
  readonly idpLoginOptions = computed(() =>
    this.loginOptions().filter(option => option.method !== LoginMethod.PASSWORD),
  );

  /**
   * Local username/password is not an IdP but the fallback for people without one, so it is offered
   * separately below the IdPs instead of competing with them in the same list.
   */
  readonly passwordLoginOption = computed(() =>
    this.loginOptions().find(option => option.method === LoginMethod.PASSWORD),
  );

  readonly checking = signal(true);
  readonly authenticated = signal(false);

  private readonly rawRd = this.readQueryParam('rd');

  readonly sanitizedRd = this.sanitizeRedirect(this.rawRd);

  ngOnInit(): void {
    this.oauth2ProxyService.checkAuthentication().subscribe(result => {
      this.checking.set(false);
      if (result === 'authenticated') {
        this.forwardToMainApp();
      }
      // 'unauthenticated' and 'unknown' both render the selection (fail open)
    });
  }

  onSelect(option: LoginOptionJson): void {
    this.oauth2ProxyService.startOAuthFlow(this.sanitizedRd, { kc_idp_hint: option.idpHint });
  }

  logoOf(option: LoginOptionJson): LoginOptionLogo | undefined {
    return LOGIN_OPTION_LOGOS[option.method];
  }

  private sanitizeRedirect(rd: string | null): string {
    return rd !== null && this.isValidRedirect(rd) ? rd : MAIN_APP_ROOT;
  }

  /**
   * Same-origin relative paths only: must start with a single `/`, must not start with `//` or `/\`
   * (protocol-relative / backslash tricks), must reject control characters (browsers strip TAB/LF/CR
   * before URL parsing, turning `/\t/…` into `//…`), and must not be an absolute `http(s)://` URL.
   */
  private isValidRedirect(rd: string): boolean {
    // eslint-disable-next-line no-control-regex
    if (/[\u0000-\u001F\u007F]/.test(rd)) {
      return false;
    }
    if (rd.length === 0 || !rd.startsWith('/')) {
      return false;
    }
    if (rd.startsWith('//') || rd.startsWith('/\\')) {
      return false;
    }
    return !/^https?:\/\//i.test(rd);
  }

  private readQueryParam(name: string): string | null {
    return new URLSearchParams(window.location.search).get(name);
  }

  private forwardToMainApp(): void {
    const target = this.mainAppUrl();
    if (target === undefined) {
      return;
    }
    this.authenticated.set(true);
    this.navigateTo(target);
  }

  /**
   * Absolute URL to forward to, or `undefined` while the deployment config is missing.
   *
   * Resolved against the deployment's `publicUrl` rather than against the origin that happens to
   * serve this page. The landing page lives under its own base href (`/publicapp/`) and, during
   * development, on its own dev server - and there `/` is not the main app but a 302 straight back
   * to this page, so a bare `/` trampolines forever.
   */
  private mainAppUrl(): string | undefined {
    const root = this.mainAppRoot();
    return root === undefined ? undefined : new URL(this.sanitizedRd, root).toString();
  }

  /**
   * `DeploymentStore.publicUrl` throws while the config has not been loaded; turn that into an
   * absent target instead of an error.
   */
  private mainAppRoot(): string | undefined {
    try {
      return this.deploymentStore.publicUrl();
    } catch {
      return undefined;
    }
  }

  /**
   * Performs the actual browser navigation. `replace` rather than assigning to `href`: this page is
   * a trampoline, so it must not stay in the session history.
   */
  private navigateTo(url: string): void {
    globalThis.location.replace(url);
  }
}
