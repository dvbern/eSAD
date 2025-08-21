import {computed} from '@angular/core';
import {TestBed} from '@angular/core/testing';
import {AppFailureErrorJson} from '@dvb/data-access-backend';
import {of, pipe, Subject, switchMap} from 'rxjs';
import {rxMethod} from '../store';
import {Loader} from './loader';

describe('Loader', () => {
  describe('with initial state', () => {
    let loader: Loader;

    beforeEach(() => {
      loader = new Loader();
    });

    it('has initial values', () => {
      expect(loader.isLoading())
        .toBe(false);
      expect(loader.isFailure())
        .toBe(false);
      expect(loader.isSuccess())
        .toBe(false);
      expect(loader.isStopped())
        .toBe(true);
      expect(loader.failure())
        .toBeUndefined();
    })

    describe('when starting', () => {
      beforeEach(() => {
        loader.start();
      });

      it('is loading', () => {
        expect(loader.isLoading())
          .toBe(true);
        expect(loader.isFailure())
          .toBe(false);
        expect(loader.isSuccess())
          .toBe(false);
        expect(loader.isStopped())
          .toBe(false);
        expect(loader.failure())
          .toBeUndefined();
      });

      it('can be reset', () => {
        loader.loadingReset();
        expect(loader.isLoading())
          .toBe(false);
      });

      describe('and then finishing with success', () => {
        beforeEach(() => {
          loader.success();
        });

        it('is successful', () => {
          expect(loader.isLoading())
            .toBe(false);
          expect(loader.isFailure())
            .toBe(false);
          expect(loader.isSuccess())
            .toBe(true);
          expect(loader.isStopped())
            .toBe(true);
          expect(loader.failure())
            .toBeUndefined();
        });
      });

      describe('and then failing', () => {
        const failure: AppFailureErrorJson = {type: 'FAILURE', id: '123', summary: 'Failed'};
        beforeEach(() => {
          loader.failWith(failure);
        });

        it('is failed', () => {
          expect(loader.isLoading())
            .toBe(false);
          expect(loader.isFailure())
            .toBe(true);
          expect(loader.isSuccess())
            .toBe(false);
          expect(loader.isStopped())
            .toBe(true);
          expect(loader.failure())
            .toEqual(failure);
        });
      });
    });
  });

  describe('tapLoading$', () => {
    class TestingLoader extends Loader {
      public actualProgress = computed(() => this.progress());
    }

    let loader: TestingLoader;
    const triggerResponse = new Subject<'ok' | 'error'>();
    let triggerRequest: () => void;

    beforeEach(async () => {
      loader = new TestingLoader();

      triggerRequest = TestBed.runInInjectionContext(() => rxMethod<void>(
        pipe(
          switchMap(() =>
            loader.tapLoading$(
              triggerResponse.pipe(
                switchMap(ok => {
                  if (ok === 'ok') {
                    return of(undefined);
                  } else {
                    throw new Error('Failed');
                  }
                }),
              ),
              // a real-world implementation should handle errors here
              error => console.log(error),
            ),
          ),
        ),
      ));
    });

    it('continues after error', () => {
      triggerRequest();
      triggerResponse.next('error');
      expect(loader.actualProgress())
        .toBe('failure')

      triggerRequest()
      expect(loader.actualProgress())
        .toBe('loading')
    })

    it('extended demo: follows an observable and is reusable', () => {
      expect(loader.actualProgress())
        .toBe('initial');

      triggerRequest();
      expect(loader.actualProgress())
        .toBe('loading');

      triggerResponse.next('ok');
      expect(loader.actualProgress())
        .toBe('success');

      triggerRequest();
      expect(loader.actualProgress())
        .toBe('loading');

      triggerResponse.next('ok');
      expect(loader.actualProgress())
        .toBe('success');

      triggerRequest();
      expect(loader.actualProgress())
        .toBe('loading');

      triggerResponse.next('error');
      expect(loader.actualProgress())
        .toBe('failure');

      triggerRequest();
      expect(loader.actualProgress())
        .toBe('loading');

      triggerResponse.next('ok');
      expect(loader.actualProgress())
        .toBe('success');

    })
  });
});
