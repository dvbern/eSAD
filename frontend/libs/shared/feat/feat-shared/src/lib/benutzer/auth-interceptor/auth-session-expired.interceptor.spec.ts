import {HttpClient, provideHttpClient, withInterceptors} from '@angular/common/http';
import {HttpTestingController, provideHttpClientTesting} from '@angular/common/http/testing';
import {TestBed} from '@angular/core/testing';
import {firstValueFrom, of} from 'rxjs';
import {OAuth2ProxyService} from '../oauth2-proxy-service/oauth2-proxy.service';
import {authSessionExpiredInterceptorFn} from './auth-session-exipred.interceptor';
import {requireValue} from "@dvb/util-shared";

describe('authSessionExpiredInterceptorFn', () => {
  let httpClient: HttpClient;
  let httpTesting: HttpTestingController;
  let oauth2ProxyService: OAuth2ProxyService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(
          withInterceptors([
            authSessionExpiredInterceptorFn(), // The actual interceptor you're testing
          ]),
        ),
        provideHttpClientTesting(),
      ],
    });

    // Create the actual OAuth2ProxyService instance
    oauth2ProxyService = TestBed.inject(OAuth2ProxyService);
    httpClient = TestBed.inject(HttpClient);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTesting.verify();
  });

  describe('an OK request with body', () => {
    it('returns an OK response', async () => {
      // ARRANGE


      const response = firstValueFrom(httpClient.get('/admin/foo'));

      const request = httpTesting.expectOne('/admin/foo');
      request.flush({ data: 'test' });

      await expect(response).resolves.toEqual({ data: 'test' });
    });
  });

  describe('when request fails with 401 Unauthorized', () => {
    it('checks authentication and does nothing if still authenticated', async () => {
      // Mock the response for the authentication check to simulate the user being authenticated
      vi.spyOn(oauth2ProxyService, 'checkAuthentication').mockReturnValue(of('authenticated'));

      const responsePromise = firstValueFrom(httpClient.get('/admin/foo')).catch(err => err);

      const request = httpTesting.expectOne('/admin/foo');
      request.flush({data: 'some auth error'}, { status: 401, statusText: 'Unauthorized' });

      await expect(responsePromise).resolves.not.toBeNull();
    });

    it('starts the flow with a relative rd and no kc_idp_hint (Keycloak offers the methods)', async () => {
      vi.spyOn(oauth2ProxyService, 'checkAuthentication').mockReturnValue(of('unauthenticated'));
      vi.spyOn(oauth2ProxyService, 'signIn');
      vi.spyOn(oauth2ProxyService, 'startOAuthFlow').mockImplementation(() => undefined);

      const responsePromise = firstValueFrom(httpClient.get('/admin/foo')).catch(err => err);

      const request = httpTesting.expectOne('/admin/foo');
      request.flush({}, { status: 401, statusText: 'Unauthorized' });

      expect(oauth2ProxyService.signIn).not.toHaveBeenCalled();
      expect(oauth2ProxyService.startOAuthFlow).toHaveBeenCalledTimes(1);
      const [rd, params] = requireValue(vi.mocked(oauth2ProxyService.startOAuthFlow).mock.calls[0]);
      // rd must be relative (pathname+search+hash), never an absolute URL
      expect(rd?.startsWith('/')).toBe(true);
      expect(rd).not.toBe(window.location.href);
      expect(rd).not.toMatch(/^https?:\/\//);
      // Option 1 hybrid: no hint replay - the tenant realm's Keycloak login page performs the selection
      expect(params).toBeUndefined();

      await expect(responsePromise).resolves.not.toBeNull();
    });
  });
});
