import {Injectable} from '@angular/core';
import {patchState, signalStore, withMethods, withState} from '@ngrx/signals';
import {delay, map, of} from 'rxjs';
import {AppNotification} from './app-notification';

export interface NotificationState {
  notifications: AppNotification[];
}

const initialState: NotificationState = {
  notifications: [],
};

export const AUTOHIDE_MS = 6000; // very long for accessibility
export const AUTOHIDE_MS_LONG = 15000; // very very long for accessibility

@Injectable({providedIn: 'root'})
export class AppNotificationStore extends signalStore(
  {protectedState: false},
  // withDevtools('notifications'),
  withState(initialState),
  withMethods((store) => {
    function addNotification(notification: AppNotification): void {
      patchState(store, {
        notifications: [...store.notifications(), notification],
      });
      if (notification.autohide) {
        of(1).pipe(
          delay(notification.autohideMs ?? AUTOHIDE_MS),
          map(() =>
            removeNotification(notification),
          ),
        ).subscribe();
      }
    }

    function removeNotification(notification: AppNotification): void {
      patchState(store, {
        notifications: store.notifications().filter(n => n !== notification),
      })
    }

    function addSuccessNotification(summary: string): void {
      addNotification({
        alertType: 'success',
        id: crypto.randomUUID(),
        summary: summary,
        autohide: true,
      });
    }
    function addDangerNotification(summary: string): void {
      addNotification({
        alertType: 'danger',
        id: crypto.randomUUID(),
        summary: summary,
        autohide: false,
      });
    }
    return {addNotification, removeNotification, addSuccessNotification, addDangerNotification};
  }),
) {

}
