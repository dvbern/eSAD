import {DestroyRef} from '@angular/core';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {AbstractControl, FormGroup} from '@angular/forms';
import {distinctUntilChanged, filter, firstValueFrom, map, startWith} from 'rxjs';
import {FormWalker} from './FormWalker';

/**
 * Wartet bis alle Validatoren fertig sind und liefert dann das Validierungsergebnis.
 */
export async function whenFormValid<T extends FormGroup>(
  form: T,
  destroyRef: DestroyRef,
): Promise<boolean> {
  if (form.disabled) {
    return false;
  }

  form.markAllAsTouched();
  updateValueAndValidityRecursive(form);

  return firstValueFrom(
    form.statusChanges.pipe(
      startWith(form.status),
      filter((status) => status !== 'PENDING'),
      distinctUntilChanged(),
      map((status) => status === 'VALID'),
      takeUntilDestroyed(destroyRef),
    ),
  );
}

function updateValueAndValidityRecursive(root: AbstractControl): void {
  new FormWalker().walkDepthFirst({
    root,
    descentStrategy: 'top-down',
    visitor: {
      onFormControl: (ctx) => {
        ctx.control.updateValueAndValidity();
      },
    },
  });
}
