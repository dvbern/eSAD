import {computed, Inject, inject, Injectable, Optional} from '@angular/core';
import {
  AgbResponsability,
  BenutzerService,
  FachRolle,
  OrganisationRollenJson,
  SystemRollenJson
} from '@dvb/data-access-backend';

import {AbstractSignalStore} from '@dvb/util-angular';
import {isNotNullish, requireValue, RoleMatcher} from '@dvb/util-shared';
import * as Sentry from '@sentry/browser';
import {exhaustMap, firstValueFrom, pipe} from 'rxjs';
import {BackendEntityId, ISODateTimeString} from "@dvb/data-access-types";
import {rxMethod} from "@ngrx/signals/rxjs-interop";
import {tapResponse} from "@ngrx/operators";
import {TenantSettingsStore} from "../../tenant/tenant-settings.store";

export interface AppUser {
  id: BackendEntityId;
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  fullName: string;
  kuerzel: string;
  currentOrganisationRollen?: OrganisationRollenJson;
  allOrganisationRollen: OrganisationRollenJson[]; // not used at the moment. later add dropdown in user menu
  systemRollen?: SystemRollenJson;
  timestampAGBAccepted: ISODateTimeString | undefined;
}

export interface BenutzerState {
  user: AppUser | undefined;
  errorMessage: string | undefined;
}

const initialState: BenutzerState = {
  user: undefined,
  errorMessage: undefined,
};

@Injectable({providedIn: 'root'})
export class BenutzerStore extends AbstractSignalStore<BenutzerState> {
  readonly benutzerService = inject(BenutzerService);
  readonly tenantSettingsStore = inject(TenantSettingsStore);

  /**
   * @param testInit - for testing purposes only
   */
  // eslint-disable-next-line @angular-eslint/prefer-inject
  constructor(@Inject('nothing') @Optional() testInit?: BenutzerState) {
    super(testInit ?? initialState);
  }

  // Signals
  isLoggedIn = computed(() => this.state().user !== undefined);
  isLoggedOut = computed(() => this.state().user === undefined);
  benutzer = computed(() => this.state().user);
  readonly #organisationBerechtigungen = computed(() => this.state().user?.currentOrganisationRollen?.berechtigungen ?? []);
  readonly #systemBerechtigungen = computed(() => this.state().user?.systemRollen?.berechtigungen ?? []);
  berechtigungen = computed(() =>
    this.#organisationBerechtigungen().length > 0
    ?  this.#organisationBerechtigungen()
    : this.#systemBerechtigungen());
  readonly #organisationFachrollen = computed(() => this.state().user?.currentOrganisationRollen?.fachRollen ?? []);
  readonly #systemFachrollen = computed(() => this.state().user?.systemRollen?.fachRollen ?? []);
  fachrollen = computed(() =>
    this.#organisationFachrollen().length > 0
      ?  this.#organisationFachrollen()
      : this.#systemFachrollen());
  hasFachrolleSupport = computed(() => this.fachrollen().includes(FachRolle.SUPPORT));
  isAgbAccepted = computed(() => {
    if (this.tenantSettingsStore.agbResponsibility() == AgbResponsability.AGB_MANAGED_BY_USER) {
      return isNotNullish(this.state().user?.timestampAGBAccepted);
    }
    return true; // Durch Arbeitsvertrag akzeptiert
  });
  requireBenutzer = computed(() => requireValue(this.state().user));

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
          principal.benutzerJson.data.organisationRollen,
          principal.benutzerJson.data.systemRollen,
          principal.benutzerJson.timestampAGBAccepted
        );
      })
      .catch(err => {
        // todo ESC-143 decide how to handle errors here, if the user is not logged in what do we do? it should not
        //  happen since we always have a user logged in

        this.onUserAuthenticationError(err instanceof Error ? err.message : JSON.stringify(err));
      });
  }

  onUserAuthenticated(
    id: BackendEntityId,
    username: string,
    email: string,
    firstName: string,
    lastName: string,
    allOrganisationRollen: OrganisationRollenJson[],
    systemRollen: SystemRollenJson | undefined,
    timestampAGBAccepted: ISODateTimeString | undefined,
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
        currentOrganisationRollen: allOrganisationRollen.length > 0 ? allOrganisationRollen[0] : undefined,
        allOrganisationRollen,
        systemRollen,
        timestampAGBAccepted,
      };
      console.debug('BenutzerStore.onUserAuthenticated: user', user);

      Sentry.setUser({email: user.email, id: user.id, username: user.username});

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

  // Factory-Function that returns a computed signal based on the passed argument
  createHasRequiredRoleSig = (matcher: RoleMatcher) =>
    computed(() => {
      return matcher(this.berechtigungen());
    });

  acceptAGB = rxMethod<{ onSuccess: () => void; }>(
    pipe(
      exhaustMap((params) =>
        this.benutzerService.acceptAGB$().pipe(
          tapResponse({
            next: (result) => {
              // patch only the statusAGB on the current user
              this.patchState(state => ({
                user: state.user
                  ? {...state.user, timestampAGBAccepted: result.timestampAGBAccepted}
                  : undefined,
                errorMessage: state.errorMessage,
              }));
              if (isNotNullish(params.onSuccess)) {
                params.onSuccess();
              }
            },
            error: (err) => console.error(err),
          }),
        )
      ),
    ),
  );
}
