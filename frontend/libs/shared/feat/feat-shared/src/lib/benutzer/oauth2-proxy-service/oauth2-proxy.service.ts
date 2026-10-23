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

import { HttpErrorResponse, HttpClient, HttpStatusCode } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { asyncScheduler, Observable, of, scheduled, timeout } from 'rxjs';
import { catchError, map } from 'rxjs/operators';

export interface UserInfo {
  email: string;
}

/**
 * Result of a three-state authentication probe against the oauth2-proxy `/oauth2/auth` endpoint.
 * `'unknown'` covers every case where the probe itself is inconclusive (timeout, network error,
 * unexpected status code) - it must never be treated the same as `'unauthenticated'`.
 */
export type AuthCheckResult = 'authenticated' | 'unauthenticated' | 'unknown';

const AUTH_CHECK_TIMEOUT_MS = 3000; // 3 seconds timeout for auth check


/**
 * This is a simple angular http service to interact with the OAuth2 proxy. Normally this will only be used to
 * check if the user is authenticated and to send him to the login if he is not
 * https://oauth2-proxy.github.io/oauth2-proxy/features/endpoints/
 */
// noinspection UnnecessaryLocalVariableJS
@Injectable({
  providedIn: 'root'
})
export class OAuth2ProxyService {

  public readonly OAUTH2_PROXY_BASEURL = '/oauth2';
  private http: HttpClient = inject(HttpClient);


  /**
   * Redirects user to the sign-in page (which doubles as sign-out by clearing cookies)
   * @param redirectUri Optional URI to redirect to after successful authentication
   */
  signIn(redirectUri?: string): void {

    const url = redirectUri
      ? `${this.OAUTH2_PROXY_BASEURL}/sign_in?rd=${encodeURIComponent(redirectUri)}`
      : `${this.OAUTH2_PROXY_BASEURL}/sign_in`;
    this.navigateTo(url);
  }

  /**
   * Signs out the user by clearing the session cookie
   * @param redirectUri Optional URI to redirect to after signing out
   */
  signOut(redirectUri?: string): void {
    const url = redirectUri
      ? `${this.OAUTH2_PROXY_BASEURL}/sign_out?rd=${encodeURIComponent(redirectUri)}`
      : `${this.OAUTH2_PROXY_BASEURL}/sign_out`;
    this.navigateTo(url);
  }

  /**
   * Starts the OAuth cycle.
   * Builds the URL with {@link URLSearchParams} so that empty string param values (e.g. the explicitly
   * empty `kc_idp_hint` used for the PASSWORD login method) are preserved as `key=` rather than being
   * dropped - this is required by Keycloak's identity-provider-redirector semantics.
   * @param redirectUri Optional URI to redirect to after authentication (added as `rd`)
   * @param params Optional additional query parameters (e.g. `{ kc_idp_hint: '' }`)
   */
  startOAuthFlow(redirectUri?: string, params?: Record<string, string>): void {
    const searchParams = new URLSearchParams();
    if (redirectUri) {
      searchParams.set('rd', redirectUri);
    }
    if (params) {
      for (const [key, value] of Object.entries(params)) {
        searchParams.set(key, value);
      }
    }
    const query = searchParams.toString();
    const url = query
      ? `${this.OAUTH2_PROXY_BASEURL}/start?${query}`
      : `${this.OAUTH2_PROXY_BASEURL}/start`;
    this.navigateTo(url);
  }

  /**
   * Gets user information from the session
   * @returns Observable with user info or null if unauthorized
   */
  getUserInfo(): Observable<UserInfo | null> {
    return this.http.get<UserInfo>(`${this.OAUTH2_PROXY_BASEURL}/userinfo`).pipe(
      catchError(error => {
        if (error.status === HttpStatusCode.Unauthorized) {
          return of(null);
        }
        throw error;
      })
    );
  }

  /**
   * Three-state authentication probe against `/oauth2/auth`, sent with `Cache-Control: no-store` so
   * intermediaries never serve a stale answer.
   * - HTTP 202 -> `'authenticated'`
   * - HTTP 401 -> `'unauthenticated'`
   * - anything else (timeout, network error, unexpected status) -> `'unknown'`
   * `'unknown'` must be treated as "fail open", never as `'unauthenticated'`.
   * @returns Observable of the three-state result
   */
  checkAuthentication(): Observable<AuthCheckResult> {
    return this.http.get(this.OAUTH2_PROXY_BASEURL + '/auth', {
      observe: 'response',
      headers: { 'Cache-Control': 'no-store' },
    }).pipe(
      timeout(AUTH_CHECK_TIMEOUT_MS),
      map((response): AuthCheckResult =>
        response.status === HttpStatusCode.Accepted ? 'authenticated' : 'unknown'),
      catchError((error: unknown) => {
        const result: AuthCheckResult =
          error instanceof HttpErrorResponse && error.status === HttpStatusCode.Unauthorized
            ? 'unauthenticated'
            : 'unknown';
        return scheduled([result], asyncScheduler);
      })
    );
  }

  /**
   * Composes the relative part of a location (pathname + search + hash), never an absolute URL.
   * This is the ONLY producer of `rd` values across the codebase - the oauth2-proxy only accepts
   * absolute redirect targets for whitelisted hosts, so every `rd` must stay relative.
   * @param loc a `Location` (e.g. `window.location`) or an object with the same three properties
   */
  toRelativeUrl(loc: Location | { pathname: string; search: string; hash: string }): string {
    return `${loc.pathname}${loc.search}${loc.hash}`;
  }

  /**
   * Handles 401 unauthorized responses by redirecting to sign in
   * @param redirectUri Optional URI to redirect to after successful authentication
   */
  handleUnauthorized(redirectUri?: string): void {
    this.signIn(redirectUri || window.location.href);
  }

  /**
   * Performs the actual browser navigation. Extracted into its own method (instead of inlining
   * `window.location.href = url` at every call site) purely so tests can observe navigation without
   * having to stub the non-configurable `window.location` object.
   */
  private navigateTo(url: string): void {
    window.location.href = url;
  }
}
