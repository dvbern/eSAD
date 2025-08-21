import {computed, signal} from '@angular/core';
import {AppFailureErrorJson, AppValidationErrorJson} from '@dvb/data-access-backend';
import {catchError, Observable, of, tap} from 'rxjs';
import {
  isAppFailureErrorJson,
  isAppValidationErorrJson,
  makeAppFailureErrorJson,
} from '../error-handling';

export type Initial = {
  progress: 'initial';
};
export type Loading = {
  progress: 'loading';
};
export type Success = {
  progress: 'success';
};
export type Failure = {
  progress: 'failure';
  error: AppValidationErrorJson | AppFailureErrorJson;
};

export type LoaderState = {
  loading: Initial | Loading | Success | Failure;
};

const initialState: LoaderState = {loading: {progress: 'initial'}};

export class Loader {
  private readonly loadingState = signal<LoaderState>(initialState);

  start(): void {
    this.loadingState.set({
      loading: {progress: 'loading'},
    });
  }

  success(): void {
    this.loadingState.set({loading: {progress: 'success'}});
  }

  failWith(error: AppFailureErrorJson | AppValidationErrorJson): void {
    this.loadingState.set({
      loading: {progress: 'failure', error},
    });
  }

  loadingReset(): void {
    this.loadingState.set(initialState);
  }

  protected progress = computed(() =>
    this.loadingState().loading.progress,
  );

  /**
   * If the operation is in progress.
   */
  isLoading = computed(
    () => this.progress() === 'loading',
  );
  /**
   * If the operation has completed with a failure (this excludes validation errors!).
   */
  isFailure = computed(
    () => this.progress() === 'failure',
  );
  /**
   * If the operation has completed with a success.
   */
  isSuccess = computed(
    () => this.progress() === 'success',
  );
  /**
   * Convenience: any stopped state (initial|success|failure).
   */
  isStopped = computed(() => !this.isLoading());
  /**
   * Return the error message if the operation has completed with an error.
   */
  failure = computed(() => {
    const loading = this.loadingState().loading;
    return loading.progress === 'failure' ? loading.error : undefined;
  });

  tapLoading$<Data>(
    source$: Observable<Data>,
    failureHandler: (error: unknown) => void,
  ): Observable<Data> {
    this.start();

    return source$.pipe(
      tap({
        // complete is bad because complete is later and all alerts that were thrown after success case will be deleted
        next: () => {
          this.success();
        },
      }),
      catchError((error: unknown) => {
        // update the loading state accordingly
        // backend errors get parsed by our interceptors!
        if (isAppFailureErrorJson(error) || isAppValidationErorrJson(error)) {
          this.failWith(error);
        } else {
          const message = `Unknown error (should never get here): ${JSON.stringify(error)}`;
          this.failWith(makeAppFailureErrorJson('-1', message));
        }

        // handle the error
        failureHandler(error);

        // Error nicht weiterwerfen, weil sonst die aeussere Pipe kaputt ist
        return of();
      }),
    );
  }
}
