import {bootstrapApplication} from '@angular/platform-browser';
import {AppComponent} from './app/app.component';
import {appConfig} from './app/app.config';
import * as Sentry from "@sentry/angular";
Sentry.init({
  dsn: "https://3fbe7f2204044f52a0be49f4cf469c08@sentry-relay.dvbern.ch/54",
  integrations: [
    // Registers and configures the Tracing integration,
    // which automatically instruments your application to monitor its
    // performance, including custom Angular routing instrumentation
    Sentry.browserTracingIntegration(),
  ],
  // Set tracesSampleRate to 1.0 to capture 100%
  // of transactions for tracing.
  // We recommend adjusting this value in production
  // Learn more at
  // https://docs.sentry.io/platforms/javascript/configuration/options/#traces-sample-rate
  tracesSampleRate: 0.2,
  // Set `tracePropagationTargets` to control for which URLs trace propagation should be enabled
  // we match all keycloaks and all /api/ URLs except if the contain localhost
  tracePropagationTargets: [
    'https://auth-esad.hemed.ch',
    'https://uat-auth-esc.dvbern.ch',
    'https://dev-auth-esc.apps.mercury.ocp.dvbern.ch/',
    /^(?!.*localhost).*\/api\//
  ],
});

bootstrapApplication(AppComponent, appConfig).catch(err => console.error(err));
