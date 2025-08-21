import {withDevtools} from '@angular-architects/ngrx-toolkit';
import {computed, inject, Injectable} from '@angular/core';
import {TimeTravelJson, TimetravelService} from '@dvb/data-access-backend';
import {ISODateString} from '@dvb/data-access-types';
import {AppErrorHandlerService, Loader} from '@dvb/util-angular';
import {fromISODateString} from '@dvb/util-shared';
import {tapResponse} from '@ngrx/operators';
import {EmptyFeatureResult, patchState, signalStore, SignalStoreFeature, withState} from '@ngrx/signals';
import {rxMethod} from '@ngrx/signals/rxjs-interop';
import {exhaustMap, pipe, switchMap} from 'rxjs';

type TimetravelState = {
  now: ISODateString | undefined,
  timetravelling: boolean
};

const initialState: TimetravelState = {
  now: undefined,
  timetravelling: false,
};

@Injectable()
export class TimetravelStore extends signalStore(
  {protectedState: false},
  withDevtools('timetravel') as SignalStoreFeature<EmptyFeatureResult, EmptyFeatureResult>,
  withState(initialState),
) {
  readonly timetravelService = inject(TimetravelService);
  readonly appErrorHandlerService = inject(AppErrorHandlerService);

  readonly loader = new Loader();
  readonly updateLoader = new Loader();
  readonly resetLoader = new Loader();

  loadNow = rxMethod<void>(
    pipe(
      switchMap(() => this.loader
        .tapLoading$(this.timetravelService.getNow$(),
          error => this.appErrorHandlerService.handleError(error),
        ),
      ),
      tapResponse({
        next: (result: TimeTravelJson) => patchState(this, {
          now: result.now,
          timetravelling: result.timetravelling,
        }),
        error: console.error,
      }),
    ),
  );

  updateNow = rxMethod<{ date: ISODateString, onSuccess: () => void }>(
    pipe(
      exhaustMap(({date, onSuccess}) => this.updateLoader
        .tapLoading$(
          this.timetravelService.setNow$({
            timeTravelJson: {
              now: date,
              timetravelling: true,
            },
          }), error => this.appErrorHandlerService.handleError(error))
        .pipe(
          tapResponse({
            next: (result: TimeTravelJson) => {
              console.log('timetravel', result);

              patchState(this, {
                now: result.now,
                timetravelling: result.timetravelling,
              });
              onSuccess();
            },
            error: console.error,
          }),
        ),
      )),
  );

  resetNow = rxMethod<{ onSuccess: () => void }>(
    pipe(
      exhaustMap(({onSuccess}) => this.resetLoader
        .tapLoading$(
          this.timetravelService.resetNow$(), error => this.appErrorHandlerService.handleError(error))
        .pipe(
          tapResponse({
            next: (result: TimeTravelJson) => {
              console.log('timetravel', result);
              patchState(this, {
                now: result.now,
                timetravelling: result.timetravelling,
              });
              onSuccess();
            },
            error: console.error,
          }),
        ),
      )),
  );

  currentDate = computed(() => fromISODateString(this.now()) || new Date())

}
