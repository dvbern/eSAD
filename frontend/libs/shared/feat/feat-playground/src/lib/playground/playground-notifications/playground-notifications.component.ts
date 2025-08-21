import {ChangeDetectionStrategy, Component, inject} from '@angular/core';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {DeveloperService} from '@dvb/data-access-backend';
import {ButtonComponent, CardComponent} from '@dvb/ui-components';
import {AppErrorHandlerService, AppNotificationStore, AUTOHIDE_MS_LONG} from '@dvb/util-angular';
import {tapResponse} from '@ngrx/operators';
import {first, of} from 'rxjs';

@Component({
    selector: 'feat-playground-notifications',
    templateUrl: './playground-notifications.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [
        ButtonComponent,
        FormsModule,
        ReactiveFormsModule,
        CardComponent,
    ]
})
export class PlaygroundNotificationsComponent {
  appNotificationStore = inject(AppNotificationStore);
  developerService = inject(DeveloperService);
  readonly appErrorHandlerService = inject(AppErrorHandlerService);

  counter = 1;

  simulateBackendFailure(): void {
    this.appNotificationStore.addNotification({
      alertType: 'danger',
      id: 'playgrounderror' + this.counter++,
      summary: 'Nice failure description from backend',
      autohide: false,
    });
  }

  simulateLongBackendFailure(): void {
    this.appNotificationStore.addNotification({
      alertType: 'danger',
      id: 'playgrounderror' + this.counter++,
      summary: 'Nice failure description from backend with some more information to see how it will be displayed because we do not want it to look absolutely horrible',
      autohide: false,
    });
  }

  simulateBackendValidation(): void {
    this.appNotificationStore.addNotification({
      alertType: 'danger',
      id: 'playgrounderror' + this.counter++,
      summary: 'Backend found a validation failure: blabla',
      autohide: false,
    });
  }

  simulateSuccess(): void {
    this.appNotificationStore.addNotification({
      alertType: 'success',
      id: 'playgrounderror' + this.counter++,
      summary: 'Successfully updated something',
      autohide: true,
    });
  }

  simulateLongSuccess(): void {
    this.appNotificationStore.addNotification({
      alertType: 'success',
      id: 'playgrounderror' + this.counter++,
      summary: 'Successfully done something and I will give you some long explanation here and therefore the message stays longer than normal',
      autohide: true,
      autohideMs: AUTOHIDE_MS_LONG,
    });
  }

  triggerBackendFailure(): void {
    this.developerService.triggerFailureMessage$({exampleMsg: {msg: 'This is a failure message from the backend'}})
      .pipe(
        first(),
        tapResponse({
          next: (response) => alert('success' + response.summary),
          error: (error) => this.appErrorHandlerService.handleError(error),
        }))
      .subscribe();
  }

  triggerBackendValidation(): void {
    this.developerService.triggerValidationMessage$({exampleMsg: {msg: 'This is a validation message from the backend'}})
      .pipe(
        first(),
        tapResponse({
          next: (response) => alert('success' + response.violations),
          error: (error) => this.appErrorHandlerService.handleError(error),
        }))
      .subscribe();
  }

  triggerBackendRuntimeException(): void {
    this.developerService.triggerRuntimeException$({exampleMsg: {msg: 'This is a runtime exception from the backend'}})
      .pipe(
        first(),
        tapResponse({
          next: (response) => alert('success' + response.summary),
          error: (error) => this.appErrorHandlerService.handleError(error),
        }))
      .subscribe();
  }

  simulateHttpError(): void {
    of({
      message: 'Kein Zugriff',
      error: 'bla',
      ok: false,
      status: 401,
    })
      .pipe(first())
      .subscribe((response) => this.appErrorHandlerService.handleError(response));
  }

}
