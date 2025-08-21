import {ErrorHandler, inject, Injectable, signal} from '@angular/core';
import {SentryErrorHandler} from '@sentry/angular';
import {AppNotificationStore} from '../notifications';
import {convertUnknownAppErrorToNotification} from './app-error-transformer';

@Injectable({ providedIn: 'root' })
export class AppErrorHandlerService extends SentryErrorHandler implements ErrorHandler {
  readonly #errors = signal<unknown[]>([]);
  readonly errors = this.#errors.asReadonly();
  readonly #notificationStore = inject(AppNotificationStore);

  constructor() {
    // Pass options to Sentry's ErrorHandler here
    super();
  }

  override handleError(error: unknown): void {
    // FIXME: starter: implement smarter:
    //  * Create your own global error component
    //  * maybe call Sentry or similar
    //  FYI: console.error is the default Angular implementation
    console.error(error);
    this.#notificationStore.addNotification(convertUnknownAppErrorToNotification(error));
    this.#errors.update(errors => [...errors, error]);
    // Call Sentry's error handler to capture errors
    super.handleError(error);
  }
}
