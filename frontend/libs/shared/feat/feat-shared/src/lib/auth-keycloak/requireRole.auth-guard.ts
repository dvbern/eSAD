/*
 *
 * Copyright (C) 2025 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

import {inject} from '@angular/core';
import {ActivatedRouteSnapshot, CanActivateFn, Router, RouterStateSnapshot, UrlTree} from '@angular/router';
import {BenutzerStore} from '../benutzer/benutzer/benutzer.store';
import {Path} from '../core/path';
import {AppNotificationStore} from '@dvb/util-angular';
import {RoleMatcher} from '@dvb/util-shared';
import {TranslocoService} from '@jsverse/transloco';

export function requireRole(roleMatcher: RoleMatcher): CanActivateFn {
  return async (route: ActivatedRouteSnapshot, state: RouterStateSnapshot) => {
    const authGuard = new RequireRoleAuthGuard(
      roleMatcher,
      inject(Router),
      inject(BenutzerStore),
      inject(TranslocoService),
      inject(AppNotificationStore),
    );
    return authGuard.canActivate(route, state);
  };
}

class RequireRoleAuthGuard {
  constructor(
    readonly roleMatcher: RoleMatcher,
    private readonly router: Router,
    private readonly benutzerStore: BenutzerStore,
    private readonly translationService: TranslocoService,
    private readonly appNotificationStore: AppNotificationStore,
  ) {

  }

  public async canActivate(_route: ActivatedRouteSnapshot, _state: RouterStateSnapshot): Promise<boolean | UrlTree> {
    const isLoggedIn = this.benutzerStore.isLoggedIn();

    // Get the current Benutzer's Berechtigungen from the store
    const userRoles = this.benutzerStore.berechtigungen();

    const isAllowed = this.roleMatcher(userRoles);

    if (isLoggedIn) {
      if (isAllowed) {
        // all good
        return true;
      } else {
        // logged in but missing roles
        console.warn('missing role, redirecting to start');
        return this.router.createUrlTree(
          [Path.NO_ROLES],
          // just for screenshot purposes:
          {queryParams: {missingRole: true}});

      }
    } else {
      // logged out
      console.log('redirecting to login');
      triggerUnauthenticatedNotification(this.appNotificationStore, this.translationService);
      return this.router.navigateByUrl('/info');
    }
  }
}

function triggerUnauthenticatedNotification(
  appNotificationStore: AppNotificationStore,
  translocoService: TranslocoService,
) {
  appNotificationStore.addNotification({
    alertType: 'danger',
    id: crypto.randomUUID(),
    summary: translocoService.translate('common.auth.route.unauthenticated.title'),
    autohide: true,
  });
}

