import {computed, Inject, inject, Injectable, Optional} from '@angular/core';
import { AppValidationErrorJson } from '@dvb/data-access-backend';
import { AbstractSignalStore, AppErrorHandlerService, Loader, rxMethod } from '@dvb/util-angular';
import { pipe, switchMap, tap } from 'rxjs';
import { FakeStammdatenService, MyStammdatenDTO } from './fake-stammdaten.service';

interface StammdatenState {
  id: string | undefined;
  value: MyStammdatenDTO | undefined;
  backendErrors: AppValidationErrorJson | undefined;
}

const initialState: StammdatenState = {
  id: undefined,
  value: undefined,
  backendErrors: undefined,
};

@Injectable()
export class StammdatenStore extends AbstractSignalStore<StammdatenState> {
  readonly #service = inject(FakeStammdatenService);
  readonly #globalErrorHandler = inject(AppErrorHandlerService);
  readonly loader = new Loader();
  readonly saveLoader = new Loader();

  #loadStammdaten = rxMethod<{ id: string }>(
    pipe(
      switchMap(({ id }) =>
        this.loader.tapLoading$(this.#service.loadById$(id), error => this.#globalErrorHandler.handleError(error)),
      ),
      this.tapResponse(
        (draft, response) => {
          draft.value = response;
        },
        (draft, error) => {
          draft.backendErrors = error;
        },
      ),
    ),
  );

  #updateStammdaten = rxMethod<MyStammdatenDTO>(
    pipe(
      tap(stammdaten =>
        this.patchState(draft => {
          draft.value = stammdaten;
          draft.backendErrors = undefined;
        }),
      ),
      switchMap(stammdaten =>
        this.saveLoader
          .tapLoading$(this.#service.update$(stammdaten), error => this.#globalErrorHandler.handleError(error))
          .pipe(
            this.tapResponse(
              // FIXME: starter: decide: refresh store from response?
              () => undefined,
              (draft, error) => {
                draft.backendErrors = error;
              },
            ),
          ),
      ),
    ),
  );

  /**
   * @param testInit - for testing purposes only
   */
  // eslint-disable-next-line @angular-eslint/prefer-inject
  constructor(@Inject('nothing') @Optional() testInit?: StammdatenState) {
    super(testInit ?? initialState);
  }

  initFromRoute(id: string): void {
    this.patchState(draft => {
      draft.id = id;
      draft.value = undefined;
    });

    this.#loadStammdaten({ id });
  }

  isInitialized = computed(() => this.state().id !== undefined);
  isLoaded = computed(() => this.state().value !== undefined);
  backendErrors = computed(() => this.state().backendErrors);

  stammdaten = computed(() => this.state().value);

  // FIXME: starter: some stores might find this useful:
  // private requireStammdaten = computed<MyStammdatenDTO>(() =>
  //   requireValue(this.state().value),
  // );

  updateStammdaten(stammdaten: MyStammdatenDTO): void {
    this.#updateStammdaten(stammdaten);
  }
}
