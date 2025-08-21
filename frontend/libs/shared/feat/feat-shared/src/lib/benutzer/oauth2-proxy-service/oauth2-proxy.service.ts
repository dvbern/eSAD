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

import {HttpClient, HttpStatusCode} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {asyncScheduler, Observable, of, scheduled, timeout} from 'rxjs';
import {catchError, map} from 'rxjs/operators';

export interface UserInfo {
  email: string;
}
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

  constructor(
    private http: HttpClient,
  ) {}

  /**
   * Redirects user to the sign-in page (which doubles as sign-out by clearing cookies)
   * @param redirectUri Optional URI to redirect to after successful authentication
   */
  signIn(redirectUri?: string): void {

    const url = redirectUri
      ? `${this.OAUTH2_PROXY_BASEURL}sign_in?rd=${encodeURIComponent(redirectUri)}`
      : `${this.OAUTH2_PROXY_BASEURL}/sign_in`;
    window.location.href = url;
  }

  /**
   * Signs out the user by clearing the session cookie
   * @param redirectUri Optional URI to redirect to after signing out
   */
  signOut(redirectUri?: string): void {
    const url = redirectUri
      ? `${this.OAUTH2_PROXY_BASEURL}/sign_out?rd=${encodeURIComponent(redirectUri)}`
      : `${this.OAUTH2_PROXY_BASEURL}/sign_out`;
    window.location.href = url;
  }

  /**
   * Starts the OAuth cycle
   * @param redirectUri Optional URI to redirect to after authentication
   */
  startOAuthFlow(redirectUri?: string): void {
    const url = redirectUri
      ? `${this.OAUTH2_PROXY_BASEURL}/start?rd=${encodeURIComponent(redirectUri)}`
      : `${this.OAUTH2_PROXY_BASEURL}/start`;
    window.location.href = url;
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
   * Checks if the user is authenticated
   * @returns Observable boolean indicating authentication status
   */
  isAuthenticated(): Observable<boolean> {
    return this.http.get(this.OAUTH2_PROXY_BASEURL + '/auth', { observe: 'response' }).pipe(
      timeout(AUTH_CHECK_TIMEOUT_MS),
      map(response => response.status === HttpStatusCode.Accepted),
      catchError(() => scheduled([false], asyncScheduler))
    );
  }

  /**
   * Handles 401 unauthorized responses by redirecting to sign in
   * @param redirectUri Optional URI to redirect to after successful authentication
   */
  handleUnauthorized(redirectUri?: string): void {
    this.signIn(redirectUri || window.location.href);
  }
}
