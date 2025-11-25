import {Injectable, Signal, signal, WritableSignal} from '@angular/core';
import {AppValidationErrorJson} from '@dvb/data-access-backend';
import {enableMapSet, produce} from 'immer';
import {catchError, MonoTypeOperatorFunction, of, tap} from 'rxjs';
import {isAppValidationErorrJson} from '../error-handling';

export type ReceipeOutcome<State> = State | undefined | void;

// FIXME: starter: decide if you want to use this or other state management (e.g. @ngrx/signalstore)
@Injectable()
export abstract class AbstractSignalStore<State> {
  static {
    // FIXME: do this in application init?
    enableMapSet();
  }
  readonly #state: WritableSignal<State>;
  protected readonly state: Signal<State>;

  protected constructor(
    // this is not actually injected
    // eslint-disable-next-line @angular-eslint/prefer-inject
    readonly initialState: State,
  ) {
    this.#state = signal(produce(undefined as State, () => initialState));
    this.state = this.#state;
  }

  /**
   * Update the state by applying the producer function to the current state.
   * See <a href="https://immerjs.github.io/immer/docs/produce">ImmerJS: produce</a> for more information.
   * <p>
   * <i>If</i> the producer returns a new state, the state will be completely replaced.
   */
  patchState(receipe: (draft: State) => ReceipeOutcome<State>): void {
    this.#state.update(oldState => produce(oldState, receipe))
  }

  tapResponse<Response>(
    responseHandler: (draft: State, response: Response) => ReceipeOutcome<State>,
    validationErrorHandler: (draft: State, error: AppValidationErrorJson | undefined) => ReceipeOutcome<State>,
  ): MonoTypeOperatorFunction<Response> {
    return source$ => source$.pipe(
      tap({
        next: response => {
          this.patchState(draft => responseHandler(draft, response));
          this.patchState(draft => validationErrorHandler(draft, undefined))
        },
      }),
      catchError((error: unknown) => {
        if (isAppValidationErorrJson(error)) {
          this.patchState(draft => validationErrorHandler(draft, error))
          return of();
        }
        throw error;
      }),
    )
  }
}
