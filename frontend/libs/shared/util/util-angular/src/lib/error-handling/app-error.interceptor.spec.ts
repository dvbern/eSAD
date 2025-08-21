import {HttpClient, provideHttpClient, withInterceptors} from '@angular/common/http';
import {HttpTestingController, provideHttpClientTesting} from '@angular/common/http/testing';
import {TestBed} from '@angular/core/testing';
import {AppFailureErrorJson, AppValidationErrorJson} from '@dvb/data-access-backend';
import {ensureProps} from '@dvb/util-shared';
import {firstValueFrom} from 'rxjs';
import {appErrorInterceptor} from './app-error.interceptor';

describe('appErrorInterceptor', () => {
  let httpClient: HttpClient;
  let httpTesting: HttpTestingController;

  beforeEach(() => {

    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(
          withInterceptors([
            appErrorInterceptor(),
          ]),
        ),
        provideHttpClientTesting(),
      ],
    });

    httpClient = TestBed.inject(HttpClient);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  describe('an OK request with body', () => {
    it('returns an OK response', async () => {
      const response = firstValueFrom(
        httpClient.get('/'),
      );

      httpTesting.expectOne('/')
        .flush({data: 'test'});

      await expect(response)
        .resolves
        .toEqual({data: 'test'});
    });
  });

  describe('a validation error response', () => {
    it('is thrown as is', async () => {
      const response = firstValueFrom(
        httpClient.get('/'),
      );

      const validation = ensureProps<AppValidationErrorJson>({
        type: 'VALIDATION',
        violations: [
          {
            path: 'name',
            key: 'the-key',
            message: 'Name is required',
          },
        ],
      });
      httpTesting.expectOne('/')
        .flush(validation, {status: 400, statusText: 'Bad request'});

      await expect(response)
        .rejects
        .toEqual(validation);
    });
  });

  describe('a failure error response', () => {
    it('is thrown as is', async () => {
      const response = firstValueFrom(
        httpClient.get('/'),
      );

      const failure = ensureProps<AppFailureErrorJson>({
        type: 'FAILURE',
        id: '123',
        summary: 'Not found',
      });
      httpTesting.expectOne('/')
        .flush(failure, {status: 404, statusText: 'Not found'});

      await expect(response)
        .rejects
        .toEqual(failure);
    });
  });

  describe('an internal server error', () => {
    it('converts to an AppFailureErrorJson', async () => {
      const response = firstValueFrom(
        httpClient.get('/'),
      );

      httpTesting.expectOne('/')
        .flush('Alles kaputt', {status: 500, statusText: 'Internal server error'});

      await expect(response)
        .rejects
        .toEqual(ensureProps<AppFailureErrorJson>({
          id: '-1',
          type: 'FAILURE',
          summary: 'Internal error (HTTP): Internal server error (500) = "Alles kaputt"',
        }));
    });
  });

});
