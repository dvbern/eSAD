import {computed, Inject, inject, Injectable, Optional} from '@angular/core';
import { BenutzerService, Berechtigung, FachRolle } from '@dvb/data-access-backend';

import { AbstractSignalStore } from '@dvb/util-angular';
import { requireValue, RoleMatcher } from '@dvb/util-shared';
import * as Sentry from '@sentry/browser';
import { firstValueFrom } from 'rxjs';

export interface AppUser {
  id: string;
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  fullName: string;
  kuerzel: string;
  fachRollen: FachRolle[];
  berechtigungen: Berechtigung[];
}

export interface BenutzerState {
  user: AppUser | undefined;
  errorMessage: string | undefined;
}

const initialState: BenutzerState = {
  user: undefined,
  errorMessage: undefined,
};

@Injectable({ providedIn: 'root' })
export class BenutzerStore extends AbstractSignalStore<BenutzerState> {
  readonly benutzerService = inject(BenutzerService);

  /**
   * @param testInit - for testing purposes only
   */
  // eslint-disable-next-line @angular-eslint/prefer-inject
  constructor(@Inject('nothing') @Optional() testInit?: BenutzerState) {
    super(testInit ?? initialState);
  }

  async init(): Promise<void> {
    // nothing to do here
    return firstValueFrom(this.benutzerService.getMyBenutzerData$())
      .then(principal => {
        console.log('handleAuthSuccess', principal);

        this.onUserAuthenticated(
          requireValue(principal.benutzerJson.id),
          requireValue(principal.benutzerJson.data.username),
          requireValue(principal.benutzerJson.data.email),
          requireValue(principal.benutzerJson.data.vorname),
          requireValue(principal.benutzerJson.data.name),
          requireValue(principal.benutzerJson.data.fachRollen),
          requireValue(principal.berechtigungen),
        );
      })
      .catch(err => {
        // todo ESC-143 decide how to handle errors here, if the user is not logged in what do we do? it should not
        //  happen since we always have a user logged in

        this.onUserAuthenticationError(err instanceof Error ? err.message : JSON.stringify(err));
      });
  }

  onUserAuthenticated(
    id: string,
    username: string,
    email: string,
    firstName: string,
    lastName: string,
    fachRollen: FachRolle[],
    berechtigungen: Berechtigung[],
  ) {
    const fullName = `${firstName} ${lastName}`;
    const kuerzel = this.buildKuerzel(firstName, lastName);

    this.patchState(_state => {
      const user: AppUser = {
        id,
        username,
        email,
        firstName,
        lastName,
        fullName,
        kuerzel,
        fachRollen,
        berechtigungen,
      };
      console.debug('BenutzerStore.onUserAuthenticated: user', user);

      Sentry.setUser({ email: user.email, id: user.id, username: user.username });

      return {
        user,
        errorMessage: undefined,
      };
    });
  }

  private buildKuerzel(firstName: string, lastName: string): string {
    return `${firstName.charAt(0)}${lastName.charAt(0)}`.toUpperCase();
  }

  onUserAuthenticationError(errorMessage: string) {
    this.patchState(_state => {
      return {
        user: undefined,
        errorMessage,
      };
    });
  }

  /**
   * <strong>Important:</strong> Use KeykloakService.logout() to logout the user.
   * This method is only a callback for keycloak to update the state.
   */
  onUserLoggedOut() {
    this.patchState(_state => {
      return {
        user: undefined,
        errorMessage: undefined,
      };
    });
  }

  isLoggedIn = computed(() => this.state().user !== undefined);
  isLoggedOut = computed(() => this.state().user === undefined);

  benutzer = computed(() => this.state().user);

  berechtigungen = computed(() => this.state().user?.berechtigungen ?? []);

  requireBenutzer = computed(() => requireValue(this.state().user));

  // Factory-Function that returns a computed signal based on the passed argument
  createHasRequiredRoleSig = (matcher: RoleMatcher) =>
    computed(() => {
      const berechtigungen = this.state().user?.berechtigungen ?? [];
      return matcher(berechtigungen);
    });
}
