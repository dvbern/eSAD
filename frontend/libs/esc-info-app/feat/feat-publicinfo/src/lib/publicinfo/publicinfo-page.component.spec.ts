import { ComponentFixture, TestBed } from '@angular/core/testing';
import { signal } from '@angular/core';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { provideTestingTransloco } from '@dvb/util-i18n';
import { DeploymentStore, OAuth2ProxyService, TenantSettingsStore } from '@dvb/feat-shared';
import { LoginMethod, LoginOptionJson } from '@dvb/data-access-backend';
import { of } from 'rxjs';
import type { Mock } from 'vitest';
import { MAIN_APP_ROOT, PublicinfoPageComponent } from './publicinfo-page.component';

function setUrl(pathAndQuery: string): void {
  window.history.pushState({}, '', pathAndQuery);
}

describe('PublicinfoPageComponent', () => {
  let fixture: ComponentFixture<PublicinfoPageComponent>;
  let component: PublicinfoPageComponent;

  let loginOptions: ReturnType<typeof signal<LoginOptionJson[]>>;
  let mockTenantSettingsStore: TenantSettingsStore;

  let checkAuthenticationResult: 'authenticated' | 'unauthenticated' | 'unknown';
  let startOAuthFlow: Mock;
  let mockOAuth2ProxyService: OAuth2ProxyService;
  /** `undefined` stands for "deployment config not loaded". */
  let publicUrl: string | undefined;
  let mockDeploymentStore: DeploymentStore;

  const threeOptions: LoginOptionJson[] = [
    { method: LoginMethod.PASSWORD, idpHint: '', order: 0 },
    { method: LoginMethod.AGOV_ZH, idpHint: 'agov', order: 1 },
    { method: LoginMethod.HIN, idpHint: 'hin', order: 2 },
  ];

  beforeEach(() => {
    loginOptions = signal<LoginOptionJson[]>(threeOptions);
    // @ts-expect-error we mocked just the required things
    mockTenantSettingsStore = {
      loginOptions,
      settings: signal(undefined),
      supportedLanguages: signal([]),
      footerEnabled: signal(true),
      zentraleOrganisationEnabled: signal(true),
    };

    publicUrl = 'https://esc.example.ch/';
    // partial mock - publicUrl is a plain function that throws while the config is "not loaded"
    mockDeploymentStore = {
      publicUrl: () => {
        if (publicUrl === undefined) {
          throw new Error('deployment config not loaded');
        }
        return publicUrl;
      },
      version: signal('1.0.0'),
      isTimetravelEnabled: signal(false),
    } as unknown as DeploymentStore;

    checkAuthenticationResult = 'unauthenticated';
    startOAuthFlow = vi.fn();
    // @ts-expect-error we mocked just the required things
    mockOAuth2ProxyService = {
      checkAuthentication: vi.fn(() => of(checkAuthenticationResult)),
      startOAuthFlow,
    };
  });

  async function createComponent(): Promise<void> {
    // some tests create a second component with different inputs - the TestBed may not be
    // reconfigured once it has instantiated one, so start from a clean slate every time
    TestBed.resetTestingModule();
    await TestBed.configureTestingModule({
      imports: [PublicinfoPageComponent],
      providers: [
        provideTestingTransloco(),
        provideRouter([]),
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: TenantSettingsStore, useValue: mockTenantSettingsStore },
        { provide: DeploymentStore, useValue: mockDeploymentStore },
        {
          provide: OAuth2ProxyService,
          useValue: {
            ...mockOAuth2ProxyService,
            checkAuthentication: vi.fn(() => of(checkAuthenticationResult)),
          },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(PublicinfoPageComponent);
    component = fixture.componentInstance;
  }

  /** The branded identity provider buttons, in the configured order. */
  function idpButtons(): HTMLButtonElement[] {
    return Array.from(fixture.nativeElement.querySelectorAll('.publicinfo__login-button'));
  }

  function hinButton(): HTMLButtonElement {
    return idpButtons().filter(button => button.getAttribute('data-login-method') === LoginMethod.HIN)[0];
  }

  /** The username/password fallback, which lives outside the IdP list. */
  function passwordButton(): HTMLButtonElement | null {
    return fixture.nativeElement.querySelector('.publicinfo__password button[svuiButton]');
  }

  /**
   * The testing transloco loader resolves asynchronously, so a single change detection renders the
   * structure but leaves the `transloco` pipes empty - tests asserting on text have to wait.
   */
  async function renderTranslations(): Promise<void> {
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();
  }

  describe('rendering the login options', () => {
    beforeEach(async () => {
      setUrl('/');
      await createComponent();
      fixture.detectChanges();
    });

    it('renders one button per IdP option, in order, with translated labels', () => {
      const buttons = idpButtons();
      expect(buttons.length).toBe(2);
      expect(buttons[0].textContent).toContain('LoginMethod.AGOV_ZH');
      expect(buttons[1].textContent).toContain('LoginMethod.HIN');
    });

    it('starts the flow with the correct kc_idp_hint on click, including the empty string', () => {
      passwordButton()?.click();

      expect(startOAuthFlow).toHaveBeenCalledWith(MAIN_APP_ROOT, { kc_idp_hint: '' });
    });

    it('passes the non-empty idpHint for other login methods', () => {
      idpButtons()[0].click();

      expect(startOAuthFlow).toHaveBeenCalledWith(MAIN_APP_ROOT, { kc_idp_hint: 'agov' });
    });

    it('never auto-starts without a click, even with a single configured option', async () => {
      loginOptions.set([{ method: LoginMethod.AGOV_ZH, idpHint: 'agov', order: 0 }]);
      await createComponent();
      fixture.detectChanges();

      // the selection is always rendered - deep links and expiry are Keycloak's job (Option 1 hybrid)
      expect(startOAuthFlow).not.toHaveBeenCalled();
      expect(idpButtons().length).toBe(1);
    });
  });

  describe('username/password as the fallback below the IdPs', () => {
    beforeEach(() => {
      setUrl('/');
    });

    it('offers it outside the IdP list, with its own lead text and a secondary button', async () => {
      await createComponent();
      await renderTranslations();

      const block = fixture.nativeElement.querySelector('.publicinfo__password');
      expect(block.textContent).toContain('publicinfo.password.lead');
      const button = passwordButton();
      expect(button?.textContent).toContain('publicinfo.password.action');
      expect(button?.classList).toContain('btn-secondary');
      // it must not appear a second time among the branded buttons
      expect(idpButtons().map(b => b.getAttribute('data-login-method')))
        .toEqual([LoginMethod.AGOV_ZH, LoginMethod.HIN]);
    });

    it('separates it from the IdP buttons', async () => {
      await createComponent();
      await renderTranslations();

      expect(fixture.nativeElement.querySelector('.publicinfo__separator').textContent)
        .toContain('publicinfo.or');
    });

    it('stacks the lead above the button so it spans the column like the IdP buttons', async () => {
      await createComponent();
      fixture.detectChanges();

      // a flex row would shrink the button to its caption width
      const row = passwordButton()?.parentElement;
      expect(row?.classList).toContain('flex-column');
      // and the caption is centred in the wide button, as in the branded ones
      expect(passwordButton()?.querySelector('.publicinfo__login-button-content')).not.toBeNull();
    });

    it('drops the separator when it is the only configured method', async () => {
      loginOptions.set([{ method: LoginMethod.PASSWORD, idpHint: '', order: 0 }]);
      await createComponent();
      fixture.detectChanges();

      // nothing to separate from, so the "oder" would be dangling
      expect(fixture.nativeElement.querySelector('.publicinfo__separator')).toBeNull();
      expect(passwordButton()).not.toBeNull();
    });

    it('renders nothing password-related when the tenant does not offer it', async () => {
      loginOptions.set([{ method: LoginMethod.HIN, idpHint: 'hin', order: 0 }]);
      await createComponent();
      fixture.detectChanges();

      expect(fixture.nativeElement.querySelector('.publicinfo__password')).toBeNull();
      expect(passwordButton()).toBeNull();
    });
  });

  describe('IdP branding of the buttons', () => {
    beforeEach(async () => {
      setUrl('/');
      await createComponent();
      fixture.detectChanges();
    });

    it('marks every button with its login method so the styles can key off it', () => {
      const buttons = idpButtons();
      expect(buttons[0].getAttribute('data-login-method')).toBe(LoginMethod.AGOV_ZH);
      expect(buttons[1].getAttribute('data-login-method')).toBe(LoginMethod.HIN);
      expect(passwordButton()?.getAttribute('data-login-method')).toBe(LoginMethod.PASSWORD);
    });

    it('renders the corporate logo for HIN and AGOV', () => {
      const buttons = idpButtons();
      expect(buttons[0].querySelector('img')?.getAttribute('src')).toBe('img/idp-agov.svg');
      expect(buttons[1].querySelector('img')?.getAttribute('src')).toBe('img/idp-hin.png');
    });

    it('renders the corporate logo for DVBERN_HEMED_ENTRA', async () => {
      loginOptions.set([{ method: LoginMethod.DVBERN_HEMED_ENTRA, idpHint: 'dvbern-hemed-entra-client', order: 0 }]);
      await createComponent();
      fixture.detectChanges();

      const button = idpButtons()[0];
      const logo = button.querySelector('img');
      expect(logo?.getAttribute('src')).toBe('img/idp-azure.svg');
      expect(logo?.getAttribute('width')).toBe('16');
      expect(logo?.getAttribute('height')).toBe('16');
    });

    it('renders no logo for methods without corporate design, such as PASSWORD', () => {
      expect(passwordButton()?.querySelector('img')).toBeNull();
    });

    it('hides the decorative logo from assistive technology', () => {
      const logo = hinButton().querySelector('img');
      expect(logo?.getAttribute('alt')).toBe('');
      expect(logo?.getAttribute('aria-hidden')).toBe('true');
    });

    it('sizes the logo up front so the button does not reflow while it loads', () => {
      const logo = hinButton().querySelector('img');
      expect(logo?.getAttribute('width')).toBe('60');
      expect(logo?.getAttribute('height')).toBe('25');
    });

    it('keeps the translated label next to the logo', () => {
      expect(hinButton().textContent).toContain('LoginMethod.HIN');
    });

    it('groups logo and label in one content element so the styles can align the pair', () => {
      // HIN centres the pair, AGOV left-aligns it - both need the logo and the label in one box
      const content = hinButton().querySelector('.publicinfo__login-button-content');
      expect(content?.querySelector('img')).not.toBeNull();
      expect(content?.querySelector('.publicinfo__login-button-label')).not.toBeNull();
    });
  });

  describe('rd sanitising', () => {
    it('accepts a relative path with query and hash', async () => {
      setUrl('/?rd=%2Fanlass%2F123%3Fx%3D1%23y');
      await createComponent();
      fixture.detectChanges();
      expect(component.sanitizedRd).toBe('/anlass/123?x=1#y');
    });

    it('rejects a protocol-relative url (//evil.com)', async () => {
      setUrl('/?rd=%2F%2Fevil.com');
      await createComponent();
      fixture.detectChanges();
      expect(component.sanitizedRd).toBe(MAIN_APP_ROOT);
    });

    it('rejects a backslash-prefixed url (/\\evil.com)', async () => {
      setUrl('/?rd=%2F%5Cevil.com');
      await createComponent();
      fixture.detectChanges();
      expect(component.sanitizedRd).toBe(MAIN_APP_ROOT);
    });

    it('rejects an absolute http(s) url', async () => {
      setUrl('/?rd=https%3A%2F%2Fevil.com');
      await createComponent();
      fixture.detectChanges();
      expect(component.sanitizedRd).toBe(MAIN_APP_ROOT);
    });

    it('rejects a redirect containing a TAB', async () => {
      setUrl('/?rd=%2F%09%2Fevil.com');
      await createComponent();
      fixture.detectChanges();
      expect(component.sanitizedRd).toBe(MAIN_APP_ROOT);
    });

    it('rejects a redirect containing an LF', async () => {
      setUrl('/?rd=%2F%0A%2Fevil.com');
      await createComponent();
      fixture.detectChanges();
      expect(component.sanitizedRd).toBe(MAIN_APP_ROOT);
    });

    it('rejects a redirect containing a CR', async () => {
      setUrl('/?rd=%2F%0D%2Fevil.com');
      await createComponent();
      fixture.detectChanges();
      expect(component.sanitizedRd).toBe(MAIN_APP_ROOT);
    });

    it('falls back to / when rd is absent', async () => {
      setUrl('/');
      await createComponent();
      fixture.detectChanges();
      expect(component.sanitizedRd).toBe(MAIN_APP_ROOT);
    });

    it('falls back to / when rd is empty', async () => {
      setUrl('/?rd=');
      await createComponent();
      fixture.detectChanges();
      expect(component.sanitizedRd).toBe(MAIN_APP_ROOT);
    });

    it('threads a valid rd into the button flow', async () => {
      setUrl('/?rd=%2Fanlass%2F123');
      await createComponent();
      fixture.detectChanges();

      hinButton().click();

      expect(startOAuthFlow).toHaveBeenCalledWith('/anlass/123', { kc_idp_hint: 'hin' });
    });
  });

  describe('session check', () => {
    it('forwards to the configured main app root, not to the origin of this page', async () => {
      setUrl('/');
      checkAuthenticationResult = 'authenticated';
      await createComponent();

      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      const navigateToSpy = vi.spyOn(component as any, 'navigateTo').mockImplementation(() => undefined);
      fixture.detectChanges();

      expect(navigateToSpy).toHaveBeenCalledWith('https://esc.example.ch/');
    });

    it('forwards to the deep link in rd, resolved against the configured root', async () => {
      setUrl('/?rd=%2Fanlass%2F123%3Fx%3D1');
      checkAuthenticationResult = 'authenticated';
      await createComponent();

      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      const navigateToSpy = vi.spyOn(component as any, 'navigateTo').mockImplementation(() => undefined);
      fixture.detectChanges();

      expect(navigateToSpy).toHaveBeenCalledWith('https://esc.example.ch/anlass/123?x=1');
    });

    it('renders the selection instead of guessing a target when the deployment config is missing', async () => {
      setUrl('/');
      checkAuthenticationResult = 'authenticated';
      publicUrl = undefined;
      await createComponent();

      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      const navigateToSpy = vi.spyOn(component as any, 'navigateTo').mockImplementation(() => undefined);
      fixture.detectChanges();

      expect(navigateToSpy).not.toHaveBeenCalled();
      expect(idpButtons().length).toBe(2);
    });

    it('renders the selection when checkAuthentication resolves "unauthenticated"', async () => {
      setUrl('/');
      checkAuthenticationResult = 'unauthenticated';
      await createComponent();
      fixture.detectChanges();

      expect(idpButtons().length).toBe(2);
      expect(passwordButton()).not.toBeNull();
    });

    it('renders the selection when checkAuthentication resolves "unknown" (fail open)', async () => {
      setUrl('/');
      checkAuthenticationResult = 'unknown';
      await createComponent();
      fixture.detectChanges();

      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      const navigateToSpy = vi.spyOn(component as any, 'navigateTo').mockImplementation(() => undefined);
      expect(navigateToSpy).not.toHaveBeenCalled();

      expect(idpButtons().length).toBe(2);
      expect(passwordButton()).not.toBeNull();
    });
  });
});
