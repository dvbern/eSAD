import {
  AfterViewInit,
  ChangeDetectorRef,
  ContentChildren,
  DestroyRef,
  Directive,
  DoCheck,
  inject,
  QueryList,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { AbstractControl, FormGroup, Validators } from '@angular/forms';
import { Nullish, requireValue } from '@dvb/util-shared';
import { ValidatedInputWithIdDirective } from '../inputs/validated-input-with-id.directive';
import { RecursiveValidityCheckTrigger } from './recursive-validity-check-trigger';

function detectRequiredValidator(control: AbstractControl): boolean {
  return hasRequiredValidator(control);
}

/**
 *
 * Aus evok <-- B3000 kopiert
 *
 * Determine if a control has a {@link Validators#required()} validator.
 *
 */
// see: https://stackoverflow.com/questions/39819123/angular2-find-out-if-formcontrol-has-required-validator
export function hasRequiredValidator(abstractControl: AbstractControl): boolean {
  if (abstractControl.validator) {
    const validator = abstractControl.validator({} as AbstractControl);
    if (validator && validator['required']) {
      return true;
    }
  }
  if (abstractControl instanceof FormGroup) {
    for (const controlName in abstractControl['controls']) {
      if (abstractControl['controls'][controlName]) {
        if (hasRequiredValidator(abstractControl['controls'][controlName])) {
          return true;
        }
      }
    }
  }
  return false;
}

@Directive({ standalone: true })
export class ValidatedControlWrapperDirective implements DoCheck, AfterViewInit {
  required = false;

  @ContentChildren(ValidatedInputWithIdDirective, { descendants: true })
  controlDirective: QueryList<ValidatedInputWithIdDirective> | undefined;

  readonly #changeDetectorRef = inject(ChangeDetectorRef);
  readonly #destoryRef = inject(DestroyRef);

  #validityChecker: RecursiveValidityCheckTrigger | undefined;

  protected get control(): AbstractControl | Nullish {
    return this.controlDirective?.filter(d => !!d.ngControl).map(d => d.ngControl)[0]?.control;
  }

  ngDoCheck(): void {
    if (!this.control) {
      return;
    }
    this.#validityChecker?.trigger();
  }

  ngAfterViewInit(): void {
    // only initialize validation check if there is a control
    if (this.control) {
      // FIXME this does not recognize required-Validators at the beginning
      // required status: initial check
      this.required = detectRequiredValidator(this.control);
      // required status: whenever the status changes
      this.control.statusChanges.pipe(takeUntilDestroyed(this.#destoryRef)).subscribe({
        next: () => {
          this.required = detectRequiredValidator(requireValue(this.control));
        },
        error: console.error,
      });

      this.#validityChecker = new RecursiveValidityCheckTrigger(
        this.control,
        this.#changeDetectorRef,
        this.#destoryRef,
      );
    }
  }
}
