import {registerLocaleData} from '@angular/common';
import {HttpClient, provideHttpClient, withInterceptors} from '@angular/common/http';
import localeDeCH from '@angular/common/locales/de-CH';
import localeDeCHExtra from '@angular/common/locales/extra/de-CH';
import localeFrCHExtra from '@angular/common/locales/extra/fr-CH';
import localeFrCH from '@angular/common/locales/fr-CH';
import {
  ApplicationConfig,
  importProvidersFrom,
  inject,
  LOCALE_ID,
  provideAppInitializer,
  provideZonelessChangeDetection,
} from '@angular/core';
import {provideAnimationsAsync} from '@angular/platform-browser/animations/async';
import {
  PreloadAllModules,
  provideRouter,
  Route,
  Router,
  withComponentInputBinding,
  withInMemoryScrolling,
  withPreloading,
  withRouterConfig,
} from '@angular/router';
import {appErrorInterceptor} from '@dvb/util-angular';

import {
  acceptLanguageInterceptorFn,
  LanguageStore,
  provideAppTransloco,
} from '@dvb/util-i18n';
import {hasBenutzerOptionReadBerechtigung, hasSchuleViewBerechtigung} from '@dvb/util-shared';
import {TranslocoService} from '@jsverse/transloco';
import {provideNgIconLoader, withCaching} from '@ng-icons/core';
import * as Sentry from '@sentry/angular';
import {lastValueFrom} from 'rxjs';
import {BenutzerOptionsStore} from '../admin-shared/benutzer-options.store';
import {authSessionExpiredInterceptorFn} from '../benutzer/auth-interceptor/auth-session-exipred.interceptor';
import {BenutzerStore} from '../benutzer/benutzer/benutzer.store';
import {DeploymentStore} from '../deployment/deployment.store';
import {TenantSettingsStore} from '../tenant/tenant-settings.store';
import {provideAppErrorHandler} from './provideAppErrorHandler';
import {TimetravelStore} from './timetravel-store';
import {TranslocoMultiLoader} from './TranslocoMultiLoader';
import {SchulenOptionsStore} from "../admin-shared/schulen-options.store";

// needed for number and date formatting
registerLocaleData(localeDeCH, 'de-CH', localeDeCHExtra);
registerLocaleData(localeDeCH, 'de', localeDeCHExtra);
registerLocaleData(localeFrCH, 'fr-CH', localeFrCHExtra);
registerLocaleData(localeFrCH, 'fr', localeFrCHExtra);

export function provideCore(appRoutes: Route[]): ApplicationConfig['providers'] {
  return [
    provideAppErrorHandler(),
    provideAnimationsAsync(),
    provideZonelessChangeDetection(),
    provideRouter(
      appRoutes,
      // withDebugTracing(),

      // FIXME: starter: preloading: after app is ready, the lazy modules are loaded. Can be customized,
      //   e.g. using RouterPreloader or ngx-quicklink
      withPreloading(PreloadAllModules),

      withRouterConfig({
        onSameUrlNavigation: 'reload',
      }),
      // Mit dieser Option werden Parameter, welche im Routing ins data geschrieben werden,
      // direkt zu InputSignals in der Component, siehe z.B. EinladungNotizPageComponent
      withComponentInputBinding(),

      // FIXME: starter: decide if this is needed for your project:
      //  disable initial navigation: otherwise, authGuard starts before Keycloak is initialized
      //withDisabledInitialNavigation(),
      //withEnabledBlockingInitialNavigation(),

      withInMemoryScrolling({
        anchorScrolling: 'enabled',
        scrollPositionRestoration: 'top',
      }),
    ),
    provideHttpClient(
      withInterceptors([
        appErrorInterceptor(), // note that the order is important, msg outflow is 'reversed'
        acceptLanguageInterceptorFn, // adds the selected language as first param to the AcceptLanguage header
        authSessionExpiredInterceptorFn(),

        // FIXME: check backend version
        // DeploymentConfigInterceptor,
      ]),
    ),
    // FIXME: implement helper for i18n title strategy
    //  that makes reading a title (or parts of it) from a store easier
    provideAppTransloco(TranslocoMultiLoader),

    // modules which don't support Angular Standalone APIs yet
    importProvidersFrom([
      // FooModule
    ]),
    {
      provide: Sentry.TraceService,
      deps: [Router],
    },
    // init (has to be last, order matters!)
    // time travel
    TimetravelStore,

    // app init
    provideAppInitializer(() => {
      const initializerFn = appInitializer();
      return initializerFn();
    }),
    // Locale_ID
    {
      provide: LOCALE_ID,
      useFactory: () => inject(LanguageStore).activeLocale(),
    },

    // Custom Icons (relative URL, so that the <base href> is respected)
    provideNgIconLoader(name => {
      const http = inject(HttpClient);
      return http.get(`icons/${name}.svg`,
        {responseType: 'text'},
      );
    }, withCaching()),

  ];
}

function appInitializer(
  languageStore = inject(LanguageStore),
  transloco = inject(TranslocoService),
  deploymentStore = inject(DeploymentStore),
  tenantSettingsStore = inject(TenantSettingsStore),
  benutzerStore = inject(BenutzerStore),
  benutzerOptionsStore = inject(BenutzerOptionsStore),
  timetravelStore = inject(TimetravelStore),
  schulenOptionsStore = inject(SchulenOptionsStore),
) {
  return async () => {
    await languageStore.init();
    transloco.setActiveLang(languageStore.activeLocale());
    await deploymentStore.init();
    await benutzerStore.init();
    if (benutzerStore.createHasRequiredRoleSig(hasBenutzerOptionReadBerechtigung())()) {
      benutzerOptionsStore.loadAllBenutzerOptions();
    }
    if (benutzerStore.createHasRequiredRoleSig(hasSchuleViewBerechtigung())()) {
      schulenOptionsStore.loadSchulenOptions();
    }

    await tenantSettingsStore.init();

    // load current timetravel state
    timetravelStore.loadNow();

    // wait for the translation to load:
    await lastValueFrom(transloco.load(languageStore.activeLocale()));
  };
}
