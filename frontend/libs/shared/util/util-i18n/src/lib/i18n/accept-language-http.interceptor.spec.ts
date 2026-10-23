import {TestBed} from '@angular/core/testing';
import {HttpClient, provideHttpClient, withInterceptors} from '@angular/common/http';
import {HttpTestingController, provideHttpClientTesting} from '@angular/common/http/testing';
import {acceptLanguageInterceptorFn} from './accept-language-http.interceptor';
import {LanguageStore} from './language.store';
import {Language} from './Language';
import {signal} from '@angular/core';

describe('acceptLanguageInterceptorFn', () => {
  let httpMock: HttpTestingController;
  let httpClient: HttpClient;
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  let languageStoreMock: any;

  beforeEach(() => {
    languageStoreMock = {
      activeLocale: signal<Language>('de')
    };

    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([acceptLanguageInterceptorFn])),
        provideHttpClientTesting(),
        { provide: LanguageStore, useValue: languageStoreMock }
      ]
    });

    httpMock = TestBed.inject(HttpTestingController);
    httpClient = TestBed.inject(HttpClient);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should add Accept-Language header for api requests', () => {
    languageStoreMock.activeLocale.set('fr');

    // Mock navigator.languages
    Object.defineProperty(navigator, 'languages', {
      value: ['en-US', 'en'],
      configurable: true
    });

    httpClient.get('/api/test').subscribe();

    const req = httpMock.expectOne('/api/test');
    expect(req.request.headers.get('Accept-Language')).toBe('fr, en-US, en');
    req.flush({});
  });

  it('should remove duplicates even if they have quality values', () => {
    languageStoreMock.activeLocale.set('de');

    // Mock navigator.languages with something that might contain quality values or matches
    Object.defineProperty(navigator, 'languages', {
      value: ['de-CH', 'de;q=0.9', 'en'],
      configurable: true
    });

    httpClient.get('/api/test').subscribe();

    const req = httpMock.expectOne('/api/test');
    expect(req.request.headers.get('Accept-Language')).toBe('de, de-CH, en');
    req.flush({});
  });

  it('should not add Accept-Language header for non-api requests', () => {
    httpClient.get('/assets/test.json').subscribe();

    const req = httpMock.expectOne('/assets/test.json');
    expect(req.request.headers.has('Accept-Language')).toBeFalsy();
    req.flush({});
  });
});
