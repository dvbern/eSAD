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

import {
  HttpErrorResponse,
  HttpEvent,
  HttpHandlerFn,
  HttpInterceptorFn,
  HttpRequest,
  HttpStatusCode,
} from '@angular/common/http';
import {inject} from '@angular/core';
import {LogFactory} from '@dvb/util-shared';

import {catchError, Observable, switchMap, throwError} from 'rxjs';
import {BenutzerStore} from '../benutzer/benutzer.store';
import {OAuth2ProxyService} from '../oauth2-proxy-service/oauth2-proxy.service';

const LOG = LogFactory.createLog('authSessionExpiredInterceptorFn');

export function authSessionExpiredInterceptorFn(): HttpInterceptorFn {
  return (req: HttpRequest<unknown>, next: HttpHandlerFn): Observable<HttpEvent<unknown>> => {
    const benutzerStore = inject(BenutzerStore);
    const oauth2Service = inject(OAuth2ProxyService);
    // Prevent intercepting OAuth-related requests (e.g., token refresh, auth chec) to avoid infinite loops
    if (req.url.includes(oauth2Service.OAUTH2_PROXY_BASEURL)) {
      LOG.debug('ignoring request that seems to be going to  OAuth2 proxy ', req.url);
      return next(req);
    }

    return next(req).pipe(
      catchError((exception: unknown) => {
        if (exception instanceof HttpErrorResponse && exception.status === HttpStatusCode.Unauthorized) {
          LOG.info('Session is probably expired. Checking with proxy...');

          return oauth2Service.isAuthenticated().pipe(
            switchMap((isAuthenticated) => {
              if (!isAuthenticated) {
                LOG.info('User is not authenticated. Triggering OAuth login...');
                benutzerStore.onUserLoggedOut();
                oauth2Service.signIn(window.location.href);
              } else {
                LOG.info('User is authenticated according to oauth-proxy, relogin will not be triggered  ');
              }

              // If still authenticated, rethrow the error
              return throwError(() => exception);
            })
          );
        }
        LOG.debug("Not an HTTP 401 error, so we don't care about it", exception);
        // Re-throw the errors
        return throwError(() => exception);
      }),
    );
  };
}
