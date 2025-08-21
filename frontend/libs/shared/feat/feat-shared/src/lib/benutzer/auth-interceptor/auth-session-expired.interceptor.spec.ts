import {HttpClient, provideHttpClient, withInterceptors} from '@angular/common/http';
import {HttpTestingController, provideHttpClientTesting} from '@angular/common/http/testing';
import {TestBed} from '@angular/core/testing';
import {firstValueFrom, of} from 'rxjs';
import {OAuth2ProxyService} from '../oauth2-proxy-service/oauth2-proxy.service';
import {authSessionExpiredInterceptorFn} from './auth-session-exipred.interceptor';

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
      jest.spyOn(oauth2ProxyService, 'isAuthenticated').mockReturnValue(of(true));

      const responsePromise = firstValueFrom(httpClient.get('/admin/foo')).catch(err => err);

      const request = httpTesting.expectOne('/admin/foo');
      request.flush({data: 'some auth error'}, { status: 401, statusText: 'Unauthorized' });

      await expect(responsePromise).resolves.not.toBeNull();
    });

    it('triggers OAuth login if user is not authenticated', async () => {
      // Mock the response for the authentication check to simulate the user not authenticated
      jest.spyOn(oauth2ProxyService, 'isAuthenticated').mockReturnValue(of(false));
      jest.spyOn(oauth2ProxyService, 'signIn');

      const responsePromise = firstValueFrom(httpClient.get('/admin/foo')).catch(err => err);

      const request = httpTesting.expectOne('/admin/foo');
      request.flush({}, { status: 401, statusText: 'Unauthorized' });
      expect(oauth2ProxyService.signIn).toHaveBeenCalled();

      await expect(responsePromise).resolves.not.toBeNull();
    });
  });
});
