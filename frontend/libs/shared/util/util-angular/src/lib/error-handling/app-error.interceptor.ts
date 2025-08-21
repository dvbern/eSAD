import {
  HttpErrorResponse,
  HttpEvent,
  HttpHandlerFn,
  HttpInterceptorFn,
  HttpRequest,
  HttpStatusCode,
} from '@angular/common/http';
import {catchError, Observable} from 'rxjs';
import {isAppFailureErrorJson, isAppValidationErorrJson, makeAppFailureErrorJson} from './backend-errors';

export function appErrorInterceptor(): HttpInterceptorFn {
  return (req: HttpRequest<unknown>, next: HttpHandlerFn): Observable<HttpEvent<unknown>> => {
    return next(req).pipe(
      catchError(async (exception: unknown) => {
        if (exception instanceof HttpErrorResponse) {
          const error = exception.error instanceof Blob ? await handleBlobError(exception.error, exception) : exception.error;
          if (isAppValidationErorrJson(error) || isAppFailureErrorJson(error)) {
            throw error;
          } else {
            throw makeAppFailureErrorJson(
              '-1',
              `Internal error (HTTP): ${exception.statusText} (${exception.status}) = ${JSON.stringify(error)}`,
            );
          }
        }

        // Is there any way to test this?
        throw makeAppFailureErrorJson(
          '-1',
          `Unexpected internal error (Interceptor): ${JSON.stringify(exception)}`,
        );
      }),
    );
  };

  /**
   * Blobs require async access to the text, the text (body) also needs to be parsed as JSON but could be empty
   * in the case of a 403.
   */
  async function handleBlobError(error: Blob, exception: HttpErrorResponse): Promise<unknown> {
    const text = await error.text();
    if(isJsonString(text)) {
      return JSON.parse(text);
    }
    if (exception.status === HttpStatusCode.Forbidden) {
      throw makeAppFailureErrorJson(
        '-1',
        `Access forbidden (HTTP-BLOB): ${exception.statusText} (${exception.status})`,
      );
    }
    return {} //return empty object, this will trigger generic error message
  }
  function isJsonString(text: string): boolean {
    return text.trim().startsWith('{');
  }

}
