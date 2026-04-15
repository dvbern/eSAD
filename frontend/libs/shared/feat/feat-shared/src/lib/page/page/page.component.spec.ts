import {HttpClientTestingModule} from '@angular/common/http/testing';
import {ComponentRef, signal} from '@angular/core';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {provideRouter} from '@angular/router';
import {AufgabenStore} from '../../admin-shared/aufgaben.store';
import {AppUser, BenutzerStore} from '../../benutzer/benutzer/benutzer.store';
import {TimetravelStore} from '../../core/timetravel-store';
import {DeploymentStore} from '../../deployment/deployment.store';
import {LanguageStore, provideTestingTransloco} from '@dvb/util-i18n';
import {NgbOffcanvas} from '@ng-bootstrap/ng-bootstrap';
import {PageComponent} from './page.component';
import {TenantSettingsStore} from "../../tenant/tenant-settings.store";
import {AppLanguages} from "@dvb/data-access-backend";

describe('PageComponent', () => {
  let component: PageComponent;
  let componentRef: ComponentRef<PageComponent>;
  let fixture: ComponentFixture<PageComponent>;

  // @ts-expect-error we mocked just the required things
  const mockBenutzerStore: BenutzerStore = {
    isLoggedIn: signal(true),
    createHasRequiredRoleSig: jest.fn().mockReturnValue(signal(true)),
    requireBenutzer: signal<AppUser>({
      id: '1',
      username: 'user123',
      email: 'user123@dvbern.ch',
      firstName: 'Frida',
      lastName: 'Hunger',
      fullName: 'Frida Hunger',
      kuerzel: 'FH',
      fachRollen: [],
      berechtigungen: [],
    }),
  };

  // @ts-expect-error we mocked just the required things
  const mockAufgabenStore: AufgabenStore = {
    numOfOffeneFaelligeAufgaben: signal(1),
  };


  // @ts-expect-error we mocked just the required things
  const mockTenantSettingStore: TenantSettingsStore = {
    supportedLanguages: signal([AppLanguages.DE, AppLanguages.FR]),
    footerEnabled: signal(true),
    settings: signal(undefined),
  };

  const mockLanguageStore: Partial<LanguageStore> = {
    activeLocale: jest.fn().mockReturnValue('de'),
    setActiveLanguage: jest.fn(),
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PageComponent, HttpClientTestingModule],
      providers: [
        provideTestingTransloco(),
        {
          provide: BenutzerStore,
          useValue: mockBenutzerStore,
        },
        {
          provide: AufgabenStore,
          useValue: mockAufgabenStore,
        },
        {
          provide: DeploymentStore,
          useValue: {
            isTimetravelEnabled: signal(true),
            version: signal('1.0.0'),
          },
        },
        {
          provide: LanguageStore,
          useValue: mockLanguageStore,
        },
        {
          provide: TenantSettingsStore,
          useValue: mockTenantSettingStore,
        },
        provideRouter([]),
        {
          provide: NgbOffcanvas,
          useValue: {},
        },
        TimetravelStore,
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(PageComponent);
    component = fixture.componentInstance;
    componentRef = fixture.componentRef;
    componentRef.setInput('heading', 'ABC');
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
