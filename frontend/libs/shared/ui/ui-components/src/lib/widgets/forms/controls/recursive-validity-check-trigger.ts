import { ChangeDetectorRef, DestroyRef } from '@angular/core';
import {
  combineLatest,
  distinctUntilChanged,
  map,
  Observable,
  startWith,
  Subject,
} from 'rxjs';
import { AbstractControl, FormControlStatus } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

export class RecursiveValidityCheckTrigger {
  readonly #validityCheckTrigger$ = new Subject<boolean | null>();

  constructor(
    private readonly control: AbstractControl,
    private readonly changeDetectorRef: ChangeDetectorRef,
    private readonly destoryRef: DestroyRef
  ) {
    combineLatest({
      validityHasChanged$: this.createTouchedObservable$(),
      touched$: this.createValidityObservable$(),
    })
      .pipe(takeUntilDestroyed(this.destoryRef))
      .subscribe({
        next: () => {
          this.changeDetectorRef.detectChanges();
        },
        error: (error: unknown) =>
          console.error('Error in RecursiveValidityCheckTrigger', error),
      });
  }

  trigger(): void {
    this.#validityCheckTrigger$.next(this.control.touched);
  }

  private createTouchedObservable$(): Observable<boolean | null> {
    return this.#validityCheckTrigger$.pipe(
      startWith(false),
      distinctUntilChanged()
    );
  }

  private createValidityObservable$(): Observable<FormControlStatus> {
    return this.control.statusChanges.pipe(
      startWith(null),
      map(() => this.control.status)
    );
  }
}
