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

import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { firstValueFrom } from 'rxjs';
import type { MockInstance } from 'vitest';
import { OAuth2ProxyService } from './oauth2-proxy.service';

describe('OAuth2ProxyService', () => {
  let service: OAuth2ProxyService;
  let httpMock: HttpTestingController;
  let navigateToSpy: MockInstance;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(OAuth2ProxyService);
    httpMock = TestBed.inject(HttpTestingController);

    // window.location is a non-configurable own accessor in this jsdom version and cannot be stubbed
    // directly, so the service exposes navigation through a private `navigateTo` method that we spy on.
    navigateToSpy = vi.spyOn(service as unknown as { navigateTo: (url: string) => void }, 'navigateTo')
      .mockImplementation(() => undefined);
  });

  afterEach(() => {
    httpMock.verify();
    navigateToSpy.mockRestore();
  });

  describe('startOAuthFlow', () => {
    it('builds a plain /oauth2/start url without params', () => {
      service.startOAuthFlow();
      expect(navigateToSpy).toHaveBeenCalledWith('/oauth2/start');
    });

    it('adds rd when redirectUri is given', () => {
      service.startOAuthFlow('/anlass/123?x=1#y');
      expect(navigateToSpy).toHaveBeenCalledWith('/oauth2/start?rd=%2Fanlass%2F123%3Fx%3D1%23y');
    });

    it('adds additional params', () => {
      service.startOAuthFlow('/target', { kc_idp_hint: 'agov' });
      expect(navigateToSpy).toHaveBeenCalledWith('/oauth2/start?rd=%2Ftarget&kc_idp_hint=agov');
    });

    it('preserves an explicitly empty param value as `kc_idp_hint=`, not dropping it', () => {
      service.startOAuthFlow('/target', { kc_idp_hint: '' });
      const calledWith = navigateToSpy.mock.calls[0][0] as string;
      expect(calledWith).toContain('kc_idp_hint=');
      expect(calledWith.endsWith('kc_idp_hint=')).toBe(true);
      // this is the crux of the fix: URLSearchParams keeps an empty string value, it does not drop the key
      expect(new URLSearchParams({ kc_idp_hint: '' }).toString()).toBe('kc_idp_hint=');
    });

    it('supports params without a redirectUri', () => {
      service.startOAuthFlow(undefined, { kc_idp_hint: '' });
      expect(navigateToSpy).toHaveBeenCalledWith('/oauth2/start?kc_idp_hint=');
    });
  });

  describe('signIn', () => {
    it('produces /oauth2/sign_in without a redirectUri', () => {
      service.signIn();
      expect(navigateToSpy).toHaveBeenCalledWith('/oauth2/sign_in');
    });

    it('produces a correctly-slashed /oauth2/sign_in url with a redirectUri (regression: missing slash bug)', () => {
      service.signIn('/back');
      const calledWith = navigateToSpy.mock.calls[0][0] as string;
      expect(calledWith.startsWith('/oauth2/sign_in?rd=')).toBe(true);
      expect(calledWith).not.toContain('/oauth2sign_in');
    });
  });

  describe('checkAuthentication', () => {
    it('maps HTTP 202 to "authenticated"', async () => {
      const resultPromise = firstValueFrom(service.checkAuthentication());
      const req = httpMock.expectOne('/oauth2/auth');
      expect(req.request.headers.get('Cache-Control')).toBe('no-store');
      req.flush(null, { status: 202, statusText: 'Accepted' });
      await expect(resultPromise).resolves.toBe('authenticated');
    });

    it('maps HTTP 401 to "unauthenticated"', async () => {
      const resultPromise = firstValueFrom(service.checkAuthentication());
      const req = httpMock.expectOne('/oauth2/auth');
      req.flush(null, { status: 401, statusText: 'Unauthorized' });
      await expect(resultPromise).resolves.toBe('unauthenticated');
    });

    it('maps any other error status to "unknown"', async () => {
      const resultPromise = firstValueFrom(service.checkAuthentication());
      const req = httpMock.expectOne('/oauth2/auth');
      req.flush(null, { status: 500, statusText: 'Server Error' });
      await expect(resultPromise).resolves.toBe('unknown');
    });

    it('maps a network error to "unknown"', async () => {
      const resultPromise = firstValueFrom(service.checkAuthentication());
      const req = httpMock.expectOne('/oauth2/auth');
      req.error(new ProgressEvent('error'));
      await expect(resultPromise).resolves.toBe('unknown');
    });
  });

  describe('toRelativeUrl', () => {
    it('composes pathname + search + hash', () => {
      const relative = service.toRelativeUrl({ pathname: '/anlass/123', search: '?x=1', hash: '#y' });
      expect(relative).toBe('/anlass/123?x=1#y');
    });

    it('never returns an absolute URL, even if window.location is passed', () => {
      const relative = service.toRelativeUrl(window.location);
      expect(relative.startsWith('/')).toBe(true);
      expect(relative).not.toMatch(/^https?:\/\//);
    });
  });
});
